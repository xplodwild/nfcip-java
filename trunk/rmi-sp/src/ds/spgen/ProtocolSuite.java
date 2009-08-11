package ds.spgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ProtocolSuite {
	/* we start counting steps from FIRST_STEP_NO */
	private final static byte FIRST_STEP_NO = (byte) 10;

	private String pkg;
	private List<Protocol> protocols;

	/*
	 * save the step during the lifetime of this object as it is used for all
	 * the protocols (interfaces)
	 */
	private byte currentStep;

	ProtocolSuite(String pkg) {
		this.pkg = pkg;
		protocols = new ArrayList<Protocol>();
		currentStep = FIRST_STEP_NO;
	}

	/**
	 * Add a protocol to the Suite
	 * 
	 * @param protocolNumber
	 *            the (artbirary, but unique) number of the protocol
	 * @param protocolName
	 *            the name of the protocol
	 * @throws Exception
	 */
	void addProtocol(byte protocolNumber, String protocolName) throws Exception {
		try {
			Protocol m = new Protocol(protocolNumber, protocolName, currentStep);
			Class<?> c = Class.forName(pkg + "." + protocolName);
			m.addSteps(c.getMethods());
			protocols.add(m);
			currentStep += c.getMethods().length;
		} catch (ClassNotFoundException e) {
			throw new Exception("Class not found, \"" + pkg + "."
					+ protocolName + "\" needs to be in your class path!");
		} catch (SecurityException e) {
			throw new Exception("No permission to load class, \"" + pkg + "."
					+ protocolName + "\"");
		}
	}

	/**
	 * Get all the protocols in the Suite
	 * 
	 * @return the protocols
	 */
	List<Protocol> getProtocols() {
		return protocols;
	}

	/**
	 * Walk through all the protocols (interfaces) and get their method names
	 * and step numbers to create a text listing defining them as constants
	 * 
	 * @return the step listing constants
	 */
	String codeStepConstants() {
		String output = new String();
		for (Protocol p : protocols) {
			output += "\n/* defined step(s) of protocol " + p.getProtocolName()
					+ "*/\n";
			for (Step s : p.getSteps()) {
				output += String.format("private final byte %s = (byte) %s;", s
						.getConstName(), s.getStepNumber());
			}
		}
		return output + "\n";
	}

	/**
	 * The parameters to the methods need to be retrieved from the byte array by
	 * the server, for this to work there need to be variables available to
	 * store this before passing it on to the server RMI implementation.
	 * Complication here is that we don't want to have redundant variables
	 * allocated so we count the occurrence of every type in the step parameters
	 * to see how much we need of each of them maximally
	 * 
	 * @return the temporary storage declarations
	 */
	String codeParameterStorage() {
		String output = new String();

		int no_of_bigints = 0;
		int no_of_shorts = 0;
		int no_of_byte_arrays = 0;
		int no_of_bigint_arrays = 0;

		for (Protocol protocol : protocols) {
			for (Step s : protocol.getSteps()) {
				if (s.getNumberOfBigIntegers() > no_of_bigints)
					no_of_bigints = s.getNumberOfBigIntegers();
				if (s.getNumberOfShorts() > no_of_shorts)
					no_of_shorts = s.getNumberOfShorts();
				if (s.getNumberOfByteArrays() > no_of_byte_arrays)
					no_of_byte_arrays = s.getNumberOfByteArrays();
				if (s.getNumberOfBigIntegerArrays() > no_of_bigint_arrays)
					no_of_bigint_arrays = s.getNumberOfBigIntegerArrays();
			}
		}

		for (int i = 0; i < no_of_bigints; i++) {
			output += "private " + "java.math.BigInteger" + " b" + i + ";\n";
			output += "private short b" + i + "_size;\n";
		}
		for (int i = 0; i < no_of_shorts; i++) {
			output += "private short s" + i + ";\n";
		}
		for (int i = 0; i < no_of_byte_arrays; i++) {
			output += "private " + "byte[]" + " ba" + i + ";\n";
			output += "private short ba" + i + "_size;\n";
		}
		for (int i = 0; i < no_of_bigint_arrays; i++) {
			output += "private " + "java.math.BigInteger[]" + " Ba" + i + ";\n";
			output += "private short Ba" + i + "_size;\n";
		}

		return output;
	}

	/**
	 * This code is used for storing the return values of the server before
	 * converting them to a byte array. We only want to include the return types
	 * that are actually used by the interfaces.
	 * 
	 * It is also used in the RMIClient for storing the data converted from the
	 * byte array before passing them back to the Client class
	 * 
	 * @return the return types actually used by the protocols
	 */
	String codeReturnTypeStorage() {
		Vector<String> returnTypes = new Vector<String>();

		String output = new String();
		for (Protocol protocol : protocols) {
			for (Step s : protocol.getSteps()) {
				String rT = s.getReturnType();
				boolean contains = false;
				for (String i : returnTypes) {
					if (i.equals(rT)) {
						contains = true;
					}
				}
				if (!contains) {
					returnTypes.add(rT);
				}
			}
		}
		/* all the used return types are now stored in the vector */
		for (String i : returnTypes) {
			if (i.equals("void")) {
				continue;
			}
			output += i + " ";
			if (i.equals("java.math.BigInteger")) {
				output += "rb";
			} else if (i.equals("short")) {
				output += "rs";
			} else if (i.equals("byte[]")) {
				output += "rba";
			} else if (i.equals("java.math.BigInteger[]")) {
				output += "rBa";
			} else {
				output += "<BROKEN>";
			}
			output += ";";
		}
		return output;
	}
}
