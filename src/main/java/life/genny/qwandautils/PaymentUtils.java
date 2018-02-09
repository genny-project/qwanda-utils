package life.genny.qwandautils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.vertx.core.json.JsonObject;
import life.genny.qwanda.Answer;
import life.genny.qwanda.entity.BaseEntity;
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
	

	
	
	@SuppressWarnings("unchecked")
	public static String getAssemblyId(String token) {
		
		String userCode = QwandaUtils.getUserCode(token);
		
		JSONObject authobj = new JSONObject();
		authobj.put("userCode", userCode);
		//authobj.put("UUID", UUID.randomUUID().toString());

		String encodedAuthString = base64Encoder(authobj.toJSONString());
		return encodedAuthString;
		
	}
	
	
	public static Boolean checkIfAssemblyUserExists(String assemblyUserId) {
		
		if(assemblyUserId != null) {
			String authToken = getAssemblyAuthKey();
			
			String assemblyUserString = PaymentEndpoint.getAssemblyUserById(assemblyUserId, authToken);
			if(!assemblyUserString.contains("error")) {
				return true;
			} 
			System.out.println("assembly user string ::"+assemblyUserString);
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public static void createAssemblyUser(String assemblyUserId, String authToken, String token) { 
		
		String userCode = QwandaUtils.getUserCode(token);
		BaseEntity be = MergeUtil.getBaseEntityForAttr(userCode, token);

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

	
		PaymentEndpoint.createAssemblyUser(JsonUtils.toJson(userobj), authToken);		
	}
	
	public static String getPaymentsUser(String assemblyUserId, String authToken){
		String responseString = PaymentEndpoint.getAssemblyUserById(assemblyUserId, authToken);
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
			responseString = PaymentEndpoint.updateAssemblyUser(assemblyUserId, JsonUtils.toJson(userobj), authToken);
			System.out.println("response string from payments user updation ::"+responseString);
		}
		
		/* For Assembly User Company Information Update */
		if(companyContactInfoObj != null && companyId != null) {
			companyObj = new JSONObject();
			companyObj.put("contactInfo", companyContactInfoObj);
			companyObj.put("id", companyId);
		}
		
		
		if(companyId != null && companyObj != null) {
			System.out.println("updating company object in assembly ::"+companyObj);
			responseString = PaymentEndpoint.updateCompany(companyId, JsonUtils.toJson(companyObj), authToken);
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
			createCompanyResponse = PaymentEndpoint.createCompany(JsonUtils.toJson(companyObj), authtoken);
			
			if(!createCompanyResponse.contains("error")) {
				JSONObject companyResponseObj = JsonUtils.fromJson(createCompanyResponse, JSONObject.class);
				
				if(companyResponseObj.get("id") != null) {
					companyCode = companyResponseObj.get("id").toString();
				}		
			}
			
			/*if ("{\"error\":\"Invalid token and / or secret.\"}".equalsIgnoreCase(createCompanyResponse)) {
				return createCompanyResponse;
			} else {
				JSONObject companyResponseObj = JsonUtils.fromJson(createCompanyResponse, JSONObject.class);
				companyCode = companyResponseObj.get("id").toString();
			}*/
		}
		
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
		
		if(begBe != null) {
			
			String feeId = getPaymentFeeId(begBe, assemblyauthToken);
			System.out.println("fee Id ::"+feeId);
				
			String begTitle = MergeUtil.getBaseEntityAttrValueAsString(begBe, "PRI_TITLE");
			String begDescription = MergeUtil.getBaseEntityAttrValueAsString(begBe, "PRI_DESCRIPTION");
			itemObj.put("name", begTitle);
			itemObj.put("description", begDescription);
			
			/* driverPriceIncGST = ownerPriceIncGST.subtract(feePriceIncGST), Creating Payments Fee with feePriceIncGST */
			String begPriceString = MergeUtil.getBaseEntityAttrValueAsString(begBe, "PRI_OFFER_DRIVER_PRICE_INC_GST");
			
			if(begPriceString != null) {	
				
				System.out.println("begpriceString ::"+begPriceString);	
				
				String currency = QwandaUtils.getCurrencyAsString(begPriceString);
				String amount = QwandaUtils.getAmountAsString(begPriceString);
				
				BigDecimal begPrice = new BigDecimal(amount);
				
				//350 Dollars sent to Assembly as 3.50$, so multiplying with 100 
				BigDecimal finalPrice = begPrice.multiply(new BigDecimal(100));
							
				itemObj.put("amount", finalPrice.toString());		
				itemObj.put("currency", currency);
				
				if(feeId != null) {
					String[] feeArr = {feeId};
					itemObj.put("fees", feeArr);
				}
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
		}
		
		/* DRIVER -> Seller */
		if(itemContextMap.containsKey("QUOTER")) {
			
			BaseEntity driverBe = itemContextMap.get("QUOTER");
			System.out.println("Context map contains QUOTER");
			
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
			
			String itemCreationResponse = PaymentEndpoint.createItem(JsonUtils.toJson(itemObj), assemblyauthToken);
			
			if(!itemCreationResponse.contains("error")) {
				itemId = itemObj.get("id").toString();
				log.info("Item object ::"+itemObj);
			}
			
			log.info("Item creation response ::"+itemCreationResponse);		
			
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
	
	public static void saveTokenAnswers(String qwandaServiceUrl, String userId, String tokenString, String assemblyId,
			String assemblyAuthToken) {

		JSONParser parser = new JSONParser();

		if (assemblyId != null) {

			String tokenResponse = authenticatePaymentProvider(assemblyId, assemblyAuthToken);

			if (!tokenResponse.contains("error")) {
				
				try {
					JSONObject tokenObj = (JSONObject) parser.parse(tokenResponse);
					System.out.println("token object ::" + tokenObj);

					String providerToken = tokenObj.get("token").toString();

					Answer cardTokenAnswer = new Answer(userId, userId, "PRI_ASSEMBLY_CARD_TOKEN", providerToken);
					saveAnswer(qwandaServiceUrl, cardTokenAnswer, tokenString);

					Answer bankTokenAnswer = new Answer(userId, userId, "PRI_ASSEMBLY_BANK_TOKEN", providerToken);
					saveAnswer(qwandaServiceUrl, bankTokenAnswer, tokenString);

				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		} else {
			log.error("ASSEMBLY USER ID IS NULL");
		}

	}
	
	
	@SuppressWarnings("unchecked")
	private static String authenticatePaymentProvider(String assemblyId, String assemblyAuthToken) {
		
		JSONObject paymentProviderObj = new JSONObject();
		JSONObject userObj = new JSONObject();
		
		userObj.put("id", assemblyId);
		
		paymentProviderObj.put("type", PROVIDER_TYPE_BANK);
		paymentProviderObj.put("user", userObj);
		
		String tokenResponse = PaymentEndpoint.authenticatePaymentProvider(JsonUtils.toJson(paymentProviderObj), assemblyAuthToken);
		
		return tokenResponse;
	}
	
	
	@SuppressWarnings("unchecked")
	public static String getPaymentFeeId(BaseEntity begBe, String assemblyAuthToken) {
		
		JSONParser parser = new JSONParser();
		String feeId = null;
		
		//Object fee = MergeUtil.getBaseEntityAttrObjectValue(begBe, "PRI_FEE");
		String begFeeString = MergeUtil.getBaseEntityAttrValueAsString(begBe, "PRI_FEE_INC_GST");
		
		if (begFeeString != null) {
			System.out.println("begpriceString ::" + begFeeString);

			
			String currency = QwandaUtils.getCurrencyAsString(begFeeString);
			String amount = QwandaUtils.getAmountAsString(begFeeString);

			BigDecimal begPrice = new BigDecimal(amount);

			// 350 Dollars sent to Assembly as 3.50$, so multiplying with 100
			BigDecimal finalFee = begPrice.multiply(new BigDecimal(100));

			JSONObject feeObj = new JSONObject();
			feeObj.put("name", "Channel40 fee");
			feeObj.put("type", 1);
			feeObj.put("amount", finalFee);
			feeObj.put("cap", null);
			feeObj.put("min", null);
			feeObj.put("max", null);
			feeObj.put("to", "buyer");

			String feeResponse = PaymentEndpoint.createFees(JsonUtils.toJson(feeObj), assemblyAuthToken);

			if (feeResponse != null) {
				JSONObject feeResponseObj;
				try {
					feeResponseObj = (JSONObject) parser.parse(feeResponse);

					if (feeResponseObj.get("id") != null) {
						feeId = feeResponseObj.get("id").toString();
						return feeId;
					}

				} catch (ParseException e) {
					e.printStackTrace();
				}

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
	public static String makePayment(String begCode, String authToken, String tokenString) {
		
		String userCode = QwandaUtils.getUserCode(tokenString);
		BaseEntity userBe = MergeUtil.getBaseEntityForAttr(userCode, tokenString);
		BaseEntity begBe = MergeUtil.getBaseEntityForAttr(begCode, tokenString);
		
		String paymentResponse = null;
		
		Object ipAddress = MergeUtil.getBaseEntityAttrObjectValue(userBe, "PRI_IP_ADDRESS");
		Object deviceId = MergeUtil.getBaseEntityAttrObjectValue(userBe, "PRI_DEVICE_ID");
		Object itemId = MergeUtil.getBaseEntityAttrObjectValue(begBe, "PRI_ITEM_ID");
		Object accountId = MergeUtil.getBaseEntityAttrObjectValue(begBe, "PRI_ACCOUNT_ID");
		
		if(itemId != null && deviceId != null && ipAddress != null && accountId != null) {
			
			JSONObject paymentObj = new JSONObject();
			paymentObj.put("id", itemId);
			
			JSONObject accountObj = new JSONObject();
			accountObj.put("id", accountId);
			
			paymentObj.put("ipAddress", ipAddress);
			paymentObj.put("deviceID", deviceId);
			paymentObj.put("account", accountObj);
			
			paymentResponse = PaymentEndpoint.makePayment(itemId.toString(), JsonUtils.toJson(paymentObj), authToken);
			
			log.debug("Make payment response ::"+paymentResponse);
			return paymentResponse;
			
		} else {
			
			if(itemId == null) {
				log.error("ITEM ID");
			}
			
			if(deviceId == null) {
				log.error("deviceId ID");
			}
			
			if(ipAddress == null) {
				log.error("ipAddress ID");
			}
			
			if(accountId == null) {
				log.error("accountId ID");
			}
			
			log.error("One of the attribute for making payment is null ! PAYMENT CANNOT BE MADE");
		}
		
		
		return paymentResponse;
	}
	
	public static String releasePayment(String begCode, String authToken, String tokenString) {
		
		System.out.println("BEG Code for release payment ::"+begCode);
		BaseEntity begBe = MergeUtil.getBaseEntityForAttr(begCode, tokenString);
		
		String paymentResponse = null;
		Object itemId = MergeUtil.getBaseEntityAttrObjectValue(begBe, "PRI_ITEM_ID");
		
		if(itemId != null) {
			paymentResponse = PaymentEndpoint.releasePayment(itemId.toString(), authToken);
			log.debug("release payment response ::"+paymentResponse);
		}
		
		return paymentResponse;
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
	public static String disburseAccount(String assembyUserId, String accountId, String authToken) {
				
		String disburseAccountResponse = null;
		
		if(assembyUserId != null && accountId != null) {
			
			JSONObject disburseAccObj = new JSONObject();			
			
			JSONObject accObj = new JSONObject();
			accObj.put("id", accountId);
			disburseAccObj.put("account", accObj);
			
			disburseAccountResponse = PaymentEndpoint.disburseAccount(assembyUserId, JsonUtils.toJson(disburseAccObj), authToken);
			log.debug("release payment response ::"+disburseAccountResponse);
		}
		
		return disburseAccountResponse;
	}
	

}
