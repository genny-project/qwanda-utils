package life.genny.qwandautils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;
// import org.keycloak.OAuth2Constants;
// import org.keycloak.common.util.KeycloakUriBuilder;
// import org.keycloak.constants.ServiceUrlConstants;
// import org.keycloak.representations.AccessTokenResponse;
// import org.keycloak.util.JsonSerialization;
import org.json.JSONException;
import org.json.JSONObject;
import org.keycloak.OAuth2Constants;
import org.keycloak.representations.AccessTokenResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import life.genny.qwanda.message.QDataRegisterMessage;


public class KeycloakUtils {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static String register(final String token,QDataRegisterMessage register) throws IOException 
	{
		String newRealmRoles = "user,offline_access,uma_authorization";
		String newGroupRoles = "/users";
		return createUser(token, register.getRealm(), register.getUsername(), register.getFirstname(), register.getLastname(), register.getEmail(), register.getPassword(),newRealmRoles, newGroupRoles);
	}

	public static String getToken(String keycloakUrl, String realm, String clientId, String secret, String username,
			String password) throws IOException {
		
    try {
    	
    		JsonObject content = KeycloakUtils.getAccessToken(keycloakUrl, realm, clientId, secret, username, password);
    		if(content != null) {
    			return content.getString("access_token");
    		}
    		
    		return null;
    }
		catch (Exception e ) {
          e.printStackTrace();
    }
		return null;
	}
	
	public static AccessTokenResponse getAccessTokenResponse(String keycloakUrl, String realm, String clientId, String secret,
			String username, String password) throws IOException {

		JsonObject content = KeycloakUtils.getAccessToken(keycloakUrl, realm, clientId, secret, username, password);
		if(content != null) {
			return JsonUtils.fromJson(content.toString(), AccessTokenResponse.class);
		}
		
		return null;
	}

