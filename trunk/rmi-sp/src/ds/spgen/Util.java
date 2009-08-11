package ds.spgen;

public class Util {

	/**
	 * Join a list of Strings together
	 * 
	 * @param str
	 *            list of Strings
	 * @param delimiter
	 *            the delimiter for the join (e.g. ",")
	 * @return a joined String
	 */
	public static String joinString(Object[] str, String delimiter) {
		if (str == null || str.length == 0)
			return "";
		String output = "";
		for (int i = 0; i < str.length - 1; i++) {
			output += str[i] + ",";
		}
		output += str[str.length - 1];
		return output;
	}

}
