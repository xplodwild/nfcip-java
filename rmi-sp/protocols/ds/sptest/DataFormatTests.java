package ds.sptest;

import ds.sp.RMIException;
import ds.sp.Util;

/**
 * This class sends raw data to the Server in order to play with the data stream
 * 
 * @author fkooman
 * 
 */
public class DataFormatTests {
	private static Server s;
	private static boolean DEBUG = false;

	public static void main(String[] args) {
		s = new Server();

		testData(1, true, null);
		/* valid protocol, too short */
		testData(2, true, new byte[] { (byte) 0x99 });
		/* valid protocol, valid step, too short */
		testData(3, true, new byte[] { 0x05, 0x0a });
		/* valid protocol, valid step, wrong parameter count */
		testData(4, true, new byte[] { 0x05, 0x0a, 0x00 });
		/* valid protocol, valid step, valid param count, missing param length */
		testData(5, true, new byte[] { 0x05, 0x0a, 0x01 });
		/*
		 * valid protocol, valid step, valid param count, not enough param
		 * length bytes
		 */
		testData(6, true, new byte[] { 0x05, 0x0a, 0x01, 0x00 });
		/*
		 * valid protocol, valid step, valid param count, valid param length
		 * bytes, but no actual param
		 */
		testData(7, true, new byte[] { 0x05, 0x0a, 0x01, 0x00, 0x01 });

		/*
		 * valid protocol, valid step, valid param count, valid param length
		 * bytes, but invalid (not enough) length param as opposed to specified
		 * length
		 */
		testData(8, true,
				new byte[] { 0x05, 0x0a, 0x01, 0x00, 0x05, 0x01, 0x02 });

		/*
		 * valid protocol, valid step, valid param count, valid param length
		 * bytes, but too many parameter bytes as opposed to specified length
		 */
		testData(9, true,
				new byte[] { 0x05, 0x0a, 0x01, 0x00, 0x01, 0x33, 0x44 });

		/*
		 * [VALID] valid protocol, valid step, valid param count, valid param
		 * length bytes, with no actual parameter data (null)
		 */
		testData(10, false, new byte[] { 0x05, 0x0a, 0x01, 0x00, 0x00 });
		/*
		 * [VALID] valid protocol, valid step, valid param count, valid param
		 * length bytes, with param data
		 */
		s = new Server(); /*
						 * need to reinitialize server because otherwise the
						 * step would be wrong
						 */
		testData(11, false, new byte[] { 0x05, 0x0a, 0x01, 0x00, 0x01, 0x33 });
	}

	/**
	 * Sends and receives data to and from the RMIServer and display the result
	 * of the test performed
	 * 
	 * @param testNumber
	 *            number of the test to keep them apart in the on screen display
	 * @param supposedToFail
	 *            whether or not the test is supposed to fail, if it is supposed
	 *            to fail and fails it prints SUCCESS otherwise FAILED, if it is
	 *            not supposed to fail and it fails it prints FAILED otherwise
	 *            SUCCESS
	 * @param data
	 *            the data to test on
	 */
	private static void testData(int testNumber, boolean supposedToFail,
			byte[] data) {
		if (DEBUG) {
			System.out.println("**** TEST ****");
			if (data == null) {
				System.out.println("Sending: <null>");
			} else {
				System.out.println("Sending: " + Util.byteArrayToString(data));
			}
		}
		s.process(data);
		byte[] r = s.getResult();
		if (DEBUG)
			System.out.println("Receiving: " + Util.byteArrayToString(r));
		System.out.print("[" + testNumber + "] ");
		if (supposedToFail) {
			if (r[0] == 0x00) {
				System.out.println("FAILED");
			} else {
				System.out.println("SUCCESS (Exception: "
						+ new RMIException(r[0]) + ")");
			}
		} else {
			if (r[0] == 0x00) {
				System.out.println("SUCCESS");
			} else {
				System.out.println("FAILED (Exception: "
						+ new RMIException(r[0]) + ")");
			}
		}
	}

}
