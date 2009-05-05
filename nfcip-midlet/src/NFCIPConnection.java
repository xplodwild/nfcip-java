/*
 * NFCIPConnection - NFCIP Java ME Library
 * 
 * Copyright (C) 2009  François Kooman <F.Kooman@student.science.ru.nl>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import java.util.Vector;
import java.io.IOException;

public class NFCIPConnection {

	private final byte[] END_BLOCK = { 0x04 };
	private final byte[] EMPTY_BLOCK = { 0x08 };

	public final static int INITIATOR = 0;
	public final static int TARGET = 1;

	private final static int RECEIVE = 0;
	private final static int SEND = 1;

	private com.nokia.nfc.p2p.NFCIPConnection c;
	private static final String INITIATOR_URL = "nfc:rf;type=nfcip;mode=initiator";
	private static final String TARGET_URL = "nfc:rf;type=nfcip;mode=target";

	/**
	 * The debug level
	 */
	private int debugLevel;

	/**
	 * temporary buffer for storing data from send() when in initiator mode
	 */
	private byte[] receiveBuffer;

	/**
	 * The maximum block size to use for individual blocks
	 */
	private int blockSize = 240;

	/**
	 * backup for previously send data in case of connection problem where the
	 * previous data needs to be resent
	 */
	private byte[] oldData = null;

	/**
	 * The block number currently expected
	 */
	private byte expectedBlockNumber = 0;

	/**
	 * The mode of operation of the NFCIPConnection (either INITIATOR or TARGET)
	 */
	protected int mode;

	/**
	 * The expected direction of the transfer (either SEND or RECEIVE)
	 */
	protected int transmissionMode;

	/**
	 * Instantiate a new NFCIPConnection object
	 */
	public NFCIPConnection() {
	}

	/**
	 * Close current connection (release target when in INITIATOR mode)
	 * 
	 * @throws NFCIPException
	 *             if the operation fails
	 */
	public void close() throws NFCIPException {
		try {
			c.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Set mode INITIATOR
	 * 
	 * @throws NFCIPException
	 *             if the operation fails
	 */
	private void setInitiatorMode() throws NFCIPException {
		this.transmissionMode = SEND;
		try {
			c = (com.nokia.nfc.p2p.NFCIPConnection) javax.microedition.io.Connector
					.open(INITIATOR_URL);
		} catch (Exception e) {
		}
	}

	/**
	 * Set mode TARGET
	 * 
	 * @throws NFCIPException
	 *             if the operation fails
	 */
	private void setTargetMode() throws NFCIPException {
		this.transmissionMode = RECEIVE;
		try {
			c = (com.nokia.nfc.p2p.NFCIPConnection) javax.microedition.io.Connector
					.open(TARGET_URL);
		} catch (Exception e) {
		}
	}

	/**
	 * Set the mode of operation (either INITIATOR or TARGET)
	 * 
	 * @param mode
	 *            the mode (INITIATOR or TARGET)
	 * @throws NFCIPException
	 *             if the operation fails
	 */
	public void setMode(int mode) throws NFCIPException {
		this.mode = mode;
		switch (mode) {
		case INITIATOR:
			setInitiatorMode();
			break;
		case TARGET:
			setTargetMode();
			break;
		default:
			throw new NFCIPException("wrong mode specified");
		}
		Util.debugMessage(debugLevel, 1, "UID of peer: "
				+ Util.byteArrayToString(c.getUID()));
	}

	/**
	 * Set block size for data transfer
	 * 
	 * @param bs
	 *            the block size
	 * @throws NFCIPException
	 *             if invalid block size
	 */
	public void setBlockSize(int bs) throws NFCIPException {
		if (blockSize >= 2 && blockSize <= 240) {
			blockSize = bs;
			Util.debugMessage(debugLevel, 1, "Setting Block Size to "
					+ blockSize);
		} else {
			throw new NFCIPException("invalid block size");
		}
	}

	/**
	 * Set debug mode
	 * 
	 * @param b
	 *            the debug level
	 */
	public void setDebugging(int b) {
		debugLevel = b;
	}

	/**
	 * Send arbitrary amount of data
	 * 
	 * @param data
	 *            the data to send
	 * @throws NFCIPException
	 *             if the send operation is called when a receive is expected
	 */
	public void send(byte[] data) throws NFCIPException {
		if (transmissionMode != SEND)
			throw new NFCIPException("expected receive");
		Util.debugMessage(debugLevel, 2, "We want to send: "
				+ Util.byteArrayToString(data));
		if (mode == INITIATOR)
			sendInitiator(data);
		else
			sendTarget(data);
		transmissionMode = RECEIVE;

	}

	private void sendInitiator(byte[] data) throws NFCIPException {
		Vector v = Util.dataToBlockVector(data, blockSize);
		for (int i = 0; i < v.size(); i++) {
			sendBlockInitiator((byte[]) v.elementAt(i));
		}
	}

	private void sendTarget(byte[] data) throws NFCIPException {
		Vector v = Util.dataToBlockVector(data, blockSize);
		for (int i = 0; i < v.size(); i++) {
			sendBlockTarget((byte[]) v.elementAt(i));
		}
		endBlockTarget();
	}

	private void sendBlock(byte[] data) {
		Util.debugMessage(debugLevel, 3, "BLOCK SEND: "
				+ Util.byteArrayToString(data));
		if (mode == INITIATOR)
			sendBlockInitiator(data);
		else
			sendBlockTarget(data);
	}

	private void sendBlockInitiator(byte[] data) {
		try {
			if (Util.isChained(data)) {
				// transmit(IN_DATA_EXCHANGE, data);
				sendCommand(data);
				receiveCommand();
			} else {
				// receiveBuffer = transmit(IN_DATA_EXCHANGE, data);
				sendCommand(data);
				receiveBuffer = receiveCommand();
			}
		} catch (NFCIPException e) {
			resetMode();
			sendBlock(data);
		}
	}

	private void sendBlockTarget(byte[] data) {
		sendBlockTarget(data, true);
	}

	private void sendBlockTarget(byte[] data, boolean backupData) {
		if (backupData)
			oldData = data;
		try {
			// transmit(TG_SET_DATA, data);
			sendCommand(data);
			if (Util.isChained(data)) {
				// byte[] checkForNullBlock = transmit(TG_GET_DATA, null);
				byte[] checkForNullBlock = receiveCommand();
				if (Util.isNullBlock(checkForNullBlock))
					throw new NFCIPException("empty block");
			}
		} catch (NFCIPException e) {
			resetMode();
			receiveBlock();
			sendBlockTarget(data, false);
		}
	}

	/**
	 * Receive arbitrary amount of data
	 * 
	 * @return the data received
	 * @throws NFCIPException
	 *             if the receive operation is called when a send is expected
	 */
	public byte[] receive() throws NFCIPException {
		if (transmissionMode != RECEIVE)
			throw new NFCIPException("expected send");
		expectedBlockNumber = 0;
		byte[] res;
		if (mode == INITIATOR)
			res = receiveInitiator();
		else
			res = receiveTarget();
		transmissionMode = SEND;
		return res;
	}

	private byte[] receiveInitiator() throws NFCIPException {
		Vector responses = new Vector();
		byte[] result = receiveBlock();
		// responses.add(result);
		responses.addElement(result);
		expectedBlockNumber = (byte) ((expectedBlockNumber + 1) % 2);
		while (Util.isChained(result)) {
			result = receiveBlock();
			if (Util.getBlockNumber(result) == expectedBlockNumber) {
				// responses.add(result);
				responses.addElement(result);
				expectedBlockNumber = (byte) ((expectedBlockNumber + 1) % 2);
			} else {
				Util.debugMessage(debugLevel, 2, "unexpected block received");
			}
		}
		endBlockInitiator();
		return Util.blockVectorToData(responses);
	}

	private byte[] receiveTarget() throws NFCIPException {
		Vector responses = new Vector();
		byte[] result = receiveBlock();
		// responses.add(result);
		responses.addElement(result);
		while (Util.isChained(result)) {
			sendBlock(EMPTY_BLOCK);
			expectedBlockNumber = (byte) ((expectedBlockNumber + 1) % 2);
			result = receiveBlock();
			// responses.add(result);
			responses.addElement(result);
		}
		return Util.blockVectorToData(responses);
	}

	private byte[] receiveBlock() {
		byte[] res;
		if (mode == INITIATOR)
			res = receiveBlockInitiator();
		else
			res = receiveBlockTarget();
		Util.debugMessage(debugLevel, 3, "BLOCK RECV: "
				+ Util.byteArrayToString(res));
		return res;
	}

	private byte[] receiveBlockInitiator() {
		byte[] returnBuffer = receiveBuffer;
		if (Util.isChained(returnBuffer)) {
			try {
				// receiveBuffer = transmit(IN_DATA_EXCHANGE, EMPTY_BLOCK);
				sendCommand(EMPTY_BLOCK);
				receiveBuffer = receiveCommand();
			} catch (NFCIPException e) {
				resetMode();
				return receiveBlock();
			}
		}
		return returnBuffer;
	}

	private byte[] receiveBlockTarget() {
		byte[] resultBuffer;
		try {
			// resultBuffer = transmit(TG_GET_DATA, null);
			resultBuffer = receiveCommand();
			if (Util.isNullBlock(resultBuffer))
				throw new NFCIPException("empty block");
		} catch (NFCIPException e) {
			resetMode();
			return receiveBlock();
		}
		if (Util.isEmptyBlock(resultBuffer)) {
			/*
			 * this is from an empty IN_DATA_EXCHANGE request from the initiator
			 * when requesting more data when chaining is triggered
			 */
			return EMPTY_BLOCK;
		} else if (Util.isEndBlock(resultBuffer)) {
			Util.debugMessage(debugLevel, 3, "end block received");
			sendBlock(END_BLOCK);
			return receiveBlock();
		} else if (Util.getBlockNumber(resultBuffer) == expectedBlockNumber) {
			return resultBuffer;
		} else if (resultBuffer != null && resultBuffer.length != 0) {
			Util.debugMessage(debugLevel, 2, "unexpected block received");
			sendBlock(oldData);
			return receiveBlock();
		} else {
			Util.debugMessage(debugLevel, 0,
					"we received an empty message here, impossible");
			return null;
		}
	}

	private void endBlockInitiator() {
		try {
			Util.debugMessage(debugLevel, 3, "sending end block");
			// transmit(IN_DATA_EXCHANGE, END_BLOCK);
			sendCommand(END_BLOCK);
			receiveCommand();
		} catch (NFCIPException e) {
			resetMode();
			endBlockInitiator();
		}
	}

	private void endBlockTarget() {
		byte[] data = null;
		try {
			// data = transmit(TG_GET_DATA, null);
			data = receiveCommand();
			if (Util.isNullBlock(data))
				throw new NFCIPException("empty block");
			if (Util.isEndBlock(data)) {
				// transmit(TG_SET_DATA, data);
				sendCommand(data);
			} else {
				sendBlock(oldData);
				endBlockTarget();
			}
		} catch (NFCIPException e) {
			resetMode();
			endBlockTarget();
		}
	}

	private void resetMode() {
		try {
			close();
			setMode(mode);
		} catch (NFCIPException e) {
		}
	}

	private void sendCommand(byte[] data) throws NFCIPException {
		try {
			c.send(data);
		} catch (Exception e) {
			throw new NFCIPException("native send error");
		}
	}

	private byte[] receiveCommand() throws NFCIPException {
		try {
			return c.receive();
		} catch (Exception e) {
			throw new NFCIPException("native receive error");
		}
	}
}