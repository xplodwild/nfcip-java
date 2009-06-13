package ds.nfcip;

import java.io.PrintStream;
import java.util.Vector;

public abstract class NFCIPAbstract implements NFCIPInterface {
	protected final byte[] END_BLOCK = { 0x04 };
	protected final byte[] EMPTY_BLOCK = { 0x08 };

	protected final static int RECEIVE = 0;
	protected final static int SEND = 1;

	/**
	 * temporary buffer for storing data from send() when in initiator mode
	 */
	private byte[] receiveBuffer;

	/**
	 * backup for previously send data in case of connection problem where the
	 * previous data needs to be resent
	 */
	private byte[] oldData;

	/**
	 * The debug level
	 */
	protected int debugLevel;

	protected PrintStream ps;

	/**
	 * The maximum block size to use for individual blocks
	 */
	protected int blockSize;

	/**
	 * The block number currently expected
	 */
	protected byte expectedBlockNumber;

	/**
	 * The mode of operation of the NFCIPConnection (either INITIATOR or TARGET)
	 */
	protected int mode;

	/**
	 * The expected direction of the transfer (either SEND or RECEIVE)
	 */
	protected int transmissionMode;

	/**
	 * Counts the number of connection resets required to complete the
	 * transmission
	 */
	protected int numberOfResets;

	protected int noOfSentBytes;

	protected int noOfReceivedBytes;

	protected int noOfSentBlocks;

	protected int noOfReceivedBlocks;

	protected int noOfRawSentBlocks;

	protected int noOfRawReceivedBlocks;

