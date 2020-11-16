package life.genny.qwandautils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.keycloak.OAuth2Constants;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.util.JsonSerialization;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.json.JsonObject;
import life.genny.models.GennyToken;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.entity.Group;
import life.genny.qwanda.entity.Person;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QDataRegisterMessage;
import life.genny.qwandautils.KeycloakService;
import org.apache.http.impl.client.HttpClientBuilder;


public class KeycloakUtils {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static String register(final String token,QDataRegisterMessage register) throws IOException 
	{
		String newRealmRoles = "user,offline_access,uma_authorization";
		String newGroupRoles = "/users";
		String keycloakUrl = (new GennyToken(token)).getString("iss");
		String ret = createUser(token, register.getRealm(), register.getUsername(), register.getFirstname(), register.getLastname(), register.getEmail(), register.getPassword(),newRealmRoles, newGroupRoles);

		return ret;
	}

	public static String getAccessToken(String keycloakUrl, String realm, String clientId, String secret, String username,
			String password) throws IOException {
		
    try {
    	
    		JsonObject content = KeycloakUtils.getToken(keycloakUrl, realm, clientId, secret, username, password);
    		if(content != null) {
    			return content.getString("access_token");
    		}
    		
    		return null;
    }
		catch (Exception e ) {
      
    }
		return null;
	}
	
	public static AccessTokenResponse getAccessTokenResponse(String keycloakUrl, String realm, String clientId, String secret,
			String username, String password) throws IOException {

		JsonObject content = KeycloakUtils.getSecureTokenPayload(keycloakUrl, realm, clientId, secret, username, password);
		if(content != null) {
			return JsonUtils.fromJson(content.toString(), AccessTokenResponse.class);
		}
		
		return null;
	}

	public static JsonObject getSecureTokenPayload(String keycloakUrl, String realm, String clientId, String secret, String username, String password, String refreshToken) throws IOException {

		JsonObject fullTokenPayload = KeycloakUtils.getToken(keycloakUrl, realm, clientId, secret, username, password, refreshToken);
		JsonObject secureTokenPayload = new JsonObject();
		secureTokenPayload.put("access_token", fullTokenPayload.getString("access_token"));
		secureTokenPayload.put("refresh_token", fullTokenPayload.getString("refresh_token"));
		return secureTokenPayload;
	}

	public static JsonObject getSecureTokenPayload(String keycloakUrl, String realm, String clientId, String secret, String username, String password) throws IOException {
		return KeycloakUtils.getSecureTokenPayload(keycloakUrl, realm, clientId, secret, username, password, null);
	}

