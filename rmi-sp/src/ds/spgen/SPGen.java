package ds.spgen;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author F. Kooman
 * 
 *         <pre>
 * - implement a close method the close the RMI loop at server?!?!
 * - custom return types with multiple variables, maybe just special objects
 *   (Hash -&gt; BigInteger, byte[] ) you have to create this by hand, no multiple
 *   returns possible
 * - limit wrappers in Server as much as possible (for Java Card)
 * - how to have a BigNat that can work with ints and with bytes?
 * - check the return stuff for BigInteger[]'s ! breaks if it is null etc.
 * - implement PROTOCOL DEPENDENCIES! 
 * - in case you have an init protocol! and not just a step you might want to have
 *   that one executed before the next!
 *</pre>
 */

public class SPGen {
	ProtocolSuite ps;
	List<String> protocolList;

	private String pkg;
	String dateTime;

	/**
	 * Initializes all the data structures for easy access to details about
	 * protocols, steps and parameters
	 * 
	 * @param pkg
	 *            the Java package name
	 * @param protocols
	 *            List of interface names separated by spaces
	 * @param fpn
	 *            The number from which to start numbering protocols, in case
	 *            you have different protocol suites you have to make sure they
	 *            don't overlap!
	 * @throws Exception
	 */
	SPGen(int fpn, String pkg, String protocols) throws Exception {
		this.pkg = pkg;
		protocolList = Arrays.asList(protocols.split(" "));
		// System.out.println("Package: " + pkg);
		// System.out.println("Protocols: " + protocolList);
		ps = new ProtocolSuite(pkg);

		for (int i = 0; i < protocolList.size(); i++) {
			System.out.println("Adding protocol " + protocolList.get(i));
			ps.addProtocol((byte) (i + fpn), protocolList.get(i));
		}

		Date today;
		DateFormat dateFormatter;
		dateFormatter = DateFormat.getDateTimeInstance(DateFormat.LONG,
				DateFormat.LONG);
		today = new Date();
		dateTime = dateFormatter.format(today);
	}

	public String generateNewClientStub(String name) throws Exception {
		String s = generateClientStub(name);
		s = s.replaceAll("SERVER_SEND", "try { server.send(data);");
		s = s
				.replaceAll(
						"SERVER_RECV",
						"server.receive(); } catch (Exception e) { throw new RMIException(\"bluetooth communication error\"); }");
		s = s.replaceAll("ARRAY_COPY", "System.arraycopy");
		s = s.replaceAll("GET_SHORT", "Util.byteArrayToShort");
		s = s.replaceAll("SET_SHORT", "Util.shortToByteArray");
		return s;
	}

