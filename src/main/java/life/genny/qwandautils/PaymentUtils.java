package life.genny.qwandautils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.json.JsonObject;
import life.genny.qwanda.Answer;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.exception.PaymentException;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;

public class PaymentUtils {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	
	//public static final String DEFAULT_CURRENCY = "AUD";
	public static final String DEFAULT_PAYMENT_TYPE = "escrow";
	public static final String PROVIDER_TYPE_BANK = "bank"; 
	
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
			throws IOException, PaymentException {
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
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		System.out.println("response code ::"+responseCode);
		System.out.println("response ::"+response.getStatusLine());
		System.out.println("response body::"+retJson);
		
		if(responseCode != 200) {
			throw new PaymentException("Payment exception, "+response.getEntity().getContent());
		}
		return retJson;
	}
	
	public static String apiPostPaymentEntity(final String postUrl, final String authToken)
			throws IOException, PaymentException {
		String retJson = "";
		//final HttpClient client = new DefaultHttpClient();
		final HttpClient client = HttpClientBuilder.create().build();
		System.out.println("http request payments ::"+postUrl);
		final HttpPost post = new HttpPost(postUrl);
		post.addHeader("Authorization", authToken); 

		final HttpResponse response = client.execute(post);
		
		final BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			retJson += line;
			;
		}
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		System.out.println("response code ::"+responseCode);
		System.out.println("response ::"+response.getStatusLine());
		
		System.out.println("response body::"+retJson);
		
