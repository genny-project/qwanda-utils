package life.genny.qwandautils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;

public class MergeUtil {
	
	public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
	
	final static Gson gson = new GsonBuilder()
	        .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
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
	
	public static final String REGEX_START = "{{";
	public static final String REGEX_END = "}}";
	public static final String REGEX_START_PATTERN = Pattern.quote(REGEX_START);
    public static final String REGEX_END_PATTERN = Pattern.quote(REGEX_END);
    public static final Pattern PATTERN = Pattern.compile(REGEX_START_PATTERN + "(.*?)" + REGEX_END_PATTERN);
    public static final String DEFAULT = "";
	
	public static String merge(String mergeStr, Map<String, BaseEntity> templateEntityMap) {
				
		Matcher match = PATTERN.matcher(mergeStr);		
		
		while (match.find()) {

			Object mergedtext = wordMerge(templateEntityMap, match.group(1));
			mergeStr = mergeStr.replace(REGEX_START + match.group(1) + REGEX_END, mergedtext.toString());

		}
		
		return mergeStr;
		
	}
	
	private static String wordMerge(Map<String, BaseEntity> entitymap, String mergeText){
		
		if(mergeText != null && !mergeText.isEmpty()){
			String[] entityArr = mergeText.split("\\.");
			String baseent = entityArr[0];
			
			if(!(entityArr.length > 1))
				return DEFAULT;
			
			if (entitymap.containsKey(baseent)) {
				try {

					BaseEntity be = entitymap.get(baseent);

					//return be.findEntityAttribute(entityArr[1]).get().getValueString();
					return getBaseEntityAttrValue(be, entityArr[1]);
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		return DEFAULT;	
	
	}
	
	/**
	 * 
	 * @param attributeCode
	 * @param token
	 * @return BaseEntity with children (alias code, code of the attributes for the BaseEntity) for the BaseEntity code
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, BaseEntity> getBaseEntWithChildrenForAttributeCode(String attributeCode, String token) {

		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		Map<String, BaseEntity> entityTemplateContextMap = new HashMap<String, BaseEntity>();


		try {
			String attributeString = QwandaUtils
					.apiGet(qwandaServiceUrl + "/qwanda/entityentitys/" + attributeCode + "/linkcodes/LNK_BEG/children", token);

			JSONParser parser = new JSONParser();
			JSONArray jsonarr;	

			if(attributeString != null && !attributeString.isEmpty()) {
				System.out.println(ANSI_BLUE+"Got BEG string" + ANSI_RESET);

				jsonarr = (JSONArray) parser.parse(attributeString);
				jsonarr.forEach(item -> {
					JSONObject obj = (JSONObject) item;
					String baseEntAttributeCode = (String) obj.get("targetCode");
					if(obj.get("linkValue") != null){
						entityTemplateContextMap.put(obj.get("linkValue").toString(),getBaseEntityForAttr(baseEntAttributeCode, token));
					}
					//BaseEntity be = getBaseEntityForAttr("PER_USER2", token); //this is for testing 
				});
						
			}		

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		System.out.println("base entity context map ::"+entityTemplateContextMap);
		return entityTemplateContextMap;

	}

	
	/**
	 * 
	 * @param baseEntAttributeCode
	 * @param token
	 * @return Deserialized BaseEntity model object with values for a BaseEntity code that is passed
	 */
	public static BaseEntity getBaseEntityForAttr(String baseEntAttributeCode, String token) {
		
		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		String attributeString;
		BaseEntity be = null;
		try {
			attributeString = QwandaUtils
					.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" +baseEntAttributeCode, token);
			System.out.println(ANSI_BLUE + "Base entity attribute code::"+baseEntAttributeCode + ANSI_RESET);

						
			be = gson.fromJson(attributeString, BaseEntity.class);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return be;
	}
	
	/**
	 * 
	 * @param BaseEntity object
	 * @param attributeCode
	 * @return The attribute value for the BaseEntity attribute code passed
	 */
	public static String getBaseEntityAttrValue(BaseEntity be, String attributeCode) {
		//return be.findEntityAttribute(attributeCode).get().getValueString();	
		
		String attributeVal = "";
		for(EntityAttribute ea : be.getBaseEntityAttributes()) {
			if(ea.getAttributeCode().equals(attributeCode)) {
				attributeVal = ea.getValueString();
			}
		}
		return attributeVal;
	}
	
	
	
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
	 * @param baseEntityCode
	 * @param attributeCode
	 * @param token
	 * @return atttribute value
	 */
	public String getAttrValue(String baseEntityCode, String attributeCode, String token) {
		
		String attrValue = null;
		
		if(baseEntityCode != null && token != null) {
			
			BaseEntity be = getBaseEntityForAttr(baseEntityCode, token);
			attrValue = getBaseEntityAttrValue(be, attributeCode);			
		}
		
		return attrValue;
	}

}
