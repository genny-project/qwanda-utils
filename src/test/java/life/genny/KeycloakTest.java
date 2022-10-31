package life.genny;

import life.genny.qwandautils.KeycloakUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.util.JsonSerialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class KeycloakTest {



  private static final Logger log = org.apache.logging.log4j.LogManager
      .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

  // @Test
  // // run the keycloak docker located in the root of the project runKeycloak.sh
  // public void tokenTest() {
  // try {
  // AccessTokenResponse accessToken = KeycloakUtils.getAccessToken("http://localhost:8180",
  // "wildfly-swarm-keycloak-example", "curl", "056b73c1-7078-411d-80ec-87d41c55c3b4", "user1",
  // "password1");
  // String tokenString = accessToken.getToken();
  // log.info(tokenString);
  //
  // } catch (IOException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  //
  // }

//  @Test
  public void decodeTokenTest() {
    final String token =
        "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJrcE9kQzY3NGdRRUJJd2hQUVRaemJsbTRXN3N4ZG85NnVaTDNEUkFPczdvIn0.eyJqdGkiOiI0MjVlMjM4ZS0wOGFhLTQzNGUtOWYzNC1kOTI2MDI2NzgzYzUiLCJleHAiOjE1MDc4NzM5MTAsIm5iZiI6MCwiaWF0IjoxNTA3ODczNjEwLCJpc3MiOiJodHRwOi8vMTAuMS4xLjczOjgxODAvYXV0aC9yZWFsbXMvd2lsZGZseS1zd2FybS1rZXljbG9hay1leGFtcGxlIiwiYXVkIjoiY3VybCIsInN1YiI6IjZlYTcwNWEzLWY1MjMtNDVhNC1hY2EzLWRjMjJlNmMyNGY0ZiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImN1cmwiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiI5M2JlNWVlNy1kNjZjLTQ5NzEtYTM5ZC04MTdjNWNlMDUwNTIiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly8xOTIuMTY4LjY0Ljc6ODA4OC8qIiwiaHR0cDovL2xvY2FsaG9zdDo4MjgwIiwiaHR0cDovL2xvY2FsaG9zdDo1MDAwIiwiaHR0cDovL2xvY2FsaG9zdCIsImh0dHA6Ly8xOTIuMTY4Ljk5LjEwMDo1MDAwIiwiaHR0cDovL2xvY2FsaG9zdDo4MDg4IiwiaHR0cDovL2xvY2FsaG9zdDozMDAwIiwiaHR0cDovLzEwLjY0LjAuNjo4MjgwIiwiaHR0cDovLzE5Mi4xNjguOTkuMTAwOjMwMDAiLCJodHRwOi8vbG9jYWxob3N0OjU4MDgwIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ1bWFfYXV0aG9yaXphdGlvbiIsInVzZXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50Iiwidmlldy1wcm9maWxlIl19fSwibmFtZSI6IkphbWVzIEJvbmQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ1c2VyMSIsImdpdmVuX25hbWUiOiJKYW1lcyIsImZhbWlseV9uYW1lIjoiQm9uZCIsImVtYWlsIjoiYWRhbWNyb3c2NEBnbWFpbC5jb20ifQ.QKAOG3EHncby8jufaYmo1hxDWjq-s_yHRpHS2U6ZkLVp4d4kNBDDhIyEFhNyPsqBVZ_nt-w67TbeO6bz2V_C4p7diAGk6jgefsYtqzlGCtHymlq7SJp-sEiPtjrQg84C0Lj3GIqJfnZucGTBUGyKADLE35-w8G7p3YjGz4vI4sLOYFWlX47OmHRjMb4z-Q6dSjAdTjeNmG0a1TK3IPB4onA_LPkuJdXcGwTVvG_iWssf-uL4D_Y78luj7u5Jgs_RDjN-fOhYkbuJH9fuZ6eNK38-69Mqv76HBXdeclCWl2_B2_uiY-f1ukwHZy567v4SBcM_QGHuOVPLVoUee2mm3Q";

    KeycloakUtils.getDecodedToken(token);

 
  }

//  @Test
  public void mapTokenTest() {
    final String token =
        "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJrcE9kQzY3NGdRRUJJd2hQUVRaemJsbTRXN3N4ZG85NnVaTDNEUkFPczdvIn0.eyJqdGkiOiI0MjVlMjM4ZS0wOGFhLTQzNGUtOWYzNC1kOTI2MDI2NzgzYzUiLCJleHAiOjE1MDc4NzM5MTAsIm5iZiI6MCwiaWF0IjoxNTA3ODczNjEwLCJpc3MiOiJodHRwOi8vMTAuMS4xLjczOjgxODAvYXV0aC9yZWFsbXMvd2lsZGZseS1zd2FybS1rZXljbG9hay1leGFtcGxlIiwiYXVkIjoiY3VybCIsInN1YiI6IjZlYTcwNWEzLWY1MjMtNDVhNC1hY2EzLWRjMjJlNmMyNGY0ZiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImN1cmwiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiI5M2JlNWVlNy1kNjZjLTQ5NzEtYTM5ZC04MTdjNWNlMDUwNTIiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly8xOTIuMTY4LjY0Ljc6ODA4OC8qIiwiaHR0cDovL2xvY2FsaG9zdDo4MjgwIiwiaHR0cDovL2xvY2FsaG9zdDo1MDAwIiwiaHR0cDovL2xvY2FsaG9zdCIsImh0dHA6Ly8xOTIuMTY4Ljk5LjEwMDo1MDAwIiwiaHR0cDovL2xvY2FsaG9zdDo4MDg4IiwiaHR0cDovL2xvY2FsaG9zdDozMDAwIiwiaHR0cDovLzEwLjY0LjAuNjo4MjgwIiwiaHR0cDovLzE5Mi4xNjguOTkuMTAwOjMwMDAiLCJodHRwOi8vbG9jYWxob3N0OjU4MDgwIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ1bWFfYXV0aG9yaXphdGlvbiIsInVzZXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50Iiwidmlldy1wcm9maWxlIl19fSwibmFtZSI6IkphbWVzIEJvbmQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ1c2VyMSIsImdpdmVuX25hbWUiOiJKYW1lcyIsImZhbWlseV9uYW1lIjoiQm9uZCIsImVtYWlsIjoiYWRhbWNyb3c2NEBnbWFpbC5jb20ifQ.QKAOG3EHncby8jufaYmo1hxDWjq-s_yHRpHS2U6ZkLVp4d4kNBDDhIyEFhNyPsqBVZ_nt-w67TbeO6bz2V_C4p7diAGk6jgefsYtqzlGCtHymlq7SJp-sEiPtjrQg84C0Lj3GIqJfnZucGTBUGyKADLE35-w8G7p3YjGz4vI4sLOYFWlX47OmHRjMb4z-Q6dSjAdTjeNmG0a1TK3IPB4onA_LPkuJdXcGwTVvG_iWssf-uL4D_Y78luj7u5Jgs_RDjN-fOhYkbuJH9fuZ6eNK38-69Mqv76HBXdeclCWl2_B2_uiY-f1ukwHZy567v4SBcM_QGHuOVPLVoUee2mm3Q";

    KeycloakUtils.getJsonMap(token);

    // log.info(jsonMap);

  }


public  List<LinkedHashMap>  fetchAllKeycloakUsers()
{
	String realm = System.getenv("PROJECT_REALM");
	if (realm != null) {
		String keycloakUrl = System.getenv("KEYCLOAK_URL");
		String secret = System.getenv("SECRET");
		String username = System.getenv("USERNAME");
		String password = System.getenv("PASSWORD");
		log.info("Realm Users to be fetched from "+keycloakUrl+" realm="+realm);
		String token = null;
		try {
			token = getToken(realm, keycloakUrl, secret,username, password).getToken();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		   // log.info(json);
	    final HttpClient client = new DefaultHttpClient();
	    try {
	      final HttpGet get =
	          new HttpGet(keycloakUrl + "/admin/realms/" + realm + "/users");
	      get.addHeader("Authorization", "Bearer " + token);
	      try {
	        final HttpResponse response = client.execute(get);
	        if (response.getStatusLine().getStatusCode() != 200) {
	          throw new IOException();
	        }
	        final HttpEntity entity = response.getEntity();
	        final InputStream is = entity.getContent();
	        try {
	          return JsonSerialization.readValue(is, (new ArrayList<UserRepresentation>()).getClass());
	        } finally {
	          is.close();
	        }
	      } catch (final IOException e) {
	        throw new RuntimeException(e);
	      }
	    } finally {
	      client.getConnectionManager().shutdown();
	    }
		
	}
	return null;
}

private AccessTokenResponse getToken(String realm, String keycloakUrl, String secret,String username, String password) throws IOException {

    final HttpClient httpClient = new DefaultHttpClient();

    try {
      final HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(keycloakUrl + "/auth")
          .path(ServiceUrlConstants.TOKEN_PATH).build(realm));

      post.addHeader("Content-Type", "application/x-www-form-urlencoded");

      final List<NameValuePair> formParams = new ArrayList<NameValuePair>();
      formParams.add(new BasicNameValuePair("username", username));
      formParams.add(new BasicNameValuePair("password", password));
      formParams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "password"));
      formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, "admin-cli"));
      if (!secret.equals("public")) {
        formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_SECRET, secret));
      }
      final UrlEncodedFormEntity form = new UrlEncodedFormEntity(formParams, "UTF-8");

      post.setEntity(form);

      final HttpResponse response = httpClient.execute(post);

      final int statusCode = response.getStatusLine().getStatusCode();
      final HttpEntity entity = response.getEntity();
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

}
