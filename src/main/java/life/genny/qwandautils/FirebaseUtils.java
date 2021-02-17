package life.genny.qwandautils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.MediaType;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class FirebaseUtils {
	public static String send(final String userNotificationToken, final String title, final String body,
			final String apiKey) {

		CloseableHttpResponse response2 = null;
		String firebaseUrl = "https://fcm.googleapis.com/fcm/send";
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost postRequest = new HttpPost(firebaseUrl);
		postRequest.addHeader("Authorization", "key=" + apiKey);
		postRequest.addHeader("Content-Type", MediaType.APPLICATION_JSON);
		postRequest.setHeader("Accept", MediaType.APPLICATION_JSON);
		String actionsArray = "{\"notification\": {\"body\": \"" + body + "\",\"title\": \"" + title
				+ "\"}, \"priority\": \"high\", \"data\": {\"click_action\": \"FLUTTER_NOTIFICATION_CLICK\", \"id\": \"1\", \"status\": \"done\"}, \"to\": \""
				+ userNotificationToken + "\"}";
	
		
		try {
			StringEntity jSonEntity = new StringEntity(actionsArray);
			postRequest.setEntity(jSonEntity);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			response2 = httpclient.execute(postRequest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response2.toString();
	}

	
}
