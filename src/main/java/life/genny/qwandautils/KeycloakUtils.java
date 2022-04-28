package life.genny.qwandautils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URL;
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
import javax.ws.rs.core.MediaType;
import java.security.SecureRandom;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.util.JsonSerialization;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.json.JsonObject;
import life.genny.models.GennyToken;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.Answer;
import life.genny.qwanda.message.QDataRegisterMessage;
import life.genny.utils.BaseEntityUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

public class KeycloakUtils {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	private static String getKeycloakUrlFromToken(GennyToken token) {
		String keycloakUrl = token.getKeycloakUrl();
		keycloakUrl = keycloakUrl.replaceAll(":-1", ""); // get rid of weird -1
		return keycloakUrl;
	}

	public static String register(final GennyToken token, QDataRegisterMessage register) throws IOException {
		String newRealmRoles = "user,offline_access,uma_authorization";
		String newGroupRoles = "/users";
		String keycloakUrl = token.getString("iss");
		String ret = createUser(token, register.getRealm(), register.getUsername(), register.getFirstname(),
				register.getLastname(), register.getEmail(), register.getPassword(), newRealmRoles, newGroupRoles);

		return ret;
	}

	public static String getAccessToken(String keycloakUrl, String realm, String clientId, String secret,
			String username,
			String password) throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		try {

			JsonObject content = KeycloakUtils.getToken(keycloakUrl, realm, clientId, secret, username, password);
			if (content != null) {
				return content.getString("access_token");
			}

			return null;
		} catch (Exception e) {
			log.error("Cannot get Token for USername " + username + " for realm " + realm + " on " + keycloakUrl
					+ " and clientId " + clientId);
		}
		return null;
	}

	public static AccessTokenResponse getAccessTokenResponse(String keycloakUrl, String realm, String clientId,
			String secret,
			String username, String password) throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";

		JsonObject content = KeycloakUtils.getSecureTokenPayload(keycloakUrl, realm, clientId, secret, username,
				password);
		if (content != null) {
			return JsonUtils.fromJson(content.toString(), AccessTokenResponse.class);
		}

		return null;
	}

	public static JsonObject getSecureTokenPayload(String keycloakUrl, String realm, String clientId, String secret,
			String username, String password, String refreshToken) throws IOException {

		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		JsonObject fullTokenPayload = KeycloakUtils.getToken(keycloakUrl, realm, clientId, secret, username, password,
				refreshToken);
		//log.info("Got fullTokenPayLoad :" + fullTokenPayload);
		JsonObject secureTokenPayload = new JsonObject();
		secureTokenPayload.put("access_token", fullTokenPayload.getString("access_token"));
		secureTokenPayload.put("refresh_token", fullTokenPayload.getString("refresh_token"));
		return secureTokenPayload;
	}

	public static JsonObject getSecureTokenPayload(String keycloakUrl, String realm, String clientId, String secret,
			String username, String password) throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		return KeycloakUtils.getSecureTokenPayload(keycloakUrl, realm, clientId, secret, username, password, null);
	}

	public static JsonObject getToken(String keycloakUrl, String realm, String clientId, String secret, String username,
			String password, String refreshToken) throws IOException {

		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		HashMap<String, String> postDataParams = new HashMap<>();
		postDataParams.put("Content-Type", "application/x-www-form-urlencoded");
		/* if we have a refresh token */
		if (refreshToken != null) {

			/* we decode it */
			JSONObject decodedServiceToken = KeycloakUtils.getDecodedToken(refreshToken);

			/* we get the expiry timestamp */
			long expiryTime = decodedServiceToken.getLong("exp");

			/* we get the current time */
			long nowTime = LocalDateTime.now().atZone(TimeZone.getDefault().toZoneId()).toEpochSecond();

			/* we calculate the differencr */
			long duration = expiryTime - nowTime;

			/*
			 * if the difference is negative it means the expiry time is less than the
			 * nowTime
			 * if the difference < 180000, it means the token will expire in 3 hours
			 */
			if (duration <= GennySettings.ACCESS_TOKEN_EXPIRY_LIMIT_SECONDS) {

				/* if the refresh token is about to expire, we must re-generate a new one */
				refreshToken = null;
			}
		}
		/*
		 * if we don't have a refresh token, we generate a new token using username and
		 * password
		 */
		if (refreshToken == null) {
			log.info("refreshToken is null");
		}
		if (refreshToken == null) {
			postDataParams.put("username", username);
			postDataParams.put("password", password);
			log.info("using username:" + username + " and password " + password);
			postDataParams.put(OAuth2Constants.GRANT_TYPE, "password");
		} else {
			postDataParams.put("refresh_token", refreshToken);
			postDataParams.put(OAuth2Constants.GRANT_TYPE, "refresh_token");
			log.info("using refresh token");
			log.info(refreshToken);
		}

		postDataParams.put(OAuth2Constants.CLIENT_ID, clientId);
		log.info("using clientId:" + clientId);
		if (!StringUtils.isBlank(secret)) {
			postDataParams.put(OAuth2Constants.CLIENT_SECRET, secret);
			log.info("using secret:" + secret);
		}

		String requestURL = keycloakUrl + "/auth/realms/" + realm + "/protocol/openid-connect/token";
		log.info("using requestUrl:" + requestURL);
		String str = QwandaUtils.performPostCall(requestURL,
				postDataParams);
		log.info("returned str:" + str);

		JsonObject json = new JsonObject(str);
		return json;

	}

	public static JsonObject getToken(String keycloakUrl, String realm, String clientId, String secret,
			String username, String password) throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
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
	public static JSONObject getDecodedToken(final GennyToken bearerToken) {
		return getDecodedToken(bearerToken.getToken());
	}

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

	// TODO: Talk more abut this
	public static String getRealmFromToken(final GennyToken bearerToken) {
		return bearerToken.getString("azp"); // return the realm

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
	public static String resetUserPassword(String userId, GennyToken token, String realm)
			throws ClientProtocolException, IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";

		HttpClient httpClient = new DefaultHttpClient();

		try {

			String keycloakUrl = getKeycloakUrlFromToken(token);

			HttpPut putRequest = new HttpPut(
					keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId + "/reset-password");
			log.info(keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId + "/reset-password");

			putRequest.addHeader("Content-Type", "application/json");
			putRequest.addHeader("Authorization", "Bearer " + token.getToken());

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

	public static String createUser(GennyToken token, String realm, String newUsername,
			String newFirstname, String newLastname, String newEmail)
			throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		String newRealmRoles = "user,offline_access,uma_authorization";
		String newGroupRoles = "/users";
		String password = UUID.randomUUID().toString().substring(0, 10);
		return createUser(token, realm, newUsername,
				newFirstname, newLastname, newEmail, password, newRealmRoles, newGroupRoles);
	}

	public static String createUser(GennyToken token, String realm, String newUsername,
			String newFirstname, String newLastname, String newEmail, String password)
			throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		String newRealmRoles = "user,offline_access,uma_authorization";
		String newGroupRoles = "/users";

		return createUser(token, realm, newUsername,
				newFirstname, newLastname, newEmail, password, newRealmRoles, newGroupRoles);
	}

	// This is the one called from rules to create a keycloak user
	public static String createUser(GennyToken token, String realm, String newUsername,
			String newFirstname, String newLastname, String newEmail, String password, String newRealmRoles,
			String newGroupRoles)
			throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		return createUser(null, token, realm, newUsername,
				newFirstname, newLastname, newEmail, password, newRealmRoles, newGroupRoles);
	}

	// This is the one called from rules to create a keycloak user
	public static String createDummyUser(GennyToken token, String realm)
			throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		String keycloakUrl = getKeycloakUrlFromToken(token);

		String randomCode = UUID.randomUUID().toString().substring(0, 18);
		String randomPassword = "password1"; // UUID.randomUUID().toString().substring(0, 18);
		String json = "{ " + "\"username\" : \"" + randomCode + "\"," + "\"email\" : \"" + randomCode
				+ "@gmail.com\" , "
				+ "\"enabled\" : true, " + "\"emailVerified\" : true, " + "\"firstName\" : \"" + randomCode + "\", "
				+ "\"lastName\" : \"" + randomCode + "\", " + "\"groups\" : [" + " \"users\" " + "], "
				+ "\"requiredActions\" : [\"terms_and_conditions\"], "
				+ "\"realmRoles\" : [\"user\"],\"credentials\": [{"
				+ "\"type\":\"password\","
				+ "\"value\":\"" + randomPassword + "\","
				+ "\"temporary\":true }]}";

		// String json = "{ " +"\"username\" : \"" + randomCode + "\"," + "\"email\" :
		// \"" + randomCode + "@gmail.com\" , "
		// + "\"enabled\" : true, " + "\"emailVerified\" : true, " + "\"firstName\" :
		// \"" + randomCode + "\", "
		// + "\"lastName\" : \"" + randomCode + "\", " + "\"groups\" : [" + " \"users\"
		// " + "],"
		// + "\"realmRoles\" : [\"user\"]}";

		log.info("CreateUserjsonDummy=" + json);

		HttpClient httpClient = new DefaultHttpClient();
		// log.info("Keycloak token used is "+token);
		try {
			String uri = keycloakUrl + "/auth/admin/realms/" + realm + "/users";
			HttpPost post = new HttpPost(uri);
			// HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(keycloakUrl +
			// "/auth/admin/realms/"+realm+"/users"));

			post.addHeader("Content-Type", "application/json");
			post.addHeader("Authorization", "Bearer " + token.getToken());
			log.info("DEBUG, create keycloak user, url:" + uri + ", token:" + token);

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
				// throw new IOException("Email is already taken. Please use a different email
				// address.");
				log.warn("Email is already taken for " + randomCode);
				// fetch existing email user
				String userId = getKeycloakUserId(token, realm, randomCode);
				return userId;
			} else if (statusCode == 401) {
				// throw new IOException("Account is already taken. Please use a different email
				// address.");
				log.warn("Unauthorized token used to create " + randomCode);
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
	// public static String getUserToken(String keycloakUrl,String keycloakUUID,
	// String serviceToken, String realm)
	// throws IOException {
	// keycloakUUID = keycloakUUID.toLowerCase();
	//
	//
	// return KeycloakUtils.getImpersonatedToken(keycloakUrl, realm, keycloakUUID,
	// serviceToken);
	//
	// }

	// This is the one called from rules to create a keycloak user
	public static String updateUser(String keycloakUUID, GennyToken token, String realm, String newUsername,
			String newFirstname, String newLastname, String newEmail, String password, String newRealmRoles,
			String newGroupRoles)
			throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		String keycloakUrl = getKeycloakUrlFromToken(token);
		keycloakUUID = keycloakUUID.toLowerCase();

		String json = "{ " + "\"username\" : \"" + newUsername + "\"," + "\"email\" : \"" + newEmail + "\" , "
				+ "\"enabled\" : true, " + "\"emailVerified\" : true, " + "\"firstName\" : \"" + newFirstname + "\", "
				+ "\"lastName\" : \"" + newLastname + "\", " + "\"groups\" : [" + " \"" + newGroupRoles + "\" " + "],"
				+ "\"realmRoles\" : [" + "\"" + newRealmRoles + "\" " + "]" + "}";

		log.info("CreateUserjson=" + json);

		HttpClient httpClient = new DefaultHttpClient();
		// log.info("Keycloak token used is "+token);
		try {
			HttpPut post = new HttpPut(keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + keycloakUUID);
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
				if (password != null) {
					KeycloakUtils.setPassword(token, realm, keycloakUUID, password, false);
				}
				return keycloakUUID;
			} else if (statusCode == 204) {
				if (password != null) {
					KeycloakUtils.setPassword(token, realm, keycloakUUID, password, false);
				}
				return keycloakUUID;
			} else if (statusCode == 409) {
				// throw new IOException("Email is already taken. Please use a different email
				// address.");
				log.warn("Email is already taken for " + newUsername);
				// fetch existing email user
				return keycloakUUID;
			} else if (statusCode == 401) {
				// throw new IOException("Account is already taken. Please use a different email
				// address.");
				log.warn("Unauthorized token used to create " + newUsername);
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

	public static int updateUserField(String keycloakUUID, GennyToken token, String realm,
			String fieldName, String newValue) throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";

		String keycloakUrl = getKeycloakUrlFromToken(token);
		keycloakUUID = keycloakUUID.toLowerCase();

		String json = "{\"" + fieldName + "\":\"" + newValue + "\"}";
		log.info("Update field " + fieldName + "json=" + json);
		HttpClient httpClient = new DefaultHttpClient();

		int statusCode = -1;

		try {
			HttpPut post = new HttpPut(keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + keycloakUUID);
			post.addHeader("Content-Type", "application/json");
			post.addHeader("Authorization", "Bearer " + token);

			StringEntity postingString = new StringEntity(json);
			post.setEntity(postingString);

			HttpResponse response = httpClient.execute(post);
			statusCode = response.getStatusLine().getStatusCode();
			log.info("StatusCode: " + statusCode);

			if (statusCode == 401) {
				log.error("Unauthorized token used to create " + keycloakUUID);
			} else if (statusCode == 400) {
				log.error("Request is invalid, check request content.");
			}
		} finally {
			httpClient.getConnectionManager().shutdown();
		}

		return statusCode;
	}

	public static int updateUserEmail(String keycloakUUID, GennyToken token, String realm, String newEmail)
			throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		String keycloakUrl = getKeycloakUrlFromToken(token);
		keycloakUUID = keycloakUUID.toLowerCase();

		String json = "{ \"email\" : \"" + newEmail + "\" , "
				+ "\"enabled\" : true, " + "\"emailVerified\" : true}";

		log.info("UpdateUserEmailjson=" + json);

		HttpClient httpClient = new DefaultHttpClient();
		// log.info("Keycloak token used is "+token);
		try {
			HttpPut post = new HttpPut(keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + keycloakUUID);
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
				return statusCode;
			} else if (statusCode == 204) {
				return statusCode;
			} else if (statusCode == 409) {
				// throw new IOException("Email is already taken. Please use a different email
				// address.");
				log.warn("Email is already taken for " + keycloakUUID);
				// fetch existing email user
				return statusCode;
			} else if (statusCode == 401) {
				// throw new IOException("Account is already taken. Please use a different email
				// address.");
				log.warn("Unauthorized token used to create " + keycloakUUID);
				// fetch existing email user
				return statusCode;
			}
			if (entity == null) {
				throw new IOException("We could not update the user EMail. Please try again.");
			} else {
				log.info("Keycloak User ID: " + keycloakUUID);
				return 200;
			}
		}

		finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	// This is the one called from rules to create a keycloak user
	public static String createUser(String keycloakUUID, GennyToken token, String realm, String newUsername,
			String newFirstname, String newLastname, String newEmail, String password, String newRealmRoles,
			String newGroupRoles)
			throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		String keycloakUrl = getKeycloakUrlFromToken(token);

		String idJson = "";
		if (!StringUtils.isBlank(keycloakUUID)) {
			idJson = "\"id\" : \"" + keycloakUUID.trim().toLowerCase() + "\",";
		}
		String json = "{ " + idJson + "\"username\" : \"" + newUsername + "\"," + "\"email\" : \"" + newEmail + "\" , "
				+ "\"enabled\" : true, " + "\"emailVerified\" : true, " + "\"firstName\" : \"" + newFirstname + "\", "
				+ "\"lastName\" : \"" + newLastname + "\", " + "\"groups\" : [" + " \"" + newGroupRoles + "\" " + "],"
				+ "\"realmRoles\" : [" + "\"" + newRealmRoles + "\" " + "]" + "}";

		log.info("CreateUserjson=" + json);

		HttpClient httpClient = new DefaultHttpClient();
		// log.info("Keycloak token used is "+token);
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
				if (content.length() > 0) {
					KeycloakUtils.setPassword(token, realm, content, password, false);
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
				// throw new IOException("Email is already taken. Please use a different email
				// address.");
				log.warn("Email is already taken for " + newUsername);
				// fetch existing email user
				String userId = getKeycloakUserId(token, realm, newUsername);
				return userId;
			} else if (statusCode == 401) {
				// throw new IOException("Account is already taken. Please use a different email
				// address.");
				log.warn("Unauthorized token used to create " + newUsername);
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
	public static String removeUser(GennyToken token, String realm, String userId)
			throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";

		String keycloakUrl = getKeycloakUrlFromToken(token);
		HttpClient httpClient = new DefaultHttpClient();

		try {
			HttpDelete post = new HttpDelete(keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId);

			post.addHeader("Content-Type", "application/json");
			post.addHeader("Authorization", "Bearer " + token);

			HttpResponse response = httpClient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				return "Success";
			} else {
				return "Failed";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Failed";
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	public static String getKeycloakUserId(final GennyToken token, String realm, final String username)
			throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		final List<LinkedHashMap> users = fetchKeycloakUsers(token, realm, username);
		if (!users.isEmpty()) {
			return (String) users.get(0).get("id");
		}
		return null;
	}

	public static List<LinkedHashMap> fetchKeycloakUsers(final GennyToken token, String realm,
			final String username) {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		String keycloakUrl = getKeycloakUrlFromToken(token);
		List<LinkedHashMap> results = new ArrayList<LinkedHashMap>();
		final HttpClient client = new DefaultHttpClient();

		try {
			String encodedUsername = encodeValue(username);
			String uri = keycloakUrl + "/auth/admin/realms/" + realm + "/users?username=" + encodedUsername;
			final HttpGet get = new HttpGet(uri);
			get.addHeader("Authorization", "Bearer " + token);
			try {
				final HttpResponse response = client.execute(get);
				if (response.getStatusLine().getStatusCode() != 200) {
					log.error("Failed to get user from Keycloak, url:" + uri + ", response code:"
							+ response.getStatusLine().getStatusCode() + ", token:" + token);
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
	// public static String getKeycloakUrl() {
	// String keycloakProto =
	// System.getenv("KEYCLOAK_PROTO") != null ? System.getenv("KEYCLOAK_PROTO") :
	// "http://";
	// String keycloakPort =
	// System.getenv("KEYCLOAK_PORT") != null ? System.getenv("KEYCLOAK_PORT") :
	// "8180";
	// String keycloakIP = System.getenv("HOSTIP") != null ? System.getenv("HOSTIP")
	// : "localhost";
	//
	// String keycloakURL = System.getenv("KEYCLOAKURL") != null ?
	// System.getenv("KEYCLOAKURL")
	// : keycloakProto + keycloakIP + ":" + keycloakPort;
	// return keycloakURL;
	// }

	public static int setPassword(GennyToken token, String realm, String userId, String password)
			throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		return setPassword(token, realm, userId, password, false);
	}

	public static int setPassword(GennyToken token, String realm, String userId, String password,
			Boolean askUserToResetPassword)
			throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		String keycloakUrl = getKeycloakUrlFromToken(token);
		String json = "{\"type\": \"password\", " + "\"temporary\": \"" + (askUserToResetPassword ? "true" : "false")
				+ "\",\"value\": \"" + password + "\"" + "}";

		int responseCode = 0;
		userId = userId.toLowerCase();

		try {
			if (userId != null) {
				HttpClient httpClient = new DefaultHttpClient();

				String requestURL = keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId
						+ "/reset-password";
				HttpPut putRequest = new HttpPut(requestURL);

				log.info(requestURL);

				putRequest.addHeader("Content-Type", "application/json");
				putRequest.addHeader("Authorization", "Bearer " + token);

				putRequest.setEntity(new StringEntity(json));

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
				log.info("reset-password statusCode is " + statusCode + " with userId=" + userId);
				return statusCode;
			} else {
				log.error("userId is null! Not contacting keycloak server...");
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return responseCode;
	}

	public static void updateUserDetails(BaseEntityUtils beUtils, BaseEntity userBE)
	/**
	 * Initialising User in keycloak and setup random password
	 **/
	{
		if (userBE == null) {
			log.error(ANSIColour.RED + "User BE is NULL" + ANSIColour.RESET);
			return;
		}

		String uuid = userBE.getValue("PRI_UUID", null);
		if (uuid == null) {
			log.error(ANSIColour.RED + "No PRI_UUID found for user " + userBE.getCode() + ANSIColour.RESET);
			return;
		}

		String firstname = userBE.getValue("PRI_FIRSTNAME", null);
		if (firstname == null) {
			log.error(ANSIColour.RED + "No PRI_FIRSTNAME found for user " + userBE.getCode() + ANSIColour.RESET);
			return;
		}

		String lastname = userBE.getValue("PRI_LASTNAME", null);
		if (lastname == null) {
			log.error(ANSIColour.RED + "No PRI_LASTNAME found for user " + userBE.getCode() + ANSIColour.RESET);
			return;
		}

		String email = userBE.getValue("PRI_EMAIL", null);
		if (email == null) {
			log.error(ANSIColour.RED + "No PRI_EMAIL found for user " + userBE.getCode() + ANSIColour.RESET);
			return;
		}

		// Update The users email, first and last name, also fetch userID
		try {
			updateUser(uuid, beUtils.getServiceToken(), beUtils.getServiceToken().getRealm(), email,
					firstname, lastname, email, null, "user", "users");
		} catch (IOException e) {
			log.error(ANSIColour.RED + e.getStackTrace().toString() + ANSIColour.RESET);
		}
		return;
	}

	public static String generateRandomPassword(BaseEntityUtils beUtils, BaseEntity userBE)
	/**
	 * Setup a random password in keycloak
	 **/
	{
		if (userBE == null) {
			log.error(ANSIColour.RED + "User BE is NULL" + ANSIColour.RESET);
			return null;
		}

		String uuid = userBE.getValue("PRI_UUID", null);
		if (uuid == null) {
			log.error(ANSIColour.RED + "No PRI_UUID found for user " + userBE.getCode() + ANSIColour.RESET);
			return null;
		}

		/* Generate a random 15 char password */
		char[] allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789^$?!@#%&".toCharArray();
		String newPassword = new SecureRandom().ints(15, 0, allowed.length).mapToObj(i -> String.valueOf(allowed[i]))
				.collect(Collectors.joining());

		// Update The Keycloak Password
		try {
			setPassword(beUtils.getServiceToken(), beUtils.getServiceToken().getRealm(), uuid, newPassword,
					true);
		} catch (IOException e) {
			log.error(ANSIColour.RED + e.getStackTrace().toString() + ANSIColour.RESET);
			return null;
		}

		return newPassword;
	}

	public String createEncryptedPassword(String key, final String customercode, final String password) {
		String newkey = null;
		if (key.length() > 16) {
			newkey = key.substring(0, 16);
		} else {
			newkey = StringUtils.rightPad(key, 16, '*');
		}
		String initVector = "PRJ_" + customercode.toUpperCase();
		initVector = StringUtils.rightPad(initVector, 16, '*');

		String encryptedPassword = encrypt(newkey, initVector, password);
		if (!key.equals(newkey)) {
			log.info("NEW KEY = [" + newkey + "]");
			;
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
			// log.info("encrypted string: "
			// + Base64.encodeBase64String(encrypted));

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

	public static String sendVerifyEmail(final String username, final GennyToken serviceToken) {
		return sendVerifyEmail(serviceToken.getRealm(), username, serviceToken);
	}

	public static String sendVerifyEmail(String realm, final String username, final GennyToken servicetoken) {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		String keycloakUrl = getKeycloakUrlFromToken(servicetoken);
		String userId;
		try {
			userId = getKeycloakUserId(servicetoken, realm, username);
			if (userId != null) {
				HttpClient httpClient = new DefaultHttpClient();

				HttpPut putRequest = new HttpPut(
						keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId + "/send-verify-email");

				log.info(keycloakUrl + "/auth/admin/realms/" + "internmatch" + "/users/" + userId
						+ "/send-verify-email");

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
				log.info("sendVerifyMail statusCode is " + statusCode + " with userId=" + userId);
				if ((statusCode == 200) || (statusCode == 201)) {
					return userId;
				}
			} else {
				log.error("Could not retrieve userId from " + keycloakUrl + " for " + username);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;

	}

	private void sendVerifyTestMail(GennyToken token, String emailusername, String firstname, String lastname) {

		String password = UUID.randomUUID().toString().substring(0, 8);
		String userId;
		try {
			userId = KeycloakUtils.createUser(token, token.getRealm(), emailusername, firstname, lastname,
					emailusername, password,
					"user", "user");
		} catch (IOException e) {
		}
		userId = KeycloakUtils.sendVerifyEmail(token.getRealm(), emailusername, token);
		System.out.println("UserId=" + userId);
	}

	private void sendVerifyTestMail(GennyToken token, String username, String domain, String firstname, String lastname) {
		LocalDateTime now = LocalDateTime.now();
		String mydatetime = new SimpleDateFormat("yyyyMMddHHmmss").format(now.toLocalDate());
		// System.out.println(username+" serviceToken=" + token);
		String emailusername = username + "+" + mydatetime + "@" + domain;

		sendVerifyTestMail(token, emailusername, firstname, lastname);
	}

	public static String getKeycloakUUIDByUserCode(String code, HashMap<String, String> userCodeUUIDMapping) {
		String keycloakUUID = null;
		if (userCodeUUIDMapping.containsKey(code)) {
			keycloakUUID = userCodeUUIDMapping.get(code);
			log.debug(String.format("DEBUG:Find user baseentity code:%s, update to keycloak uuid:%s",
					code, keycloakUUID));
		} else {
			keycloakUUID = code;
			log.debug(String.format("DEBUG:Can not find user baseentity code:%s, set keycloak uuid:%s",
					code, keycloakUUID));
		}
		return keycloakUUID;
	}

	public static Integer getKeycloakUserCount(String keycloakUrl, String realm, String servicePassword) {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		Integer count = -1;
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			String accessToken = getAccessToken(keycloakUrl, realm, "admin-cli", null, "service", servicePassword);
			HttpGet getUserCount = new HttpGet(keycloakUrl + "/auth/admin/realms/" + realm + "/users/count");
			getUserCount.addHeader("Authorization", "Bearer " + accessToken);
			HttpResponse response = client.execute(getUserCount);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new IOException("Get keycloak user response code:" + response.getStatusLine().getStatusCode());
			}
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			count = JsonSerialization.readValue(is, Integer.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count;
	}

	public static HashMap<String, String> getUsersByRealm(String keycloakUrl, String realm, String servicePassword) {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		HashMap<String, String> userCodeUUIDMapping = new HashMap<>();
		List<LinkedHashMap> results = new ArrayList<>();

		Integer count = getKeycloakUserCount(keycloakUrl, realm, servicePassword);
		log.info("Total keycloak user:" + count);

		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			String accessToken = getAccessToken(keycloakUrl, realm, "admin-cli", null, "service", servicePassword);

			int loopCount = count / GennySettings.MAX_KEYCLOAK_USER_PER_CALL;
			for (int index = 0; index <= loopCount; index++) {
				int startNumber = index * GennySettings.MAX_KEYCLOAK_USER_PER_CALL;
				HttpGet get = new HttpGet(keycloakUrl + "/auth/admin/realms/" + realm
						+ "/users?first=" + startNumber
						+ "&max=" + GennySettings.MAX_KEYCLOAK_USER_PER_CALL);
				get.addHeader("Authorization", "Bearer " + accessToken);
				HttpResponse response = client.execute(get);
				if (response.getStatusLine().getStatusCode() != 200) {
					throw new IOException(
							"Get keycloak user response code:" + response.getStatusLine().getStatusCode());
				}
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				results.addAll(JsonSerialization.readValue(is, (new ArrayList<UserRepresentation>()).getClass()));
				log.info("Get user range:" + startNumber + ":"
						+ (startNumber + GennySettings.MAX_KEYCLOAK_USER_PER_CALL - 1));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (LinkedHashMap userMap : results) {
			String code = "";
			String username = (String) userMap.get("username");
			String email = (String) userMap.get("email");
			// Username is Email address
			if (username.contains("@")) {
				code = QwandaUtils.getNormalisedUsername("PER_" + username);
			} else {
				code = QwandaUtils.getNormalisedUsername("PER_" + email);
			}
			String id = (String) userMap.get("id");
			String uuid = "PER_" + id.toUpperCase();
			if (userCodeUUIDMapping.containsKey(code)) {
				log.error(String.format("Duplicate user in keycloak, user code:%s, user name:%s, email:%s.",
						code, username, email));
			} else {
				userCodeUUIDMapping.put(code, uuid);
			}
		}
		log.info("Get " + results.size() + " keycloak users");
		return userCodeUUIDMapping;
	}

	public static String getImpersonatedToken(String keycloakUrl, String realm, BaseEntity project, BaseEntity userBE,
			GennyToken exchangedToken) throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";

		if (userBE == null) {
			log.error(ANSIColour.RED + "User BE is NULL" + ANSIColour.RESET);
			return null;
		}

		String uuid = userBE.getValue("PRI_UUID", null);
		if (uuid == null) {
			log.warn(ANSIColour.YELLOW + "No PRI_UUID found for user " + userBE.getCode()
					+ ", attempting to use PRI_EMAIL instead" + ANSIColour.RESET);
			uuid = userBE.getValue("PRI_EMAIL", null);
			if (uuid == null) {
				log.error(ANSIColour.RED + "No PRI_EMAIL found for user " + userBE.getCode() + ANSIColour.RESET);
				return null;
			}
		}

		return getImpersonatedToken(keycloakUrl, realm, project, uuid, exchangedToken);
	}

	public static String getImpersonatedToken(String keycloakUrl, String realm, BaseEntity project, String uuid,
			GennyToken exchangedToken) throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";

		String keycloakJson = project.getValueAsString("ENV_KEYCLOAK_JSON");
		JsonObject json = new JsonObject(keycloakJson);
		JsonObject credentials = json.getJsonObject("credentials");
		String secret = credentials.getString("secret");

		return getImpersonatedToken(keycloakUrl, realm, realm, secret, uuid, exchangedToken);

	}

	public static String getImpersonatedToken(String keycloakUrl, String realm, String clientId, String secret,
			String username, GennyToken exchangedToken) throws IOException {
		// TODO: Please for the love of god lets fix this
		realm = "internmatch";

		HttpClient httpClient = new DefaultHttpClient();

		try {
			ArrayList<NameValuePair> postParameters;

			try {
				// // this needs -Dkeycloak.profile.feature.token_exchange=enabled
				HttpPost post = new HttpPost(keycloakUrl + "/auth/realms/" + realm + "/protocol/openid-connect/token");
				postParameters = new ArrayList<NameValuePair>(); // urn:ietf:params:oauth:grant-type:token-exchange
				postParameters
						.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange"));
				postParameters.add(new BasicNameValuePair("client_id", clientId));
				if (secret != null) {
					postParameters.add(new BasicNameValuePair("client_secret", secret));
				}
				postParameters.add(new BasicNameValuePair("subject_token", exchangedToken.getToken()));
				// postParameters.add(new
				// BasicNameValuePair("client_auth_method","client-secret"));
				// postParameters.add(new BasicNameValuePair("audience", "target-client"));
				postParameters.add(new BasicNameValuePair("requested_subject", username));
				// postParameters.add(new BasicNameValuePair("requested_token_type",
				// "urn:ietf:params:oauth:token-type:access_token"));
				post.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));

				post.addHeader("Content-Type", "application/x-www-form-urlencoded");
				post.addHeader("Authorization", "Bearer " + exchangedToken);

				HttpResponse response = httpClient.execute(post);

				int statusCode = response.getStatusLine().getStatusCode();
				log.info("StatusCode: " + statusCode);

				HttpEntity entity = response.getEntity();

				String content = null;
				if (statusCode != 200) {
					content = getContent(entity);
					throw new IOException("" + statusCode + " " + content);
				}
				if (entity == null) {
					throw new IOException("Null Entity");
				} else {
					content = getContent(entity);
					log.info("IMPERSONATION2 content=" + content);
					JsonObject jsonToken = new JsonObject(content);
					String token = jsonToken.getString("access_token");
					return token;
					// Header[] cookies = response.getHeaders("Set-Cookie");
					// if (cookies.length > 0) {
					// for (Header cookie : cookies) {
					// String value = cookie.getValue();
					// if (value.startsWith("KEYCLOAK_IDENTITY=")) {
					// if (!value.startsWith("KEYCLOAK_IDENTITY=;")) {
					// String token = cookie.getValue();
					//
					// token = token.substring("KEYCLOAK_IDENTITY=".length());
					// log.info(token);
					// // return token;
					// }
					// }
					// }
					// }
				}

				//
				// System.out.println(content);
			} catch (Exception ee) {
				System.out.println(ee.getMessage());
			} finally {
				httpClient.getConnectionManager().shutdown();
			}
			//
			// System.out.println(content);
		} catch (Exception ee) {

		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return null;
	}

	public static String executeActions(String keycloakUrl, String realm, String clientId, String secret,
			Integer lifespan, String uuid, String redirectUrl, List<String> actions, String exchangedToken)
			throws IOException {

		// TODO: Please for the love of god lets fix this
		realm = "internmatch";
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String urlResetPassword = keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + uuid
				+ "/execute-actions-email?redirect_uri=" + redirectUrl + "&client_id=" + clientId + "&lifespan="
				+ lifespan;
		HttpPut putRequest = new HttpPut(urlResetPassword);
		putRequest.addHeader("Authorization", "bearer " + exchangedToken);
		putRequest.addHeader("content-type", MediaType.APPLICATION_JSON);
		putRequest.setHeader("Accept", MediaType.APPLICATION_JSON);
		String actionsArray = "[";
		for (String action : actions) {
			actionsArray += "\"" + action + "\",";
		}
		actionsArray = actionsArray.substring(0, actionsArray.length() - 1);
		actionsArray += "]";
		StringEntity jSonEntity = new StringEntity(actionsArray);
		putRequest.setEntity(jSonEntity);
		CloseableHttpResponse response2 = httpclient.execute(putRequest);
		return "OK";
	}

	public static String sendGET(String url, String token) throws IOException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.addRequestProperty("Content-Type", "application/json");
		con.addRequestProperty("Authorization", "Bearer " + token);

		// con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			return response.toString();
		} else {
			return null;
		}

	}

	public static String sendDELETE(String url, String token) throws IOException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("DELETE");
		// con.addRequestProperty("Content-Type", "application/json");
		con.addRequestProperty("Authorization", "Bearer " + token);

		// con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			return response.toString();
		} else {
			return null;
		}

	}
}
