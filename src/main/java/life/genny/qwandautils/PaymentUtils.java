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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertx.core.json.JsonObject;
import life.genny.qwanda.Answer;
import life.genny.qwanda.DateTimeDeserializer;
import life.genny.qwanda.entity.BaseEntity;

public class PaymentUtils {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	
	final static Gson gson = new Gson();
	public static final String DEFAULT_CURRENCY = "AUD";
	public static final String DEFAULT_PAYMENT_TYPE = "escrow";
	
	@SuppressWarnings("unchecked")
	public static String getAssemblyAuthKey() {
		
		String paymentMarketPlace = System.getenv("PAYMENT_MARKETPLACE_NAME");
	    String paymentToken = System.getenv("PAYMENT_TOKEN");
	    String paymentSecret = System.getenv("PAYMENT_SECRET"); 
		
		JSONObject authObj = new JSONObject();
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

	public static JSONObject base64Decoder(String plainString) {

		String decodedString = null;
		JSONParser parser = new JSONParser();
		JSONObject authobj = new JSONObject();

		decodedString = new String(Base64.getDecoder().decode(plainString));
		
		try {
			authobj = (JSONObject) parser.parse(decodedString);
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
		
		org.json.JSONObject decodedToken = KeycloakUtils.getDecodedToken(token);
		System.out.println("decoded token object ::"+decodedToken);
		
		String username = decodedToken.getString("preferred_username");
		String uname = QwandaUtils.getNormalisedUsername(username);
		String code = "PER_" + uname.toUpperCase();
		
		return code;
	}
	
	
	@SuppressWarnings("unchecked")
	public static String getAssemblyId(String token) {
		
		String userCode = getUserCode(token);
		
		JSONObject authobj = new JSONObject();
		authobj.put("userCode", userCode);
		//authobj.put("UUID", UUID.randomUUID().toString());

		String encodedAuthString = base64Encoder(authobj.toJSONString());
		return encodedAuthString;
		
	}
	
	
	public static Boolean checkIfAssemblyUserExists(String assemblyUserId) {
		
		String authToken = getAssemblyAuthKey();
		
		String assemblyUserString = PaymentEndpoint.getAssemblyUserById(assemblyUserId, authToken);
		if(!assemblyUserString.contains("error")) {
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

		JSONObject userobj = new JSONObject();
		JSONObject personalInfoObj = new JSONObject();
		JSONObject contactInfoObj = new JSONObject();
		JSONObject locationObj = new JSONObject();
		
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
			} else {
				locationObj.put("country", "AU");
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
	public static String updateUserPersonalInfo(String companyId, String assemblyUserId, String attributeCode, String value, String authToken) {

		System.out.println("attributeCode ::" + attributeCode + ", value ::" + value);
		String responseString = null;
		//String companyId = MergeUtil.getAttrValue(userCode, "PRI_ASSEMBLY_COMPANY_ID", token)

		/* Personal Info Update Objects */
		JSONObject userobj = null;
		JSONObject personalInfoObj = null;
		JSONObject personalContactInfoObj = null;
		JSONObject locationObj = null;
		
		/* Company Info Update Objects */
		JSONObject companyObj = null;
		JSONObject companyContactInfoObj = null;

		switch (attributeCode) {
		case "PRI_FIRSTNAME":
			personalInfoObj = new JSONObject();
			personalInfoObj.put("firstName", value);
			break;
		case "PRI_LASTNAME":
			personalInfoObj = new JSONObject();
			personalInfoObj.put("lastName", value);
			break;
		case "PRI_DOB":
			personalInfoObj = new JSONObject();
			DateTimeFormatter assemblyDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate date = LocalDate.parse(value.toString(), formatter);
			String formattedDOBString = assemblyDateFormatter.format(date);
			System.out.println("another formatted dob ::" + formattedDOBString);
			personalInfoObj.put("dob", formattedDOBString.toString());
			break;
		case "PRI_EMAIL":
			personalContactInfoObj = new JSONObject();
			personalContactInfoObj.put("email", value);
			break;
		case "PRI_MOBILE":
			personalContactInfoObj = new JSONObject();
			personalContactInfoObj.put("mobile", value);
			break;
		case "PRI_ADDRESS_ADDRESS1":
			personalContactInfoObj = new JSONObject();
			personalContactInfoObj.put("addressLine1", value);
			break;
		case "PRI_ADDRESS_CITY":
			locationObj = new JSONObject();
			locationObj.put("city", value);
			break;
		case "PRI_ADDRESS_STATE":
			locationObj = new JSONObject();
			locationObj.put("state", value);
			break;
		case "PRI_ADDRESS_COUNTRY":
			locationObj = new JSONObject();
			locationObj.put("country", value);	
			break;
		case "PRI_ADDRESS_POSTCODE":
			locationObj = new JSONObject();
			locationObj.put("postcode", value);
			break;
		case "PRI_NAME":
			companyObj = new JSONObject();
			companyObj.put("name", value);
			break;
		case "PRI_GST":
			companyObj = new JSONObject();
			companyObj.put("chargesTax", value);
			break;
		case "PRI_LANDLINE":
			companyContactInfoObj = new JSONObject();
			companyContactInfoObj.put("phone", value);
			break;
		}
		
		/* For Assembly Personal Information Update */
		
		if(personalInfoObj != null) {
			userobj = new JSONObject();
			userobj.put("personalInfo", personalInfoObj);
			userobj.put("id", assemblyUserId);
		}

		if (personalContactInfoObj != null) {
			userobj = new JSONObject();
			userobj.put("contactInfo", personalContactInfoObj);
			userobj.put("id", assemblyUserId);	
		}

		if (locationObj != null) {
			userobj = new JSONObject();
			userobj.put("location", locationObj);
			userobj.put("id", assemblyUserId);
			
			companyObj = new JSONObject();
			companyObj.put("location", locationObj);
		}
		
		if(userobj != null) {
			responseString = PaymentEndpoint.updateAssemblyUser(assemblyUserId, gson.toJson(userobj), authToken);
		}
		
		/* For Assembly User Company Information Update */
		if(companyContactInfoObj != null && companyId != null) {
			companyObj = new JSONObject();
			companyObj.put("contactInfo", companyContactInfoObj);
			companyObj.put("id", companyId);
		}
		
		
		if(companyId != null && companyObj != null) {
			responseString = PaymentEndpoint.updateCompany(companyId, gson.toJson(companyObj), authToken);
		}
		
		return responseString;

	}
	
	@SuppressWarnings("unchecked")
	public static String createCompany(String assemblyId, String authtoken,String tokenString) {
		
		String userCode = getUserCode(tokenString);
		BaseEntity be = MergeUtil.getBaseEntityForAttr(userCode, tokenString);
		String createCompanyResponse = null;
		
		JSONObject companyObj = new JSONObject();
		JSONObject userObj = new JSONObject();
		JSONObject contactObj = new JSONObject();
		JSONObject locationObj = new JSONObject();
		
		Object companyName = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_NAME");
		Object taxNumber = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_ABN");
		Object chargeTax = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_GST");
		Object companyPhoneNumber = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_LANDLINE");
		Object countryName = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_ADDRESS_COUNTRY");
		Object assemblyUserId = MergeUtil.getBaseEntityAttrObjectValue(be, "PRI_ASSEMBLY_USER_ID");
		
		if(companyName != null) {
			companyObj.put("name", companyName.toString());
		}
		
		if(taxNumber != null) {
			companyObj.put("taxNumber", taxNumber);
		}
		
		if(chargeTax != null) {
			companyObj.put("chargesTax", (Boolean) chargeTax);
		}
		
		if(companyPhoneNumber != null) {
			contactObj.put("phone", companyPhoneNumber.toString());
		}
		
		if(assemblyUserId != null) {
			userObj.put("id", assemblyUserId.toString());
		}
		
		if(countryName != null) {
			locationObj.put("country", countryName.toString());
		} else {
			locationObj.put("country", "AU");
		}
		
		companyObj.put("contactInfo", contactObj);
		companyObj.put("user", userObj);
		companyObj.put("location", locationObj);
		
		log.info("Company object ::"+companyObj);
		
		createCompanyResponse = PaymentEndpoint.createCompany(gson.toJson(companyObj), authtoken);
		JSONObject companyResponseObj = gson.fromJson(createCompanyResponse, JSONObject.class);
		String companyCode = companyResponseObj.get("id").toString();
		
		return companyCode;
		
	}
	
	@SuppressWarnings("unchecked")
	public static String createPaymentItem(String BEGGroupCode, String assemblyauthToken, String token) {
		
		String itemId = null;
		Map<String, BaseEntity> itemContextMap = QwandaUtils.getBaseEntWithChildrenForAttributeCode(BEGGroupCode, token);
		BaseEntity begBe = MergeUtil.getBaseEntityForAttr(BEGGroupCode, token);
		
		JSONObject itemObj = new JSONObject();
		JSONObject buyerObj = null;
		JSONObject sellerObj = null;
		
		System.out.println("item context Map ::"+itemContextMap);
		itemObj.put("paymentType", DEFAULT_PAYMENT_TYPE);
		itemObj.put("currency", DEFAULT_CURRENCY);
		
		
		if(begBe != null) {
			Object begTitle = MergeUtil.getBaseEntityAttrValueAsString(begBe, "PRI_TITLE");
			Object begPrice = MergeUtil.getBaseEntityAttrValueAsString(begBe, "PRI_PRICE");
			
			//350 Dollars sent to Assembly as 3.50$
			Integer assemblyPrice = (int)begPrice * 100;
			
			Object begDescription = MergeUtil.getBaseEntityAttrValueAsString(begBe, "PRI_DESCRIPTION");
			
			itemObj.put("name", begTitle);
			itemObj.put("amount", assemblyPrice.toString());
			itemObj.put("description", begDescription);
			itemObj.put("currency", DEFAULT_CURRENCY);
		}
		
		
		/* OWNER -> Buyer */
		if(itemContextMap.containsKey("OWNER")) {
			BaseEntity ownerBe = itemContextMap.get("OWNER");
			System.out.println("Context map contains OWNER");
			
			if(ownerBe != null) {
				buyerObj = new JSONObject();
				buyerObj.put("id", MergeUtil.getBaseEntityAttrValueAsString(ownerBe, "PRI_ASSEMBLY_USER_ID"));
			}
			
		}
		
		/* DRIVER -> Seller */
		if(itemContextMap.containsKey("DRIVER")) {
			BaseEntity driverBe = itemContextMap.get("DRIVER");
			System.out.println("Context map contains DRIVER");
			
			if(driverBe != null) {
				sellerObj = new JSONObject();
				sellerObj.put("id", MergeUtil.getBaseEntityAttrValueAsString(driverBe, "PRI_ASSEMBLY_USER_ID"));
			}
			
		}
		
		/* If both buyer and seller is available for a particular BEG, Create Payment Item */
		if(buyerObj != null && sellerObj != null) {
			itemObj.put("buyer", buyerObj);
			itemObj.put("seller", sellerObj);
			itemObj.put("id", UUID.randomUUID().toString());
			
			System.out.println("Item object ::"+itemObj);
			
			String itemCreationResponse = PaymentEndpoint.createItem(gson.toJson(itemObj), assemblyauthToken);
			
			if(!itemCreationResponse.contains("error")) {
				itemId = itemObj.get("id").toString();
				log.info("Item object ::"+itemObj);
			}
			
			log.info("Item creation response ::"+itemCreationResponse);		
			
		}			
		
		return itemId;
	}
	
	public static String makePayment(String BEGCode, String authToken, String tokenString){
		
		String userCode = getUserCode(tokenString);
		BaseEntity userBe = MergeUtil.getBaseEntityForAttr(userCode, tokenString);
		BaseEntity begCode = MergeUtil.getBaseEntityForAttr(BEGCode, tokenString);
		
		Object ipAddress = MergeUtil.getBaseEntityAttrObjectValue(userBe, "PRI_IP_ADDRESS");
		Object deviceId = MergeUtil.getBaseEntityAttrObjectValue(userBe, "PRI_DEVICE_ID");
		Object itemId = MergeUtil.getBaseEntityAttrObjectValue(begCode, "PRI_ITEM_ID");
		
		JSONObject paymentObj = new JSONObject();
		paymentObj.put("id", itemId.toString());
		paymentObj.put("accountId", null);
		paymentObj.put("ipAddress", ipAddress.toString());
		paymentObj.put("deviceId", deviceId);
		paymentObj.put("itemId", itemId.toString());
		
		String paymentResponse = PaymentEndpoint.makePayment(gson.toJson(paymentObj), authToken);
		
		log.info("Item creation response ::"+paymentResponse);
		
		return paymentResponse;
	}
	
	
	public static String getBegCode(String offerCode, String tokenString) {
		
		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		String begCode = null;
		List entityAttributeList = null;
		try {
			String attributeString = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" + offerCode + "/attributes", tokenString);
			entityAttributeList = QwandaUtils.gson.fromJson(attributeString, List.class);
			
			System.out.println("Entity Attribute List:"+entityAttributeList);
			
			for(Object eaObj : entityAttributeList) {
				
				JsonObject offerObj = (JsonObject) eaObj;
                String attributeCode = offerObj.getString("attributeCode");
                String attributeValue = offerObj.getString("valueString");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		return begCode;
	}
	
	public static void saveAnswer(String qwandaServiceUrl, Answer answer, String token) {
		
		Gson gson = new Gson();
	    GsonBuilder gsonBuilder = new GsonBuilder();
	    gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer());
	    gson = gsonBuilder.create();
		
		try {
			QwandaUtils.apiPostEntity(qwandaServiceUrl+"/qwanda/answers",gson.toJson(answer), token);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
