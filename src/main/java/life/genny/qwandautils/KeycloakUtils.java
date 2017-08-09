package life.genny.qwandautils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.keycloak.OAuth2Constants;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.JsonSerialization;

public class KeycloakUtils {
	
	static public String getToken(String keycloakUrl, String realm, String clientId, String secret, String username, String password) throws IOException {
 		
		return getAccessToken(keycloakUrl,realm,clientId,secret,username,password).getToken();
	}
	
	static public  AccessTokenResponse getAccessToken(String keycloakUrl, String realm, String clientId, String secret, String username, String password) throws IOException {

	    HttpClient httpClient = new DefaultHttpClient();

	    try {
	        HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(keycloakUrl+"/auth")
	                .path(ServiceUrlConstants.TOKEN_PATH).build(realm));
	        System.out.println("url token post="+keycloakUrl+"/auth"+",tokenpath="+ServiceUrlConstants.TOKEN_PATH+":realm="+realm+":clientid="+clientId+":secret"+secret+":un:"+username+"pw:"+password);;
	        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
	        
	        List <NameValuePair> formParams = new ArrayList <NameValuePair>();
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
	            throw new IOException(""+statusCode);
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
	
	public static String getContent(HttpEntity httpEntity) throws IOException {
	    if (httpEntity == null) return null;
	    InputStream is = httpEntity.getContent();
	    try {
	        ByteArrayOutputStream os = new ByteArrayOutputStream();
	        int c;
	        while ((c = is.read()) != -1) {
	            os.write(c);
	        }
	        byte[] bytes = os.toByteArray();
	        String data = new String(bytes);
	        return data;
	    } finally {
	        try {
	            is.close();
	        } catch (IOException ignored) {

	        }
	    }

	}
}