	public static JsonObject getToken(String keycloakUrl, String realm, String clientId, String secret, String username, String password, String refreshToken) throws IOException {

		HttpClient httpClient = new DefaultHttpClient();

		try {

			URI uri = new URI(keycloakUrl+"/auth/realms/"+realm+"/protocol/openid-connect/token");
			HttpPost post = new HttpPost(uri);

			post.addHeader("Content-Type", "application/x-www-form-urlencoded");

			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
			
			log.info("===================== Generating new token (KeycloakUtils) =====================");

			/* if we have a refresh token */
			if(refreshToken != null) {

				/* we decode it */
				JSONObject decodedServiceToken = KeycloakUtils.getDecodedToken(refreshToken);

				/* we get the expiry timestamp */
				long expiryTime = decodedServiceToken.getLong("exp");

				/* we get the current time */
				long nowTime = LocalDateTime.now().atZone(TimeZone.getDefault().toZoneId()).toEpochSecond();

				/* we calculate the differencr */ 
				long duration = expiryTime - nowTime;

				/* if the difference is negative it means the expiry time is less than the nowTime 
					if the difference < 180000, it means the token will expire in 3 hours
				*/
				if(duration <= GennySettings.ACCESS_TOKEN_EXPIRY_LIMIT_SECONDS) {

					/* if the refresh token is about to expire, we must re-generate a new one */
					refreshToken = null;
				}
			}

			/* if we don't have a refresh token, we generate a new token using username and password */
			if(refreshToken == null) {
				formParams.add(new BasicNameValuePair("username", username));
				formParams.add(new BasicNameValuePair("password", password));
				log.info("using username");
				formParams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "password"));
			}
			else {
				formParams.add(new BasicNameValuePair("refresh_token", refreshToken));
				formParams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "refresh_token"));
				log.info("using refresh token");
				log.info(refreshToken);
			}

			formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, clientId));
			if (secret != null) {
				formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_SECRET, secret));
			}
			UrlEncodedFormEntity form = new UrlEncodedFormEntity(formParams, "UTF-8");

			
			
			post.setEntity(form);

			HttpResponse response = httpClient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode != 200) {
				content = getContent(entity);
				throw new IOException("" + statusCode);
			}
			if (entity == null) {
				throw new IOException("Null Entity");
			} else {
				content = getContent(entity);
			}
			
			try {
				
				JsonObject obj = new JsonObject(content);
				return obj;
			}
			catch(Exception e) {
				
			}
			
		} catch (URISyntaxException e) {

			httpClient.getConnectionManager().shutdown();
			return null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		
		return null;
	}

	public static JsonObject getToken(String keycloakUrl, String realm, String clientId, String secret,
			String username, String password) throws IOException {
				return KeycloakUtils.getToken(keycloakUrl, realm, clientId, secret, username, password, null);
	}

	public static String getContent(final HttpEntity httpEntity) throws IOException {
		if (httpEntity == null)
			return null;
		final InputStream is = httpEntity.getContent();
		try {
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			int c;
			while ((c = is.read()) != -1) {
				os.write(c);
			}
			final byte[] bytes = os.toByteArray();
			final String data = new String(bytes);
			return data;
		} finally {
			try {
				is.close();
			} catch (final IOException ignored) {

			}
		}

	}

	// Decode the keycloak token string and send back in Json Format
	public static JSONObject getDecodedToken(final String bearerToken) {
		JSONObject jsonObj = null;
		String decodedJson = null;
		try {
			final String[] jwtToken = bearerToken.split("\\.");
			final Base64 decoder = new Base64(true);
			final byte[] decodedClaims = decoder.decode(jwtToken[1]);
			decodedJson = new String(decodedClaims);
			jsonObj = new JSONObject(decodedJson);
		} catch (final JSONException e1) {
			log.info("bearerToken=" + bearerToken + "  decodedJson=" + decodedJson + ":" + e1.getMessage());
		}
		return jsonObj;
	}
	
	public static String getRealmFromToken(final String bearerToken)
	{
		return getDecodedToken(bearerToken).getString("azp"); // return the realm
		
	}

	// Send the decoded Json token in the map
	public static Map<String, Object> getJsonMap(final String json) {
		final JSONObject jsonObj = getDecodedToken(json);
		return getJsonMap(jsonObj);
	}

	public static Map<String, Object> getJsonMap(final JSONObject jsonObj) {
		final String json = jsonObj.toString();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			final ObjectMapper mapper = new ObjectMapper();
			// convert JSON string to Map
			final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
			};

			map = mapper.readValue(json, typeRef);

		} catch (final JsonGenerationException e) {
			e.printStackTrace();
		} catch (final JsonMappingException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return map;
	}

	/*
	 * Return the Set of access roles of the user logged in based on the keycloak
	 * access_roles
	 */
	public static HashSet<String> getRoleSet(final String role) {
		String accessRole = role.substring(role.indexOf("[") + 1, role.indexOf("]"));
		String[] strs = accessRole.trim().split("\\s*,\\s*");
		HashSet<String> roles = new HashSet<String>(Arrays.asList(strs));
		return roles;

	}

	/*
	 * For testing user with different realm name in dev env as dev/genny keycloak
	 * can't be added with other realms Gets the Project realm from Genny Env if it
	 * is present
	 */
	static String projectRealm = null;

	public static String getPRJRealmFromDevEnv() {
		if (System.getenv("PROJECT_REALM") != null) {
			projectRealm = System.getenv("PROJECT_REALM");
		}
		return projectRealm;
	}

	@SuppressWarnings("deprecation")
	public static String resetUserPassword(String userId, String token, String realm)
			throws ClientProtocolException, IOException {

		HttpClient httpClient = new DefaultHttpClient();

		try {

			String keycloakUrl = (new GennyToken(token)).getKeycloakUrl();
			
			HttpPut putRequest = new HttpPut(
					keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId + "/reset-password");
			log.info(keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId + "/reset-password");

			putRequest.addHeader("Content-Type", "application/json");
			putRequest.addHeader("Authorization", "Bearer " + token);

			HttpResponse response = httpClient.execute(putRequest);

			int statusCode = response.getStatusLine().getStatusCode();

			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode == 201) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)", "$1");
				return content;
			} else if (statusCode == 204) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)", "$1");
				return content;
			} else if (statusCode == 409) {
				throw new IOException("Already exists");
			}
			if (entity == null) {
				throw new IOException("Null Entity");
			} else {
				content = getContent(entity);
				throw new IOException(response + "");
			}
		}

		finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	public static String createUser(String token, String realm, String newUsername,
			String newFirstname, String newLastname, String newEmail)
			throws IOException {
		String newRealmRoles = "user,offline_access,uma_authorization";
		String newGroupRoles = "/users";
		String password = UUID.randomUUID().toString().substring(0,10);
		return createUser(token, realm, newUsername,
				newFirstname,newLastname, newEmail, password,newRealmRoles, newGroupRoles);
	}
	
	public static String createUser(String token, String realm, String newUsername,
			String newFirstname, String newLastname, String newEmail, String password)
			throws IOException {
		String newRealmRoles = "user,offline_access,uma_authorization";
		String newGroupRoles = "/users";
				
		return createUser(token, realm, newUsername,
				newFirstname,newLastname, newEmail, password,newRealmRoles, newGroupRoles);
	}

	// This is the one called from rules to create a keycloak user
	public static String createUser(String token, String realm, String newUsername,
			String newFirstname, String newLastname, String newEmail, String password,String newRealmRoles, String newGroupRoles)
			throws IOException {
		return createUser(null,token, realm, newUsername,
				newFirstname, newLastname, newEmail, password,newRealmRoles, newGroupRoles);
	}

	
	// This is the one called from rules to create a keycloak user
	public static String createDummyUser(String token, String realm)
			throws IOException {
		String keycloakUrl = (new GennyToken(token)).getKeycloakUrl();
		
		String randomCode = UUID.randomUUID().toString().substring(0, 18);
		String json = "{ " +"\"username\" : \"" + randomCode + "\"," + "\"email\" : \"" + randomCode + "@gmail.com\" , "
				+ "\"enabled\" : true, " + "\"emailVerified\" : true, " + "\"firstName\" : \"" + randomCode + "\", "
				+ "\"lastName\" : \"" + randomCode + "\", " + "\"groups\" : [" + " \"users\" " + "],"
				+ "\"realmRoles\" : [" + "\"user\" " + "]" + "}";

		log.info("CreateUserjsonDummy="+json);
		
		HttpClient httpClient = new DefaultHttpClient();
		//log.info("Keycloak token used is "+token);
		try {
			HttpPost post = new HttpPost(keycloakUrl + "/auth/admin/realms/" + realm + "/users");
			// HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(keycloakUrl +
			// "/auth/admin/realms/"+realm+"/users"));

			post.addHeader("Content-Type", "application/json");
			post.addHeader("Authorization", "Bearer " + token);

			StringEntity postingString = new StringEntity(json);
			post.setEntity(postingString);

			HttpResponse response = httpClient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();
			log.info("StatusCode: " + statusCode);

			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode == 201) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)", "$1");
				String keycloakUserId = getKeycloakUserId(token, realm, randomCode);
                log.info("Keycloak User ID: " + keycloakUserId);
                return keycloakUserId;
			} else if (statusCode == 204) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)", "$1");
				String keycloakUserId = getKeycloakUserId(token, realm, randomCode);
                log.info("Keycloak User ID: " + keycloakUserId);
                return keycloakUserId;
			} else if (statusCode == 409) {
				//throw new IOException("Email is already taken. Please use a different email address.");
				log.warn("Email is already taken for "+randomCode);
				// fetch existing email user
				String userId = getKeycloakUserId(token, realm, randomCode);
				return userId;
			} else if (statusCode == 401) {
				//throw new IOException("Account is already taken. Please use a different email address.");
				log.warn("Unauthorized token used to create "+randomCode);
				// fetch existing email user
				String userId = getKeycloakUserId(token, realm, randomCode);
				return userId;
			}
			if (entity == null) {
				throw new IOException("We could not create the new user. Please try again.");
			} else {
				String keycloakUserId = getKeycloakUserId(token, realm, randomCode);
	              log.info("Keycloak User ID: " + keycloakUserId);
	              return keycloakUserId;
			}
		}

		finally {
			httpClient.getConnectionManager().shutdown();
		}
	}
	
	
	// This is the one called from rules to create a keycloak user
	public static String updateUser(String keycloakUUID,String token, String realm, String newUsername,
			String newFirstname, String newLastname, String newEmail, String password,String newRealmRoles, String newGroupRoles)
			throws IOException {
		String keycloakUrl = (new GennyToken(token)).getKeycloakUrl();
		keycloakUUID = keycloakUUID.toLowerCase();
	
		String json = "{ " +  "\"username\" : \"" + newUsername + "\"," + "\"email\" : \"" + newEmail + "\" , "
				+ "\"enabled\" : true, " + "\"emailVerified\" : true, " + "\"firstName\" : \"" + newFirstname + "\", "
				+ "\"lastName\" : \"" + newLastname + "\", " + "\"groups\" : [" + " \"" + newGroupRoles + "\" " + "],"
				+ "\"realmRoles\" : [" + "\"" + newRealmRoles + "\" " + "]" + "}";

		log.info("CreateUserjson="+json);
		
		HttpClient httpClient = new DefaultHttpClient();
		//log.info("Keycloak token used is "+token);
		try {
			HttpPut post = new HttpPut(keycloakUrl + "/auth/admin/realms/" + realm + "/users/"+keycloakUUID);
			// HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(keycloakUrl +
			// "/auth/admin/realms/"+realm+"/users"));

			post.addHeader("Content-Type", "application/json");
			post.addHeader("Authorization", "Bearer " + token);

			StringEntity postingString = new StringEntity(json);
			post.setEntity(postingString);

			HttpResponse response = httpClient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();
			log.info("StatusCode: " + statusCode);

			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode == 201) {
					KeycloakUtils.setPassword(token,  realm, keycloakUUID, password);
               return keycloakUUID;
			} else if (statusCode == 204) {
				KeycloakUtils.setPassword(token,  realm, keycloakUUID, password);
                return keycloakUUID;
			} else if (statusCode == 409) {
				//throw new IOException("Email is already taken. Please use a different email address.");
				log.warn("Email is already taken for "+newUsername);
				// fetch existing email user
				return keycloakUUID;
			} else if (statusCode == 401) {
				//throw new IOException("Account is already taken. Please use a different email address.");
				log.warn("Unauthorized token used to create "+newUsername);
				// fetch existing email user
				return keycloakUUID;
			}
			if (entity == null) {
				throw new IOException("We could not create the new user. Please try again.");
			} else {
	              log.info("Keycloak User ID: " + keycloakUUID);
	              return keycloakUUID;
			}
		}

		finally {
			httpClient.getConnectionManager().shutdown();
		}
	}
	
	// This is the one called from rules to create a keycloak user
	public static String createUser(String keycloakUUID,String token, String realm, String newUsername,
			String newFirstname, String newLastname, String newEmail, String password,String newRealmRoles, String newGroupRoles)
			throws IOException {
		String keycloakUrl = (new GennyToken(token)).getKeycloakUrl();
		
		String idJson = "";
		if (!StringUtils.isBlank(keycloakUUID)) {
			idJson = "\"id\" : \""+keycloakUUID.trim().toLowerCase() + "\",";
		}
		String json = "{ " + idJson+ "\"username\" : \"" + newUsername + "\"," + "\"email\" : \"" + newEmail + "\" , "
				+ "\"enabled\" : true, " + "\"emailVerified\" : true, " + "\"firstName\" : \"" + newFirstname + "\", "
				+ "\"lastName\" : \"" + newLastname + "\", " + "\"groups\" : [" + " \"" + newGroupRoles + "\" " + "],"
				+ "\"realmRoles\" : [" + "\"" + newRealmRoles + "\" " + "]" + "}";

		log.info("CreateUserjson="+json);
		
		HttpClient httpClient = new DefaultHttpClient();
		//log.info("Keycloak token used is "+token);
		try {
			HttpPost post = new HttpPost(keycloakUrl + "/auth/admin/realms/" + realm + "/users");
			// HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(keycloakUrl +
			// "/auth/admin/realms/"+realm+"/users"));

			post.addHeader("Content-Type", "application/json");
			post.addHeader("Authorization", "Bearer " + token);

			StringEntity postingString = new StringEntity(json);
			post.setEntity(postingString);

			HttpResponse response = httpClient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();
			log.info("StatusCode: " + statusCode);

			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode == 201) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)", "$1");
				if(content.length() > 0) {
					KeycloakUtils.setPassword(token,  realm, content, password);
				}
				String keycloakUserId = getKeycloakUserId(token, realm, newUsername);
                log.info("Keycloak User ID: " + keycloakUserId);
                return keycloakUserId;
			} else if (statusCode == 204) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)", "$1");
				String keycloakUserId = getKeycloakUserId(token, realm, newUsername);
                log.info("Keycloak User ID: " + keycloakUserId);
                return keycloakUserId;
			} else if (statusCode == 409) {
				//throw new IOException("Email is already taken. Please use a different email address.");
				log.warn("Email is already taken for "+newUsername);
				// fetch existing email user
				String userId = getKeycloakUserId(token, realm, newUsername);
				return userId;
			} else if (statusCode == 401) {
				//throw new IOException("Account is already taken. Please use a different email address.");
				log.warn("Unauthorized token used to create "+newUsername);
				// fetch existing email user
				String userId = getKeycloakUserId(token, realm, newUsername);
				return userId;
			}
			if (entity == null) {
				throw new IOException("We could not create the new user. Please try again.");
			} else {
				String keycloakUserId = getKeycloakUserId(token, realm, newUsername);
	              log.info("Keycloak User ID: " + keycloakUserId);
	              return keycloakUserId;
			}
		}

		finally {
			httpClient.getConnectionManager().shutdown();
		}
	}
	/** Remove user from Keycloak using the URL: /admin/realms/{realm}/users/{id} */
	public static String removeUser(String token, String realm, String userId)
        throws IOException {

		String keycloakUrl = (new GennyToken(token)).getKeycloakUrl();
    HttpClient httpClient = new DefaultHttpClient();
    
    try {
        HttpDelete post = new HttpDelete(keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId);

        post.addHeader("Content-Type", "application/json");
        post.addHeader("Authorization", "Bearer " + token);

        HttpResponse response = httpClient.execute(post);

        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode == 200) {
          return "Success";
        } else {
          return "Failed";
        }
    } catch (Exception e) {
      e.printStackTrace();
      return "Failed";
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }
 }

    
	
	public static String getKeycloakUserId(final String token, final String realm, final String username) throws IOException {
		String keycloakUrl = (new GennyToken(token)).getKeycloakUrl();
    final List<LinkedHashMap> users = fetchKeycloakUsers(token, realm, username);
    if(!users.isEmpty()) {
      return (String) users.get(0).get("id");
    }
    return null;
}
	
	public static List<LinkedHashMap> fetchKeycloakUsers(final String token, final String realm, final String username) {
		String keycloakUrl = (new GennyToken(token)).getKeycloakUrl();
		List<LinkedHashMap> results = new ArrayList<LinkedHashMap>();
	    final HttpClient client = new DefaultHttpClient();

	    
	    try {
	    	String encodedUsername = encodeValue(username);
	      final HttpGet get =
	          new HttpGet(keycloakUrl + "/auth/admin/realms/" + realm + "/users?username=" + encodedUsername);
	      get.addHeader("Authorization", "Bearer " + token);
	      try {
	        final HttpResponse response = client.execute(get);
	        if (response.getStatusLine().getStatusCode() != 200) {
	          throw new IOException();
	        }
	        final HttpEntity entity = response.getEntity();
	        final InputStream is = entity.getContent();
	        try {
	          results = JsonSerialization.readValue(is, (new ArrayList<UserRepresentation>()).getClass());
	        } finally {
	          is.close();
	        }
	      } catch (final IOException e) {
	        throw new RuntimeException(e);
	      }
	    } finally {
	      client.getConnectionManager().shutdown();
	    }
	    return results;
	  }

	private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
