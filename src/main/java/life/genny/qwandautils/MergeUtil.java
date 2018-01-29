package life.genny.qwandautils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import life.genny.qwanda.DateTimeDeserializer;
import life.genny.qwanda.Link;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;

public class MergeUtil {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
	
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
    
    
	public static final String REGEX_START = "[";
	public static final String REGEX_END = "]";
	public static final String REGEX_START_PATTERN = Pattern.quote(REGEX_START);
    public static final String REGEX_END_PATTERN = Pattern.quote(REGEX_END);
    public static final Pattern PATTERN = Pattern.compile(REGEX_START_PATTERN + "(?s)(.*?)" + REGEX_END_PATTERN);
    public static final String DEFAULT = "";
    
    public static final String NEWPATTERN1 = REGEX_START_PATTERN + "(?s)(.*?)" + REGEX_END_PATTERN;
    public static final Pattern PATTEN_MATCHER = Pattern.compile(NEWPATTERN1);
   
    
	public static String merge(String mergeStr, Map<String, BaseEntity> templateEntityMap) {

		Matcher match = PATTEN_MATCHER.matcher(mergeStr);
		
		while (match.find()) {
			Object mergedtext = wordMerge(templateEntityMap, match.group(1));
			log.info("merge text ::"+mergedtext);
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
					return getBaseEntityAttrValueAsString(be, entityArr[1]);
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		return DEFAULT;	
	
	}

	
	/**
	 * 
	 * @param baseEntAttributeCode
	 * @param token
	 * @return Deserialized BaseEntity model object with values for a BaseEntity code that is passed
	 */
	public static BaseEntity getBaseEntityForAttr(String baseEntAttributeCode, String token) {
		
		//String qwandaServiceUrl = "http://localhost:8280";
		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		String attributeString;
		BaseEntity be = null;
		try {
			attributeString = QwandaUtils
					.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" +baseEntAttributeCode, token);
		//	System.out.println(ANSI_BLUE + "Base entity attribute code::"+baseEntAttributeCode + ANSI_RESET);					
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
	public static String getBaseEntityAttrValueAsString(BaseEntity be, String attributeCode) {
		
		String attributeVal = null;
		for(EntityAttribute ea : be.getBaseEntityAttributes()) {
			if(ea.getAttributeCode().equals(attributeCode)) {
				attributeVal = ea.getObjectAsString();
			}
		}
		
		return attributeVal;
		
		/*String value =  null;
		
		try {
			value = be.findEntityAttribute(attributeCode).get().getObjectAsString();
		} catch (Exception e) {
			log.error("Attribute not found");
			return null;
		}
		
		return value;*/
	}
	
	
	/**
	 * 
	 * @param baseEntityCode
	 * @param attributeCode
	 * @param token
	 * @return attribute value
	 */
	public static String getAttrValue(String baseEntityCode, String attributeCode, String token) {
		
		String attrValue = null;
		
		if(baseEntityCode != null && token != null) {
			
			BaseEntity be = getBaseEntityForAttr(baseEntityCode, token);
			attrValue = getBaseEntityAttrValueAsString(be, attributeCode);			
		}
		
		return attrValue;
	}
	
	public static String getFullName(String baseEntityCode, String token) {
		
		String fullName = null;

		if(baseEntityCode != null && token != null) {

			String firstName = MergeUtil.getAttrValue(baseEntityCode, "PRI_FIRSTNAME", token);
			String lastName = MergeUtil.getAttrValue(baseEntityCode, "PRI_LASTNAME", token);
			fullName = firstName + " " + lastName;
			System.out.println("PRI_FULLNAME   ::   "+ fullName);		
		}
		
		return fullName;
	}

	public static boolean createBaseEntity(String sourceCode, String linkCode, String targetCode, String name, Long id, String token) {
		
		BaseEntity be = new BaseEntity(targetCode, name);
		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		
		Gson gson1 = new Gson();
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer());
		gson1 = gsonBuilder.create();
        
        String jsonBE = gson1.toJson(be);
        try {
            String output= QwandaUtils.apiPostEntity(qwandaServiceUrl + "/qwanda/baseentitys", jsonBE, token);
            
            QwandaUtils.apiPostEntity(qwandaServiceUrl + "/qwanda/entityentitys", gson.toJson(new Link(sourceCode, targetCode, linkCode)),token);
            System.out.println("this is the output :: "+ output);
            
        }catch (Exception e) {
            e.printStackTrace();
        }
		
		return true;
		
	}
	
	public static Object getBaseEntityAttrObjectValue(BaseEntity be, String attributeCode) {

		Object attributeVal = null;
		for (EntityAttribute ea : be.getBaseEntityAttributes()) {
			if (ea.getAttributeCode().equals(attributeCode)) {
				attributeVal = ea.getObject();
			}
		}

		return attributeVal;

	}

}
