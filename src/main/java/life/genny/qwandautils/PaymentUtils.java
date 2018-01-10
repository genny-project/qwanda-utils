package life.genny.qwandautils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.Base64;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PaymentUtils {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	@SuppressWarnings("unchecked")
	public static String createAuthKey(String tenant, String paymentToken, String paymentSecret) {

		JSONObject authObj = new JSONObject();
		authObj.put("tenant", tenant);
		authObj.put("token", paymentToken);
		authObj.put("secret", paymentSecret);

		String encodedAuthString = base64Encoder(authObj.toJSONString());

		return encodedAuthString;
	}

	public static String base64Encoder(String plainString) {

		String encodedString = null;
		try {
			encodedString = Base64.getEncoder().encodeToString(plainString.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println("encoded string ::" + encodedString);
		return encodedString;
	}

	public static JSONObject base64Decoder(String plainString) {

		String decodedString = null;
		JSONParser parser = new JSONParser();
		JSONObject authobj = new JSONObject();
		try {
			decodedString = new String(Base64.getDecoder().decode(plainString));
			System.out.println("decoded string ::"+decodedString);
			authobj = (JSONObject) parser.parse(decodedString);
			System.out.println("decoded object ::" + authobj);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		return authobj;
	}
	
	
	public static String apiPostPaymentEntity(final String postUrl, final String entityString, final String authToken)
			throws IOException {
		String retJson = "";
		//final HttpClient client = new DefaultHttpClient();
		final HttpClient client = HttpClientBuilder.create().build();

		final HttpPost post = new HttpPost(postUrl);
		post.addHeader("Authorization", authToken); 

		final StringEntity input = new StringEntity(entityString);
		input.setContentType("application/json");
		post.setEntity(input);
		final HttpResponse response = client.execute(post);
		final BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			retJson += line;
			;
		}
		return retJson;
	}
	
	
	public static String apiGetPaymentResponse(final String getUrl, final String authToken)
			throws ClientProtocolException, IOException {
		String retJson = "";
		log.debug("GET:" + getUrl + ":");
		final HttpClient client = HttpClientBuilder.create().build();
		final HttpGet request = new HttpGet(getUrl);
		if (authToken != null) {
			request.addHeader("Authorization", authToken);
		}
		final HttpResponse response = client.execute(request);
		BufferedReader rd = null;
		
		try {
			rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				retJson += line;
				;
			}
		} catch (NullPointerException e) {
			return null;
		}

		return retJson;
	}

}
