package life.genny.qwandautils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
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
		
		return encodedString;
	}

	public static org.json.simple.JSONObject base64Decoder(String plainString) {

		String decodedString = null;
		JSONParser parser = new JSONParser();
		org.json.simple.JSONObject authobj = new org.json.simple.JSONObject();

		decodedString = new String(Base64.getDecoder().decode(plainString));
		
		try {
			authobj = (org.json.simple.JSONObject) parser.parse(decodedString);
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
		System.out.println("http request payments ::"+postUrl);
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
	
	
	public static String getAssemblyId(String token) {
		
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
	
	@SuppressWarnings("unchecked")
	public static void createAssemblyUser(String assemblyUserId, String authToken, String token) { 
		
		Gson gson = new Gson();

		String userCode = getUserCode(token);
		BaseEntity be = MergeUtil.getBaseEntityForAttr(userCode, token);

		org.json.simple.JSONObject userobj = new org.json.simple.JSONObject();
		org.json.simple.JSONObject personalInfoObj = new org.json.simple.JSONObject();
		org.json.simple.JSONObject contactInfoObj = new org.json.simple.JSONObject();
		org.json.simple.JSONObject locationObj = new org.json.simple.JSONObject();
		
		if(be != null) {
			
			Object firstName = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_FIRSTNAME");
			Object lastName = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_LASTNAME");
			Object dobString = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_DOB");
			Object email = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_EMAIL");
			Object phoneNumber = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_MOBILE");
			Object addressLine1 = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_ADDRESS_ADDRESS1");
			Object city = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_ADDRESS_CITY");
			Object state = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_ADDRESS_STATE");
			Object country = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_ADDRESS_COUNTRY");
			Object postCode = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_ADDRESS_POSTCODE");
			
			if(firstName != null) {
				personalInfoObj.put("firstName", firstName.toString());
			}
			
			if(lastName != null) {
				personalInfoObj.put("lastName", lastName.toString());
			} 
			
			if(dobString != null) {
				System.out.println("dob string ::"+dobString);
				//DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				DateTimeFormatter assemblyDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				
				LocalDate dobDate = (LocalDate) dobString;
				String formattedDOBString = assemblyDateFormatter.format(dobDate);
				System.out.println("another formatted dob ::"+formattedDOBString);
				
				personalInfoObj.put("dob", formattedDOBString.toString());
			}
			
			if(email != null) {
				contactInfoObj.put("email", email.toString());
			}
			
			if(phoneNumber != null) {
				contactInfoObj.put("mobile", phoneNumber.toString());
			}
			
			if(addressLine1 != null) {
				locationObj.put("addressLine1", addressLine1.toString());
			}
			
			if(city != null) {
				locationObj.put("city", city.toString());
			}
			
			if(state != null) {
				locationObj.put("state", state.toString());
			}
			
			if(country != null) {
				locationObj.put("country", country.toString());
			}
			
			if(postCode != null) {
				locationObj.put("postcode", postCode.toString());
			}
		}
	
		//personalInfoObj.put("governmentNumber", "123456789");
		userobj.put("personalInfo", personalInfoObj);
		userobj.put("contactInfo", contactInfoObj);
		userobj.put("location", locationObj);
		userobj.put("id", assemblyUserId);
		
		System.out.println("user obj ::"+userobj);
	
		PaymentEndpoint.createAssemblyUser(gson.toJson(userobj), authToken);		
	}
	
	public static String getPaymentsUser(String assemblyUserId, String authToken){
		String responseString = PaymentEndpoint.getAssemblyUserById(assemblyUserId, authToken);
		return responseString;
	}
	
	
	@SuppressWarnings("unchecked")
	public static String updateUserPersonalInfo(String assemblyUserId, String attributeCode, String value, String authToken) {

		System.out.println("attributeCode ::" + attributeCode + ", value ::" + value);
		String responseString = null;

		org.json.simple.JSONObject userobj = null;
		org.json.simple.JSONObject personalInfoObj = null;
		org.json.simple.JSONObject contactInfoObj = null;
		org.json.simple.JSONObject locationObj = null;

		switch (attributeCode) {
		case "PRI_FIRSTNAME":
			personalInfoObj = new org.json.simple.JSONObject();
			personalInfoObj.put("firstName", value);
			break;
		case "PRI_LASTNAME":
			personalInfoObj = new org.json.simple.JSONObject();
			personalInfoObj.put("lastName", value);
			break;
		case "PRI_DOB":
			personalInfoObj = new org.json.simple.JSONObject();
			DateTimeFormatter assemblyDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate date = LocalDate.parse(value.toString(), formatter);
			String formattedDOBString = assemblyDateFormatter.format(date);
			System.out.println("another formatted dob ::" + formattedDOBString);
			personalInfoObj.put("dob", formattedDOBString.toString());
			break;
		case "PRI_EMAIL":
			contactInfoObj = new org.json.simple.JSONObject();
			contactInfoObj.put("email", value);
			break;
		case "PRI_MOBILE":
			contactInfoObj = new org.json.simple.JSONObject();
			contactInfoObj.put("mobile", value);
			break;
		case "PRI_ADDRESS_ADDRESS1":
			contactInfoObj = new org.json.simple.JSONObject();
			contactInfoObj.put("addressLine1", value);
			break;
		case "PRI_ADDRESS_CITY":
			locationObj = new org.json.simple.JSONObject();
			locationObj.put("city", value);
			break;
		case "PRI_ADDRESS_STATE":
			locationObj = new org.json.simple.JSONObject();
			locationObj.put("state", value);
			break;
		case "PRI_ADDRESS_COUNTRY":
			locationObj = new org.json.simple.JSONObject();
			locationObj.put("country", value);
			break;
		case "PRI_ADDRESS_POSTCODE":
			locationObj = new org.json.simple.JSONObject();
			locationObj.put("postcode", value);
			break;
		}

		if (personalInfoObj != null) {
			userobj = new org.json.simple.JSONObject();
			userobj.put("personalInfo", personalInfoObj);
			userobj.put("id", assemblyUserId);
		}

		if (contactInfoObj != null) {
			userobj = new org.json.simple.JSONObject();
			userobj.put("contactInfo", contactInfoObj);
			userobj.put("id", assemblyUserId);
		}

		if (locationObj != null) {
			userobj = new org.json.simple.JSONObject();
			userobj.put("location", locationObj);
			userobj.put("id", assemblyUserId);
		}
		
		if(userobj != null) {
			responseString = PaymentEndpoint.updateAssemblyUser(assemblyUserId, gson.toJson(userobj), authToken);
		}
		
		return responseString;

	}

}
