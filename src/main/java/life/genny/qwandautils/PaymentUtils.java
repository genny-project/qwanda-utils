package life.genny.qwandautils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;

import life.genny.qwanda.entity.BaseEntity;

public class PaymentUtils {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	
	final static Gson gson = new Gson();
	
	
	public static String getAssemblyAuthKey() {
		
		String paymentMarketPlace = System.getenv("PAYMENT_MARKETPLACE_NAME");
	    String paymentToken = System.getenv("PAYMENT_TOKEN");
	    String paymentSecret = System.getenv("PAYMENT_SECRET"); 
		
		org.json.simple.JSONObject authObj = new org.json.simple.JSONObject();
		authObj.put("tenant", paymentMarketPlace);
		authObj.put("token", paymentToken);
		authObj.put("secret", paymentSecret);
		
		System.out.println("assembly auth obj::"+authObj);
		
		String encodedAuthString = base64Encoder(authObj.toJSONString());

		return encodedAuthString;
	}

	public static String base64Encoder(String plainString) {

		String encodedString = null;
		try {
			encodedString = Base64.getEncoder().encodeToString(plainString.getBytes("utf-8"));
			System.out.println("encoded string ::" + encodedString);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return encodedString;
	}

	public static org.json.simple.JSONObject base64Decoder(String plainString) {

		String decodedString = null;
		JSONParser parser = new JSONParser();
		org.json.simple.JSONObject authobj = new org.json.simple.JSONObject();

		decodedString = new String(Base64.getDecoder().decode(plainString));
		System.out.println("decoded string ::" + decodedString);
		try {
			authobj = (org.json.simple.JSONObject) parser.parse(decodedString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		System.out.println("decoded object ::" + authobj);

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
	
	
	public static String apiPutPaymentEntity(final String postUrl, final String entityString, final String authToken)
			throws IOException {
		String retJson = "";
		//final HttpClient client = new DefaultHttpClient();
		final HttpClient client = HttpClientBuilder.create().build();

		final HttpPut put = new HttpPut(postUrl);
		put.addHeader("Authorization", authToken); 

		final StringEntity input = new StringEntity(entityString);
		input.setContentType("application/json");
		put.setEntity(input);
		final HttpResponse response = client.execute(put);
		final BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			retJson += line;
			;
		}
		return retJson;
	}
	
	public static String getUserCode(String token) {
		
		JSONObject decodedToken = KeycloakUtils.getDecodedToken(token);
		System.out.println("decoded token object ::"+decodedToken);
		
		String username = decodedToken.getString("preferred_username");
		String uname = QwandaUtils.getNormalisedUsername(username);
		String code = "PER_" + uname.toUpperCase();
		
		return code;
	}
	
	
	public static String createAssemblyId(String token) {
		
		String userCode = getUserCode(token);
		
		org.json.simple.JSONObject authobj = new org.json.simple.JSONObject();
		authobj.put("userCode", userCode);
		//authobj.put("UUID", UUID.randomUUID().toString());

		String encodedAuthString = base64Encoder(authobj.toJSONString());
		return encodedAuthString;
		
	}
	
	
	public static Boolean checkIfAssemblyUserExists(String assemblyUserId, String authToken) {
		
		String assemblyUserString = PaymentEndpoint.getAssemblyUserById(assemblyUserId, authToken);
		if(!assemblyUserString.contains("errors")) {
			return true;
		} 
		System.out.println("assembly user string ::"+assemblyUserString);
		return false;
	}
	
	public static void createAssemblyUser(String assemblyUserId, String authToken, String token) { 
		
		Gson gson = new Gson();

		String userCode = getUserCode(token);
		BaseEntity be = MergeUtil.getBaseEntityForAttr(userCode, token);

		JSONObject userobj = new JSONObject();
		JSONObject personalInfoObj = new JSONObject();
		JSONObject contactInfoObj = new JSONObject();
		JSONObject locationObj = new JSONObject();

		personalInfoObj.put("firstName", MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_FIRSTNAME"));
		personalInfoObj.put("lastName", MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_LASTNAME"));
		
		personalInfoObj.put("dob", MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_DOB"));
		
		String dobString = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_DOB").toString();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter assemblyDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate date = LocalDate.parse(dobString, formatter);
		String formattedDOBString = assemblyDateFormatter.format(date);
		
		personalInfoObj.put("dob", formattedDOBString);
		//personalInfoObj.put("governmentNumber", "123456789");

		contactInfoObj.put("email", MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_EMAIL"));
		contactInfoObj.put("mobile", MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_MOBILE"));

		locationObj.put("addressLine1", MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_ADDRESS_ADDRESS1"));
		//locationObj.put("addressLine2", MergeUtil.getBaseEntityAttrObjectValue(be, "PER_"));
		locationObj.put("city", MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_ADDRESS_CITY"));
		locationObj.put("state", MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_ADDRESS_STATE"));
		locationObj.put("country", MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_ADDRESS_COUNTRY"));
		locationObj.put("postcode", MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_ADDRESS_POSTCODE"));

		userobj.put("personalInfo", personalInfoObj);
		userobj.put("contactInfo", contactInfoObj);
		userobj.put("location", locationObj);
		userobj.put("id", assemblyUserId);
		
		PaymentEndpoint.createAssemblyUser(gson.toJson(userobj), authToken);

	}

}
