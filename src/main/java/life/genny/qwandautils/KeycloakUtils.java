package life.genny.qwandautils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
// import org.keycloak.OAuth2Constants;
// import org.keycloak.common.util.KeycloakUriBuilder;
// import org.keycloak.constants.ServiceUrlConstants;
// import org.keycloak.representations.AccessTokenResponse;
// import org.keycloak.util.JsonSerialization;
import org.json.JSONException;
import org.json.JSONObject;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.OAuth2Constants;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KeycloakUtils {

	// static public String getToken(String keycloakUrl, String realm, String
	// clientId, String secret,
	// String username, String password) throws IOException {
	//
	// return
	// getAccessToken(keycloakUrl,realm,clientId,secret,username,password).getToken();
	// }

	public static AccessTokenResponse getAccessToken(String keycloakUrl, String realm, String clientId, String secret,
			String username, String password) throws IOException {

		HttpClient httpClient = new DefaultHttpClient();

		try {
			HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(keycloakUrl + "/auth")
					.path(ServiceUrlConstants.TOKEN_PATH).build(realm));
//			System.out.println("url token post=" + keycloakUrl + "/auth" + ",tokenpath="
//					+ ServiceUrlConstants.TOKEN_PATH + ":realm=" + realm + ":clientid=" + clientId + ":secret" + secret
//					+ ":un:" + username + "pw:" + password);
//			;
			post.addHeader("Content-Type", "application/x-www-form-urlencoded");

			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
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
			return JsonSerialization.readValue(content, AccessTokenResponse.class);
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
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
	public static String resetUserPassword(String userId, String token, String realm) throws ClientProtocolException, IOException {
			
		HttpClient httpClient = new DefaultHttpClient();

		try {
			
			String keycloakUrl = getKeycloakUrl();
			HttpPut putRequest = new HttpPut(keycloakUrl+"/auth/admin/realms/"+realm+"/users/" + userId + "/reset-password");
			System.out.println(keycloakUrl+"/auth/admin/realms/"+realm+"/users/" + userId + "/reset-password");
			
			putRequest.addHeader("Content-Type", "application/json");
			putRequest.addHeader("Authorization", "Bearer "+token);
				
			HttpResponse response = httpClient.execute(putRequest);

			int statusCode = response.getStatusLine().getStatusCode();
			
			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode == 201) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)","$1");
				return content;
				} else if (statusCode == 204) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)","$1");
				return content;
			}
				else if (statusCode == 409) {
					throw new IOException("Already exists");
				}
			if (entity == null) {
				throw new IOException("Null Entity");
			} else {
				content = getContent(entity);
				throw new IOException(response+"");
			}
		} 
		
		finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	public static String createUser(String token, String keycloakUrl, String realm, 
			String newUsername,
			String newFirstname,
			String newLastname,
			String newEmail
			) throws IOException {
			return createUser(token,keycloakUrl,realm,newUsername,newFirstname,newLastname,newEmail);
	}
	
	public static String createUser(String token, String keycloakUrl, String realm, 
			String newUsername,
			String newFirstname,
			String newLastname,
			String newEmail,
			String newRealmRoles,
			String newGroupRoles
			) throws IOException {
		
		String json = "{ " 
				+ "\"username\" : \""+newUsername+"\"," 
				+ "\"email\" : \""+newEmail+"\" , " 
				+ "\"enabled\" : true, " 
				+ "\"emailVerified\" : true, " 
				+ "\"firstName\" : \""+newFirstname+"\", " 
				+ "\"lastName\" : \""+newLastname+"\", " 
				+ "\"groups\" : ["  
				+  " \""+newGroupRoles+"\" "  
				+ "],"  
				+ "\"realmRoles\" : [" 
				+   "\""+newRealmRoles+"\" " 
				+ "]" 
				+"}";


		
		HttpClient httpClient = new DefaultHttpClient();

		try {
			HttpPost post = new HttpPost(keycloakUrl+"/auth/admin/realms/"+realm+"/users");
	//		HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(keycloakUrl + "/auth/admin/realms/"+realm+"/users"));

			post.addHeader("Content-Type", "application/json");
			post.addHeader("Authorization", "Bearer "+token);
			
			StringEntity postingString = new StringEntity(json);
			post.setEntity(postingString);

	
			HttpResponse response = httpClient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();
			
			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode == 201) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)","$1");
				return content;
				} else if (statusCode == 204) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)","$1");
				return content;
			}
				else if (statusCode == 409) {
					throw new IOException("Already exists");
				}
			if (entity == null) {
				throw new IOException("Null Entity");
			} else {
				content = getContent(entity);
				throw new IOException(response+"");

			}
		
		} 
		
		finally {
			httpClient.getConnectionManager().shutdown();
		}
	}
	
	public static String getKeycloakUrl() {
		
		String keycloakProto = System.getenv("KEYCLOAK_PROTO") !=null ? System.getenv("KEYCLOAK_PROTO"): "http://";
	    String keycloakPort = System.getenv("KEYCLOAK_PORT") != null ? System.getenv("KEYCLOAK_PORT"): "8180";
	    String keycloakIP = System.getenv("HOSTIP") != null ? System.getenv("HOSTIP"): "localhost";
		return keycloakProto+keycloakIP+":"+keycloakPort;
	}

	public static int setPassword(String token, String keycloakUrl, String realm, String userId, String password) throws IOException 
	{
	String json = "{\"type\": \"password\", " +
		 "\"temporary\": false," + 
		 "\"value\": \""+password+"\"" +
		"}";
	
	HttpClient httpClient = new DefaultHttpClient();

	try {
		HttpPut put = new HttpPut(keycloakUrl+"/auth/admin/realms/"+realm+"/users/"+userId+"/reset-password");

		put.addHeader("Content-Type", "application/json");
		put.addHeader("Authorization", "Bearer "+token);
		
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
	

	
}