//	public static String getKeycloakUrl() {
//		String keycloakProto =
//				System.getenv("KEYCLOAK_PROTO") != null ? System.getenv("KEYCLOAK_PROTO") : "http://";
//				String keycloakPort =
//						System.getenv("KEYCLOAK_PORT") != null ? System.getenv("KEYCLOAK_PORT") : "8180";
//						String keycloakIP = System.getenv("HOSTIP") != null ? System.getenv("HOSTIP") : "localhost";
//
//						String keycloakURL = System.getenv("KEYCLOAKURL") != null ? System.getenv("KEYCLOAKURL")
//								: keycloakProto + keycloakIP + ":" + keycloakPort;
//						return keycloakURL;
//	}

	public static int setPassword(String token,String realm, String userId, String password)
			throws IOException {
		String keycloakUrl = (new GennyToken(token)).getKeycloakUrl();
		String json = "{\"type\": \"password\", " + "\"temporary\": false," + "\"value\": \"" + password + "\"" + "}";

		HttpClient httpClient = new DefaultHttpClient();

		try {
			HttpPut put = new HttpPut(
					keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId + "/reset-password");

			put.addHeader("Content-Type", "application/json");
			put.addHeader("Authorization", "Bearer " + token);

			StringEntity postingString = new StringEntity(json);
			put.setEntity(postingString);

			HttpResponse response = httpClient.execute(put);

			int statusCode = response.getStatusLine().getStatusCode();

			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode != 204) {
				content = getContent(entity);
				throw new IOException("" + statusCode);
			}
			if (statusCode == 403) {
				throw new IOException("403 Forbidden");
			}

			return statusCode;
		}

		finally {
			httpClient.getConnectionManager().shutdown();
		}
	}
	
	public String createEncryptedPassword(String key, final String customercode, final String password) {
		String newkey = null;
		if (key.length()>16) {
			newkey = key.substring(0, 16);
		} else {
			newkey = StringUtils.rightPad(key, 16, '*');
		}
		String initVector = "PRJ_" + customercode.toUpperCase();
		initVector = StringUtils.rightPad(initVector, 16, '*');

        String encryptedPassword = encrypt(newkey, initVector, password);
        if (!key.equals(newkey)) {
        	log.info("NEW KEY = ["+newkey+"]");;
        }
		return encryptedPassword;
	}

    public static String encrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