	public String generateClientStub(String name) throws Exception {
		String output = new String();
		output += "/* *** DO NOT EDIT *** Automatically generated on "
				+ dateTime + " */\n";
		output += "package " + pkg + ";";
		output += "import ds.sp.Util;";
		output += "import ds.sp.RMIException;";
		output += "public class " + name + " implements "
				+ Util.joinString(protocolList.toArray(), ",") + " {";
		output += "\n/* constant positions of protocol and step in the data stream */\n";
		output += "private final byte PROTOCOL = (byte) 0;";
		output += "private final byte STEP = (byte) 1;";
		output += "private final byte NUMBER_OF_PARAMETERS = (byte) 2;";
		// output += "private final byte SIZES_OFFSET = (byte) 3;";

		output += ps.codeStepConstants();

		/*
		 * temporary storage for results from byte array before passing back to
		 * Client
		 */
		output += ps.codeReturnTypeStorage();

		output += "ds.sp.RMIConnection server;";
		output += "private byte[] tmp_storage;";
		output += "private byte[] res;";
		output += "private byte[] temp_res;";

		output += name + "(ds.sp.RMIConnection i) {";
		output += "server = i;";
		output += "}";

		output += "public void close() throws RMIException { ";
		output += "byte[] data = new byte[] {(byte)0xff, (byte)0xff, (byte)0x00};";
		output += "try { server.send(data); server.close(); } catch (Exception e) {}";
		output += "}";

		/* For all methods defined in the interfaces */
		for (Protocol protocol : ps.getProtocols()) {
			for (Step s : protocol.getSteps()) {
				output += String.format(
						"public %s %s (%s) throws RMIException {", s
								.getReturnType(), s.getName(), s
								.listParameters(true));

				output += "int offset = " + (s.getNumberOfParameters() * 2 + 3)
						+ ";";

				/* size variables for shorts */
				for (int i = 0; i < s.getNumberOfShorts(); i++) {
					output += "short s" + i + "_size = (short) 2;";
				}
				/* size variables for big ints */
				for (int i = 0; i < s.getNumberOfBigIntegers(); i++) {
					output += "short b" + i + "_size = (b" + i
							+ " != null) ? (short) b" + i
							+ ".toByteArray().length : 0;";
				}
				/* size variables for byte arrays */
				for (int i = 0; i < s.getNumberOfByteArrays(); i++) {
					output += "short ba" + i + "_size = (ba" + i
							+ " != null) ? (short) ba" + i + ".length : 0;";
				}

				/* size variables for BigInteger arrays */
				for (int i = 0; i < s.getNumberOfBigIntegerArrays(); i++) {
					output += "short Ba" + i + "_size = (Ba" + i
							+ " != null) ? Util.BigIntegerArraySize(Ba" + i
							+ ",true) : 0;";
				}

				/*
				 * buffer for all the data to send, number of shorts times two,
				 * plus the sizes of all the big integers
				 */
				output += "byte[] data = new byte[offset + "
						+ (s.getNumberOfShorts() * 2);
				for (int i = 0; i < s.getNumberOfBigIntegers(); i++) {
					output += " + b" + i + "_size";
				}
				for (int i = 0; i < s.getNumberOfByteArrays(); i++) {
					output += " + ba" + i + "_size";
				}
				for (int i = 0; i < s.getNumberOfBigIntegerArrays(); i++) {
					output += " + Ba" + i + "_size";
				}

				output += "];";

				/* the interface number for this protocol */
				output += "data[PROTOCOL] = " + protocol.getProtocolNumber()
						+ ";";

				/* method converted to byte */
				output += "data[STEP] = " + s.getConstName() + ";";
				/* number of parameters */
				output += "data[NUMBER_OF_PARAMETERS] = "
						+ s.getNumberOfParameters() + ";";

				/* store the sizes in the data structure */
				for (Parameter p : s.getParameters()) {
					output += String.format(
							"System.arraycopy(SET_SHORT(%s), 0, data, %s, 2);",
							p.getSizeName(), (p.getAbsPosition() * 2 + 3));
				}

				/* perform the actual copying of the data to the buffer */
				for (Parameter p : s.getParameters()) {
					output += p.codeToByteArray("data", "offset");
				}

				/* send to server */
				output += "SERVER_SEND";
				/* receive from server */
				output += "temp_res = SERVER_RECV";
				/*
				 * the first byte contains the status code, if 0x00 no error has
				 * occurred
				 */
				output += "if (temp_res[0] == (byte)0x00) { ";
				output += "res = new byte[temp_res.length -1];";
				output += "ARRAY_COPY(temp_res, 1, res, 0, res.length);";
				output += "} else {";
				output += "throw new RMIException(temp_res[0]); }";
				/*
				 * convert the byte array to the correct return type based on
				 * the return type of the method
				 */
				output += s.codeReturnTypeInstance();
				/* end of method */
				output += "}";
			}
		}
		/* end of class */
		output += "}";
		return output;
	}

	public String generateMEServerStub(String name) throws Exception {
		String s = generateSEServerStub(name);
		return s;
	}

	public String generateSEServerStub(String name) throws Exception {
		String s = generateServerStub(name);
		s = s.replaceAll("ARRAY_COPY", "System.arraycopy");
		s = s.replaceAll("GET_SHORT", "Util.byteArrayToShort");
		s = s.replaceAll("SET_SHORT", "Util.shortToByteArray");
		return s;
	}

	public String generateJCServerStub(String name) throws Exception {
		String s = generateServerStub(name);
		s = s.replaceAll("ARRAY_COPY\\((.+?),(.+?),(.+?),(.+?),(.+?)\\)",
				"ARRAY_COPY($1,(short)$2,$3,(short)$4,(short)$5)");
		s = s.replaceAll("GET_SHORT\\((.+?),(.+?)\\)",
				"GET_SHORT($1,(short)$2)");
		s = s.replaceAll("ARRAY\\_COPY", "javacard.framework.Util.arrayCopy");
		s = s.replaceAll("GET\\_SHORT", "javacard.framework.Util.getShort");
		s = s.replaceAll("SET\\_SHORT", "javacard.framework.Util.setShort");

		return s;
	}

