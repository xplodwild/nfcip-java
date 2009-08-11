package ds.sptest;

import ds.sp.RMIException;

public interface ByteArrayInterface {

	/**
	 * This method just sends an array to the server
	 * 
	 * @param ba
	 *            the array to send
	 * @throws RMIException
	 */
	void sendBA(byte[] ba) throws RMIException;

	/**
	 * This method just receives an array from the server
	 * 
	 * @return the array
	 * @throws RMIException
	 */
	byte[] receiveBA() throws RMIException;

	/**
	 * This method sends an array and gives the same array back
	 * 
	 * @param ba
	 *            the array to send
	 * @return the received array
	 * @throws RMIException
	 */
	byte[] sendReceiveBA(byte[] ba) throws RMIException;

	/**
	 * This method sends two arrays and sends back the concatenation of them
	 * 
	 * @param ba
	 *            the first array
	 * @param bb
	 *            the second array
	 * @return the concatenation of the first and second array
	 * @throws RMIException
	 */
	byte[] sendTwoReceiveBA(byte[] ba, byte[] bb) throws RMIException;

	/**
	 * This method returns the sum of the two arrays of equal length. It returns
	 * an array of the same length with values of the first and second array
	 * added in the same position in the array
	 * 
	 * @param ba
	 *            the first array
	 * @param bb
	 *            the second array
	 * @return the sum of the two arrays
	 * @throws RMIException
	 */
	byte[] addTwoByteArrays(byte[] ba, byte[] bb) throws RMIException;
}