	protected NFCIPAbstract() {
		mode = -1;
		oldData = null;
		ps = null;
		blockSize = 240;
		expectedBlockNumber = 0;
		numberOfResets = 0;
		noOfSentBytes = 0;
		noOfReceivedBytes = 0;
		noOfSentBlocks = 0;
		noOfReceivedBlocks = 0;
		noOfRawSentBlocks = 0;
		noOfRawReceivedBlocks = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ds.nfcip.NFCIPInterface#setMode(int)
	 */
	public void setMode(int mode) throws NFCIPException {
		this.mode = mode;
		NFCIPUtils.debugMessage(ps, debugLevel, 1, "Setting mode: "
				+ NFCIPUtils.modeToString(mode));
		switch (mode) {
		case INITIATOR:
			setInitiatorMode();
			break;
		case TARGET:
			setTargetMode();
			break;
		case FAKE_INITIATOR:
			setTargetMode();
			byte[] recv = receiveCommand();
			if (recv == null || recv[0] != (byte) 0x88)
				throw new NFCIPException("problem with fake initiator");
			transmissionMode = SEND;
			break;
		case FAKE_TARGET:
			setInitiatorMode();
			sendCommand(new byte[] { (byte) 0x88 });
			transmissionMode = RECEIVE;
			break;
		default:
			throw new NFCIPException("wrong mode specified");
		}
	}

	public int getMode() throws NFCIPException {
		if (mode < 0)
			throw new NFCIPException("no mode selected");
		return mode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ds.nfcip.NFCIPInterface#setBlockSize(int)
	 */
	public void setBlockSize(int bs) throws NFCIPException {
		if (blockSize >= 2 && blockSize <= 240) {
			blockSize = bs;
			NFCIPUtils.debugMessage(ps, debugLevel, 1, "Setting Block Size to "
					+ blockSize);
		} else {
			throw new NFCIPException("invalid block size");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ds.nfcip.NFCIPInterface#setDebugging(java.io.PrintStream, int)
	 */
	public void setDebugging(PrintStream p, int b) {
		debugLevel = b;
		ps = p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ds.nfcip.NFCIPInterface#send(byte[])
	 */
	public void send(byte[] data) throws NFCIPException {
		if (transmissionMode != SEND)
			throw new NFCIPException("expected receive");
		noOfSentBytes += data != null ? data.length : 0;
		noOfSentBlocks++;
		if (mode == INITIATOR || mode == FAKE_INITIATOR)
			sendInitiator(data);
		else
			sendTarget(data);
		transmissionMode = RECEIVE;

	}

	private void sendInitiator(byte[] data) {
		Vector v = NFCIPUtils.dataToBlockVector(data, blockSize);
		for (int i = 0; i < v.size(); i++) {
			sendBlock((byte[]) v.elementAt(i));
		}
	}

	private void sendTarget(byte[] data) {
		Vector v = NFCIPUtils.dataToBlockVector(data, blockSize);
		for (int i = 0; i < v.size(); i++) {
			sendBlock((byte[]) v.elementAt(i));
		}
		endBlockTarget();
	}

	private void sendBlock(byte[] data) {
		noOfRawSentBlocks++;
		if (mode == INITIATOR || mode == FAKE_INITIATOR)
			sendBlockInitiator(data);
		else
			sendBlockTarget(data);
	}

	private void sendBlockInitiator(byte[] data) {
		try {
			if (NFCIPUtils.isChained(data)) {
				sendCommand(data);
				receiveCommand();
			} else {
				sendCommand(data);
				receiveBuffer = receiveCommand();
			}
		} catch (NFCIPException e) {
			NFCIPUtils.debugMessage(ps, debugLevel, 1, e.getMessage());
			resetMode();
			sendBlockInitiator(data);
		}
	}

	private void sendBlockTarget(byte[] data) {
		sendBlockTarget(data, true);
	}

	private void sendBlockTarget(byte[] data, boolean backupData) {
		if (backupData)
			oldData = data;
		try {
			sendCommand(data);
			if (NFCIPUtils.isChained(data)) {
				byte[] checkForNullBlock = receiveCommand();
				if (NFCIPUtils.isNullBlock(checkForNullBlock))
					throw new NFCIPException("empty block");
			}
		} catch (NFCIPException e) {
			NFCIPUtils.debugMessage(ps, debugLevel, 1, e.getMessage());
			resetMode();
			receiveBlockTarget();
			sendBlockTarget(data, false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ds.nfcip.NFCIPInterface#receive()
	 */
	public byte[] receive() throws NFCIPException {
		if (transmissionMode != RECEIVE)
			throw new NFCIPException("expected send");
		expectedBlockNumber = 0;
		byte[] res;
		if (mode == INITIATOR || mode == FAKE_INITIATOR)
			res = receiveInitiator();
		else
			res = receiveTarget();
		transmissionMode = SEND;
		noOfReceivedBlocks++;
		noOfReceivedBytes += res != null ? res.length : 0;
		return res;
	}

	private byte[] receiveInitiator() {
		Vector responses = new Vector();
		byte[] result = receiveBlock();
		responses.addElement(result);
		expectedBlockNumber = (byte) ((expectedBlockNumber + 1) % 2);
		while (NFCIPUtils.isChained(result)) {
			result = receiveBlock();
			if (NFCIPUtils.getBlockNumber(result) == expectedBlockNumber) {
				responses.addElement(result);
				expectedBlockNumber = (byte) ((expectedBlockNumber + 1) % 2);
			} else {
				NFCIPUtils.debugMessage(ps, debugLevel, 2,
						"unexpected block received");
			}
		}
		endBlockInitiator();
		return NFCIPUtils.blockVectorToData(responses);
	}

	private byte[] receiveTarget() {
		Vector responses = new Vector();
		byte[] result = receiveBlock();
		responses.addElement(result);
		while (NFCIPUtils.isChained(result)) {
			sendBlock(EMPTY_BLOCK);
			expectedBlockNumber = (byte) ((expectedBlockNumber + 1) % 2);
			result = receiveBlock();
			responses.addElement(result);
		}
		return NFCIPUtils.blockVectorToData(responses);
	}

	private byte[] receiveBlock() {
		byte[] res;
		if (mode == INITIATOR || mode == FAKE_INITIATOR)
			res = receiveBlockInitiator();
		else
			res = receiveBlockTarget();
		NFCIPUtils.debugMessage(ps, debugLevel, 3, "receiveBlock: "
				+ NFCIPUtils.byteArrayToString(res));
		noOfRawReceivedBlocks += 1;
		return res;
	}

	private byte[] receiveBlockInitiator() {
		byte[] returnBuffer = receiveBuffer;
		if (NFCIPUtils.isChained(returnBuffer)) {
			try {
				sendCommand(EMPTY_BLOCK);
				receiveBuffer = receiveCommand();
			} catch (NFCIPException e) {
				NFCIPUtils.debugMessage(ps, debugLevel, 1, e.getMessage());
				resetMode();
				return receiveBlockInitiator();
			}
		}
		return returnBuffer;
	}

	private byte[] receiveBlockTarget() {
		byte[] resultBuffer;
		try {
			resultBuffer = receiveCommand();
			if (NFCIPUtils.isNullBlock(resultBuffer))
				throw new NFCIPException("empty block");
		} catch (NFCIPException e) {
			NFCIPUtils.debugMessage(ps, debugLevel, 1, e.getMessage());
			resetMode();
			return receiveBlockTarget();
		}
		if (NFCIPUtils.isEmptyBlock(resultBuffer)) {
			/*
			 * this is from an empty IN_DATA_EXCHANGE request from the initiator
			 * when requesting more data when chaining is triggered
			 */
			return EMPTY_BLOCK;
		} else if (NFCIPUtils.isEndBlock(resultBuffer)) {
			NFCIPUtils.debugMessage(ps, debugLevel, 3, "end block received");
			sendBlock(END_BLOCK);
			return receiveBlockTarget();
		} else if (NFCIPUtils.getBlockNumber(resultBuffer) == expectedBlockNumber) {
			return resultBuffer;
		} else if (resultBuffer != null && resultBuffer.length != 0) {
			NFCIPUtils.debugMessage(ps, debugLevel, 2,
					"unexpected block received");
			sendBlock(oldData);
			return receiveBlockTarget();
		} else {
			NFCIPUtils.debugMessage(ps, debugLevel, 0,
					"we received an empty message here, impossible");
			return null;
		}
	}

	private void endBlockInitiator() {
		try {
			NFCIPUtils.debugMessage(ps, debugLevel, 3, "sending end block");
			sendCommand(END_BLOCK);
			receiveCommand();
		} catch (NFCIPException e) {
			NFCIPUtils.debugMessage(ps, debugLevel, 1, e.getMessage());
			resetMode();
			endBlockInitiator();
		}
	}

	private void endBlockTarget() {
		byte[] data = null;
		try {
			data = receiveCommand();
			if (NFCIPUtils.isNullBlock(data))
				throw new NFCIPException("empty block");
			if (NFCIPUtils.isEndBlock(data)) {
				sendCommand(data);
			} else {
				sendBlock(oldData);
				endBlockTarget();
			}
		} catch (NFCIPException e) {
			NFCIPUtils.debugMessage(ps, debugLevel, 1, e.getMessage());
			resetMode();
			endBlockTarget();
		}
	}

	private void resetMode() {
		NFCIPUtils.debugMessage(ps, debugLevel, 2, "Resetting connection...");
		numberOfResets++;
		try {
			releaseTargets();
			setMode(mode);
		} catch (NFCIPException e) {
			NFCIPUtils.debugMessage(ps, debugLevel, 1, e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ds.nfcip.NFCIPInterface#getNumberOfResets()
	 */
	public int getNumberOfResets() {
		return numberOfResets;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ds.nfcip.NFCIPInterface#getNumberOfReceivedBlocks()
	 */
	public int getNumberOfReceivedBlocks() {
		return noOfReceivedBlocks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ds.nfcip.NFCIPInterface#getNumberOfSentBlocks()
	 */
	public int getNumberOfSentBlocks() {
		return noOfSentBlocks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ds.nfcip.NFCIPInterface#getNumberOfRawReceivedBlocks()
	 */
	public int getNumberOfRawReceivedBlocks() {
		return noOfRawReceivedBlocks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ds.nfcip.NFCIPInterface#getNumberOfRawSentBlocks()
	 */
	public int getNumberOfRawSentBlocks() {
		return noOfRawSentBlocks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ds.nfcip.NFCIPInterface#getNumberOfSentBytes()
	 */
	public int getNumberOfSentBytes() {
		return noOfSentBytes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ds.nfcip.NFCIPInterface#getNumberOfReceivedBytes()
	 */
	public int getNumberOfReceivedBytes() {
		return noOfReceivedBytes;
	}

	protected abstract void sendCommand(byte[] data) throws NFCIPException;

	protected abstract byte[] receiveCommand() throws NFCIPException;

	protected abstract void setInitiatorMode() throws NFCIPException;

	protected abstract void setTargetMode() throws NFCIPException;

	protected abstract void releaseTargets() throws NFCIPException;
}
