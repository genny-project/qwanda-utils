package life.genny.qwandautils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.javamoney.moneta.Money;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.CodedEntity;
import life.genny.qwanda.Link;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.entity.Person;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;

public class QwandaUtils {

	private static int apiPort = System.getenv("API_PORT") != null ? (Integer.parseInt(System.getenv("API_PORT")))
			: 8088;
	private static String hostIP = System.getenv("HOSTIP") != null ? System.getenv("HOSTIP") : "127.0.0.1";
	
	private static String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");


	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String ANSI_BOLD = "\u001b[1m";

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static String apiGet(String getUrl, final String authToken) throws ClientProtocolException, IOException {
		log.debug("GET:" + getUrl + ":");
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet request = new HttpGet(getUrl);
		if (authToken != null) {
			request.addHeader("Authorization", "Bearer " + authToken); // Authorization": `Bearer
		}

		CloseableHttpResponse response = httpclient.execute(request);
		// The underlying HTTP connection is still held by the response object
		// to allow the response content to be streamed directly from the network
		// socket.
		// In order to ensure correct deallocation of system resources
		// the user MUST call CloseableHttpResponse#close() from a finally clause.
		// Please note that if response content is not fully consumed the underlying
		// connection cannot be safely re-used and will be shut down and discarded
		// by the connection manager.
		try {
			HttpEntity entity1 = response.getEntity();
			String responseString = EntityUtils.toString(entity1);

			EntityUtils.consume(entity1);

			return responseString;
		} finally {
			IOUtils.closeQuietly(response);
			IOUtils.closeQuietly(httpclient);
		}

	}