	public String generateServerStub(String name) throws Exception {
		String output = new String();
		output += "/* *** DO NOT EDIT *** Automatically generated on "
				+ dateTime + " */\n";
		output += "package " + pkg + ";\n";
		output += "import ds.sp.Util;";
		output += "import ds.sp.RMIException;";
		output += "public abstract class " + name + " implements "
				+ Util.joinString(protocolList.toArray(), ",") + "{\n";
		output += "\n/* constant positions of protocol and step in the data stream */\n";
		output += "private final byte PROTOCOL = (byte) 0;";
		output += "private final byte STEP = (byte) 1;";
		output += "private final byte NUMBER_OF_PARAMETERS = (byte) 2;";
		output += "private final byte SIZES_OFFSET = (byte) 3;";
		output += "\n/* step numbers as extracted from interfaces for all the steps, should be called in successive order (you can start at the first step of any protocol though) */\n";
		output += ps.codeStepConstants();

		/* temporary storage for results from methods called in the Server class */
		output += ps.codeReturnTypeStorage();

		output += "private short offset;\n";
		output += "private byte expected_protocol;\n";
		output += "private byte expected_step;\n";
		output += "private byte[] tmp;\n";
		output += "private byte[] result;\n";
		output += "private byte errorCode;\n";
		output += "short sizesCounter;\n";
		output += name + "() { errorCode = (byte) RMIException.NO_ERROR; }";

		/*
		 * include the abstract classes that need to be implemented by the
		 * server
		 */
		for (Protocol prot : ps.getProtocols()) {
			for (Step s : prot.getSteps()) {
				output += "abstract public " + s.getReturnType() + " "
						+ s.getName() + "(" + s.listParameters(true) + ");\n";
			}
		}

		/*
		 * storing the parameters on the server after extracting them from the
		 * byte array
		 */
		output += ps.codeParameterStorage();

		output += "public byte[] getResult() {\n";
		output += "byte[] res;";
		output += "if (errorCode != RMIException.NO_ERROR) { ";
		output += "res = new byte[1];";
		output += "res[0] = errorCode;";
		output += "\n/* this should really move to where the error occurs!!! */\nexpected_protocol = (byte) 0x00; expected_step = (byte) 0x00;";
		output += "return res;\n";
		output += "} else { ";
		output += "if(result != null) {";
		output += "res = new byte[result.length + 1];";
		output += "ARRAY_COPY(result, 0, res, 1, result.length);";
		output += "} else {";
		output += "res = new byte[1]; ";
		output += "}";
		output += "return res;";
		output += "}}\n";

		output += "public void process(byte[] data) {\n";
		output += "errorCode = (byte) 0x00;";
		output += "if(data == null) {";
		output += "errorCode = RMIException.NO_DATA; return; }";

		output += "if(data.length < SIZES_OFFSET) {";
		output += "errorCode = RMIException.BROKEN_HEADER; return; }";

		output += "if(data[PROTOCOL] == (byte)0xff) return;";

		output += "if(expected_protocol == 0) {";
		output += "expected_protocol = data[PROTOCOL];";

		// FIXME: do something when a non existing protocol number is used!
		// for all protocols
		for (Protocol p : ps.getProtocols()) {
			output += "if(data[PROTOCOL] == " + p.getProtocolNumber() + ") {";
			output += "expected_step = " + p.getFirstStepNumber() + ";";
			output += "}else ";
		}
		output += "{ errorCode = RMIException.NO_SUCH_PROTOCOL; return; }";
		output += "}";
		output += "if(expected_protocol != data[PROTOCOL]) {";
		output += "errorCode = RMIException.UNEXPECTED_PROTOCOL; return;";
		output += "}";
		output += "if(expected_step != data[STEP]) {";
		output += "errorCode = RMIException.UNEXPECTED_STEP; return;";
		output += "}";

		output += "switch (data[STEP]) {\n";

		for (Protocol protocol : ps.getProtocols()) {
			for (Step s : protocol.getSteps()) {
				output += "case " + s.getConstName() + ":\n";
				output += "/* Protocol: " + protocol.getProtocolName()
						+ " */\n";
				output += "/* Signature: " + s.getReturnType() + " "
						+ s.getName() + "(" + s.listParameters(true) + ")"
						+ " */\n";
				output += "if(data[NUMBER_OF_PARAMETERS] != (byte)"
						+ s.getNumberOfParameters() + ") {";
				output += "errorCode = RMIException.WRONG_PARAMETER_COUNT; return;";
				output += "}";
				/* look whether the length info exists */
				output += "if(data[NUMBER_OF_PARAMETERS] * 2 + SIZES_OFFSET > data.length) {";
				output += "errorCode = RMIException.MISSING_PARAMETER_SIZES ; return; }";

				/* look whether the total size matches the data packet length */
				if (s.getNumberOfParameters() > 0) {
					output += "sizesCounter = 0;	for(int i=0;i<data[NUMBER_OF_PARAMETERS];i++) {";
					output += "sizesCounter += GET_SHORT(data, SIZES_OFFSET+(2*i));}";
					output += "if(sizesCounter + data[NUMBER_OF_PARAMETERS]*2 + SIZES_OFFSET != data.length)"
							+ "{ errorCode = RMIException.MISSING_PARAMETERS; return; }";
				}
				output += "offset = (short) "
						+ (2 * s.getNumberOfParameters() + 3) + ";\n";

				/* extract the parameters from data array */
				for (Parameter p : s.getParameters()) {
					output += p.codeFromByteArray("data");
				}

				output += s.codeCallMethod();

				/*
				 * if last step of protocol has been executed, reset to
				 * expected_step and expected_protocol to 0
				 */
				output += "expected_step = (byte) "
						+ ((protocol.getLastStepNumber() == s.getStepNumber() + 1) ? 0
								: s.getStepNumber() + 1)
						+ ";"
						+ ((protocol.getLastStepNumber() == s.getStepNumber() + 1) ? "expected_protocol = (byte) 0;"
								: "");

				output += "break;\n";
			}
		}
		output += "default:";
		output += "errorCode = RMIException.NO_SUCH_STEP; return;";
		output += "}}}\n";
		return output;
	}
}
