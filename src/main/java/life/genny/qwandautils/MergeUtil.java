package life.genny.qwandautils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.javamoney.moneta.Money;

import life.genny.qwanda.Link;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;

public class MergeUtil {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
	
    /* [[VARIABLENAME.ATTRIBUTE]] pattern */
    /* Used for baseentity-attribute merging */
	public static final String REGEX_START = "[[";
	public static final String REGEX_END = "]]";
	public static final String REGEX_START_PATTERN = Pattern.quote(REGEX_START);
    public static final String REGEX_END_PATTERN = Pattern.quote(REGEX_END);
    public static final Pattern PATTERN = Pattern.compile(REGEX_START_PATTERN + "(?s)(.*?)" + REGEX_END_PATTERN);
    public static final String DEFAULT = "";
    public static final String PATTERN_BASEENTITY = REGEX_START_PATTERN + "(?s)(.*?)" + REGEX_END_PATTERN;
    public static final Pattern PATTERN_MATCHER = Pattern.compile(PATTERN_BASEENTITY);
    
    /* {{VARIABLE}} pattern */
    /* this is for direct merging */
    public static final String VARIABLE_REGEX_START = "{{";
    public static final String VARIABLE_REGEX_END = "}}";
    public static final Pattern PATTERN_VARIABLE = Pattern.compile(Pattern.quote(VARIABLE_REGEX_START) + "(?s)(.*?)" + Pattern.quote(VARIABLE_REGEX_END));  
    
    /* ((FORMAT)) pattern */
    /* this is for formatting data such as dates or strings */
    public static final String FORMAT_VARIABLE_REGEX_START = "((";
    public static final String FORMAT_VARIABLE_REGEX_END = "))";
    public static final Pattern FORMAT_PATTERN_VARIABLE = Pattern.compile(Pattern.quote(FORMAT_VARIABLE_REGEX_START) + "(.*)" + Pattern.quote(FORMAT_VARIABLE_REGEX_END));  
    
    
	public static String merge(String mergeStr, Map<String, Object> templateEntityMap) { 
		
		/* matching [OBJECT.ATTRIBUTE] patterns */
		if (mergeStr != null) {

			Matcher match = PATTERN_MATCHER.matcher(mergeStr);
			Matcher matchVariables = PATTERN_VARIABLE.matcher(mergeStr);
		
			if (templateEntityMap != null && templateEntityMap.size() > 0) {
				
				while (match.find()) {	
					
					Object mergedObject = wordMerge(match.group(1), templateEntityMap);
					if (mergedObject != null) {
						mergeStr = mergeStr.replace(REGEX_START + match.group(1) + REGEX_END, mergedObject.toString());
					} else {
						mergeStr = mergeStr.replace(REGEX_START + match.group(1) + REGEX_END, "");
					}			
				}
				
				/* duplicating this for now. ideally wordMerge should be a bit more flexible and allows all kind of data to be passed */
				while (matchVariables.find()) {
					
					Object mergedText = templateEntityMap.get(matchVariables.group(1));
					if (mergedText != null) {
						mergeStr = mergeStr.replace(VARIABLE_REGEX_START + matchVariables.group(1) + VARIABLE_REGEX_END, mergedText.toString());
					} else {
						mergeStr = mergeStr.replace(VARIABLE_REGEX_START + matchVariables.group(1) + VARIABLE_REGEX_END, "");
					}	
				}
				
			}

		} else {
			log.warn("mergeStr is NULL");
		}
	
		return mergeStr;
	}
	
