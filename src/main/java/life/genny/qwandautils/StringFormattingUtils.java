package life.genny.qwandautils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.simple.JSONArray;

public class StringFormattingUtils {

	
	/**
	 * 
	 * @param str - String to be masked
	 * @param start - start range for masking
	 * @param end - end range for masking
	 * @param maskCharacter - character to be used for masking
	 * @param ignoreCharacterArrayForMask - If there are any character to be ignored while masking, they will put in the ignoreCharacterArrayForMask
	 * @return masked String
	 */
	/* Currently used for bank credential number masking */
	public static String maskWithRange(String str, int start, int end, String maskCharacter, Character[] ignoreCharacterArrayForMask) {
		
		/* we check if we are not out of range or if the passed str is null */
		if(str == null || str.length() == 0) return null; 
		if(end - start < 0) return null;
		
		int maskLength = end - start;
		if(maskLength > str.length()) return null;
		
		StringBuilder newStr = new StringBuilder();
		
		/* we create a mask with the right length */
		for(int i = 0; i < maskLength; i++) {
			
			char c = str.charAt(i);
			
			/* If there are any character to be ignored while masking, they will put in the ignoreCharacterArrayForMask */
			if(ignoreCharacterArrayForMask != null && ignoreCharacterArrayForMask.length > 0) {
				
				/* iterating through each ignoreMaskCharacter */
				for(Character ignoreCharacterForMask : ignoreCharacterArrayForMask) {
					if(c == ignoreCharacterForMask) {
						/* If a character of word matched character to be ignored, then the character will not be masked */
						newStr.append(c);
					} else {
						newStr.append(maskCharacter);
					}
				}
				
			} else {
				newStr.append(maskCharacter);
			}
			
			
		}
		
		/* we return: originalString until start of mask + mask + originalString from end of mask */
		/*
		 * example:
		 * str = 1234-1234
		 * start = 0
		 * end = 4
		 * return: xxxx-1234
		 */
		
		return str.substring(0, start) + newStr + str.substring(end, str.length());
	}
	
	/**
	 * 
	 * @param string
	 * @return returns character split List. Ignore all trailing spaces
	 * @example Input => String input = "str1, str2,   str3   ,str4";
	 * Output => [str1, str2, str3, str4]
	 */
	public static List<String> splitCharacterSeperatedStringToList(String characterSeperatedString, String character) {
		
		List<String> splitListIgnoringSpaces = new CopyOnWriteArrayList<>();
		if (characterSeperatedString != null) {
			if (characterSeperatedString.contains(character)) {
				/*
				 * string array is converted to list with Array.asList, the list will have a
				 * fixed size. 
				 */
				String[] items = characterSeperatedString.split(",");
				for(String item : items) {
					splitListIgnoringSpaces.add(item.trim());
				}
				
			} else {
				splitListIgnoringSpaces.add(characterSeperatedString);
			}
		}
		
		return splitListIgnoringSpaces;
	}
	
	/**
	 * 
	 * @param stringifiedJSONArray - a JSON array in String format
	 * @return returns an ArrayList of Strings that was parsed using JsonUtils
	 * 
	 */
	public static List<String> convertToStringArray(String stringifiedJSONArray) {
		if(stringifiedJSONArray != null) {
			JSONArray arr = JsonUtils.fromJson(stringifiedJSONArray, JSONArray.class);
			if(arr.size() > 0) {
				return arr;
			}
			return null;
		}
		return null;
	}
	
	


}