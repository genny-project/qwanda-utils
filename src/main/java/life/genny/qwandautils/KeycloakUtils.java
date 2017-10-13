package life.genny.qwandautils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
// import org.keycloak.OAuth2Constants;
// import org.keycloak.common.util.KeycloakUriBuilder;
// import org.keycloak.constants.ServiceUrlConstants;
// import org.keycloak.representations.AccessTokenResponse;
// import org.keycloak.util.JsonSerialization;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class KeycloakUtils {

  // static public String getToken(String keycloakUrl, String realm, String clientId, String secret,
  // String username, String password) throws IOException {
  //
  // return getAccessToken(keycloakUrl,realm,clientId,secret,username,password).getToken();
  // }

  // static public AccessTokenResponse getAccessToken(String keycloakUrl, String realm, String
  // clientId, String secret, String username, String password) throws IOException {
  //
  // HttpClient httpClient = new DefaultHttpClient();
  //
  // try {
  // HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(keycloakUrl+"/auth")
  // .path(ServiceUrlConstants.TOKEN_PATH).build(realm));
  // System.out.println("url token
  // post="+keycloakUrl+"/auth"+",tokenpath="+ServiceUrlConstants.TOKEN_PATH+":realm="+realm+":clientid="+clientId+":secret"+secret+":un:"+username+"pw:"+password);;
  // post.addHeader("Content-Type", "application/x-www-form-urlencoded");
  //
  // List <NameValuePair> formParams = new ArrayList <NameValuePair>();
  // formParams.add(new BasicNameValuePair("username", username));
  // formParams.add(new BasicNameValuePair("password", password));
  // formParams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "password"));
  // formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, clientId));
  // formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_SECRET, secret));
  // UrlEncodedFormEntity form = new UrlEncodedFormEntity(formParams, "UTF-8");
  //
  // post.setEntity(form);
  //
  // HttpResponse response = httpClient.execute(post);
  //
  // int statusCode = response.getStatusLine().getStatusCode();
  // HttpEntity entity = response.getEntity();
  // String content = null;
  // if (statusCode != 200) {
  // content = getContent(entity);
  // throw new IOException(""+statusCode);
  // }
  // if (entity == null) {
  // throw new IOException("Null Entity");
  // } else {
  // content = getContent(entity);
  // }
  // return JsonSerialization.readValue(content, AccessTokenResponse.class);
  // } finally {
  // httpClient.getConnectionManager().shutdown();
  // }
  // }

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
      System.out.println(
          "bearerToken=" + bearerToken + "  decodedJson=" + decodedJson + ":" + e1.getMessage());
    }
    return jsonObj;
  }


  public static Map<String, Object> getJsonMap(final String json) {
    Map<String, Object> map = new HashMap<String, Object>();

    try {

      final ObjectMapper mapper = new ObjectMapper();


      // convert JSON string to Map
      map = mapper.readValue(json, new TypeReference<Map<String, String>>() {});


    } catch (final JsonGenerationException e) {
      e.printStackTrace();
    } catch (final JsonMappingException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }

    return map;
  }



}
