package ds.spgen;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Step {
	String name;
	String returntype;
	List<Parameter> parameterList;
	int step;

	int s_count;
	int b_count;
	int ba_count;
	int Ba_count;

	/**
	 * Convenience Step class that stores information about a class including
	 * the parameters in more detail than the Java Method class for use with the
	 * StubGenerator
	 * 
	 * @param m
	 *            the method to convert to convenient format
	 * @param prot
	 *            the protocol this method belongs to
	 * @param step
	 *            the step for this protocol
	 * @throws Exception
	 */
	Step(Method m, int step) throws Exception {
		s_count = 0;
		b_count = 0;
		ba_count = 0;
		Ba_count = 0;
		this.step = step;
		name = m.getName();
		/* return types */
		returntype = m.getReturnType().getName();
		// System.out.println("returntype: " + returntype);
		if (returntype.equals("[B"))
			returntype = "byte[]";
		if (returntype.equals("[Ljava.math.BigInteger;"))
			returntype = "java.math.BigInteger[]";

		parameterList = new ArrayList<Parameter>();

		int i = 0;
		for (Class<?> p : m.getParameterTypes()) {
			if (p.getName().equals("java.math.BigInteger")) {
				Parameter bpi = new Parameter("java.math.BigInteger", "b",
						b_count, i);
				parameterList.add(bpi);
				b_count++;
			} else if (p.getName().equals("[Ljava.math.BigInteger;")) {
				Parameter Bapi = new Parameter("java.math.BigInteger[]", "Ba",
						Ba_count, i);
				parameterList.add(Bapi);
				Ba_count++;
			} else if (p.getName().equals("short")) {
				Parameter spi = new Parameter("short", "s", s_count, i);
				parameterList.add(spi);
				s_count++;
			} else if (p.getName().equals("[B")) {
				/* byte array */
				Parameter bapi = new Parameter("byte[]", "ba", ba_count, i);
				parameterList.add(bapi);
				ba_count++;
			} else {
				throw new Exception("[Step] Type \"" + p.getName()
						+ "\" not supported yet!");
			}
			i++;
		}
	}

	/**
	 * Get the number of short parameters for this method
	 * 
	 * @return number of shorts
	 */
	public int getNumberOfShorts() {
		return s_count;
	}

	/**
	 * Get the number of BigInteger parameters for this method
	 * 
	 * @return number of BigIntegers
	 */
	public int getNumberOfBigIntegers() {
		return b_count;
	}

	/**
	 * Get the total number of parameters for this method
	 * 
	 * @return total number of parameters
	 */
	public int getNumberOfParameters() {
		return s_count + b_count + ba_count + Ba_count;
	}

	/**
	 * Get the name of the method
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the return type of the method
	 * 
	 * @return the return type
	 */
	public String getReturnType() {
		return returntype;
	}

	public List<Parameter> getParameters() {
		return parameterList;
	}

	/**
	 * Get the number of byte[] parameters for this method
	 * 
	 * @return number of byte[]
	 */
	public int getNumberOfByteArrays() {
		return ba_count;
	}

	/**
	 * Get code for returning the correct type based on the method return type
	 * 
	 * @return the code needed to generate the instance
	 */
	public String codeReturnTypeInstance() {
		if (returntype.equals("void")) {
			return "";
		} else if (returntype.equals("java.math.BigInteger")) {
			return "return new java.math.BigInteger(res);";
		} else if (returntype.equals("short")) {
			return "return GET_SHORT(res,0);";
		} else if (returntype.equals("byte[]")) {
			return "return res;";
		} else if (returntype.equals("java.math.BigInteger[]")) {
			/*
			 * BigInteger[] is converted like this: <short number of
			 * BigIntegers> <short size_1> <... data 1 ...> ... <short size_n>
			 * <... data n ...>
			 * 
			 * FIXME: should check whether data is valid before using it! first
			 * check if length is big enough in order to continue (for shorts,
			 * and then for data!)
			 */
			String output = new String();
			output += "if(res==null||res.length==0)return null;";
			output += "java.math.BigInteger[] rBa = new java.math.BigInteger[res[0]]; int dataOffset=1;";
			output += "for(int i=0; i < res[0] ; i++) {";
			output += "short s = Util.byteArrayToShort(res, dataOffset);";
			output += "byte[] tmp = new byte[s]; System.arraycopy(res, dataOffset+2, tmp, 0, s);";
			output += "rBa[i] = new java.math.BigInteger(tmp);";
			output += "dataOffset+= s+2;";
			output += "} return rBa;";
			return output;
		} else {
			return ""; // can't reach this
		}
	}

	public String codeCallMethod() {
		if (returntype.equals("void")) {
			return String.format("%s(%s);", name, listParameters(false));
		} else if (returntype.equals("java.math.BigInteger[]")) {
			String output = new String();
			output += String.format("rBa = %s(%s); ", name,
					listParameters(false));
			output += "result = new byte[1]; result[0] = (byte)rBa.length;";
			output += "for(int i=0;i<rBa.length;i++){";
			output += "result = Util.appendToByteArray(result, SET_SHORT((short)rBa[i].toByteArray().length));";
			output += "result = Util.appendToByteArray(result, rBa[i].toByteArray());";
			output += "}";
			return output;
		} else if (returntype.equals("java.math.BigInteger")) {
			return String.format("rb = %s(%s); result = rb.toByteArray();",
					name, listParameters(false));
		} else if (returntype.equals("short")) {
			return String.format("rs = %s(%s); result = SET_SHORT(rs);", name,
					listParameters(false));
		} else if (returntype.equals("byte[]")) {
			return String.format("rba = %s(%s); result = rba;", name,
					listParameters(false));
		} else {
			return ""; // can't reach this
		}
	}

	public String listParameters(boolean include_type) {
		if (parameterList.size() == 0) {
			return "";
		} else {
			List<String> l = new ArrayList<String>();

			for (Parameter p : parameterList) {
				if (include_type) {
					l.add(p.getType() + " " + p.getName());
				} else {
					l.add(p.getName());
				}
			}
			return Util.joinString(l.toArray(), ", ");
		}
	}

	public int getStepNumber() {
		return step;
	}

	public String getConstName() {
		return name.replaceAll("([A-Z])", "_$1").toUpperCase();
	}

	public int getNumberOfBigIntegerArrays() {
		return Ba_count;
	}
}
