package life.genny.qwandautils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
// import org.apache.http.HttpEntity;
// import org.apache.http.HttpResponse;
// import org.apache.http.NameValuePair;
// import org.apache.http.client.HttpClient;
// import org.apache.http.client.entity.UrlEncodedFormEntity;
// import org.apache.http.client.methods.HttpGet;
// import org.apache.http.client.methods.HttpPost;
// import org.apache.http.impl.client.DefaultHttpClient;
// import org.apache.http.message.BasicNameValuePair;
//// import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.util.JsonSerialization;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;



public class KeycloakService {

  String keycloakUrl = null;
  String realm = null;
  String username = null;
  String password = null;

  String clientid = null;
  String secret = null;

  AccessTokenResponse accessToken = null;
  Keycloak keycloak = null;

  public KeycloakService(final String keycloakUrl, final String realm, final String username,
      final String password, final String clientid, final String secret) throws IOException {


    this.keycloakUrl = keycloakUrl;
    this.realm = realm;
    this.username = username;
    this.password = password;
    this.clientid = clientid;
    this.secret = secret;


    accessToken = getToken();

  }

  //
  private AccessTokenResponse getToken() throws IOException {

    final HttpClient httpClient = new DefaultHttpClient();

    try {
      final HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(keycloakUrl + "/auth")
          .path(ServiceUrlConstants.TOKEN_PATH).build(realm));

      post.addHeader("Content-Type", "application/x-www-form-urlencoded");

      final List<NameValuePair> formParams = new ArrayList<NameValuePair>();
      formParams.add(new BasicNameValuePair("username", username));
      formParams.add(new BasicNameValuePair("password", password));
      formParams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "password"));
      formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, "security-admin-console"));
      formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_SECRET, secret));
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

  public List<LinkedHashMap> fetchKeycloakUsers() {
    final HttpClient client = new DefaultHttpClient();
    try {
      final HttpGet get =
          new HttpGet(this.keycloakUrl + "/auth/admin/realms/" + this.realm + "/users");
      get.addHeader("Authorization", "Bearer " + this.accessToken.getToken());
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
}