	@SuppressWarnings("unused")
	public static Object wordMerge(String mergeText, Map<String, Object> entitymap) {
		
		if(mergeText != null && !mergeText.isEmpty()) {
			
			try {
				
				/* we split the text to merge into 2 components: BE.PRI... becomes [BE, PRI...] */
				String[] entityArr = mergeText.split("\\.");
				String keyCode = entityArr[0];
				log.debug(keyCode);
				
				if((entityArr.length == 0))
					return DEFAULT;
				
				if(entitymap.containsKey(keyCode)) {
					
					Object value = entitymap.get(keyCode);

					if (value == null) {
						log.info("value is NULL for key " + keyCode);
					} else {
						
						if (value.getClass().equals(BaseEntity.class)) {
							
							BaseEntity be = (BaseEntity)value;

							String attributeCode = entityArr[1];
							log.debug(attributeCode);
							
							Object attributeValue = be.getValue(attributeCode, null);
							log.debug(attributeValue);

							Matcher matchFormat = null;
							if (entityArr != null && entityArr.length > 2) {
								matchFormat = FORMAT_PATTERN_VARIABLE.matcher(entityArr[2]);
							}
							
							if (attributeValue instanceof org.javamoney.moneta.Money) {
								log.info("price attributes 1");
								DecimalFormat df = new DecimalFormat("#.00"); 
								Money money = (Money) attributeValue; 
								
								if (attributeValue != null) {
									return df.format(money.getNumber()) + " " + money.getCurrency();
								} else {
									return DEFAULT;
								}

							} else if (attributeValue instanceof java.time.LocalDateTime) {
								/* If the date-related mergeString needs to formatter to a particultar format -> we split the date-time related merge text to merge into 3 components: BE.PRI.TimeDateformat... becomes [BE, PRI...] */
								/* 1st component -> BaseEntity code ; 2nd component -> attribute code ; 3rd component -> (date-Format) */
								if (matchFormat != null && matchFormat.find()) {
									log.info("This datetime attribute code ::"+attributeCode+ " needs to be formatted and the format is ::"+entityArr[2]);
										return getFormattedDateTimeString((LocalDateTime) attributeValue, matchFormat.group(1));
								} else {
									log.info("This DateTime attribute code ::"+attributeCode+ " needs no formatting");
									return (LocalDateTime) attributeValue;
								}

							} else if (attributeValue instanceof java.time.LocalDate) {

								if (matchFormat != null && matchFormat.find()) {
									log.info("This date attribute code ::"+attributeCode+ " needs to be formatted and the format is ::"+entityArr[2]);
									return getFormattedDateString((LocalDate) attributeValue, matchFormat.group(1));
								} else {
									log.info("This Date attribute code ::"+attributeCode+ " needs no formatting");
									return (LocalDate) attributeValue;
								}

							} else if (attributeValue instanceof java.lang.String){

								if (matchFormat != null && matchFormat.find()) {
									log.info("This String attribute code ::"+attributeCode+ " needs to be formatted and the format is ::"+entityArr[2]);
									return getFormattedString((String) attributeValue, matchFormat.group(1));
								} else {
									log.info("This String attribute code ::"+attributeCode+ " needs no formatting");
									return getBaseEntityAttrValueAsString(be, attributeCode);
								}

							} else if (attributeValue instanceof java.lang.Boolean) {
								return (Boolean) attributeValue;
							} else if (attributeValue instanceof java.lang.Integer) {
								return (Integer) attributeValue;
							} else if (attributeValue instanceof java.lang.Long) {
								return (Long) attributeValue;
							} else if (attributeValue instanceof java.lang.Double) {
								return (Double) attributeValue;
							} else {
								return getBaseEntityAttrValueAsString(be, attributeCode);
							}
							
						} else if (value.getClass().equals(String.class)) {
							return (String) value;
						}
					}
				}

			} catch (Exception e) {
				log.error("ERROR",e);
			}
		
		}
		
		return DEFAULT;	
	}


