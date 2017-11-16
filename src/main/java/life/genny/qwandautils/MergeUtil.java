package life.genny.qwandautils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.entity.BaseEntity;

public class MergeUtil {
	
	public static final String REGEX_START = "{{";
	public static final String REGEX_END = "}}";
	public static final String REGEX_START_PATTERN = Pattern.quote(REGEX_START);
    public static final String REGEX_END_PATTERN = Pattern.quote(REGEX_END);
    public static final Pattern PATTERN = Pattern.compile(REGEX_START_PATTERN + "(.*?)" + REGEX_END_PATTERN);
    public static final String DEFAULT = "";
	
	public static String merge(String mergeStr, Map<String, BaseEntity> templateEntityMap) {
				
		Matcher match = PATTERN.matcher(mergeStr);		
		
		while (match.find()) {

			System.out.println("to be merged ::" + match.group(1));
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
			
			if (entitymap.containsKey(baseent) && entitymap.get(baseent).containsEntityAttribute(entityArr[1])) {
				try {

					BaseEntity be = entitymap.get(baseent);
					Attribute ea = be.findEntityAttribute(entityArr[1]).get().getAttribute();

					return be.findEntityAttribute(entityArr[1]).get().getValueString();
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		return DEFAULT;	
	
	}

}
