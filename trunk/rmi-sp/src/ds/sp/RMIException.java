package ds.sp;

public class RMIException extends Exception {

	public final static byte NO_ERROR = (byte) 0x00;
	public final static byte NO_DATA = (byte) 0x01;
	public final static byte BROKEN_HEADER = (byte) 0x02;
	public final static byte MISSING_PARAMETER_SIZES = (byte) 0x03;
	public final static byte MISSING_PARAMETERS = (byte) 0x04;
	public final static byte NO_SUCH_PROTOCOL = (byte) 0x05;
	public final static byte NO_SUCH_STEP = (byte) 0x06;
	public final static byte UNEXPECTED_PROTOCOL = (byte) 0x07;
	public final static byte UNEXPECTED_STEP = (byte) 0x08;
	public final static byte WRONG_PARAMETER_COUNT = (byte) 0x09;

	private static final long serialVersionUID = 5021729420063232208L;

	public RMIException(String reason) {
		super(reason);
	}

	public RMIException(byte reason) {
		super(errorToString(reason));
	}

	/**
	 * Convert an error byte reason code to a String reason code
	 * 
	 * @param b
	 *            error byte
	 * @return reason String
	 */
	private static String errorToString(byte b) {
		String reason = "[" + String.valueOf((char) (b + 0x30)) + "] ";
		switch (b) {
		case NO_ERROR:
			reason += "no error";
			break;
		case NO_DATA:
			reason += "no data";
			break;
		case NO_SUCH_STEP:
			reason += "no such step";
			break;
		case UNEXPECTED_STEP:
			reason += "unexpected step";
			break;
		case NO_SUCH_PROTOCOL:
			reason += "no such protocol";
			break;
		case UNEXPECTED_PROTOCOL:
			reason += "unexpected protocol";
			break;
		case WRONG_PARAMETER_COUNT:
			reason += "wrong parameter count";
			break;
		case BROKEN_HEADER:
			reason += "broken header";
			break;
		case MISSING_PARAMETER_SIZES:
			reason += "missing parameter sizes";
			break;
		case MISSING_PARAMETERS:
			reason += "missing parameters";
			break;

		default:
			reason += "unknown error code [" + b + "]";
		}
		return reason;
	}

}