	public static JsonObject getAccessToken(String keycloakUrl, String realm, String clientId, String secret,
			String username, String password) throws IOException {

		HttpClient httpClient = new DefaultHttpClient();

		try {
			URI uri = new URI(keycloakUrl+"/auth/realms/"+realm+"/protocol/openid-connect/token");
			HttpPost post = new HttpPost(uri);
			post.addHeader("Content-Type", "application/x-www-form-urlencoded");

			List<NameValuePair> formParams = new ArrayList<>();
			formParams.add(new BasicNameValuePair("username", username));
			formParams.add(new BasicNameValuePair("password", password));
			formParams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "password"));
			formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, clientId));
			formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_SECRET, secret));
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
				
				return new JsonObject(content);
			}
			catch(Exception e) {
			  e.printStackTrace();
			}
			
		} catch (URISyntaxException e) {

			httpClient.getConnectionManager().shutdown();
			return null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		
		return null;
	}

	public static String getContent(final HttpEntity httpEntity) throws IOException {
		if (httpEntity == null) {
      return null;
    }
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
			    ignored.printStackTrace();
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
			System.out.println("bearerToken=" + bearerToken + "  decodedJson=" + decodedJson + ":" + e1.getMessage());
		}
		return jsonObj;
	}

	// Send the decoded Json token in the map
	public static Map<String, Object> getJsonMap(final String json) {
		final JSONObject jsonObj = getDecodedToken(json);
		return getJsonMap(jsonObj);
	}

	public static Map<String, Object> getJsonMap(final JSONObject jsonObj) {
		final String json = jsonObj.toString();
		Map<String, Object> map = new HashMap<>();
		try {
			final ObjectMapper mapper = new ObjectMapper();
			// convert JSON string to Map
			final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
			};

			map = mapper.readValue(json, typeRef);

		} catch (final IOException e) {
			e.printStackTrace();
		} 
		return map;
	}

	/*
	 * Return the Set of access roles of the user logged in based on the keycloak
	 * access_roles
	 */
	public static Set<String> getRoleSet(final String role) {
		String accessRole = role.substring(role.indexOf('[') + 1, role.indexOf(']'));
		String[] strs = accessRole.trim().split("\\s*,\\s*");
		return new HashSet<>(Arrays.asList(strs));

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
			throws IOException {

		HttpClient httpClient = new DefaultHttpClient();

		try {

			String keycloakUrl = getKeycloakUrl();
			HttpPut putRequest = new HttpPut(
					keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId + "/reset-password");
			System.out.println(keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId + "/reset-password");

			putRequest.addHeader("Content-Type", "application/json");
			putRequest.addHeader("Authorization", "Bearer " + token);

			HttpResponse response = httpClient.execute(putRequest);

			int statusCode = response.getStatusLine().getStatusCode();

			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode == 201 || statusCode == 204) {
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
			String newFirstname, String newLastname, String newEmail) throws IOException {
		
		String newRealmRoles = "user,offline_access,uma_authorization";
		String newGroupRoles = "/users";
		
		return createUser(token, realm, newUsername, newFirstname, newLastname, newEmail, newRealmRoles, newGroupRoles);
	}

	public static String createUser(String token, String realm, String newUsername,
			String newFirstname, String newLastname, String newEmail, String newRealmRoles, String newGroupRoles)
			throws IOException {
		return createUser(token, realm, newUsername, newFirstname, newLastname, newEmail, "password1",newRealmRoles, newGroupRoles);
	}
	public static String createUser(String token, String realm, String newUsername,
			String newFirstname, String newLastname, String newEmail, String password,String newRealmRoles, String newGroupRoles)
			throws IOException {

		String json = "{ " + "\"username\" : \"" + newUsername + "\"," + "\"email\" : \"" + newEmail + "\" , "
				+ "\"enabled\" : true, " + "\"emailVerified\" : true, " + "\"firstName\" : \"" + newFirstname + "\", "
				+ "\"lastName\" : \"" + newLastname + "\", " + "\"groups\" : [" + " \"" + newGroupRoles + "\" " + "],"
				+ "\"realmRoles\" : [" + "\"" + newRealmRoles + "\" " + "]" + "}";

		log.info("CreateUserjson="+json);
		
		HttpClient httpClient = new DefaultHttpClient();
		String keycloakUrl = getKeycloakUrl();
		
		try {
			HttpPost post = new HttpPost(keycloakUrl + "/auth/admin/realms/" + realm + "/users");

			post.addHeader("Content-Type", "application/json");
			post.addHeader("Authorization", "Bearer " + token);

			StringEntity postingString = new StringEntity(json);
			post.setEntity(postingString);

			HttpResponse response = httpClient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();

			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode == 201) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)", "$1");
				if(content.length() > 0) {
					KeycloakUtils.setPassword(token, keycloakUrl, realm, content, password);
				}
				
				return content;
			} else if (statusCode == 204) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)", "$1");
				return content;
			} else if (statusCode == 409) {
				throw new IOException("Email is already taken. Please use a different email address.");
			}
			if (entity == null) {
				throw new IOException("We could not create the new user. Please try again.");
			} else {
				content = getContent(entity);
				throw new IOException(response + "");
			}
		}

		finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	public static String getKeycloakUrl() {
		String keycloakProto =
				System.getenv("KEYCLOAK_PROTO") != null ? System.getenv("KEYCLOAK_PROTO") : "http://";
				String keycloakPort =
						System.getenv("KEYCLOAK_PORT") != null ? System.getenv("KEYCLOAK_PORT") : "8180";
						String keycloakIP = System.getenv("HOSTIP") != null ? System.getenv("HOSTIP") : "localhost";

						return System.getenv("KEYCLOAKURL") != null ? System.getenv("KEYCLOAKURL")
								: keycloakProto + keycloakIP + ":" + keycloakPort;
	}

	public static int setPassword(String token, String keycloakUrl, String realm, String userId, String password)
			throws IOException {
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

	public static String generateServiceToken(String realm, String keycloakUrl, String secret) {

		if (GennySettings.DEV_MODE) {
			realm = "genny";
		}

		// fetch token from keycloak
		String key = null;
		String initVector = "PRJ_" + realm.toUpperCase();
		initVector = StringUtils.rightPad(initVector, 16, '*');
		String encryptedPassword = null;
		if (GennySettings.DEV_MODE) {
			initVector = "PRJ_GENNY*******";
		}

		try {
			key = System.getenv("ENV_SECURITY_KEY"); 
		} catch (Exception e) {
			log.info("PRJ_" + realm.toUpperCase() + " ENV ENV_SECURITY_KEY  is missing!");
		}

		try {
			encryptedPassword = System.getenv("ENV_SERVICE_PASSWORD");
		} catch (Exception e) {
			log.info("PRJ_" + realm.toUpperCase() + " attribute ENV_SECURITY_KEY  is missing!");
		}

		String password = SecurityUtils.decrypt(key, initVector, encryptedPassword);


		try {
			return KeycloakUtils.getToken(keycloakUrl, realm, realm, secret, "service", password);

		} catch (Exception e) {
			log.error(e);
		}

		return null;
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
        	System.out.println("NEW KEY = ["+newkey+"]");
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

}
