package ds.nfcip;

import java.io.PrintStream;

public interface NFCIPInterface {

	/**
	 * Initiator mode
	 */
	public final static int INITIATOR = 0;

	/**
	 * Target mode
	 */
	public final static int TARGET = 1;

	/**
	 * Fake initiator mode.
	 * 
	 * This is actually target mode, but acts like an initiator in that it first
	 * wants to send and then receive data.
	 */
	public final static int FAKE_INITIATOR = 2;

	/**
	 * Fake target mode
	 * 
	 * This is actually initiator mode, but acts like a target in that it first
	 * wants to receive and then send data.
	 */
	public final static int FAKE_TARGET = 3;

	/**
	 * Set the mode of operation (either INITIATOR or TARGET)
	 * 
	 * @param mode
	 *            the mode (INITIATOR or TARGET)
	 * @throws NFCIPException
	 *             if the operation fails
	 */
	public abstract void setMode(int mode) throws NFCIPException;

	/**
	 * Set block size for data transfer
	 * 
	 * @param bs
	 *            the block size
	 * @throws NFCIPException
	 *             if invalid block size
	 */
	public abstract void setBlockSize(int bs) throws NFCIPException;

	/**
	 * Set debug mode
	 * 
	 * @param b
	 *            the debug level
	 */
	public abstract void setDebugging(PrintStream p, int b);

	/**
	 * Send arbitrary amount of data
	 * 
	 * @param data
	 *            the data to send
	 * @throws NFCIPException
	 *             if the send operation is called when a receive is expected
	 */
	public abstract void send(byte[] data) throws NFCIPException;

	/**
	 * Receive arbitrary amount of data
	 * 
	 * @return the data received
	 * @throws NFCIPException
	 *             if the receive operation is called when a send is expected
	 */
	public abstract byte[] receive() throws NFCIPException;

	/**
	 * Close the connection
	 * 
	 * @throws NFCIPException
	 *             if closing the connection fails
	 */
	public abstract void close() throws NFCIPException;

	/**
	 * Get the current mode of the connection
	 * 
	 * @return indicator for initiator or target
	 * @throws NFCIPException
	 *             if no mode currently set
	 */
	public abstract int getMode() throws NFCIPException;

	/**
	 * Gets the number of resets required during the transmission
	 * 
	 * @return the number of resets
	 */
	public abstract int getNumberOfResets();

	public abstract int getNumberOfReceivedBlocks();

	public abstract int getNumberOfSentBlocks();

	public abstract int getNumberOfRawReceivedBlocks();

	public abstract int getNumberOfRawSentBlocks();

	public abstract int getNumberOfSentBytes();

	public abstract int getNumberOfReceivedBytes();

}