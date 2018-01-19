package life.genny.qwandautils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.DateTimeDeserializer;
import life.genny.qwanda.Link;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.entity.Person;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.message.QDataAskMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;



public class QwandaUtils {
	
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
	
	final static Gson gson = new GsonBuilder()
	        .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
	          @Override
	          public LocalDate deserialize(final JsonElement json, final Type type,
	              final JsonDeserializationContext jsonDeserializationContext)
	              throws JsonParseException {
	            return LocalDate.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
	          }

	          public JsonElement serialize(final LocalDate date, final Type typeOfSrc,
	              final JsonSerializationContext context) {
	            return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE)); 
	          }
	        }).registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
		          @Override
		          public LocalDateTime deserialize(final JsonElement json, final Type type,
		              final JsonDeserializationContext jsonDeserializationContext)
		              throws JsonParseException {
		            return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		          }

		          public JsonElement serialize(final LocalDateTime date, final Type typeOfSrc,
		              final JsonSerializationContext context) {
		            return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); 
		          }
		        }).create();

	
	public static String apiGet(final String getUrl, final String authToken)
			throws ClientProtocolException, IOException {
		String retJson = "";
		log.debug("GET:" + getUrl + ":");
		int timeout = 10;
		RequestConfig config = RequestConfig.custom()
		  .setConnectTimeout(timeout * 1000)
		  .setConnectionRequestTimeout(timeout * 1000)
		  .setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient client = 
		  HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		HttpGet request = new HttpGet(getUrl); // GET Request
		
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

		return retJson;
	}

	public static String apiPostEntity(final String postUrl, final String entityString, final String authToken)
			throws IOException {
		String retJson = "";
		int timeout = 10;
		RequestConfig config = RequestConfig.custom()
				  .setConnectTimeout(timeout * 1000)
				  .setConnectionRequestTimeout(timeout * 1000)
				  .setSocketTimeout(timeout * 1000).build();
				CloseableHttpClient client = 
				  HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		final HttpPost post = new HttpPost(postUrl);
		post.addHeader("Authorization", "Bearer " + authToken); // Authorization": `Bearer

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


	public static String apiPost(final String postUrl, final ArrayList<BasicNameValuePair> nameValuePairs,
			final String authToken) throws IOException {
		String retJson = "";
		int timeout = 10;
		RequestConfig config = RequestConfig.custom()
				  .setConnectTimeout(timeout * 1000)
				  .setConnectionRequestTimeout(timeout * 1000)
				  .setSocketTimeout(timeout * 1000).build();
				CloseableHttpClient client = 
				  HttpClientBuilder.create().setDefaultRequestConfig(config).build();

				final HttpPost post = new HttpPost(postUrl);
		post.addHeader("Authorization", "Bearer " + authToken); // Authorization": `Bearer

		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		final HttpResponse response = client.execute(post);
		final BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			retJson += line;
			;
		}
		return retJson;
	}
	
	public static String apiDelete(final String deleteUrl,  final String entityString, final String authToken) throws IOException {
		String retJson = "";
		int timeout = 10;
		RequestConfig config = RequestConfig.custom()
				  .setConnectTimeout(timeout * 1000)
				  .setConnectionRequestTimeout(timeout * 1000)
				  .setSocketTimeout(timeout * 1000).build();
				CloseableHttpClient client = 
				  HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		HttpDeleteWithBody delete = new HttpDeleteWithBody(deleteUrl);
		delete.addHeader("Authorization", "Bearer " + authToken); // Authorization": `Bearer

		final StringEntity input = new StringEntity(entityString);
		input.setContentType("application/json");
		delete.setEntity(input);
		final HttpResponse response = client.execute(delete);
		final BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			retJson += line;
			;
		}
		return retJson;
	}
	
	public static String apiPutEntity(final String postUrl, final String entityString, final String authToken)
			throws IOException {
		String retJson = "";
		//final HttpClient client = new DefaultHttpClient();
		final HttpClient client = HttpClientBuilder.create().build();

		final HttpPut put = new HttpPut(postUrl);
		put.addHeader("Authorization", "Bearer " + authToken); 

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

	public static BaseEntity createUserFromToken(final String qwandaUrl, final String serviceToken, final String userToken) throws IOException {
		JSONObject decodedToken = KeycloakUtils.getDecodedToken(userToken);
		
		String username = decodedToken.getString("preferred_username");
		String firstname = decodedToken.getString("given_name");
		String lastname =decodedToken.getString("family_name");
		String email = decodedToken.getString("email");
		
		BaseEntity be = createUser(qwandaUrl, serviceToken, username, firstname, lastname, email);
		
		return be;
	}
	
	public static BaseEntity postBaseEntity(final String qwandaUrl, final String token, final BaseEntity be
			) throws IOException
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = null;

		gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer());
		gson = gsonBuilder.create();
		String jsonBE = gson.toJson(be);
	
		
		QwandaUtils.apiPostEntity(qwandaUrl + "/qwanda/baseentitys", jsonBE,token);
		
		return be; 
	}
	public static Answer postAnswer(final String qwandaUrl, final String token, final Answer answer
			) throws IOException
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = null;

		gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer());
		gson = gsonBuilder.create();

		String id = QwandaUtils.apiPostEntity(qwandaUrl + "/qwanda/answers", gson.toJson(answer),token);
		// TODO check id is returned
		return answer;
	}

	public static Link postLink(final String qwandaUrl, final String token, final Link link
			) throws IOException
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = null;

		gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer());
		gson = gsonBuilder.create();

		QwandaUtils.apiPostEntity(qwandaUrl + "/qwanda/entityentitys", gson.toJson(link),token);

		return link;
	}	
	
	public static String getNormalisedUsername(final String rawUsername)
	{
		return 	rawUsername.replaceAll("\\&", "_AND_").replaceAll("@", "_AT_").replaceAll("\\.", "").toLowerCase();
	}
	
	public static BaseEntity createUser(final String qwandaUrl, final String token, final String username, 
			final String firstname,
			final String lastname,
			final String email
			) throws IOException
	{

		String uname = getNormalisedUsername(username);
		String code = "PER_" + uname.toUpperCase();
		
		Person person = new Person(code, firstname + " " + lastname);
		
		postBaseEntity(qwandaUrl, token, person);
		postAnswer(qwandaUrl, token, new Answer(code,code,"PRI_USERNAME",username));
		postAnswer(qwandaUrl, token, new Answer(code,code,"PRI_FIRSTNAME",firstname));
		postAnswer(qwandaUrl, token, new Answer(code,code,"PRI_LASTNAME",lastname));
		postAnswer(qwandaUrl, token, new Answer(code,code,"PRI_EMAIL",email));
					
		postLink(qwandaUrl, token, new Link("GRP_USERS",code,"LNK_CORE"));
		postLink(qwandaUrl, token, new Link("GRP_PEOPLE",code,"LNK_CORE"));
		
		return person;
	}
	
	public static Boolean checkUserTokenExists(final String qwandaUrl, final String userToken) throws IOException {
		JSONObject decodedToken = KeycloakUtils.getDecodedToken(userToken);
		String tokenSub = decodedToken.getString("sub");
		System.out.println("sub token::"+tokenSub);
		
		
		String username = decodedToken.getString("preferred_username");
		
		String uname = getNormalisedUsername(username);
		String code = "PER_" + uname.toUpperCase();
		
		System.out.println("username ::"+username);
		System.out.println("uname::"+uname);
		System.out.println("code::"+code);
		
		System.out.println("code::"+code);
		Boolean tokenExists = false;
		
		String attributeString = QwandaUtils
				.apiGet(qwandaUrl + "/qwanda/baseentitys/" +code, userToken);
		
		if((attributeString == null) || attributeString.contains("Error") || attributeString.contains("Unauthorized")) {
			System.out.println("baseentity not found");
			tokenExists = false;
		} else {
			tokenExists = true;
		}
		
		/*else {
			String attributeVal = MergeUtil.getAttrValue(code, "PRI_KEYCLOAK_UUID", userToken);
						
			System.out.println("pri_keycloak_UUID for the code::"+attributeVal);
			if(attributeVal == null){
				tokenExists = false;
				System.out.println("baseentity found and UUID is null");
			}else if(tokenSub.equals(attributeVal)) {
				System.out.println("baseentity found and UUID matched");
				tokenExists = true;
			} else if(!tokenSub.equals(attributeVal)) {
				System.out.println("baseentity code found but keycloak UUID not matched");
				tokenExists = false;
			}
		}*/
		
		return tokenExists;
	}
	
	
	 /**
     * 
     * @param sourceBaseEntityCode
     * @param targetBaseEntityCode
     * @param questionCode
     * @param token
     * @return if mandatory fields of the form has been completed
     * <p> For sourceBaseEntityCode : PER_USERXX, targetBaseEntityCode : BEG_XXX/LOD_XXXX, questionCode : Question grpCode, method checks if all the 
     * mandatory fields in the Question Group has been entered</p>
     */	
	public static Boolean isMandatoryFieldsEntered(String sourceBaseEntityCode, String targetBaseEntityCode, String questionCode, final String userToken) {

		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		//String qwandaServiceUrl = "http://localhost:8280";
		try {

			String attributeString = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" + sourceBaseEntityCode
					+ "/asks3/" + questionCode + "/" + targetBaseEntityCode, userToken);

			System.out.println("Attribute String="+attributeString);
			QDataAskMessage askMsgs = gson.fromJson(attributeString, QDataAskMessage.class);
			BaseEntity be = MergeUtil.getBaseEntityForAttr(targetBaseEntityCode, userToken);

			for (Ask parentAsk : askMsgs.getItems()) {
				for (Ask childAsk : parentAsk.getChildAsks()) {
					System.out.println("parent ask code ::"+parentAsk.getAttributeCode());
					for (Ask basicChildAsk : childAsk.getChildAsks()) {

						if (basicChildAsk.getMandatory()) {
							/*Optional<EntityAttribute> attributeVal = be.findEntityAttribute(basicChildAsk.getAttributeCode());
							if (attributeVal.isPresent()) {
								if (attributeVal.get() == null) {
								System.out.println("This attribute value of "+basicChildAsk.getAttributeCode() +" is not filled and is null");
								return false;
								}
							} else {
								return false;
							}*/
							System.out.println("child ask attribute code ::"+basicChildAsk.getAttributeCode());
							Object attributeVal = MergeUtil.getBaseEntityAttrObjectValue(be,basicChildAsk.getAttributeCode());
							if(attributeVal!= null){
								System.out.println("attribu6te value ::"+basicChildAsk.getAttributeCode()+"----"+attributeVal);
							}
							
							if(attributeVal == null) {
								System.out.println(basicChildAsk.getAttributeCode() + " is null");
								return false;
							}
								
						}

					}
				}
			}

			System.out.println("askMsgs ::" + askMsgs);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	
	/**
	 * 
	 * @param username
	 * @return baseEntity code for the userName passed
	 */
	public static String getBaseEntityCodeForUserName(String username, String userToken) {
		
		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		//String qwandaServiceUrl = "http://localhost:8280";
		
		String baseEntityCode = null;
		try {
			String attributeString = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/GRP_PEOPLE/linkcodes/LNK_CORE/attributes?PRI_USERNAME=" + username, userToken);
			System.out.println("attribute string::" + attributeString);
			
			QDataBaseEntityMessage msg = gson.fromJson(attributeString, QDataBaseEntityMessage.class);
			
			for(BaseEntity be : msg.getItems()) {
				baseEntityCode = be.getCode();
				System.out.println("baseEntity code for username ::"+baseEntityCode);
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
			attributeString = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/templates/" + templateCode,
					token);
			template = gson.fromJson(attributeString, QBaseMSGMessageTemplate.class);
			System.out.println("template sms:"+template.getSms_template());

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
		
		//String qwandaServiceUrl = "http://localhost:8280";
		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		List linkList = null;
		
		try {
			String attributeString = QwandaUtils
					.apiGet(qwandaServiceUrl + "/qwanda/entityentitys/" + groupCode + "/linkcodes/LNK_BEG/children", token);
			if(attributeString != null) {
				linkList = gson.fromJson(attributeString, List.class);
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
	 * @return BaseEntity with children (alias code, code of the attributes for the BaseEntity) for the BaseEntity code
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, BaseEntity> getBaseEntWithChildrenForAttributeCode(String attributeCode, String token) {

		
		Map<String, BaseEntity> entityTemplateContextMap = new HashMap<String, BaseEntity>();

		List linkList = getLinkList(attributeCode , token);

		if(linkList != null) {
			System.out.println(ANSI_BLUE+"Got BEG string" + ANSI_RESET + linkList.toString());			
			
			linkList.forEach(item -> {
				Link link = gson.fromJson(item.toString(), Link.class);
				String baseEntAttributeCode = link.getTargetCode();
				System.out.println("base attribute target code ::"+baseEntAttributeCode);
				if(link.getLinkValue() != null){
						
					/**
					 * Creating a template : LinkValue -> BaseEntityForCorrespondingLinkCode
					 * <Example> DRIVER - PER_USER2, OWNER - PER_USER1 </example>
					 */
					entityTemplateContextMap.put(link.getLinkValue(), MergeUtil.getBaseEntityForAttr(baseEntAttributeCode, token));
						
				}
			});
					
		}
		
		System.out.println("base entity context map ::"+entityTemplateContextMap);
		return entityTemplateContextMap;

	}
	
	
	public static String getUniqueId(String baseEntityCode, String questionId, String prefix, String token) {
		
		String uniqueID = UUID.randomUUID().toString().replaceAll("-", "");
		
		BaseEntity be = MergeUtil.getBaseEntityForAttr(baseEntityCode, token);
		String nameInitials = getInitials(be.getName().split("\\s+"));
		
		//working on it
		//String companyCode = getCompanyCode(baseEntityCode, token);
		
		return prefix + "_" + nameInitials  + uniqueID;
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
			String attributeString = QwandaUtils
					.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" + groupCode + "/linkcodes/" +linkCode+ "/attributes", token);
			dataBEMessage = gson.fromJson(attributeString, QDataBaseEntityMessage.class);
			
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
		
		if(targetAttributeCode.equalsIgnoreCase("PRI_FULL_DROPOFF_ADDRESS")){
			targetCode = "DROPOFF";
		}else if(targetAttributeCode.equalsIgnoreCase("PRI_FULL_PICKUP_ADDRESS")) {
			targetCode = "PICKUP";
		}
		
		for(EntityAttribute ea : be.getBaseEntityAttributes()) {
			if(ea.getAttributeCode().equals("PRI_"+targetCode+"_STREET_ADDRESS1")){
				street = ea.getObjectAsString();
				System.out.println("street ::"+street);
			}
			
			if(ea.getAttributeCode().equals("PRI_"+targetCode+"_CITY")){
				city = ea.getObjectAsString();
				System.out.println("city ::"+city);
			}
			
			if(ea.getAttributeCode().equals("PRI_"+targetCode+"_STATE")){
				state = ea.getObjectAsString();
				System.out.println("state ::"+state);
			}
			
			if(ea.getAttributeCode().equals("PRI_"+targetCode+"_POSTCODE")){
				postcode = ea.getObjectAsString();
				System.out.println("postcode ::"+postcode);
			}
			
			if(ea.getAttributeCode().equals("PRI_"+targetCode+"_COUNTRY")){
				country = ea.getObjectAsString();
				System.out.println("country ::"+country);
			}
		}
		
		if(targetCode != null){
			address = street + ", " + city + ", " + state + " " + postcode + " " + country;
		}
		
		return address;
		
	}
	
	//creating new BaseEntity by only baseentityCode
    public static BaseEntity createBaseEntityByCode(String entityCode, String name, String qwandaUrl, String token) {
		BaseEntity beg = new BaseEntity(entityCode, name);		
		Gson gson = new Gson();
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer());
		gson = gsonBuilder.create();
        
        String jsonBE = gson.toJson(beg);
        try {
        		// save BE
            QwandaUtils.apiPostEntity(qwandaUrl + "/qwanda/baseentitys", jsonBE, token);                       
        }catch (Exception e) {
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
     * <p> For parentCode : PER_USER, parentLinkCode : LNK_CODE, childLinkCode : OFFER_CODE, this API checks if there is a link LNK_CODE between PER_USER and OFFER_CODE.</p>
     */
    public static Boolean checkIfLinkExistsForTarget(String parentCode, String linkCode, String childCode, String token) {
    	
    	Boolean isLinkExists = false;
		QDataBaseEntityMessage dataBEMessage = getDataBEMessage(parentCode, linkCode, token);
		
		if(dataBEMessage != null) {
			BaseEntity[] beArr = dataBEMessage.getItems();
			
			if(beArr.length > 0) {
				for(BaseEntity be : beArr) {
					if(be.getCode().equals(childCode)) {
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
     * @param sourceOrTarget (The input code could be either the sourceCode or the targetCode of a groupLink)
     * @param linkValue
     * @param isSource (Determines if the input rule requires a sourceCode/targetCode)
     * @param token
     * @return sourceCode/targetCode if the link values match the input given
     */
	public static String getSourceOrTargetForGroupLink(String groupCode, String attributeCode, String sourceOrTarget,
			String linkValue, Boolean isSource, String token) {

		QDataBaseEntityMessage dataBEMessage = getDataBEMessage(groupCode, "LNK_CORE", token);
		String code = null;

		if (dataBEMessage != null) {

			for (BaseEntity be : dataBEMessage.getItems()) {

				for (EntityEntity entityEntity : be.getLinks()) {

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

				}
			}

		}

		return code;
	}
    
    
    /**
     * 
     * @param groupCode
     * @param linkCode (which is the attributeCode in the Link model)
     * @param targetCode
     * @param linkValue
     * @param token
     * @return if the link exists between parent groupCode and a child for a given linkCode, linkValue
     */
    @SuppressWarnings("unchecked")
	public static Boolean checkIfLinkExists(String groupCode, String linkCode, String targetCode, String linkValue, String token) {
    	
    	//gets all the children of a groupcode with their linkCode,linkValue and code as a list
    	List linkList = getLinkList(groupCode, token);
    	Boolean isLinkExists = false;
    	
    	//checks if a group has the child with the specified linkCode and linkValue
    	if(linkList != null) {
    		isLinkExists = linkList.stream().anyMatch(item -> {
        		Boolean isExists = false;
        		Link link = gson.fromJson(item.toString(), Link.class);
        		if(link.getAttributeCode().equals(linkCode) && link.getTargetCode().equals(targetCode) && link.getLinkValue().equals(linkValue))
        			isExists = true;
    			return isExists;
        	});
    	}   	
    	
    	return isLinkExists;
    }
	
    public static String getSystemEnvJson(final String prefix)
    {
    		String ret = "{";
    		Map<String, String> env = System.getenv();
    		for (String envName : env.keySet()) {
    			if (envName.startsWith(prefix)) {
    				ret += "\""+envName.substring(prefix.length()) + "\":\"" +  env.get(envName)+"\",";
    			}
    		}
    		
    		ret += "\"prefix\":"+prefix+"\"}";
    		
    		return ret;
    }
    
    public static String executeRuleLogger(String module) {
    	String initialLogger = ANSI_YELLOW + "================================================================================================================================================" + ANSI_RESET;
        String moduleLogger = "\n" + ANSI_GREEN + "RULE EXECUTED      ::  " +module + ANSI_RESET;
        return initialLogger + moduleLogger;
    }
    
    public static String terminateRuleLogger(String module) {
    	String initialLogger = "\n \u001B[31m RULE TERMINATED    ::   " + module + " \u001B[0m ";
        String moduleLogger = "\n \u001B[33m ------------------------------------------------------------------------------------------------------------------------------------------------  \u001B[0m";
        return initialLogger + moduleLogger;
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
	public static Link createLink(String groupCode, String targetCode, String linkCode, String linkValue,
			Double weight, String token) {
		
		log.info("CREATING LINK between "+ groupCode + "and" + targetCode + "with LINK VALUE = " + linkValue);

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
}
