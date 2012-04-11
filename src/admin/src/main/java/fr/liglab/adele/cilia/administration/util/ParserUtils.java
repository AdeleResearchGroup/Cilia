package fr.liglab.adele.cilia.administration.util;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import fr.liglab.adele.cilia.CiliaException;



public class ParserUtils {
	/**
	 */
	public static Object getProperty(String value, String type)  {
		// Check that the property has a name

		if (value == null) {
			return null;
		}

		// Get the type of the structure to create
		if (type != null) {
			if (type.equalsIgnoreCase("map")) {
				Map res = parseMap(value);
				if (res == null) {
					System.out.println("Unable to parse map");
					return null;		
				}
				return res;
			} else if (type.equalsIgnoreCase("list")) {

				return  parseList(value);
			} else if (type.equalsIgnoreCase("array")) {
				List list = parseList(value);
				boolean isString = true;
				for (int i = 0; isString && i < list.size(); i++) {
					isString = list.get(i) instanceof String;
				}
				Object[] obj = null;
				if (isString) {
					obj = new String[list.size()];
				} else {
					obj = new Object[list.size()];
				}
				// Transform the list to array
				return list.toArray(obj);
			}
		} 
		return value;
	}

	private static List parseList(String prop) {
		return parseArraysAsList(prop);
	}

	/**
	 * Parses the string form of a Map as {key1[value1], key2[value2], [key3]:[value3]} as a list.
	 * @param str the string form
	 */
	private static Map parseMap(String prop) {
		List listOfEntrys = parseArraysAsList(prop);
		Map properties = new HashMap();
		for(int i = 0; i < listOfEntrys.size() ; i++) {
			String undecodedEntry = (String)listOfEntrys.get(i);
			String key = null;
			String value = null;
			int firstIdx = -1;
			int lastIdx = -1;

			firstIdx = undecodedEntry.indexOf('[');
			if (firstIdx == -1){
				return null;
			}
			lastIdx = findClossingBracketPosition(firstIdx, undecodedEntry);
			if (lastIdx ==- 1) {
				return null;
			}
			key = undecodedEntry.substring(firstIdx+1, lastIdx);
			String restValue = undecodedEntry.substring(lastIdx+1);
			firstIdx = restValue.indexOf('[');
			if (firstIdx == -1){
				return null;
			}
			lastIdx = findClossingBracketPosition(firstIdx, restValue);
			if (lastIdx ==- 1) {
				return null;
			}
			value = restValue.substring(firstIdx+1, lastIdx);
			properties.put(key, value);
		}
		return properties;
	}



	public static int findClossingBracketPosition(int openBracket, String line) {
		Stack st = new Stack();
		for (int i = openBracket; i >= 0 && i < line.length(); i ++ ) {
			if (line.charAt(i) == '[') {
				st.push(new Integer(i));
			} if (line.charAt(i) == ']') {
				if (!st.empty()) st.pop();
				if (st.empty()){
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Split method. 
	 * This method is equivalent of the String.split in java 1.4
	 * The result array contains 'trimmed' String
	 * @param toSplit the String to split
	 * @param separator the separator
	 * @return the split array 
	 */
	public static String[] split(String toSplit, String separator) {
		StringTokenizer tokenizer = new StringTokenizer(toSplit, separator);
		String[] result = new String[tokenizer.countTokens()];
		int index = 0;
		while (tokenizer.hasMoreElements()) {
			result[index] = tokenizer.nextToken().trim();
			index++;
		}
		return result;
	}

	/**
	 * Parses the string form of an array as {a, b, c} as a list.
	 * @param str the string form
	 * @return the resulting list
	 */
	public static List parseArraysAsList(String str) {
		return Arrays.asList(parseArrays(str));
	}


	/**
	 * Parses the string form of an array as {a, b, c}
	 * @param str the string form
	 * @return the resulting string array
	 */
	public static String[] parseArrays(String str) {
		if (str.length() == 0) {
			return new String[0];
		}

		// Remove { and } or [ and ]
		if ((str.charAt(0) == '{' && str.charAt(str.length() - 1) == '}') ) {
			String internal = (str.substring(1, str.length() - 1)).trim();
			// Check empty array
			if (internal.length() == 0) {
				return new String[0];
			}
			return split(internal, ",");
		} else {
			return new String[] { str };
		}
	}

	public static String getParameter(final String name, final String line) {
		String param = null;
		String result = null;
		int index = line.indexOf(name);
		String pms[] = null;
		if (index >= 0) {
			String pm = line.substring(index);
			if (pm != null) {
				pms = split(pm,"=");
				if (pms != null && pms.length > 1) {
					param = pms[1];
					if(param.startsWith("\"") && param.length() > 1) {
						result = param.substring(param.indexOf("\"", 0)+1, param.indexOf("\"", 1));
					} else {
						String res[] = split(param," ");//eliminate other params
						if (res != null && res.length>0){
							result = res[0];
						} else {
							result = param;
						}
					}
				}
			}
		}
		return String.valueOf(result);
	}

}