	/**
	 * Check to see if all contexts are present
	 */
	public static Boolean contextsArePresent(String mergeStr, Map<String, Object> templateEntityMap) { 
		
		/* matching [OBJECT.ATTRIBUTE] patterns */
		if (mergeStr != null) {

			Matcher match = PATTERN_MATCHER.matcher(mergeStr);
			Matcher matchVariables = PATTERN_VARIABLE.matcher(mergeStr);
		
			if(templateEntityMap != null && templateEntityMap.size() > 0) {
				
				while (match.find()) {
					
					Object mergedObject = wordMerge(match.group(1), templateEntityMap);
					if (mergedObject == null || mergedObject.toString().isEmpty()) {
						return false;
					}			
				}
				
				while(matchVariables.find()) {
					
					Object mergedText = templateEntityMap.get(matchVariables.group(1));
					if (mergedText == null) {
						return false;
					}	
				}
				
			}

		} else {
			log.warn("mergeStr is NULL");
		}
		return true;
	}

	public static Boolean requiresMerging(String mergeStr) {

		if (mergeStr == null) {
			log.warn("mergeStr is NULL");
			return null;
		}

		Matcher match = PATTERN_MATCHER.matcher(mergeStr);
		Matcher matchVariables = PATTERN_VARIABLE.matcher(mergeStr);

		return (match.find() || matchVariables.find());
	}

	
	/**
	 * 
	 * @param baseEntAttributeCode
	 * @param token
	 * @return Deserialized BaseEntity model object with values for a BaseEntity code that is passed
	 */
	public static BaseEntity getBaseEntityForAttr(String baseEntAttributeCode, String token) {
		
		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		String attributeString = null;
		BaseEntity be = null;
		try {
			attributeString = QwandaUtils
					.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" +baseEntAttributeCode, token);
			be = JsonUtils.fromJson(attributeString, BaseEntity.class);
			
		} catch (IOException e) {
			log.error("ERROR", e);
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
			log.info("PRI_FULLNAME   ::   "+ fullName);		
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
            log.info("this is the output :: "+ output);
            
        }catch (Exception e) {
            log.error("ERROR",e);
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
	
	public static String getFormattedDateTimeString(LocalDateTime dateToBeFormatted, String format) {
		if(dateToBeFormatted != null && format != null) {
			DateTimeFormatter dateformat = DateTimeFormatter.ofPattern(format);
			return dateToBeFormatted.format(dateformat);
		}
		return null;
	}

	public static String getFormattedDateString(LocalDate dateToBeFormatted, String format) {
		if(dateToBeFormatted != null && format != null) {
			DateTimeFormatter dateformat = DateTimeFormatter.ofPattern(format);
			return dateToBeFormatted.format(dateformat);
		}
		return null;
	}

	public static String getFormattedString(String stringToBeFormatted, String format) {
		/*
		 * This is a utility used to format strings during merging.
		 * Feel free to add some cool little string formatting tools below. (Jasper - 27/07/21)
		 */
		if(stringToBeFormatted != null && format != null) {
			String[] formatCommands = format.split("\\.");
			for (String cmd : formatCommands) {
				// A nice little clean up command for attribute values
				if (cmd.equals("CLEAN")) {
					stringToBeFormatted = stringToBeFormatted.replace("\"", "").replace("[", "").replace("]", "").replace(" ", "");
				}
				// A nice little substring command
				if (cmd.startsWith("SUBSTRING(")) {
					String[] subStringField = cmd.replace("SUBSTRING", "").replace("(", "").replace(")", "").replace(" ", "").split(",");
					Integer begin = Integer.valueOf(subStringField[0]);
					Integer end = subStringField.length > 1 ? Integer.valueOf(subStringField[1]) : null;
					if (end != null) {
						stringToBeFormatted = stringToBeFormatted.substring(begin, end);
					} else {
						stringToBeFormatted = stringToBeFormatted.substring(begin);
					}
				}
			}
			return stringToBeFormatted;
		}
		return null;
	}

	
	/*public static void main(String[] args) {
		DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");
		DateTimeFormatter dateformat1 = DateTimeFormatter.ofPattern("H:m a, EE, dd, MMMM, yyyy");
		LocalDateTime localDateTime = LocalDateTime.now();
		String text = localDateTime.format(dateformat);
		String text1 = localDateTime.format(dateformat1);
		log.info("formattedDate ::"+text);
		log.info("formattedDate ::"+text1);
	}*/
	
	

}
