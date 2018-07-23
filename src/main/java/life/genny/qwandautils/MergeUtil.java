package life.genny.qwandautils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.javamoney.moneta.Money;

import com.amazonaws.services.simpleworkflow.flow.worker.SynchronousActivityTaskPoller;

import life.genny.qwanda.Link;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;

public class MergeUtil {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
	
    /* [VARIABLENAME.ATTRIBUTE] pattern */
    /* Used for baseentity-attribute merging */
	public static final String REGEX_START = "[";
	public static final String REGEX_END = "]";
	public static final String REGEX_START_PATTERN = Pattern.quote(REGEX_START);
    public static final String REGEX_END_PATTERN = Pattern.quote(REGEX_END);
    public static final Pattern PATTERN = Pattern.compile(REGEX_START_PATTERN + "(?s)(.*?)" + REGEX_END_PATTERN);
    public static final String DEFAULT = "";
    public static final String PATTERN_BASEENTITY = REGEX_START_PATTERN + "(?s)(.*?)" + REGEX_END_PATTERN;
    public static final Pattern PATTEN_MATCHER = Pattern.compile(PATTERN_BASEENTITY);
    
    /* {{VARIABLE}} pattern */
    /* this is for direct merging */
    public static final String VARIABLE_REGEX_START = "{{";
    public static final String VARIABLE_REGEX_END = "}}";
    public static final Pattern PATTERN_VARIABLE = Pattern.compile(Pattern.quote(VARIABLE_REGEX_START) + "(.*)" + Pattern.quote(VARIABLE_REGEX_END));  
    
    /* ((DATEFORMAT)) pattern */
    /* this is for date-format pattern merging */
    public static final String DATEFORMAT_VARIABLE_REGEX_START = "((";
    public static final String DATEFORMATVARIABLE_REGEX_END = "))";
    public static final Pattern DATEFORMAT_PATTERN_VARIABLE = Pattern.compile(Pattern.quote(DATEFORMAT_VARIABLE_REGEX_START) + "(.*)" + Pattern.quote(DATEFORMATVARIABLE_REGEX_END));  
    
    
	public static String merge(String mergeStr, Map<String, Object> templateEntityMap) { 
		
		/* matching [OBJECT.ATTRIBUTE] patterns */
		Matcher match = PATTEN_MATCHER.matcher(mergeStr);
		Matcher matchVariables = PATTERN_VARIABLE.matcher(mergeStr);
		
		if(templateEntityMap != null && templateEntityMap.size() > 0) {
			
			while (match.find()) {	
				
				Object mergedtext = wordMerge(templateEntityMap, match.group(1));
				log.info("merge text ::"+mergedtext);
				System.out.println("merge text ::"+mergedtext);
				if(mergedtext != null) {
					mergeStr = mergeStr.replace(REGEX_START + match.group(1) + REGEX_END, mergedtext.toString());
				} else {
					mergeStr = mergeStr.replace(REGEX_START + match.group(1) + REGEX_END, "");
				}			
			}
			
			/* duplicating this for now. ideally wordMerge should be a bit more flexible and allows all kind of data to be passed */
			while(matchVariables.find()) {
				
				//Object mergedtext = wordMerge(templateEntityMap, matchVariables.group(1));
				System.out.println("match variable ::"+matchVariables.group(1));
				Object mergedText = templateEntityMap.get(matchVariables.group(1));
				log.info("merge text ::"+mergedText);
				System.out.println("merge text ::"+mergedText);
				if(mergedText != null) {
					mergeStr = mergeStr.replace(VARIABLE_REGEX_START + matchVariables.group(1) + VARIABLE_REGEX_END, mergedText.toString());
				} else {
					mergeStr = mergeStr.replace(VARIABLE_REGEX_START + matchVariables.group(1) + VARIABLE_REGEX_END, "");
				}	
			}
			
		}
	
		return mergeStr;
	}
	