		if(responseCode != 200) {
			throw new PaymentException("Payment exception, "+response.getEntity().getContent());
		}
		return retJson;
	}
	
	
	public static String apiGetPaymentResponse(final String getUrl, final String authToken)
			throws ClientProtocolException, IOException, PaymentException {
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
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		System.out.println("response code ::"+responseCode);
		System.out.println("response ::"+response.getStatusLine());
		System.out.println("response body::"+retJson);
		
		if(responseCode != 200) {
			throw new PaymentException("Payment exception,"+response.getEntity().getContent());
		}

		return retJson;
	}
	
	
	public static String apiPutPaymentEntity(final String postUrl, final String entityString, final String authToken)
			throws IOException, PaymentException {
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
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		System.out.println("response code ::"+responseCode);
		System.out.println("response ::"+response.getStatusLine());
		System.out.println("response body::"+retJson);
		
		if(responseCode != 200) {
			throw new PaymentException("Payment exception, "+response.getEntity().getContent());
		}
		
		return retJson;
	}
	

		
	public static String getAssemblyId(String token) {
	
		return UUID.randomUUID().toString();
		
	}
	
	
	public static Boolean checkIfAssemblyUserExists(String assemblyUserId) {
		
		Boolean isExists = false;
		
		if(assemblyUserId != null) {
			String authToken = getAssemblyAuthKey();
			
			String assemblyUserString = null;
			try {
				assemblyUserString = PaymentEndpoint.getAssemblyUserById(assemblyUserId, authToken);
				if(assemblyUserString != null && !assemblyUserString.contains("error")) {
					System.out.println("assembly user string ::"+assemblyUserString);
					isExists = true;
				} 
			} catch (PaymentException e) {
				log.error("Assembly user not found, returning isExists=false in exception handler");
				isExists = false;
			}
		}
		
		return isExists;
	}
	
	@SuppressWarnings("unchecked")
	public static String createAssemblyUser(String assemblyUserId, String authToken, String token) { 
		
		String userCode = QwandaUtils.getUserCode(token);
		BaseEntity be = MergeUtil.getBaseEntityForAttr(userCode, token);
		String assemblyId = null;

		JSONObject userobj = new JSONObject();
		JSONObject personalInfoObj = new JSONObject();
		JSONObject contactInfoObj = new JSONObject();
		JSONObject locationObj = new JSONObject();
		
		if(be != null && assemblyUserId != null) {
			
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

		String paymentUserCreationResponse;
		try {
			paymentUserCreationResponse = PaymentEndpoint.createAssemblyUser(JsonUtils.toJson(userobj), authToken);
			if(!paymentUserCreationResponse.contains("error") && paymentUserCreationResponse != null) {
				assemblyId = assemblyUserId;
			}
		} catch (PaymentException e) {
			log.error("Assembly user not found, returning null in exception handler");
			assemblyId = null;
		}	
		
		return assemblyId;
	}
	
	public static String getPaymentsUser(String assemblyUserId, String authToken){
		String responseString = null;
		try {
			responseString = PaymentEndpoint.getAssemblyUserById(assemblyUserId, authToken);
		} catch (PaymentException e) {
			e.printStackTrace();
		}
		return responseString;
	}
	
	
	@SuppressWarnings("unchecked")
	public static String updateUserPersonalInfo(String companyId, String assemblyUserId, String attributeCode, String value, String authToken) {

		System.out.println("attributeCode ::" + attributeCode + ", value ::" + value);
		String responseString = null;

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
			companyObj.put("chargesTax", Boolean.valueOf(value));
			break;
		case "PRI_LANDLINE":
			companyContactInfoObj = new JSONObject();
			companyContactInfoObj.put("phone", value);
			break;
		}
		
		/* For Assembly Personal Information Update */
		
		if(personalInfoObj != null  && assemblyUserId != null) {
			userobj = new JSONObject();
			userobj.put("personalInfo", personalInfoObj);
			userobj.put("id", assemblyUserId);
		}

		if (personalContactInfoObj != null && assemblyUserId != null) {
			userobj = new JSONObject();
			userobj.put("contactInfo", personalContactInfoObj);
			userobj.put("id", assemblyUserId);	
		}

		if (locationObj != null && assemblyUserId != null) {
			userobj = new JSONObject();
			userobj.put("location", locationObj);
			userobj.put("id", assemblyUserId);
			
			companyObj = new JSONObject();
			companyObj.put("location", locationObj);
		}
		
		if(userobj != null && assemblyUserId!= null) {
			try {
				responseString = PaymentEndpoint.updateAssemblyUser(assemblyUserId, JsonUtils.toJson(userobj), authToken);
				System.out.println("response string from payments user updation ::"+responseString);
			} catch (PaymentException e) {
				log.error("Exception occured user updation");
				e.printStackTrace();
			}
		}
		
		/* For Assembly User Company Information Update */
		if(companyContactInfoObj != null && companyId != null) {
			companyObj = new JSONObject();
			companyObj.put("contactInfo", companyContactInfoObj);
			companyObj.put("id", companyId);
		}
		
		
		if(companyId != null && companyObj != null) {
			System.out.println("updating company object in assembly ::"+companyObj);
			try {
				responseString = PaymentEndpoint.updateCompany(companyId, JsonUtils.toJson(companyObj), authToken);
			} catch (PaymentException e) {
				log.error("Exception occured company updation");
				e.printStackTrace();
			}
		}
		
		return responseString;

	}
	
	@SuppressWarnings("unchecked")
	public static String createCompany(String authtoken, String tokenString) {

		String userCode = QwandaUtils.getUserCode(tokenString);
		BaseEntity be = MergeUtil.getBaseEntityForAttr(userCode, tokenString);
		String createCompanyResponse = null;
		String companyCode = null;

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

		if (companyName != null) {
			companyObj.put("name", companyName.toString());
		}

		if (taxNumber != null) {
			companyObj.put("taxNumber", taxNumber);
		}

		if (chargeTax != null) {
			companyObj.put("chargesTax", (Boolean) chargeTax);
		}

		if (companyPhoneNumber != null) {
			contactObj.put("phone", companyPhoneNumber.toString());
		}

		if (assemblyUserId != null) {
			userObj.put("id", assemblyUserId.toString());
		}

		if (countryName != null) {
			locationObj.put("country", countryName.toString());
		} else {
			locationObj.put("country", "AU");
		}

		companyObj.put("contactInfo", contactObj);
		companyObj.put("user", userObj);
		companyObj.put("location", locationObj);

		log.info("Company object ::" + companyObj);

		if (companyObj != null && userObj != null) {
			System.out.println("company obj is not null, company object ::"+companyObj);
			try {
				createCompanyResponse = PaymentEndpoint.createCompany(JsonUtils.toJson(companyObj), authtoken);
				if(!createCompanyResponse.contains("error")) {
					JSONObject companyResponseObj = JsonUtils.fromJson(createCompanyResponse, JSONObject.class);
					
					if(companyResponseObj.get("id") != null) {
						companyCode = companyResponseObj.get("id").toString();
					}		
				}
			} catch (PaymentException e) {
				companyCode = null;
			}
		}
		
		return companyCode;

	}
	
	@SuppressWarnings("unchecked")
	public static String createPaymentItem(String offerCode, String BEGGroupCode, String assemblyauthToken, String token) {
		
		String itemId = null;
		Map<String, BaseEntity> itemContextMap = QwandaUtils.getBaseEntWithChildrenForAttributeCode(BEGGroupCode, token);
		BaseEntity begBe = MergeUtil.getBaseEntityForAttr(BEGGroupCode, token);
		BaseEntity offerBe = MergeUtil.getBaseEntityForAttr(offerCode, token);
		
		JSONObject itemObj = new JSONObject();
		JSONObject buyerObj = null;
		JSONObject sellerObj = null;
		
		System.out.println("item context Map ::"+itemContextMap);
		itemObj.put("paymentType", DEFAULT_PAYMENT_TYPE);
		
		if (begBe != null) {

			String feeId = getPaymentFeeId(offerBe, assemblyauthToken);
			System.out.println("fee Id ::" + feeId);

			String begTitle = MergeUtil.getBaseEntityAttrValueAsString(begBe, "PRI_TITLE");
			String begDescription = MergeUtil.getBaseEntityAttrValueAsString(begBe, "PRI_DESCRIPTION");

			if (begTitle != null) {
				itemObj.put("name", begTitle);
			}

			if (begDescription != null) {
				itemObj.put("description", begDescription);
			}

			if (feeId != null) {
				String[] feeArr = { feeId };
				itemObj.put("fees", feeArr);
			}

			/*
			 * driverPriceIncGST = ownerPriceIncGST.subtract(feePriceIncGST),
			 * Creating Payments Fee with feePriceIncGST
			 */
			String offerOwnerPriceString = MergeUtil.getBaseEntityAttrValueAsString(offerBe, "PRI_OFFER_DRIVER_PRICE_INC_GST");

			System.out.println("begpriceString ::" + offerOwnerPriceString);

			String amount = null;
			String currency = null;
			if(offerOwnerPriceString != null) {
				System.out.println("begPriceString is not null");
				JSONObject moneyobj = JsonUtils.fromJson(offerOwnerPriceString, JSONObject.class);
				amount = moneyobj.get("amount").toString();
				currency = moneyobj.get("currency").toString();
				
				if(amount != null) {
					System.out.println("amount is not null");
					BigDecimal begPrice = new BigDecimal(amount);

					// 350 Dollars sent to Assembly as 3.50$, so multiplying with 100
					BigDecimal finalPrice = begPrice.multiply(new BigDecimal(100));
					itemObj.put("amount", finalPrice.toString());		
				} else {
					log.error("AMOUNT IS NULL");
				}
				
				if(currency != null) {
					System.out.println("currency is not null");
					itemObj.put("currency", currency);
				} else {
					log.error("CURRENCY IS NULL");
				}
					
			} else {
				log.error("PRI_DRIVER_PRICE_INC_GST IS NULL");
			}
			
		} else {
			log.error("BEG BASEENTITY IS NULL");
			try {
				throw new PaymentException("Payment Item creation will not succeed since Beg baseentity is null");
			} catch (PaymentException e) {
			}
		}
		
		
		/* OWNER -> Buyer */
		if(itemContextMap.containsKey("OWNER")) {
			BaseEntity ownerBe = itemContextMap.get("OWNER");
			System.out.println("Context map contains OWNER");
			
			if(ownerBe != null) {
				buyerObj = new JSONObject();
				buyerObj.put("id", MergeUtil.getBaseEntityAttrValueAsString(ownerBe, "PRI_ASSEMBLY_USER_ID"));
			}
		} else {
			log.error("BEG CONTEXT MAP HAS NO OWNER LINK, SO BUYER OBJECT IS NULL");
			try {
				throw new PaymentException("Payment Item creation will not succeed since Beg has no owner link");
			} catch (PaymentException e) {
			}
		}
		
		/* DRIVER -> Seller */
		if(itemContextMap.containsKey("QUOTER")) {
			
			BaseEntity driverBe = itemContextMap.get("QUOTER");
			System.out.println("Context map contains QUOTER");
			
			if(driverBe != null) {
				sellerObj = new JSONObject();
				sellerObj.put("id", MergeUtil.getBaseEntityAttrValueAsString(driverBe, "PRI_ASSEMBLY_USER_ID"));
			}
		} else {
			log.error("BEG CONTEXT MAP HAS NO QUOTER LINK, SO SELLER OBJECT IS NULL");
			try {
				throw new PaymentException("Payment Item creation will not succeed since Beg has no quoter link");
			} catch (PaymentException e) {
				log.error("BEG CONTEXT MAP HAS NO QUOTER LINK, SO SELLER OBJECT IS NULL");
			}
		}
		
		/* If both buyer and seller is available for a particular BEG, Create Payment Item */
		if(itemObj != null && buyerObj != null && sellerObj != null) {
			
			itemObj.put("buyer", buyerObj);
			itemObj.put("seller", sellerObj);
			itemObj.put("id", UUID.randomUUID().toString());
			
			System.out.println("Item object ::"+itemObj);
			
			String itemCreationResponse;
			try {
				itemCreationResponse = PaymentEndpoint.createItem(JsonUtils.toJson(itemObj), assemblyauthToken);
				if(!itemCreationResponse.contains("error")) {
					
					log.info("Item object ::" + itemObj);
					log.info( itemObj.get("id") );
					itemId = itemObj.get("id").toString();
					log.info("Item ID found ::" + itemId);
					return itemId;
				}
			} catch (PaymentException e) {
				log.error("PAYMENT ITEM CREATION FAILED, ITEM/BUYER/SELLER OBJECT IS NULL, exception is handled");
				itemId = null;
			}
			
		}
		
		return itemId;
	}
	
	
	public static String getBegCode(String offerCode, String tokenString) {
		
		String begCode = null;	
		begCode = MergeUtil.getAttrValue(offerCode, "PRI_BEG_CODE", tokenString);
		
		return begCode;
	}
	
	public static void saveAnswer(String qwandaServiceUrl, Answer answer, String token) {
		
		
		try {
			QwandaUtils.apiPostEntity(qwandaServiceUrl + "/qwanda/answers", JsonUtils.toJson(answer), token);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String fetchOneTimeAssemblyToken(String qwandaServiceUrl, String userId, String tokenString, String assemblyId,
			String assemblyAuthToken, String type)
	{
		String transactionToken = null;
		JSONParser parser = new JSONParser();
		JSONObject authenticationEntityObj = new JSONObject();
		

		if (assemblyId != null) {

			String tokenResponse = null;
			
			try {
				
				authenticationEntityObj.put("type", type);
				JSONObject userObj = new JSONObject();
				userObj.put("id", assemblyId);
				authenticationEntityObj.put("user", userObj);
								
				tokenResponse  = PaymentEndpoint.authenticatePaymentProvider(JsonUtils.toJson(authenticationEntityObj), assemblyAuthToken);

				if (!tokenResponse.contains("error")) {
					
					try {
						JSONObject tokenObj = (JSONObject) parser.parse(tokenResponse);
						System.out.println("token object ::" + tokenObj);

						String providerToken = tokenObj.get("token").toString();

						return providerToken;
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				log.error("PaymentUtils Exception occured during Payment authentication Token provision");
			}
		} else {
			log.error("ASSEMBLY USER ID IS NULL");
		}
		
		return transactionToken;
	}
	
//	public static void saveTokenAnswers(String qwandaServiceUrl, String userId, String tokenString, String assemblyId,
//			String assemblyAuthToken) {
//
//		JSONParser parser = new JSONParser();
//
//		if (assemblyId != null) {
//
//			String tokenResponse = null;
//			
//			try {
//				tokenResponse  = authenticatePaymentProvider(assemblyId, assemblyAuthToken);
//
//				if (!tokenResponse.contains("error")) {
//					
//					try {
//						JSONObject tokenObj = (JSONObject) parser.parse(tokenResponse);
//						System.out.println("token object ::" + tokenObj);
//
//						String providerToken = tokenObj.get("token").toString();
//
//						Answer cardTokenAnswer = new Answer(userId, userId, "PRI_ASSEMBLY_CARD_TOKEN", providerToken);
//						saveAnswer(qwandaServiceUrl, cardTokenAnswer, tokenString);
//
//						Answer bankTokenAnswer = new Answer(userId, userId, "PRI_ASSEMBLY_BANK_TOKEN", providerToken);
//						saveAnswer(qwandaServiceUrl, bankTokenAnswer, tokenString);
//
//					} catch (ParseException e) {
//						e.printStackTrace();
//					}
//				}
//			} catch (Exception e) {
//				log.error("PaymentUtils Exception occured during Payment authentication Token provision");
//			}
//		} else {
//			log.error("ASSEMBLY USER ID IS NULL");
//		}
//
//	}
//	
	
	@SuppressWarnings("unchecked")
	private static String authenticatePaymentProvider(String assemblyId, String assemblyAuthToken) throws PaymentException {
		
		JSONObject paymentProviderObj = new JSONObject();
		JSONObject userObj = new JSONObject();
		
		userObj.put("id", assemblyId);
		
		paymentProviderObj.put("type", PROVIDER_TYPE_BANK);
		paymentProviderObj.put("user", userObj);
		
		String tokenResponse = null;

		tokenResponse = PaymentEndpoint.authenticatePaymentProvider(JsonUtils.toJson(paymentProviderObj), assemblyAuthToken);
	
		
		return tokenResponse;
	}
	
	
	@SuppressWarnings("unchecked")
	public static String getPaymentFeeId(BaseEntity offerBe, String assemblyAuthToken) {

		JSONParser parser = new JSONParser();
		String feeId = null;

		// Object fee = MergeUtil.getBaseEntityAttrObjectValue(begBe, "PRI_FEE");
		String begFeeString = MergeUtil.getBaseEntityAttrValueAsString(offerBe, "PRI_OFFER_FEE_INC_GST");

		if (begFeeString != null) {
			System.out.println("begpriceString ::" + begFeeString);

			String amount = QwandaUtils.getAmountAsString(begFeeString);

			BigDecimal begPrice = new BigDecimal(amount);

			// 350 Dollars sent to Assembly as 3.50$, so multiplying with 100
			BigDecimal finalFee = begPrice.multiply(new BigDecimal(100));
			System.out.println("fees for feeId creation in Assembly::" + finalFee);

			JSONObject feeObj = new JSONObject();
			feeObj.put("name", "Channel40 fee");
			feeObj.put("type", 1);
			feeObj.put("amount", finalFee);
			feeObj.put("cap", null);
			feeObj.put("min", null);
			feeObj.put("max", null);
			feeObj.put("to", "buyer");

			String feeResponse;
			try {
				feeResponse = PaymentEndpoint.createFees(JsonUtils.toJson(feeObj), assemblyAuthToken);
				if (feeResponse != null) {
					JSONObject feeResponseObj;

					feeResponseObj = (JSONObject) parser.parse(feeResponse);

					if (feeResponseObj.get("id") != null) {
						feeId = feeResponseObj.get("id").toString();
						return feeId;
					}

				}

			} catch (PaymentException e1) {
				log.error("Exception occured during Payment Fee creation");
				e1.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}

		return feeId;

	}
	

	public static Boolean checkIfAnswerContainsPaymentAttribute(QDataAnswerMessage m) {
		
		Boolean isAnswerContainsPaymentAttribute = false;
		
		if(m != null) {
			Answer[] answers = m.getItems();
			for(Answer answer : answers) {
				String attributeCode = answer.getAttributeCode();
				if(attributeCode.contains("PRI_PAYMENT_METHOD")) {
					isAnswerContainsPaymentAttribute = true;
					break;
				}
			}
		}	
		
		return isAnswerContainsPaymentAttribute;
	}
	
	

	public static String processPaymentAnswers(String qwandaServiceUrl, QDataAnswerMessage m, String tokenString) {
		
		String begCode = null;

		try {
			
			System.out.println("----> Payments attributes Answers <------");
			
			String userCode = QwandaUtils.getUserCode(tokenString);

			Answer[] answers = m.getItems();
			for (Answer answer : answers) {

				String targetCode = answer.getTargetCode();
				String sourceCode = answer.getSourceCode();
				String attributeCode = answer.getAttributeCode();
				String value = answer.getValue();
				
				begCode = targetCode;

				log.debug("Payments value ::" + value + "attribute code ::" + attributeCode);
				System.out.println("Payments value ::" + value + "attribute code ::" + attributeCode);
				System.out.println("Beg code ::"+begCode);

				/* if this answer is actually an Payment_method, this rule will be triggered */
				if (attributeCode.contains("PRI_PAYMENT_METHOD")) {
					
					JsonObject paymentValues = new JsonObject(value);
					
					/*{ ipAddress, deviceID, accountID }*/
					String ipAddress = paymentValues.getString("ipAddress");
					String accountId = paymentValues.getString("accountID");
					String deviceId = paymentValues.getString("deviceID");
					
					if(ipAddress != null){
						Answer ipAnswer = new Answer(sourceCode, userCode, "PRI_IP_ADDRESS", ipAddress);
						saveAnswer(qwandaServiceUrl, ipAnswer, tokenString);
					}
					
					if(accountId != null) {
						Answer accountIdAnswer = new Answer(sourceCode, begCode, "PRI_ACCOUNT_ID", accountId);
						saveAnswer(qwandaServiceUrl, accountIdAnswer, tokenString);
					}
					
					if(deviceId != null) {
						Answer deviceIdAnswer = new Answer(sourceCode, userCode, "PRI_DEVICE_ID", deviceId);
						saveAnswer(qwandaServiceUrl, deviceIdAnswer, tokenString);	
					}	
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return begCode;
	}
	
	@SuppressWarnings("unchecked")
	public static Boolean makePayment(String qwandaUrl, String offerCode, String begCode, String authToken, String tokenString) {
		
		System.out.println("inside make payment");

		Boolean isMakePaymentSuccess = false;
		String userCode = QwandaUtils.getUserCode(tokenString);
		BaseEntity userBe = MergeUtil.getBaseEntityForAttr(userCode, tokenString);
		BaseEntity begBe = MergeUtil.getBaseEntityForAttr(begCode, tokenString);
		BaseEntity offerBe = MergeUtil.getBaseEntityForAttr(offerCode, tokenString);

		String paymentResponse = null;

		Object ipAddress = MergeUtil.getBaseEntityAttrObjectValue(userBe, "PRI_IP_ADDRESS");
		Object deviceId = MergeUtil.getBaseEntityAttrObjectValue(userBe, "PRI_DEVICE_ID");
		Object itemId = MergeUtil.getBaseEntityAttrObjectValue(begBe, "PRI_ITEM_ID");
		Object accountId = MergeUtil.getBaseEntityAttrObjectValue(begBe, "PRI_ACCOUNT_ID");

		JSONObject paymentObj = new JSONObject();
		JSONObject accountObj = null;

		if (itemId != null) {
			paymentObj.put("id", itemId);
			System.out.println("item is in make payment not null");
		} else {
			log.error("Make Payment - Item ID is NULL");
			try {
				throw new PaymentException("Item ID is null, so Make payment will not succeed");
			} catch (PaymentException e) {
				isMakePaymentSuccess = false;
			}
		}

		if (accountId != null) {
			accountObj = new JSONObject();
			accountObj.put("id", accountId);
			System.out.println("account id in make payment not null");
		} else {
			log.error("Make payment - accound Id is NULL");
			try {
				throw new PaymentException("Account ID is null, so Make payment will not succeed");
			} catch (PaymentException e) {
				isMakePaymentSuccess = false;
			}
		}

		if (accountObj != null) {
			paymentObj.put("account", accountObj);
		}

		if (ipAddress != null) {
			System.out.println("ip address in make payment not null");
			paymentObj.put("ipAddress", ipAddress);
		} else {
			log.error("Make payment - IP ADDRESS is NULL");
			try {
				throw new PaymentException("IP Address is null, so Make payment will not succeed");
			} catch (PaymentException e) {
				isMakePaymentSuccess = false;
			}
		}

		if (deviceId != null) {
			System.out.println("device id in make payment not null");
			paymentObj.put("deviceID", deviceId);
		} else {
			log.error("Make payment - DEVICE ID is NULL");
			try {
				throw new PaymentException("deviceID is null, so Make payment will not succeed");
			} catch (PaymentException e) {
				isMakePaymentSuccess = false;
			}
		}

		if (paymentObj.get("id") != null) {
			System.out.println("payment obj not null");
			
			String paymentType = getPaymentMethodType(userBe, accountId);

			if (paymentType != null && paymentType.equals("BANK_ACCOUNT")) {

				System.out.println("Bank account..Need to be authorized to make payment");

				/* Add Call to Matt's direct debit API */
				Boolean debitAuthorityResponse = getDebitAuthority(offerBe, begBe, accountId, authToken);
				
				if(debitAuthorityResponse) {
					log.debug("Make payment object ::" + paymentObj.toJSONString());
					System.out.println("Make payment object ::" + paymentObj.toJSONString());
					try {
						paymentResponse = PaymentEndpoint.makePayment(itemId.toString(), JsonUtils.toJson(paymentObj),
								authToken);
						log.debug("Make payment response ::" + paymentResponse);
						if (!paymentResponse.contains("error")) {
							isMakePaymentSuccess = true;
							
							/* Saving deposit reference as an answer to beg */
							saveDepositReference(qwandaUrl, paymentResponse, userCode, begCode, tokenString);
							
						}

					} catch (PaymentException e) {
						isMakePaymentSuccess = false;
						log.error("Exception occured during making payment with " + paymentType);
						e.printStackTrace();
					}
				} else {
					isMakePaymentSuccess = false;
				}
				

			} else if (paymentType != null && paymentType.equals("CARD")) {

				System.out.println("Credit card payment");

				try {
					paymentResponse = PaymentEndpoint.makePayment(itemId.toString(), JsonUtils.toJson(paymentObj),
							authToken);
					log.debug("Make payment response ::" + paymentResponse);
					if (!paymentResponse.contains("error")) {
						isMakePaymentSuccess = true;
						
						/* Save deposit reference as an answer to beg */
						saveDepositReference(qwandaUrl, paymentResponse, userCode, begCode, tokenString);
					}

				} catch (PaymentException e) {
					isMakePaymentSuccess = false;
					log.error("Exception occured during making payment with " + paymentType);
					e.printStackTrace();
				}

			}

			return isMakePaymentSuccess;

		} else {
			try {
				throw new PaymentException("Item ID is null, so Make Payment will not succeed");
			} catch (PaymentException e) {
				e.printStackTrace();
			}
		}

		return isMakePaymentSuccess;
	}
	
	private static void saveDepositReference(String qwandaServiceUrl, String paymentResponse, String userCode, String begCode,
			String tokenString) {
		
		JSONObject depositReference = JsonUtils.fromJson(paymentResponse, JSONObject.class);
		String depositReferenceId = depositReference.get("depositReference").toString();
		
		Answer answer = new Answer(userCode, begCode, "PRI_DEPOSIT_REFERENCE_ID", depositReferenceId);
		saveAnswer(qwandaServiceUrl, answer, tokenString);
		
	}

	private static Boolean getDebitAuthority(BaseEntity offerBe, BaseEntity begBe, Object accountId, String authToken) {
		
		Boolean isDebitAuthority = false;
		String getDebitAuthorityResponse = null;
		String offerOwnerPriceString = MergeUtil.getBaseEntityAttrValueAsString(offerBe, "PRI_OFFER_DRIVER_PRICE_INC_GST");

		System.out.println("begpriceString ::" + offerOwnerPriceString);

		String amount = null;
		if(offerOwnerPriceString != null) {
			System.out.println("begPriceString is not null");
			JSONObject moneyobj = JsonUtils.fromJson(offerOwnerPriceString, JSONObject.class);
			amount = moneyobj.get("amount").toString();
			
			if(amount != null) {
				System.out.println("amount is not null");
				BigDecimal begPrice = new BigDecimal(amount);

				// 350 Dollars sent to Assembly as 3.50$, so multiplying with 100
				BigDecimal finalPrice = begPrice.multiply(new BigDecimal(100));
				
				JSONObject debitAuthorityObj = new JSONObject();
				JSONObject accountObj = new JSONObject();
				
				/*{
					  "account": {
					    "id": "dkjgsxdkfesw345fidsfdsf"
					  },
					  "amount": 1000
					}*/
				
				accountObj.put("id", accountId);
				debitAuthorityObj.put("amount", finalPrice.toString());
				debitAuthorityObj.put("account", accountObj);
				
				try {
					getDebitAuthorityResponse = PaymentEndpoint.getdebitAuthorization(JsonUtils.toJson(debitAuthorityObj), authToken); 
					if(!getDebitAuthorityResponse.contains("error")) {
						isDebitAuthority = true;
					}
				} catch (PaymentException e) {
					isDebitAuthority = false;
					log.error("Exception occured during debit authorization, Make Payment will not succeed");
					e.printStackTrace();
				}
				
				
			} else {
				isDebitAuthority = false;
				log.error("AMOUNT IS NULL");
			}
			
			
		} else {
			isDebitAuthority = false;
			log.error("PRI_DRIVER_PRICE_INC_GST IS NULL");
		}
		
		return isDebitAuthority;
	}

	private static String getPaymentMethodType(BaseEntity userBe, Object accountId) {
		
		System.out.println("in getPaymentMethodType method");

		Object paymentMethods = MergeUtil.getBaseEntityAttrObjectValue(userBe, "PRI_USER_PAYMENT_METHODS");
		JSONArray array = JsonUtils.fromJson(paymentMethods.toString(), JSONArray.class);
		String paymentType = null;

		if (paymentMethods != null) {
			for (int i = 0; i < array.size(); i++) {
				Map<String, String> methodObj = (Map<String, String>) array.get(i);
				if (accountId.equals(methodObj.get("id"))) {
					paymentType = methodObj.get("type");
				}

			}
		}
		System.out.println("payment method type is ::"+paymentType);
		return paymentType;

	}
	
	public static Boolean releasePayment(String begCode, String authToken, String tokenString) {
		
		Boolean isReleasePaymentSuccess = false;
		System.out.println("BEG Code for release payment ::"+begCode);
		BaseEntity begBe = MergeUtil.getBaseEntityForAttr(begCode, tokenString);
		
		String paymentResponse = null;
		Object itemId = MergeUtil.getBaseEntityAttrObjectValue(begBe, "PRI_ITEM_ID");
		
		if(itemId != null) {
			try {
				paymentResponse = PaymentEndpoint.releasePayment(itemId.toString(), authToken);
				if(!paymentResponse.contains("error")) {
					log.debug("release payment response ::"+paymentResponse);
					isReleasePaymentSuccess = true;
				}
			} catch (PaymentException e) {
				log.error("Exception occured during release payment");
				isReleasePaymentSuccess = false;
				e.printStackTrace();
			}
			
		} else {
			try {
				log.error("Exception occured during release payment");
				throw new PaymentException("Item ID is null or invalid, hence payment cannot be released");
			} catch (PaymentException e) {
				isReleasePaymentSuccess = false;
			}
		}
		
		return isReleasePaymentSuccess;
	}
	
	
	public static String publishBaseEntityByCode(final String be, String token) {
		
		/*String[] recipientArray = new String[1];
		recipientArray[0] = be;
		publishBaseEntityByCode(be, null,
			     null, recipientArray);*/
		
		
		String[] recipientArray = new String[1];
		recipientArray[0] = be;

		BaseEntity item = MergeUtil.getBaseEntityForAttr(be, token);
		BaseEntity[]  itemArray = new BaseEntity[1];
		itemArray[0] = item;
		QDataBaseEntityMessage msg = new QDataBaseEntityMessage(itemArray, null,
			      null);
		
		msg.setRecipientCodeArray(recipientArray);
		msg.setToken(token);
		String msgStr = JsonUtils.toJson(msg);
		return msgStr;
	
	}
	
	@SuppressWarnings("unchecked")
	public static String disburseAccount(String assembyUserId, String paymentMethodString, String authToken) {
						
		String disburseAccountResponse = null;
		
		if(assembyUserId != null && paymentMethodString != null) {
			
			System.out.println( "Payment account method string is not null");
			
			JSONObject paymentMethodObj = JsonUtils.fromJson(paymentMethodString, JSONObject.class);
			String paymentAccountId = paymentMethodObj.get("accountNumber").toString();
			
			JSONObject disburseAccObj = new JSONObject();			
			
			JSONObject accObj = new JSONObject();
			accObj.put("id", paymentAccountId);
			disburseAccObj.put("account", accObj);
			
			try {
				System.out.println("disbursement account object ::"+disburseAccObj);
				disburseAccountResponse = PaymentEndpoint.disburseAccount(assembyUserId, JsonUtils.toJson(disburseAccObj), authToken);
				System.out.println("disburse payment response ::"+disburseAccountResponse);
				
			} catch (PaymentException e) {
				log.error("Payment exception during payment disimbursement response");
				log.error("disburse payment response ::"+disburseAccountResponse);
				e.printStackTrace();
			}		
			
		} else {
			try {
				throw new PaymentException("Payment Disimbursement failed because of null values, assemblyUserId ::"+assembyUserId+", payment method string ::"+paymentMethodString);
			} catch (PaymentException e) {
				log.error("Payment exception caught during payment disimbursement");
				e.printStackTrace();
			}
		}
		
		return disburseAccountResponse;
	}
	
	public static String findExistingAssemblyUserAndSetAttribute(String userId, String tokenString, String authToken) {

		BaseEntity userBe = MergeUtil.getBaseEntityForAttr(userId, tokenString);
		Object email = MergeUtil.getBaseEntityAttrObjectValue(userBe, "PRI_EMAIL");

		if (email != null) {
			
			String paymentUsersResponse;
			try {
				paymentUsersResponse = PaymentEndpoint.searchUser(email.toString(), authToken);
				
				if (!paymentUsersResponse.contains("error")) {
					
					System.out.println("payment user search response ::" + paymentUsersResponse);
					JSONObject userObj = JsonUtils.fromJson(paymentUsersResponse, JSONObject.class);
					
					ArrayList<Map> userList = (ArrayList<Map>) userObj.get("users");
					
					if (userList.size() > 0) {
						for (Map userDetails : userList) {

							Map<String, Object> contactInfoMap = (Map<String, Object>) userDetails.get("contactInfo");
							Object contactEmail = contactInfoMap.get("email");

							if (contactEmail != null && contactEmail.equals(email)) {

								String assemblyUserId = userDetails.get("id").toString();
								return assemblyUserId;

							} else {
								log.error("USER HAS NOT SET ASSEMBLY EMAIL ID");
							}
						}
					} else {
						log.error("No user found in assembly user");
					}
				} 
				
			} catch (PaymentException e) {
				log.error("Payment user search has returned a null response");
				e.printStackTrace();
			}
			

		} else {
			log.error("BASEENTITY HAS NULL EMAIL ATTRIBUTE");
		}

		return null;
	}
	

}
