/*
 * NFCIPConnection - NFCIP driver for ACS ACR122 devices
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

package ds.nfcip;

import java.util.List;
import java.util.Vector;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

// import java.io.IOException;

public class NFCIPConnection {

	/* PN53x instructions */
	// private final byte GET_GENERAL_STATUS = (byte) 0x04;
	private final byte IN_DATA_EXCHANGE = (byte) 0x40;
	// private final byte IN_LIST_PASSIVE_TARGET = (byte) 0x4a;
	// private final byte IN_ATR = (byte) 0x50;
	private final byte IN_RELEASE = (byte) 0x52;
	private final byte IN_JUMP_FOR_DEP = (byte) 0x56;
	private final byte TG_GET_DATA = (byte) 0x86;
	private final byte TG_INIT_AS_TARGET = (byte) 0x8c;
	private final byte TG_SET_DATA = (byte) 0x8e;
	// private final byte TG_SET_META_DATA = (byte) 0x94;

	private final byte[] END_BLOCK = { 0x04 };
	private final byte[] EMPTY_BLOCK = { 0x08 };

	private final byte[] GET_FIRMWARE_VERSION = { (byte) 0xff, (byte) 0x00,
			(byte) 0x48, (byte) 0x00, (byte) 0x00 };

	public final static int INITIATOR = 0;
	public final static int TARGET = 1;

	private final static int RECEIVE = 0;
	private final static int SEND = 1;

	// private com.nokia.nfc.p2p.NFCIPConnection c;
	// private static final String INITIATOR_URL =
	// "nfc:rf;type=nfcip;mode=initiator";
	// private static final String TARGET_URL = "nfc:rf;type=nfcip;mode=target";

	/**
	 * The debug level
	 */
	private int debugLevel;

	/**
	 * temporary buffer for storing data from send() when in initiator mode
	 */
	private byte[] receiveBuffer;

	/**
	 * temporary buffer for storing data from sendCommand when in initiator mode
	 */
	private byte[] tmpSendStorage;

	private CardTerminal terminal = null;
	private CardChannel ch = null;

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
	private int mode;

	/**
	 * The expected direction of the transfer (either SEND or RECEIVE)
	 */
	private int transmissionMode;

	/**
	 * Counts the number of connection resets required to complete the
	 * transmission
	 */
	private int numberOfResets = 0;

	/**
	 * Instantiate a new NFCIPConnection object
	 */
	public NFCIPConnection() {
	}

	private void connectToTerminal() throws NFCIPException {
		if (terminal == null)
			throw new NFCIPException("need to set terminal device first");
		Card card;
		try {
			if (terminal.isCardPresent()) {
				card = terminal.connect("*");
				ch = card.getBasicChannel();
			} else {
				throw new NFCIPException("unsupported device");
			}
		} catch (CardException e) {
			throw new NFCIPException("problem with connecting to reader");
		}
		Util.debugMessage(debugLevel, 2, "successful connection");
		Util.debugMessage(debugLevel, 2, "ACS ACR122 firmware version: "
				+ getFirmwareVersion());
	}

	/**
	 * Close current connection (release target when in INITIATOR mode)
	 * 
	 * @throws NFCIPException
	 *             if the operation fails
	 */
	public void close() throws NFCIPException {
		if (mode == INITIATOR) {
			/* release all targets */
			transmit(IN_RELEASE, new byte[] { 0x00 });
			/*
			 * sleep after a release of target to turn off the radio for a while
			 * which helps with reconnecting to the phone that needs a little
			 * more time to reset target mode
			 */
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			numberOfResets = 0;
		}
	}

	// public void close() throws NFCIPException {
	// try {
	// c.close();
	// } catch (IOException e) {
	// }
	// }

	/**
	 * Set mode INITIATOR
	 * 
	 * @throws NFCIPException
	 *             if the operation fails
	 */
	private void setInitiatorMode() throws NFCIPException {
		this.transmissionMode = SEND;
		// byte[] initiatorPayload = { 0x00, 0x00, 0x00 }; // passive, 106kbps
		byte[] initiatorPayload = { 0x00, 0x02, 0x01, 0x00, (byte) 0xff,
				(byte) 0xff, 0x00, 0x00 }; // passive, 424kbps
		// byte[] initiatorPayload = { 0x01, 0x00, 0x00 }; // active, 106kbps
		// byte[] initiatorPayload = { 0x01, 0x02, 0x00 }; // active, 424kbps

		transmit(IN_JUMP_FOR_DEP, initiatorPayload);
	}

	// private void setInitiatorMode() throws NFCIPException {
	// this.transmissionMode = SEND;
	// try {
	// c = (com.nokia.nfc.p2p.NFCIPConnection) javax.microedition.io.Connector
	// .open(INITIATOR_URL);
	// } catch (Exception e) {
	// }
	// }

	/**
	 * Set mode TARGET
	 * 
	 * @throws NFCIPException
	 *             if the operation fails
	 */
	private void setTargetMode() throws NFCIPException {
		this.transmissionMode = RECEIVE;
		byte[] targetPayload = { (byte) 0x00, (byte) 0x08, (byte) 0x00,
				(byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x40,
				(byte) 0x01, (byte) 0xFE, (byte) 0xA2, (byte) 0xA3,
				(byte) 0xA4, (byte) 0xA5, (byte) 0xA6, (byte) 0xA7,
				(byte) 0xC0, (byte) 0xC1, (byte) 0xC2, (byte) 0xC3,
				(byte) 0xC4, (byte) 0xC5, (byte) 0xC6, (byte) 0xC7,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xAA, (byte) 0x99,
				(byte) 0x88, (byte) 0x77, (byte) 0x66, (byte) 0x55,
				(byte) 0x44, (byte) 0x33, (byte) 0x22, (byte) 0x11,
				(byte) 0x00, (byte) 0x00 };
		transmit(TG_INIT_AS_TARGET, targetPayload);
	}

	// private void setTargetMode() throws NFCIPException {
	// this.transmissionMode = RECEIVE;
	// try {
	// c = (com.nokia.nfc.p2p.NFCIPConnection) javax.microedition.io.Connector
	// .open(TARGET_URL);
	// } catch (Exception e) {
	// }
	// }

	/**
	 * Set the terminal to use
	 * 
	 * @param terminalNumber
	 *            the terminal to use, specify this as a number, the first
	 *            terminal has number 0
	 */
	public void setTerminal(int terminalNumber) throws NFCIPException {
		List<CardTerminal> terminals;
		try {
			TerminalFactory factory = TerminalFactory.getDefault();
			terminals = factory.terminals().list();
			if (terminals.size() == 0)
				terminals = null;
		} catch (CardException c) {
			terminals = null;
		}
		if (terminals != null && terminalNumber >= 0
				&& terminalNumber < terminals.size())
			terminal = terminals.get(terminalNumber);
		connectToTerminal();
	}

	/**
	 * Sends and receives APDUs to and from the PN53x, handles APDU and NFCIP
	 * data transfer error handling.
	 * 
	 * @param instr
	 *            The PN53x instruction
	 * @param param
	 *            The payload to send
	 * 
	 * @return The response payload (without instruction bytes and status bytes)
	 */
	private byte[] transmit(byte instr, byte[] payload) throws NFCIPException {
		if (ch == null)
			throw new NFCIPException("channel not open");

		Util.debugMessage(debugLevel, 3, instructionToString(instr));

		int payloadLength = (payload != null) ? payload.length : 0;
		byte[] instruction = { (byte) 0xd4, instr };

		/* ACR122 header */
		byte[] header = { (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) (instruction.length + payloadLength) };

		/* construct the command */
		byte[] cmd = Util.appendToByteArray(header, instruction, 0,
				instruction.length);
		/*
		 * if we are initiator and we want to send data to a target we need to
		 * add the target, either 0x01 or 0x02 because the PN53x supports 2
		 * targets at once. IN_JUMP_FOR_DEP handles only one target, so we use
		 * 0x01 here
		 */
		if (instr == IN_DATA_EXCHANGE) {
			cmd = Util.appendToByteArray(cmd, new byte[] { 0x01 }, 0, 1);
			/* increase APDU length byte */
			cmd[4]++;
		}

		cmd = Util.appendToByteArray(cmd, payload);

		try {
			Util.debugMessage(debugLevel, 4, "Sent     (" + cmd.length
					+ " bytes): " + Util.byteArrayToString(cmd));

			CommandAPDU c = new CommandAPDU(cmd);
			ResponseAPDU r = ch.transmit(c);

			byte[] ra = r.getBytes();

			Util.debugMessage(debugLevel, 4, "Received (" + ra.length
					+ " bytes): " + Util.byteArrayToString(ra));

			/* check whether APDU command was accepted by the ACS ACR122 */
			if (r.getSW1() == 0x63 && r.getSW2() == 0x27) {
				throw new CardException(
						"wrong checksum from contactless response (0x63 0x27");
			} else if (r.getSW1() == 0x63 && r.getSW2() == 0x7f) {
				throw new CardException("wrong PN53x command (0x63 0x7f)");
			} else if (r.getSW1() != 0x90 && r.getSW2() != 0x00) {
				throw new CardException("unknown error ("
						+ Util.byteToString(r.getSW1()) + " "
						+ Util.byteToString(r.getSW2()));
			}

			/*
			 * some responses to commands have a status field, we check that
			 * here, this applies for TgSetData, TgGetData and InDataExchange.
			 */
			if ((instr == TG_SET_DATA || instr == TG_GET_DATA || instr == IN_DATA_EXCHANGE)
					&& ra[2] != (byte) 0x00) {
				throw new NFCIPException("communication error ("
						+ Util.byteToString(ra[2]) + ")");
			}
			/* strip of the response command codes and the status field */
			ra = Util.subByteArray(ra, 2, ra.length - 4);

			/*
			 * remove status byte from result as we don't need this for custom
			 * chaining
			 */
			if (instr == TG_GET_DATA || instr == IN_DATA_EXCHANGE) {
				ra = Util.subByteArray(ra, 1, ra.length - 1);
			}
			return ra;
		} catch (CardException e) {
			throw new NFCIPException("problem with transmitting data ("
					+ e.getMessage() + ")");
		}
	}

	/**
	 * Convert an instruction byte to a human readable text
	 * 
	 * @param instr
	 *            the instruction byte
	 * @return the human readable text
	 */
	private String instructionToString(byte instr) {
		switch (instr) {
		case IN_DATA_EXCHANGE:
			return "IN_DATA_EXCHANGE";
		case IN_RELEASE:
			return "IN_RELEASE";
		case IN_JUMP_FOR_DEP:
			return "IN_JUMP_FOR_DEP";
		case TG_GET_DATA:
			return "TG_GET_DATA";
		case TG_INIT_AS_TARGET:
			return "TG_INIT_AS_TARGET";
		case TG_SET_DATA:
			return "TG_SET_DATA";
		default:
			return "UNKNOWN INSTRUCTION (" + Util.byteToString(instr) + ")";
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
		numberOfResets++;
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
		Vector<byte[]> v = Util.dataToBlockVector(data, blockSize);
		for (int i = 0; i < v.size(); i++) {
			sendBlockInitiator(v.elementAt(i));
		}
	}

	private void sendTarget(byte[] data) throws NFCIPException {
		Vector<byte[]> v = Util.dataToBlockVector(data, blockSize);
		for (int i = 0; i < v.size(); i++) {
			sendBlockTarget(v.elementAt(i));
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
		Vector<byte[]> responses = new Vector<byte[]>();
		byte[] result = receiveBlock();
		responses.add(result);
		// responses.addElement(result);
		expectedBlockNumber = (byte) ((expectedBlockNumber + 1) % 2);
		while (Util.isChained(result)) {
			result = receiveBlock();
			if (Util.getBlockNumber(result) == expectedBlockNumber) {
				responses.add(result);
				// responses.addElement(result);
				expectedBlockNumber = (byte) ((expectedBlockNumber + 1) % 2);
			} else {
				Util.debugMessage(debugLevel, 2, "unexpected block received");
			}
		}
		endBlockInitiator();
		return Util.blockVectorToData(responses);
	}

	private byte[] receiveTarget() throws NFCIPException {
		Vector<byte[]> responses = new Vector<byte[]>();
		byte[] result = receiveBlock();
		responses.add(result);
		// responses.addElement(result);
		while (Util.isChained(result)) {
			sendBlock(EMPTY_BLOCK);
			expectedBlockNumber = (byte) ((expectedBlockNumber + 1) % 2);
			result = receiveBlock();
			responses.add(result);
			// responses.addElement(result);
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
		numberOfResets++;
		try {
			close();
			setMode(mode);
		} catch (NFCIPException e) {
		}
	}

	private void sendCommand(byte[] data) throws NFCIPException {
		if (mode == INITIATOR) {
			tmpSendStorage = transmit(IN_DATA_EXCHANGE, data);
		} else {
			transmit(TG_SET_DATA, data);
		}
	}

	private byte[] receiveCommand() throws NFCIPException {
		if (mode == INITIATOR) {
			return tmpSendStorage;
		} else {
			return transmit(TG_GET_DATA, null);
		}
	}

	// private void sendCommand(byte[] data) throws NFCIPException {
	// try {
	// c.send(data);
	// }catch(Exception e){
	// throw new NFCIPException("native send error");
	// }
	// }

	// private byte[] receiveCommand() throws NFCIPException {
	// try {
	// return c.receive();
	// }catch(Exception e){
	// throw new NFCIPException("native receive error");
	// }
	// }

	private String getFirmwareVersion() throws NFCIPException {
		try {
			CommandAPDU c = new CommandAPDU(GET_FIRMWARE_VERSION);
			if (ch == null) {
				throw new NFCIPException("channel not open");
			}
			return new String(ch.transmit(c).getBytes());
		} catch (CardException e) {
			throw new NFCIPException("problem requesting firmware version");
		}
	}

	/**
	 * Gets the number of resets required during the transmission
	 * 
	 * @return the number of resets
	 */
	public int getNumberOfResets() {
		return numberOfResets;
	}
}