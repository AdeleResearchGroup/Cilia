package fr.liglab.adele.cilia.admin.util;

import java.util.*;


public class ParserUtils {
    /**
     */
    public static Object getProperty(String value, String type) {
        // Check that the property has a name

        if (value == null) {
            return null;
        }

        // Get the type of the structure to create
        if (type != null) {
            if (type.equalsIgnoreCase("map")) {
                Hashtable res = parseTable(value);
                if (res == null) {
                    System.out.println("Unable to parse map");
                    return null;
                }
                return res;
            } else if (type.equalsIgnoreCase("list")) {

                return parseList(value);
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
     *
     * @param prop the string form
     */
    private static Hashtable<String, Object> parseTable(String prop) {
        List<String> listOfEntrys = parseArraysAsList(prop);
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        for (int i = 0; i < listOfEntrys.size(); i++) {
            String undecodedEntry = listOfEntrys.get(i);
            String key = null;
            Object value = null;
            int firstIdx = -1;
            int lastIdx = -1;

            firstIdx = undecodedEntry.indexOf('[');
            if (firstIdx == -1) {
                return null;
            }
            lastIdx = findClossingBracketPosition(firstIdx, undecodedEntry, '[', ']');
            if (lastIdx == -1) {
                return null;
            }
            key = undecodedEntry.substring(firstIdx + 1, lastIdx);
            String restValue = undecodedEntry.substring(lastIdx + 1);
            firstIdx = restValue.indexOf('[');
            if (firstIdx == -1) {
                return null;
            }
            lastIdx = findClossingBracketPosition(firstIdx, restValue, '[', ']');
            if (lastIdx == -1) {
                return null;
            }
            value = restValue.substring(firstIdx + 1, lastIdx);
            properties.put(key, value);
        }
        return properties;
    }


    public static int findClossingBracketPosition(int openBracket, String line, char init, char end) {
        Stack<Integer> st = new Stack<Integer>();
        for (int i = openBracket; i >= 0 && i < line.length(); i++) {
            if (line.charAt(i) == init) { //'['
                st.push(new Integer(i));
            } else if (line.charAt(i) == end) { //']'
                if (!st.empty()) st.pop();
                if (st.empty()) {
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
     *
     * @param toSplit   the String to split
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
     *
     * @param str the string form
     * @return the resulting list
     */
    public static List<String> parseArraysAsList(String str) {
        return Arrays.asList(parseArrays(str));
    }


    /**
     * Parses the string form of an array as {a, b, c}
     *
     * @param str the string form
     * @return the resulting string array
     */
    public static String[] parseArrays(String str) {
        if (str.length() == 0) {
            return new String[0];
        }

        // Remove { and }
        if ((str.charAt(0) == '{' && str.charAt(str.length() - 1) == '}')) {
            String internal = (str.substring(1, str.length() - 1)).trim();
            // Check empty array
            if (internal.length() == 0) {
                return new String[0];
            }
            return split(internal, ",");
        } else {
            return new String[]{str};
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
                pms = split(pm, "=");
                if (pms != null && pms.length > 1) {
                    param = pms[1];
                    if (param.startsWith("\"") && param.length() > 1) {
                        result = param.substring(param.indexOf("\"", 0) + 1, param.indexOf("\"", 1));
                    } else {
                        String res[] = split(param, " ");//eliminate other params
                        if (res != null && res.length > 0) {
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

    public static Hashtable<String, Object> getProperties(final String properties) {
        //		String [] props = parseArrays(properties);
        //		String name = null;
        //		String value = null;
        //		Hashtable<String, Object> ht = new Hashtable<String, Object>();
        //		for(String prop: props){
        //			int index = prop.indexOf('=');
        //			name = prop.substring(0, index);
        //			value = prop.substring(index + 1);
        //			ht.put(name, getValue(value));
        //		}
        //		return ht;
        return JSONStringToHashtable(properties);
    }

    public static Hashtable<String, Object> JSONStringToHashtable(final String string) {
        JSONObject jo = null;
        Hashtable properties = null;
        try {
            jo = new JSONObject(string);
            properties = JSONtoHashtable(jo);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return properties;

    }

    private static Hashtable<String, Object> JSONtoHashtable(final JSONObject jobject) throws JSONException {
        Hashtable properties = new Hashtable();
        Iterator<String> it = jobject.keys();
        while (it.hasNext()) {
            Object value = null;
            String key = it.next();
            Object job = jobject.get(key);
            if (job instanceof JSONObject) {
                value = JSONtoHashtable((JSONObject) job);
            } else if (job instanceof JSONArray) {
                JSONArray ja = (JSONArray) job;
                value = JSONtoArray(ja);
            } else {
                value = job;
            }
            properties.put(key, value);
        }
        return properties;
    }

    private static List JSONtoArray(JSONArray jarray) throws JSONException {
        List list = new ArrayList();
        for (int i = 0; i < jarray.length(); i++) {
            list.add(jarray.get(i));
        }
        return list;
    }
    //	private static Object getValue(String value){
    //		Object ovalue = null;
    //		value = value.trim();
    //		if (value.startsWith("{") && value.endsWith("}")) { //It is a Hashtable
    //			ovalue = getProperties(value);
    //		} else if (value.startsWith("[") && value.endsWith("]")) { // It is a List
    //			ovalue = parseArrays(value);
    //		} else {
    //			ovalue = value;
    //		}
    //		return ovalue;
    //	}


}
