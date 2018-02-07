package life.genny.qwandautils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.javamoney.moneta.Money;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import life.genny.qwanda.DateTimeDeserializer;
import life.genny.qwanda.Link;
import life.genny.qwanda.MoneyDeserializer;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;

public class MergeUtil {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
	
   
    
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

					// return
					// be.findEntityAttribute(entityArr[1]).get().getValueString();
					String attributeCode = entityArr[1];
					
					if (!(attributeCode.equals("PRI_PRICE")
							|| attributeCode.equals("PRI_LOAD_PRICE") || attributeCode.equals("PRI_OFFER_PRICE")
							|| attributeCode.equals("PRI_FEE") || attributeCode.equals("PRI_OWNER_PRICE")
							|| attributeCode.equals("PRI_DRIVER_PRICE") || attributeCode.equals("PRI_OFFER_FEE")
							|| attributeCode.equals("PRI_OFFER_OWNER_PRICE")
							|| attributeCode.equals("PRI_OFFER_DRIVER_PRICE") || attributeCode.equals("PRI_FEE_INC_GST")
							|| attributeCode.equals("PRI_FEE_EXC_GST") || attributeCode.equals("PRI_OFFER_FEE_INC_GST")
							|| attributeCode.equals("PRI_OFFER_FEE_EXC_GST")
							|| attributeCode.equals("PRI_OWNER_PRICE_INC_GST")
							|| attributeCode.equals("PRI_OWNER_PRICE_EXC_GST")
							|| attributeCode.equals("PRI_OFFER_OWNER_PRICE_INC_GST")
							|| attributeCode.equals("PRI_OFFER_OWNER_PRICE_EXC_GST")
							|| attributeCode.equals("PRI_DRIVER_PRICE_INC_GST")
							|| attributeCode.equals("PRI_DRIVER_PRICE_EXC_GST")
							|| attributeCode.equals("PRI_OFFER_DRIVER_PRICE_INC_GST")
							|| attributeCode.equals("PRI_OFFER_DRIVER_PRICE_EXC_GST"))) {
						
						return getBaseEntityAttrValueAsString(be, attributeCode);
						
					} else {
						System.out.println("price attributes");
						String priceString = getBaseEntityAttrValueAsString(be, attributeCode);
						
						String amount = QwandaUtils.getAmountAsString(priceString);
						String currency = QwandaUtils.getCurrencyAsString(priceString);

						return amount + " " + currency;
					}

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
		String attributeString = null;
		BaseEntity be = null;
		try {
			attributeString = QwandaUtils
					.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" +baseEntAttributeCode, token);
		//	System.out.println(ANSI_BLUE + "Base entity attribute code::"+baseEntAttributeCode + ANSI_RESET);					
			be = JsonUtils.fromJson(attributeString, BaseEntity.class);
			//be = JsonUtils.gson.fromJson(attributeString, BaseEntity.class);
			
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
		
	      
        String jsonBE = JsonUtils.toJson(be);
        try {
            String output= QwandaUtils.apiPostEntity(qwandaServiceUrl + "/qwanda/baseentitys", jsonBE, token);
            
            QwandaUtils.apiPostEntity(qwandaServiceUrl + "/qwanda/entityentitys", JsonUtils.toJson(new Link(sourceCode, targetCode, linkCode)),token);
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