//            log.info("encrypted string: "
//                    + Base64.encodeBase64String(encrypted));

            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String key, String initVector, String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String sendVerifyEmail(final String username, final String serviceToken)
    {
    	GennyToken gToken = new GennyToken(serviceToken);
    	return sendVerifyEmail(gToken.getRealm(), username, serviceToken);
    }
    
    public static String sendVerifyEmail(final String realm, final String username, final String servicetoken)
    {
    	String keycloakUrl = (new GennyToken(servicetoken)).getKeycloakUrl();
    	String userId;
		try {
			userId = getKeycloakUserId(servicetoken, realm, username);
			if (userId != null) {
			HttpClient httpClient = new DefaultHttpClient();

			HttpPut putRequest = new HttpPut(keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId + "/send-verify-email");

			log.info(keycloakUrl + "/auth/admin/realms/" + "internmatch" + "/users/" + userId + "/send-verify-email");

			putRequest.addHeader("Content-Type", "application/json");
			putRequest.addHeader("Authorization", "Bearer " + servicetoken);

			HttpResponse response = null;
			try {
				response = httpClient.execute(putRequest);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int statusCode = response.getStatusLine().getStatusCode();
			log.info("sendVerifyMail statusCode is "+statusCode+" with userId="+userId);
			if ((statusCode == 200)||(statusCode == 201)) {
				return userId;
			}
			} else {
				log.error("Could not retrieve userId from "+keycloakUrl+" for "+username);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	return null;

    }
    
	private void sendVerifyTestMail(String token, String emailusername, String firstname, String lastname) {
		GennyToken gToken = new GennyToken(token);


		String password = UUID.randomUUID().toString().substring(0, 8);
		String userId;
		try {
			userId = KeycloakUtils.createUser(token, gToken.getRealm(), emailusername, firstname, lastname, emailusername, password,
					"user", "user");
		} catch (IOException e) {
		}
		userId = KeycloakUtils.sendVerifyEmail(gToken.getRealm(), emailusername, token);
		System.out.println("UserId=" + userId);
	}
	
	private void sendVerifyTestMail(String token, String username, String domain, String firstname, String lastname) {
		GennyToken gToken = new GennyToken(token);
		LocalDateTime now = LocalDateTime.now();
		String mydatetime = new SimpleDateFormat("yyyyMMddHHmmss").format(now.toLocalDate());
		// System.out.println(username+" serviceToken=" + token);
		String emailusername = username + "+" + mydatetime + "@" + domain;

		sendVerifyTestMail(token,emailusername, firstname, lastname);
	}

	public static String getKeycloakUUIDByUserCode(String code,  HashMap<String, String> userCodeUUIDMapping) {
		String keycloakUUID = null;
		if (userCodeUUIDMapping.containsKey(code)) {
			keycloakUUID = userCodeUUIDMapping.get(code);
			log.debug(String.format("DEBUG:Find user baseentity code:%s, update to keycloak uuid:%s",
					code , keycloakUUID));
		} else {
			keycloakUUID = code;
			log.debug(String.format("DEBUG:Can not find user baseentity code:%s, set keycloak uuid:%s",
					code , keycloakUUID));
		}
		return keycloakUUID;
	}

	public static HashMap<String, String> getUsersByRealm(String keycloakUrl, String realm, String servicePassword) {
	    HashMap<String, String>  userCodeUUIDMapping = new HashMap<>();
		List<LinkedHashMap> results = new ArrayList<>();

		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			String accessToken = getAccessToken(keycloakUrl, realm, "admin-cli", null, "service", servicePassword);
			HttpGet get = new HttpGet(keycloakUrl + "/auth/admin/realms/" + realm + "/users?first=0&max=20000");
			get.addHeader("Authorization", "Bearer " + accessToken);
			HttpResponse response = client.execute(get);
			if (response.getStatusLine().getStatusCode() != 200) {
					throw new IOException("Get keycloak user response code:" + response.getStatusLine().getStatusCode());
			}
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			results = JsonSerialization.readValue(is, (new ArrayList<UserRepresentation>()).getClass());
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (LinkedHashMap userMap : results) {
			String username = (String) userMap.get("username");
			String email = (String) userMap.get("email");
			String code = QwandaUtils.getNormalisedUsername("PER_" + username);
			String id = (String) userMap.get("id");
			String uuid = "PER_" + id.toUpperCase();
			if (userCodeUUIDMapping.containsKey(code)) {
			    log.error(String.format("Duplicate user in keycloak, user code:%s, user name:%s, email:%s.",
						code, username, email));
			} else {
				userCodeUUIDMapping.put(code, uuid);
			}
		}
		return userCodeUUIDMapping;
	}
	
	public static String getImpersonatedToken(String keycloakUrl, String realm, String clientId,String secret, String requested_subject, String exchangedToken) throws IOException {

		HttpClient httpClient = new DefaultHttpClient();

		try {

			URI uri = new URI(keycloakUrl+"/auth/realms/"+realm+"/protocol/openid-connect/token");
			HttpPost post = new HttpPost(uri);

			post.addHeader("Content-Type", "application/x-www-form-urlencoded");

			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
			
			log.info("===================== Generating new token (KeycloakUtils) =====================");

			/* if we have a refresh token */

			/* if we don't have a refresh token, we generate a new token using username and password */
				formParams.add(new BasicNameValuePair("requested_subject", requested_subject));
				formParams.add(new BasicNameValuePair("subject_token", exchangedToken));
				log.info("using username");
				formParams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "urn:ietf:params:oauth:grant-type:token-exchange"));
				if (secret != null) {
					formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_SECRET, secret));
				}
			formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, clientId));
			UrlEncodedFormEntity form = new UrlEncodedFormEntity(formParams, "UTF-8");

			
			
			post.setEntity(form);

			HttpResponse response = httpClient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode != 200) {
				content = getContent(entity);
				throw new IOException("" + statusCode);
			}
			if (entity == null) {
				throw new IOException("Null Entity");
			} else {
				content = getContent(entity);
			}
			
			try {
				
				return content;
			}
			catch(Exception e) {
				
			}
			
		} catch (URISyntaxException e) {

			httpClient.getConnectionManager().shutdown();
			return null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		
		return null;
	}
	
	
}