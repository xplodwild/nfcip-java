package ds.spgen;

public class Parameter {

	String type;
	String name;
	String s_name;
	int abs_number;

	/**
	 * Store parameters in a convenient way
	 * 
	 * @param type
	 *            the type of the parameter (byte[], short, BigInteger, etc.)
	 * @param name
	 *            the name of the parameter
	 * @param rel_number
	 *            the parameter count so far of this type for the method
	 * @param abs_number
	 *            the position of the parameter in the signature of the method
	 */
	Parameter(String type, String name, int rel_number, int abs_number) {
		this.type = type;
		this.name = name + rel_number;
		this.abs_number = abs_number;
		this.s_name = name + rel_number + "_size";
	}

	/**
	 * Get the parameter type
	 * 
	 * @return the type
	 */
	String getType() {
		return type;
	}

	/**
	 * Get the parameter name
	 * 
	 * @return the name
	 */
	String getName() {
		return name;
	}

	/**
	 * Get the name of the parameter with added suffix "_size"
	 * 
	 * @return the name with suffix
	 */
	String getSizeName() {
		return s_name;
	}

	/**
	 * Get the position of the parameter in the signature of the method
	 * 
	 * @return the position
	 */
	public int getAbsPosition() {
		return abs_number;
	}

	/**
	 * Get code for copying this parameter to buffer as byte array
	 * 
	 * @param buf
	 *            the name of the buffer
	 * @param offset
	 *            the name of the offset variable
	 * @return the code needed to perform the copy
	 */
	public String codeToByteArray(String buf, String offset) {
		if (type.equals("java.math.BigInteger")) {
			return String
					.format(
							"if (%s != null) { ARRAY_COPY(%s.toByteArray(), 0, %s, %s, %s); offset += %s; }",
							name, name, buf, offset, s_name, s_name);
		} else if (type.equals("short")) {
			return String.format(
					"ARRAY_COPY(SET_SHORT(%s), 0, %s, %s, 2); offset += 2;",
					name, buf, offset);
		} else if (type.equals("byte[]")) {
			return String
					.format(
							"if (%s != null) { ARRAY_COPY(%s, 0, %s, %s, %s); offset += %s; }",
							name, name, buf, offset, s_name, s_name);
		} else if (type.equals("java.math.BigInteger[]")) {
			return String
					.format(
							"tmp_storage = Util.setBigIntegerArray(%s); System.arraycopy(tmp_storage,0,%s,%s,tmp_storage.length); offset+= tmp_storage.length;",
							name, buf, offset);

		} else {
			return ""; // can't reach this
		}
	}

	public String codeFromByteArray(String src_buf) {
		if (type.equals("java.math.BigInteger")) {

			return String.format("%s = GET_SHORT(%s, %s); if (%s != 0) {"
					+ "tmp = new byte[%s];"
					+ "ARRAY_COPY(%s, offset, tmp, 0, %s);"
					+ "%s = new java.math.BigInteger(tmp); offset += %s; "
					+ "}", s_name, src_buf, (2 * abs_number + 3), s_name,
					s_name, src_buf, s_name, name, s_name);
		} else if (type.equals("short")) {
			return String.format("%s = GET_SHORT(%s, offset); offset += 2;",
					name, src_buf);

		} else if (type.equals("byte[]")) {
			return String.format("%s = GET_SHORT(%s, %s); if (%s != 0) { "
					+ "%s = new byte[%s]; "
					+ "ARRAY_COPY(%s, offset, %s, 0, %s);" + "offset += %s; }",
					s_name, src_buf, (2 * abs_number + 3), s_name, name,
					s_name, src_buf, name, s_name, s_name);
		} else if (type.equals("java.math.BigInteger[]")) {
			return String
					.format(
							"%s = Util.getBigIntegerArray(%s,offset);offset+=Util.BigIntegerArraySize(%s,true);",
							name, src_buf, name);
		} else {
			return "<BROKEN>"; // can't reach this
		}
	}
}