	@SuppressWarnings("unused")
	private static String wordMerge(Map<String, Object> entitymap, String mergeText) {
		
		if(mergeText != null && !mergeText.isEmpty()) {
			
			try {
				
				/* we split the text to merge into 2 components: BE.PRI... becomes [BE, PRI...] */
				String[] entityArr = mergeText.split("\\.");
				String keyCode = entityArr[0];
				
				if((entityArr.length == 0))
					return DEFAULT;
				
				if(entitymap.containsKey(keyCode)) {
					
					Object value = entitymap.get(keyCode);
					
					if(value.getClass().equals(BaseEntity.class)) {
						
						BaseEntity be = (BaseEntity)value;

						String attributeCode = entityArr[1];
						
						Object attributeValue = be.getValue(attributeCode, null);
						
						if(attributeValue != null && attributeValue instanceof org.javamoney.moneta.Money) {
							System.out.println("price attributes 1");
							DecimalFormat df = new DecimalFormat("#.00"); 
							Money money = (Money) attributeValue; 
							
							if(attributeValue != null) {
								return df.format(money.getNumber()) + " " + money.getCurrency();
							} else {
								return DEFAULT;
							}
						}else if(attributeValue != null && attributeValue instanceof java.time.LocalDateTime) {
							/* we split the date-time related merge text to merge into 3 components: BE.PRI.TimeDateformat... becomes [BE, PRI...] */
							if(entityArr != null && entityArr.length > 2) {
								Matcher matchVariables = DATEFORMAT_PATTERN_VARIABLE.matcher(entityArr[2]);
								if(matchVariables.find()) {
									return mergeDateTimeAttributeValue(attributeCode, attributeValue, matchVariables.group(1));
								}					
							}
						} else if(attributeValue instanceof java.lang.String){
							return getBaseEntityAttrValueAsString(be, attributeCode);
						}
						
						/*if(attributeCode.equals("PRI_DRIVER_CONFIRM_PICKUP_DATETIME")) {
													
							LocalDateTime dateTimeRawValue = be.getValue(attributeCode, null);
							
							To print in this format : 9am AEST, Monday, 22 January 2018
							int ampmIntVal = dateTimeRawValue.get(ChronoField.AMPM_OF_DAY);
							String ampm = null;
							if(ampmIntVal == 0) {
								ampm = "AM";
							} else {
								ampm = "PM";
							}
							
						   DateTimeFormatter df =  
						            new DateTimeFormatterBuilder().appendPattern("[HH][.mm]")
						            .parseDefaulting(ChronoField.HOUR_OF_AMPM, 0)
						            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
						            .toFormatter(); 
						   String time = df.format(dateTimeRawValue);

							
							String editedDateTime = time + " " + ampm + ", " + dateTimeRawValue.getDayOfWeek().toString().toLowerCase() + ", " + dateTimeRawValue.getDayOfMonth() + " " + dateTimeRawValue + " " + dateTimeRawValue.getYear();
							return editedDateTime.toLowerCase();
							
						} else if(attributeCode.equals("PRI_DROPOFF_DATETIME")) {
							LocalDateTime dateTimeRawValue = be.getValue(attributeCode, null);
							String formattedDate = "";
							
							if(dateTimeRawValue!= null) {
								DateTimeFormatter df =  
							            new DateTimeFormatterBuilder().appendPattern("dd/MM/yy HH:mm:ss").toFormatter();
								formattedDate = df.format(dateTimeRawValue);
							}
							
							return formattedDate;
							
						}else {
							return getBaseEntityAttrValueAsString(be, attributeCode);
						}*/
					}
					else if (value.getClass().equals(String.class)) {
						return (String)value;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
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
	
	
	public static String mergeDateTimeAttributeValue(String attributeCode, Object attributeValue, String dateTimeFormatValue) {
		
		LocalDateTime dateTimeRawValue = (LocalDateTime) attributeValue;
		String[] mergeValue = dateTimeFormatValue.split(",");
		String mergedFormattedDateTimeValue = "";
		for(String format : mergeValue) {
			mergedFormattedDateTimeValue = mergedFormattedDateTimeValue + getMergedDateTimeValue(dateTimeRawValue, format.toLowerCase());
		}
		return mergedFormattedDateTimeValue;
	}

	private static Object getMergedDateTimeValue(LocalDateTime dateTimeRawValue, String formatToBeMerged) {
		
		Object mergedDateTimeValue = null;
		if(formatToBeMerged != null) {
			switch (formatToBeMerged) {
			
			/* for date 1,2*/
			case "dd":
				mergedDateTimeValue = dateTimeRawValue.getDayOfMonth();
				break;
			/* for year - 2018 */
			case "yy":
				mergedDateTimeValue = dateTimeRawValue.getYear();
				break;
			/* for month - 1 to 12 */
			case "mm":
				mergedDateTimeValue = dateTimeRawValue.getMonthValue();
				break;
			/* for space between formatting */
			case "space":
				mergedDateTimeValue = " ";
				break;
			/* for getting if the time is AM or PM */
			case "ampm":
				int ampmIntVal = dateTimeRawValue.get(ChronoField.AMPM_OF_DAY);
				String ampm = null;
				if(ampmIntVal == 0) {
					ampm = "AM";
				} else {
					ampm = "PM";
				}
				mergedDateTimeValue = ampm;
				break;
			/* for having commas between formatting */
			case "comma":
				mergedDateTimeValue = ",";
				break;
			/* for getting weekday - Monday, Tuesday */
			case "dayofweekstring":
				mergedDateTimeValue = dateTimeRawValue.getDayOfWeek().toString().toLowerCase();
				break;
			/* for getting the time */
			case "time":
				DateTimeFormatter df =  
	            new DateTimeFormatterBuilder().appendPattern("[HH][.mm]")
	            .parseDefaulting(ChronoField.HOUR_OF_AMPM, 0)
	            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
	            .toFormatter(); 
				mergedDateTimeValue = df.format(dateTimeRawValue);
				break;
			/* for getting month in string - January, February */
			case "monthstring" :
				mergedDateTimeValue = dateTimeRawValue.getMonth();
				break;
			/* for getting date-formatter slash */
			case "/":
				mergedDateTimeValue = "/";
				break;
			/* for getting hours */
			case "hours" :
				mergedDateTimeValue = String.format("%02d", dateTimeRawValue.getHour());
				break;
			/* for getting minutes */
			case "minutes" :
				mergedDateTimeValue = String.format("%02d",dateTimeRawValue.getMinute());
				break;
			/* for getting seconds */
			case "seconds" :
				mergedDateTimeValue = String.format("%02d",dateTimeRawValue.getSecond());
				break;
			/* for getting timevalues-seperator */
			case ":":
				mergedDateTimeValue = ":";
				break;
			default:
				break;
			}
		}
		return mergedDateTimeValue;
	}
	
	/*public static void main(String[] args) {
		LocalDateTime dateTime = LocalDateTime.now();
		Object dateTimeObj = dateTime;
		String format = "time,space,ampm,comma,space,dayofweekstring,comma,space,dd,space,monthstring,space,yy";
		String format1 = "dd,/,mm,/,yy,space,hours,:,minutes,:,seconds";
		String formattedDate = mergeDateTimeAttributeValue("time", dateTimeObj, format);
		String formattedDate1 = mergeDateTimeAttributeValue("some", dateTimeObj, format1);
		System.out.println("formatted date ::"+formattedDate);
		System.out.println("formatted date 1 ::"+formattedDate1);
		
		
	}*/
	
	

}