	public static String apiPostEntity(final String postUrl, final String entityString, final String authToken)
			throws IOException {
		CloseableHttpClient httpclient = HttpClientBuilder.create().build();
		CloseableHttpResponse response = null;
		try {

			HttpPost post = new HttpPost(postUrl);

			StringEntity postEntity = new StringEntity(entityString, "UTF-8");

			post.setEntity(postEntity);
			post.setHeader("Content-Type", "application/json; charset=UTF-8");
			if (authToken != null) {
				post.addHeader("Authorization", "Bearer " + authToken); // Authorization": `Bearer
			}

			response = httpclient.execute(post);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity);
			return responseString;
		} finally {
			IOUtils.closeQuietly(response);
			IOUtils.closeQuietly(httpclient);
		}

	}

	public static String apiPost(final String postUrl, final ArrayList<BasicNameValuePair> nameValuePairs,
			final String authToken) throws IOException {

		return apiPostEntity(postUrl, (new UrlEncodedFormEntity(nameValuePairs)).toString(), authToken);

	}

	public static String apiDelete(final String deleteUrl, final String entityString, final String authToken)
			throws IOException {
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpDeleteWithBody request = new HttpDeleteWithBody(deleteUrl);
		if (authToken != null) {
			request.addHeader("Authorization", "Bearer " + authToken); // Authorization": `Bearer
		}

		CloseableHttpResponse response = httpclient.execute(request);
		// The underlying HTTP connection is still held by the response object
		// to allow the response content to be streamed directly from the network
		// socket.
		// In order to ensure correct deallocation of system resources
		// the user MUST call CloseableHttpResponse#close() from a finally clause.
		// Please note that if response content is not fully consumed the underlying
		// connection cannot be safely re-used and will be shut down and discarded
		// by the connection manager.
		try {
			HttpEntity entity1 = response.getEntity();
			String responseString = EntityUtils.toString(entity1);

			EntityUtils.consume(entity1);

			return responseString;
		} finally {
			IOUtils.closeQuietly(response);
			IOUtils.closeQuietly(httpclient);
		}
	}

	public static String apiPutEntity(final String postUrl, final String entityString, final String authToken)
			throws IOException {
		CloseableHttpClient httpclient = HttpClientBuilder.create().build();
		CloseableHttpResponse response = null;
		try {

			HttpPut post = new HttpPut(postUrl);

			StringEntity postEntity = new StringEntity(entityString, "UTF-8");

			post.setEntity(postEntity);
			post.setHeader("Content-Type", "application/json; charset=UTF-8");
			if (authToken != null) {
				post.addHeader("Authorization", "Bearer " + authToken); // Authorization": `Bearer
			}

			response = httpclient.execute(post);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity);
			return responseString;
		} finally {
			IOUtils.closeQuietly(response);
			IOUtils.closeQuietly(httpclient);
		}

	}

	public static BaseEntity createUserFromToken(final String qwandaUrl, final String serviceToken,
			final String userToken) throws IOException {
		JSONObject decodedToken = KeycloakUtils.getDecodedToken(userToken);

		String username = decodedToken.getString("preferred_username");
		String firstname = decodedToken.getString("given_name");
		String lastname = decodedToken.getString("family_name");
		String email = decodedToken.getString("email");

		BaseEntity be = createUser(qwandaUrl, serviceToken, username, firstname, lastname, email);

		return be;
	}

	public static Long postBaseEntity(final String qwandaUrl, final String token, final BaseEntity be)
			throws IOException {

		String jsonBE = JsonUtils.toJson(be);

		String retStr = QwandaUtils.apiPostEntity(qwandaUrl + "/qwanda/baseentitys", jsonBE, token);
		if (retStr.equals("<html><head><title>Error</title></head><body>Internal Server Error</body></html>")) {
			return null;
		}
		try {
			Long ret = Long.parseLong(retStr);
			be.setId(ret);
			return ret;
		} catch (NumberFormatException e) {
			log.error("Error in posting BaseEntity "+retStr);
			return -1L;
		}
	}

	public static Answer postAnswer(final String qwandaUrl, final String token, final Answer answer)
			throws IOException {
		if (answer.getValue() != null) {

			String id = QwandaUtils.apiPostEntity(qwandaUrl + "/qwanda/answers", JsonUtils.toJson(answer), token);
			// TODO check id is returned
		}

		return answer;
	}

	public static Link postLink(final String qwandaUrl, final String token, final Link link) throws IOException {

		QwandaUtils.apiPostEntity(qwandaUrl + "/qwanda/entityentitys", JsonUtils.toJson(link), token);

		return link;
	}

	public static String getNormalisedUsername(final String rawUsername) {
		return rawUsername.replaceAll("\\&", "_AND_").replaceAll("@", "_AT_").replaceAll("\\.", "")
				.replaceAll("\\+", "_PLUS_").toUpperCase();
	}

	public static BaseEntity createUser(final String qwandaUrl, final String token, final String username,
			final String firstname, final String lastname, final String email) throws IOException {

		return createUser(qwandaUrl, token, username, firstname, lastname, email, "genny", firstname + " " + lastname,
				null);
	}

	public static BaseEntity createUser(final String qwandaUrl, final String token, final String username,
			final String firstname, final String lastname, final String email, final String realm, final String name,
			final String keycloakId) throws IOException {

		String uname = getNormalisedUsername(username);
		String code = "PER_" + uname.toUpperCase();

		Person person = new Person(code, firstname + " " + lastname);

		postBaseEntity(qwandaUrl, token, person);

		List<Answer> answers = new ArrayList<Answer>();

		answers.add((new Answer(code, code, "PRI_USERNAME", username)));
		answers.add((new Answer(code, code, "PRI_FIRSTNAME", firstname)));
		answers.add((new Answer(code, code, "PRI_LASTNAME", lastname)));
		answers.add((new Answer(code, code, "PRI_EMAIL", email)));
		answers.add((new Answer(code, code, "PRI_REALM", realm)));
		answers.add((new Answer(code, code, "PRI_NAME", name)));
		answers.add((new Answer(code, code, "PRI_KEYCLOAK_ID", keycloakId)));

		Answer items[] = new Answer[answers.size()];
		items = answers.toArray(items);
		QDataAnswerMessage msg = new QDataAnswerMessage(items);

		String jsonAnswer = JsonUtils.toJson(msg);
		QwandaUtils.apiPostEntity(qwandaUrl + "/qwanda/answers/bulk", jsonAnswer, token);

		postLink(qwandaUrl, token, new Link("GRP_USERS", code, "LNK_CORE"));
		postLink(qwandaUrl, token, new Link("GRP_PEOPLE", code, "LNK_CORE"));

		return person;
	}

	public static Boolean checkUserTokenExists(final String qwandaUrl, final String userToken) throws IOException {
		JSONObject decodedToken = KeycloakUtils.getDecodedToken(userToken);
		String tokenSub = decodedToken.getString("sub");
		System.out.println("sub token::" + tokenSub);

		String username = decodedToken.getString("preferred_username");

		String uname = getNormalisedUsername(username);
		String code = "PER_" + uname.toUpperCase();

		System.out.println("username ::" + username);
		System.out.println("uname::" + uname);
		System.out.println("code::" + code);

		System.out.println("code::" + code);
		Boolean tokenExists = false;

		String attributeString = QwandaUtils.apiGet(qwandaUrl + "/qwanda/baseentitys/" + code, userToken);

		if ((attributeString == null) || attributeString.contains("Error")
				|| attributeString.contains("Unauthorized")) {
			System.out.println("baseentity not found");
			tokenExists = false;
		} else {
			tokenExists = true;
		}

		/*
		 * else { String attributeVal = MergeUtil.getAttrValue(code,
		 * "PRI_KEYCLOAK_UUID", userToken);
		 * 
		 * System.out.println("pri_keycloak_UUID for the code::"+attributeVal);
		 * if(attributeVal == null){ tokenExists = false;
		 * System.out.println("baseentity found and UUID is null"); }else
		 * if(tokenSub.equals(attributeVal)) {
		 * System.out.println("baseentity found and UUID matched"); tokenExists = true;
		 * } else if(!tokenSub.equals(attributeVal)) {
		 * System.out.println("baseentity code found but keycloak UUID not matched");
		 * tokenExists = false; } }
		 */

		return tokenExists;
	}

	/**
	 * 
	 * @param sourceBaseEntityCode
	 * @param targetBaseEntityCode
	 * @param questionCode
	 * @param token
	 * @return if mandatory fields of the form has been completed
	 *         <p>
	 *         For sourceBaseEntityCode : PER_USERXX, targetBaseEntityCode :
	 *         BEG_XXX/LOD_XXXX, questionCode : Question grpCode, method checks if
	 *         all the mandatory fields in the Question Group has been entered
	 *         </p>
	 */
	static Map<String, String> askMap = new ConcurrentHashMap<String, String>();

	public static Boolean isMandatoryFieldsEntered(String sourceBaseEntityCode, String targetBaseEntityCode,
			String questionCode, final String userToken) {

		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		// String qwandaServiceUrl = "http://localhost:8280";
		try {
			String attributeString = null;
			String key = sourceBaseEntityCode + ":" + questionCode + ":" + targetBaseEntityCode
					+ userToken.substring(0, 8); // get first 8 chars
		//	if (askMap.containsKey(key)) {
		//		attributeString = askMap.get(key);
		//	} else {
				attributeString = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" + sourceBaseEntityCode
						+ "/asks3/" + questionCode + "/" + targetBaseEntityCode, userToken);
		//		askMap.put(key, attributeString);
		//	}
			log.debug("Attribute String="+attributeString);
			QDataAskMessage askMsgs = JsonUtils.fromJson(attributeString, QDataAskMessage.class);
			BaseEntity be = MergeUtil.getBaseEntityForAttr(targetBaseEntityCode, userToken);

			if (askMsgs != null) {
			for (Ask parentAsk : askMsgs.getItems()) {
				for (Ask childAsk : parentAsk.getChildAsks()) {
					// log.info("parent ask code ::"+parentAsk.getAttributeCode());
			      if( childAsk.getChildAsks() != null) {
					for (Ask basicChildAsk : childAsk.getChildAsks()) {
						if ( !isAsksMandatoryFilled(be,basicChildAsk ) ) {
						     return false;
						}

					}
			      }else {
			    	        if ( !isAsksMandatoryFilled(be, childAsk) ) {
						     return false;
						}
			    	      }
			      }

			 }
			} else {
				log.error("AskMsg is NULL! "+qwandaServiceUrl + "/qwanda/baseentitys/" + sourceBaseEntityCode
						+ "/asks3/" + questionCode + "/" + targetBaseEntityCode);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public static Boolean isAsksMandatoryFilled(BaseEntity be, Ask asks) {
		if (asks.getMandatory()) {
			/*
			 * Optional<EntityAttribute> attributeVal =
			 * be.findEntityAttribute(basicChildAsk.getAttributeCode()); if
			 * (attributeVal.isPresent()) { if (attributeVal.get() == null) {
			 * System.out.println("This attribute value of "+basicChildAsk.getAttributeCode(
			 * ) +" is not filled and is null"); return false; } } else { return false; }
			 */
			// System.out.println("child ask attribute code
			// ::"+basicChildAsk.getAttributeCode());
			Object attributeVal = MergeUtil.getBaseEntityAttrObjectValue(be,
					asks.getAttributeCode());
			if (attributeVal != null) {
			}

			if (attributeVal == null) {
				// System.out.println(basicChildAsk.getAttributeCode() + " is null");
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param baseEntAttributeCode
	 * @param token
	 * @return Deserialized BaseEntity model object with values for a BaseEntity code that is passed
	 * @throws IOException 
	 */
	public static BaseEntity getBaseEntityByCode(String baseEntAttributeCode, String token) throws  IOException {
		
		String attributeString = null;
		BaseEntity be = null;
		try {
			attributeString = QwandaUtils
					.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" +baseEntAttributeCode, token);
			be = JsonUtils.fromJson(attributeString, BaseEntity.class);
			if (be == null) {
				throw new IOException("Cannot find BE "+baseEntAttributeCode);
			}
			
		} catch (IOException e)  {
			throw new IOException("Cannot connect to QwandaURL "+qwandaServiceUrl);
		}
		
		
		return be;
	}
	
	/**
	 * 
	 * @param baseEntAttributeCode
	 * @param token
	 * @return Deserialized BaseEntity model object with values for a BaseEntity code that is passed
	 * @throws IOException 
	 */
	public static BaseEntity getBaseEntityByCodeWithAttributes(String baseEntAttributeCode, String token) throws  IOException {
		
		String attributeString = null;
		BaseEntity be = null;
		try {
			attributeString = QwandaUtils
					.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" +baseEntAttributeCode+"/attributes", token);
			be = JsonUtils.fromJson(attributeString, BaseEntity.class);
			if (be == null) {
				throw new IOException("Cannot find BE "+baseEntAttributeCode);
			}
			
		} catch (IOException e)  {
			throw new IOException("Cannot connect to QwandaURL "+qwandaServiceUrl);
		}
		
		
		return be;
	}
	
	/**
	 * 
	 * @param username
	 * @return baseEntity code for the userName passed
	 */
	public static String getBaseEntityCodeForUserName(String username, String userToken) {

		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		// String qwandaServiceUrl = "http://localhost:8280";

		String baseEntityCode = null;
		try {
			String attributeString = QwandaUtils.apiGet(qwandaServiceUrl
					+ "/qwanda/baseentitys/GRP_PEOPLE/linkcodes/LNK_CORE/attributes?PRI_USERNAME=" + username,
					userToken);
			System.out.println("attribute string::" + attributeString);

			QDataBaseEntityMessage msg = JsonUtils.fromJson(attributeString, QDataBaseEntityMessage.class);

			for (BaseEntity be : msg.getItems()) {
				baseEntityCode = be.getCode();
				System.out.println("baseEntity code for username ::" + baseEntityCode);
				return baseEntityCode;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return baseEntityCode;

	}

	/**
	 * 
	 * @param templateCode
	 * @param token
	 * @return template
	 */
	public static QBaseMSGMessageTemplate getTemplate(String templateCode, String token) {

		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		String attributeString;
		QBaseMSGMessageTemplate template = null;
		try {
			attributeString = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/templates/" + templateCode, token);
			template = JsonUtils.fromJson(attributeString, QBaseMSGMessageTemplate.class);
			System.out.println("template sms:" + template.getSms_template());

		} catch (IOException e) {
			e.printStackTrace();
		}

		return template;
	}

	/**
	 * 
	 * @param groupCode
	 * @param token
	 * @return all the children links for a given groupCode
	 */
	public static List getLinkList(String groupCode, String token) {

		// String qwandaServiceUrl = "http://localhost:8280";
		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		List linkList = null;

		try {
			String attributeString = QwandaUtils.apiGet(
					qwandaServiceUrl + "/qwanda/entityentitys/" + groupCode + "/linkcodes/LNK_BEG/children", token);
			if (attributeString != null) {
				linkList = JsonUtils.fromJson(attributeString, List.class);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return linkList;

	}

	/**
	 * 
	 * @param attributeCode
	 * @param token
	 * @return BaseEntity with children (alias code, code of the attributes for the
	 *         BaseEntity) for the BaseEntity code
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, BaseEntity> getBaseEntWithChildrenForAttributeCode(String attributeCode, String token) {

		Map<String, BaseEntity> entityTemplateContextMap = new HashMap<String, BaseEntity>();

		List linkList = getLinkList(attributeCode, token);

		if (linkList != null) {
			System.out.println(ANSI_BLUE + "Got BEG string" + ANSI_RESET + linkList.toString());

			linkList.forEach(item -> {
				Link link = JsonUtils.fromJson(item.toString(), Link.class);
				String baseEntAttributeCode = link.getTargetCode();
				System.out.println("base attribute target code ::" + baseEntAttributeCode);
				if (link.getLinkValue() != null) {

					/**
					 * Creating a template : LinkValue -> BaseEntityForCorrespondingLinkCode
					 * <Example> DRIVER - PER_USER2, OWNER - PER_USER1 </example>
					 */
					entityTemplateContextMap.put(link.getLinkValue(),
							MergeUtil.getBaseEntityForAttr(baseEntAttributeCode, token));

				}
			});

		}

		System.out.println("base entity context map ::" + entityTemplateContextMap);
		return entityTemplateContextMap;

	}

	public static String getUniqueId(String baseEntityCode, String questionId, String prefix, String token) {

		String uniqueID = UUID.randomUUID().toString().replaceAll("-", "");

		BaseEntity be = MergeUtil.getBaseEntityForAttr(baseEntityCode, token);
		String nameInitials = getInitials(be.getName().split("\\s+"));

		// working on it
		// String companyCode = getCompanyCode(baseEntityCode, token);

		return prefix + "_" + nameInitials + uniqueID;
	}

	public static String getInitials(String[] strarr) {

		String initials = "";

		for (String str : strarr) {
			System.out.println("str :" + str);
			initials = (str != null && (str.length() > 0)) ? initials.concat(str.substring(0, 2)) : initials.concat("");
		}

		return initials.toUpperCase();
	}

	public static QDataBaseEntityMessage getDataBEMessage(String groupCode, String linkCode, String token) {

		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		 //String qwandaServiceUrl = "http://localhost:8280";
		QDataBaseEntityMessage dataBEMessage = null;

		try {
			String attributeString = QwandaUtils.apiGet(
					qwandaServiceUrl + "/qwanda/baseentitys/" + groupCode + "/linkcodes/" + linkCode + "/attributes",
					token);
			dataBEMessage = JsonUtils.fromJson(attributeString, QDataBaseEntityMessage.class);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return dataBEMessage;

	}

	public static String getCompleteAddress(String code, String targetAttributeCode, String token) {

		BaseEntity be = MergeUtil.getBaseEntityForAttr(code, token);

		String street = "";
		String city = "";
		String state = "";
		String postcode = "";
		String country = "";
		String address = null;
		String targetCode = null;

		if (targetAttributeCode.equalsIgnoreCase("PRI_FULL_DROPOFF_ADDRESS")) {
			targetCode = "DROPOFF";
		} else if (targetAttributeCode.equalsIgnoreCase("PRI_FULL_PICKUP_ADDRESS")) {
			targetCode = "PICKUP";
		}

		for (EntityAttribute ea : be.getBaseEntityAttributes()) {
			if (ea.getAttributeCode().equals("PRI_" + targetCode + "_STREET_ADDRESS1")) {
				street = ea.getObjectAsString();
				System.out.println("street ::" + street);
			}

			if (ea.getAttributeCode().equals("PRI_" + targetCode + "_CITY")) {
				city = ea.getObjectAsString();
				System.out.println("city ::" + city);
			}

			if (ea.getAttributeCode().equals("PRI_" + targetCode + "_STATE")) {
				state = ea.getObjectAsString();
				System.out.println("state ::" + state);
			}

			if (ea.getAttributeCode().equals("PRI_" + targetCode + "_POSTCODE")) {
				postcode = ea.getObjectAsString();
				System.out.println("postcode ::" + postcode);
			}

			if (ea.getAttributeCode().equals("PRI_" + targetCode + "_COUNTRY")) {
				country = ea.getObjectAsString();
				System.out.println("country ::" + country);
			}
		}

		if (targetCode != null) {
			address = street + ", " + city + ", " + state + " " + postcode + " " + country;
		}

		return address;

	}

	// creating new BaseEntity by only baseentityCode
	public static BaseEntity createBaseEntityByCode(String entityCode, String name, String qwandaUrl, String token) {
		BaseEntity beg = new BaseEntity(entityCode, name);

		String jsonBE = JsonUtils.toJson(beg);
		try {
			// save BE
			QwandaUtils.apiPostEntity(qwandaUrl + "/qwanda/baseentitys", jsonBE, token);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return beg;

	}

	/**
	 * 
	 * @param parentCode
	 * @param parentLinkCode
	 * @param childLinkCode
	 * @param token
	 * @return if links exists for childLinkCode and parentCode
	 *         <p>
	 *         For parentCode : PER_USER, parentLinkCode : LNK_CODE, childLinkCode :
	 *         OFFER_CODE, this API checks if there is a link LNK_CODE between
	 *         PER_USER and OFFER_CODE.
	 *         </p>
	 */
	public static Boolean checkIfLinkExistsForTarget(String parentCode, String linkCode, String childCode,
			String token) {

		Boolean isLinkExists = false;
		QDataBaseEntityMessage dataBEMessage = getDataBEMessage(parentCode, linkCode, token);

		if (dataBEMessage != null) {
			BaseEntity[] beArr = dataBEMessage.getItems();

			if (beArr.length > 0) {
				for (BaseEntity be : beArr) {
					if (be.getCode().equals(childCode)) {
						isLinkExists = true;
						return isLinkExists;
					}
				}
			} else {
				isLinkExists = false;
				return isLinkExists;
			}

		}

		return isLinkExists;
	}

	/**
	 * 
	 * @param groupCode
	 * @param attributeCode
	 * @param sourceOrTarget
	 *            (The input code could be either the sourceCode or the targetCode
	 *            of a groupLink)
	 * @param linkValue
	 * @param isSource
	 *            (Determines if the input rule requires a sourceCode/targetCode)
	 * @param token
	 * @return sourceCode/targetCode if the link values match the input given
	 */
	public static String getSourceOrTargetForGroupLink(String groupCode, String attributeCode, String sourceOrTarget,
			String linkValue, Boolean isSource, String token) {

		QDataBaseEntityMessage dataBEMessage = getDataBEMessage(groupCode, "LNK_CORE", token);
		String code = null;

		if (dataBEMessage != null) {

			for (BaseEntity be : dataBEMessage.getItems()) {
				
				Set<EntityEntity> eeSet = be.getLinks();
				
				for(EntityEntity ee : eeSet) {
					Link finalLink = ee.getLink();
					
					if (isSource) {
						if (finalLink != null && finalLink.getAttributeCode() != null && finalLink.getTargetCode() != null
								&& finalLink.getLinkValue() != null) {
							String linkAttributeCode = finalLink.getAttributeCode();
							String linkTargetCode = finalLink.getTargetCode();
							String linkLinkValue = finalLink.getLinkValue();

							if (attributeCode.equals(linkAttributeCode) && sourceOrTarget.equals(linkTargetCode)
									&& linkValue.equals(linkLinkValue)) {
								code = finalLink.getSourceCode();
								return code;
							}
						}
					} else { // When the rule wants targetCode
						if (finalLink != null && finalLink.getAttributeCode() != null && finalLink.getTargetCode() != null
								&& finalLink.getLinkValue() != null) {
							String linkAttributeCode = finalLink.getAttributeCode();
							String linkSourceCode = finalLink.getSourceCode();
							String linkLinkValue = finalLink.getLinkValue();

							if (attributeCode.equals(linkAttributeCode) && sourceOrTarget.equals(linkSourceCode)
									&& linkValue.equals(linkLinkValue)) {
								code = finalLink.getTargetCode();
								return code;
							}
						}
					}
					
				}
				
				/*for (EntityEntity entityEntity : be.getLinks()) {
					
					

					Link link = entityEntity.getLink();

					// When the rule want the sourceCode
					if (isSource) {
						if (link != null && link.getAttributeCode() != null && link.getTargetCode() != null
								&& link.getLinkValue() != null) {
							String linkAttributeCode = link.getAttributeCode();
							String linkTargetCode = link.getTargetCode();
							String linkLinkValue = link.getLinkValue();

							if (attributeCode.equals(linkAttributeCode) && sourceOrTarget.equals(linkTargetCode)
									&& linkValue.equals(linkLinkValue)) {
								code = link.getSourceCode();
								return code;
							}
						}
					} else { // When the rule wants targetCode
						if (link != null && link.getAttributeCode() != null && link.getTargetCode() != null
								&& link.getLinkValue() != null) {
							String linkAttributeCode = link.getAttributeCode();
							String linkSourceCode = link.getSourceCode();
							String linkLinkValue = link.getLinkValue();

							if (attributeCode.equals(linkAttributeCode) && sourceOrTarget.equals(linkSourceCode)
									&& linkValue.equals(linkLinkValue)) {
								code = link.getTargetCode();
								return code;
							}
						}
					}

				}*/
			}

		}

		return code;
	}

	/**
	 * 
	 * @param groupCode
	 * @param linkCode
	 *            (which is the attributeCode in the Link model)
	 * @param targetCode
	 * @param linkValue
	 * @param token
	 * @return if the link exists between parent groupCode and a child for a given
	 *         linkCode, linkValue
	 */
	@SuppressWarnings("unchecked")
	public static Boolean checkIfLinkExists(String groupCode, String linkCode, String targetCode, String linkValue,
			String token) {

		// gets all the children of a groupcode with their linkCode,linkValue and code
		// as a list
		List linkList = getLinkList(groupCode, token);
		Boolean isLinkExists = false;

		// checks if a group has the child with the specified linkCode and linkValue
		if (linkList != null) {
			isLinkExists = linkList.stream().anyMatch(item -> {
				Boolean isExists = false;
				Link link = JsonUtils.fromJson(item.toString(), Link.class);
				if (link.getAttributeCode().equals(linkCode) && link.getTargetCode().equals(targetCode)
						&& link.getLinkValue().equals(linkValue))
					isExists = true;
				return isExists;
			});
		}

		return isLinkExists;
	}

	public static String getSystemEnvJson(final String prefix) {
		String ret = "{";
		Map<String, String> env = System.getenv();
		for (String envName : env.keySet()) {
			if (envName.startsWith(prefix)) {
				ret += "\"" + envName.substring(prefix.length()) + "\":\"" + env.get(envName) + "\",";
			}
		}

		ret += "\"prefix\":" + prefix + "\"}";

		return ret;
	}

	public static String executeRuleLogger(String module) {
		String initialLogger = ANSI_YELLOW
				+ "================================================================================================================================================"
				+ ANSI_RESET;
		String moduleLogger = "\n" + ANSI_GREEN + "RULE EXECUTED      ::  " + module + ANSI_RESET;
		return initialLogger + moduleLogger;
	}

	public static String terminateRuleLogger(String module) {
		String initialLogger = "\n \u001B[31m RULE TERMINATED    ::   " + module + " \u001B[0m ";
		String moduleLogger = "\n \u001B[33m ------------------------------------------------------------------------------------------------------------------------------------------------  \u001B[0m";
		return initialLogger + moduleLogger;
	}

	public static String jsonLogger(String module, Object data) {
		String initialLogger = "------------------------------------------------------------------------\n";
		String moduleLogger = ANSI_YELLOW + module + "   ::   " + ANSI_RESET + data + "\n";
		String finalLogger = "------------------------------------------------------------------------\n";
		return initialLogger + moduleLogger + finalLogger;
	}

	/**
	 * 
	 * @param groupCode
	 * @param targetCode
	 * @param linkCode
	 * @param linkValue
	 * @param weight
	 * @param token
	 * @return response string after creating a link in the DataBase
	 */
	public static Link createLink(String groupCode, String targetCode, String linkCode, String linkValue, Double weight,
			String token) {

		log.info("CREATING LINK between " + groupCode + "and" + targetCode + "with LINK VALUE = " + linkValue);

		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		Link link = new Link(groupCode, targetCode, linkCode, linkValue);
		link.setWeight(weight);

		try {
			postLink(qwandaServiceUrl, token, link);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return link;
	}

	public static BaseEntity getBaseEntityForAttribute(String groupCode, String linkCode, String attributeCode,
			String attributeValue, String tokenString) {
		QDataBaseEntityMessage entityMessage = getDataBEMessage(groupCode, linkCode, tokenString);

		BaseEntity[] beArr = entityMessage.getItems();
		BaseEntity resultBe = null;

		if (beArr != null) {

			for (BaseEntity be : beArr) {
				if (attributeValue.equals(MergeUtil.getBaseEntityAttrObjectValue(be, attributeCode))) {
					resultBe = be;
					return resultBe;
				}
			}

		}
		return resultBe;

	}

	public static <T extends CodedEntity> String apiPutCodedEntity(T codedEntity, final String authToken)
			throws IOException {

		String code = codedEntity.getCode();
		String realm = codedEntity.getRealm();
		String key = realm + ":" + code;
		String json = JsonUtils.toJson(codedEntity);
		String uuJson = URLEncoder.encode(json, "UTF-8");

		String retJson = "";
		int timeout = 10;
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		final HttpPost post = new HttpPost("http://" + hostIP + ":" + apiPort + "/" + key + "/" + uuJson);
		post.addHeader("Authorization", "Bearer " + authToken); // Authorization": `Bearer

		final StringEntity input = new StringEntity(json); // Do this to test out
		input.setContentType("application/json");
		post.setEntity(input);
		HttpResponse response = null;
		try {
			response = client.execute(post);
		} catch (Exception e) {
			throw new IOException("Socket error");
		}
		final BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			retJson += line;
			;
		}
		return retJson;
	}

	public static <T extends CodedEntity> T apiGetCodedEntity(String key, final Class clazz, String authToken)
			throws ClientProtocolException, IOException {
		String retJson = "";

		int timeout = 10;
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		HttpGet request = new HttpGet("http://" + hostIP + ":" + apiPort + "/" + key); // GET Request

		if (authToken != null) {
			request.addHeader("Authorization", "Bearer " + authToken); // Authorization": `Bearer
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
			// TODO Auto-generated catch block
			return null;
		}

		JsonObject json = JsonUtils.fromJson(retJson, JsonObject.class);
		if (json.get("status").getAsString().equals("ok")) {
			String value = json.get("value").getAsString();
			String decodedValue = URLDecoder.decode(value);
			T entity = (T) JsonUtils.fromJson(decodedValue, clazz);
			return entity;
		} else {
			// fetch from api

			return null;
		}
	}

	/*
	 * Checks if all the mandatory fields are completed
	 */
	public static Boolean isMandatoryFieldsCompleted(QDataAskMessage asks, List<BaseEntity> baseEntityList) {
		return isMandatoryFieldsCompleted(asks.getItems(), baseEntityList);
	}

	public static Boolean isMandatoryFieldsCompleted(Ask[] askArray, List<BaseEntity> baseEntityList) {

		for (Ask parentAsk : askArray) {
			if (parentAsk.getAttributeCode().startsWith("QQQ_")) {
				// This is the Question GRP
				Ask[] childAsks = parentAsk.getChildAsks();
				Boolean result = isMandatoryFieldsCompleted(childAsks, baseEntityList);

				if (result == false) {
					return false;
				}
			} else {
				Boolean mandatory = parentAsk.getMandatory();
				if (mandatory) {
					for (BaseEntity be : baseEntityList) {
						if (parentAsk.getTargetCode().equals(be.getCode())) {
							Optional<EntityAttribute> ea = be.findEntityAttribute(parentAsk.getAttributeCode());
							if (ea.isPresent()) {
								Object value = ea.get().getValue();
								if (value == null) {
									return false;
								}
							}
						}
					}
				}
			}

		}
		return true;
	}

	public static String getUserCode(String token) {

		org.json.JSONObject decodedToken = KeycloakUtils.getDecodedToken(token);
	//	System.out.println("decoded token object ::" + decodedToken);

		String username = decodedToken.getString("preferred_username");
		String uname = QwandaUtils.getNormalisedUsername(username);
		String code = "PER_" + uname.toUpperCase();

		return code;
	}

	/**
	 * 
	 * @param inputMoney
	 * @return stringified Money JSONObject
	 */
	public static String getMoneyString(Money inputMoney) {

		String moneyString = null;
		if (inputMoney != null) {
			moneyString = "{\"amount\":" + String.valueOf(inputMoney.getNumber().doubleValue()) + ",\"currency\":\""
					+ inputMoney.getCurrency().toString() + "\"}";
		}

		return moneyString;
	}

	/**
	 * 
	 * @param stringifiedMoneyJson
	 * @return Amount as a string
	 */
	public static String getAmountAsString(String stringifiedMoneyJson) {

		String amount = null;

		if (stringifiedMoneyJson != null) {
			// JSONObject priceObject = JsonUtils.fromJson(stringifiedMoneyJson,
			// JSONObject.class);
			JSONParser parser = new JSONParser();

			try {
				org.json.simple.JSONObject priceObject = (org.json.simple.JSONObject) parser
						.parse(stringifiedMoneyJson);
				amount = priceObject.get("amount").toString();
				System.out.println("amount ::" + amount);

			} catch (ParseException e) {
				e.printStackTrace();
			}

		}

		return amount;
	}

	/**
	 * 
	 * @param stringifiedMoneyJson
	 * @return Currency as a string
	 */
	public static String getCurrencyAsString(String stringifiedMoneyJson) {

		String currency = null;

		if (stringifiedMoneyJson != null) {

			org.json.simple.JSONObject priceObject = JsonUtils.jsonStringParser(stringifiedMoneyJson);
			currency = priceObject.get("currency").toString();

		}

		return currency;
	}
	
	public static String getZonedCurrentLocalDateTime() {

		LocalDateTime ldt = LocalDateTime.now();
		ZonedDateTime zdt = ldt.atZone(ZoneOffset.UTC);  //Using UTC Time
		String iso8601DateString = zdt.toString(); // zdt.toString(); MUST USE UMT!!!!

		System.out.println("datetime ::" + iso8601DateString);

		return iso8601DateString;

	}
	
	public static QDataBaseEntityMessage fetchResults(final BaseEntity searchBE, final String token) throws IOException
	{
		String jsonSearchBE = JsonUtils.toJson(searchBE);
		String result = QwandaUtils.apiPostEntity(qwandaServiceUrl + "/qwanda/baseentitys/search", jsonSearchBE,
				token);

		QDataBaseEntityMessage results = JsonUtils.fromJson(result, QDataBaseEntityMessage.class);
		return results;

	}
	
}
