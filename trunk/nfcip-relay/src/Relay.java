/*
 * Relay - Class that implements relaying and replaying of NFCIP communication
 *                     
 * Copyright (C) 2009  François Kooman <fkooman@tuxed.net>
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

/**
 * This class is a NFCIP relay which can be used by the ACS ACR122 firmware 101
 * and 102 (Tikitag). It needs two readers connected to the same computer and of
 * course additional hardware to test with.
 * 
 * The goal is to be a universal NFCIP relay tool, but not every possible
 * combination was tested, and it is not complete yet.
 * 
 * @author F. Kooman <fkooman@tuxed.net>
 * 
 */
public class Relay extends Thread {
	/* PN53x instructions */
	private final byte SET_PARAMETERS = (byte) 0x12;
	private final byte IN_DATA_EXCHANGE = (byte) 0x40;
	private final byte IN_RELEASE = (byte) 0x52;
	private final byte IN_JUMP_FOR_DEP = (byte) 0x56;
	private final byte TG_GET_DATA = (byte) 0x86;
	private final byte TG_INIT_AS_TARGET = (byte) 0x8c;
	private final byte TG_SET_DATA = (byte) 0x8e;
	private final byte TG_SET_META_DATA = (byte) 0x94;

	private final byte[] targetPayload = { (byte) 0x00, (byte) 0x04,
			(byte) 0x00, (byte) 0x00, (byte) 0xb0, (byte) 0x0b, (byte) 0x20,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

	private PrintStream ps;
	private CardChannel relay_target;
	private CardChannel relay_initiator;

	private Trace trace;
	private boolean replay;
	private boolean replayInitiator;

	public Relay() {
		ps = System.out;
		replay = false;
		trace = new Trace();
		replayInitiator = true;
	}

	public void run() {
		try {
			if (replay && replayInitiator) {
				/* we only need 1 reader! */
				relay_initiator = connectToTerminal(setTerminal(0));

				ps.println(Utils.byteArrayToString(trace.getItoT(0)));
				transmit(relay_initiator, IN_JUMP_FOR_DEP, trace.getItoT(0),
						null);
				ps.println(Utils.byteArrayToString(trace.getItoT(1)));
				transmit(relay_initiator, SET_PARAMETERS, trace.getItoT(1),
						null);
				int ti = 2;
				while (ti < trace.sizeItoT()) {
					/*
					 * FIXME: we should store the MI byte in the trace as well
					 * so we can use it here again!
					 */
					byte[] dx = trace.getItoT(ti);
					ps.println(Utils.byteArrayToString(dx));
					transmit(relay_initiator, IN_DATA_EXCHANGE, dx, new byte[2]);
					ti++;
				}
			} else if (replay && !replayInitiator) {
				/* we want to replay the target */
				ps.println("Replaying TARGET");
				/* we only need 1 reader, but we use the same one as before... */
				relay_target = connectToTerminal(setTerminal(1));

				ps.println(Utils.byteArrayToString(trace.getTtoI(0)));
				transmit(relay_target, TG_INIT_AS_TARGET, trace.getTtoI(0),
						null);
				int ti = 2;
				while (ti < trace.sizeTtoI()) {
					/* data get, we don't even look at the response */
					transmit(relay_target, TG_GET_DATA, null, new byte[2]);

					/*
					 * FIXME: we should store the MI byte in the trace as well
					 * so we can use it here again to use TG_SET_META_DATA
					 * instead!
					 */
					byte[] dx = trace.getTtoI(ti);
					ps.println(Utils.byteArrayToString(dx));
					transmit(relay_target, TG_SET_DATA, dx, new byte[2]);
					ti++;
				}
			} else {
				/* make initiator the same device as in replay case above */
				relay_initiator = connectToTerminal(setTerminal(0));
				relay_target = connectToTerminal(setTerminal(1));

				/*
				 * First we initialize the target to wait for an initiator to
				 * relay traffic for. We store the information we got from the
				 * initiator
				 */
				trace.addTtoI(targetPayload);
				byte[] response = transmit(relay_target, TG_INIT_AS_TARGET,
						targetPayload, null);
				/* log dummy, target does not have SET_PARAMETERS */
				trace.addTtoI(new byte[0]);
				/* Determine the mode */
				ps.println("*** Initiated By Initiator ***");
				ps.print("Mode                          : ");
				boolean active = false;
				switch (response[0] & 0x03) {
				case 0x00:
					ps.print("MIFARE");
					active = false;
					break;
				case 0x01:
					ps.print("ACTIVE");
					active = true;
					break;
				case 0x02:
					ps.print("FELICA");
					active = false;
					break;
				}
				if ((response[0] & 0x04) == 0x04)
					ps.print(", DEP");
				else
					ps.print(", NO DEP");
				if ((response[0] & 0x08) == 0x08)
					ps.print(", PICC");
				else
					ps.print(", NO PICC");

				ps.print(", ");
				byte speed = 0;
				switch (response[0] & 0x30) {
				case 0x00:
					ps.println("106kbps");
					speed = 0;
					break;
				case 0x10:
					ps.println("212kbps");
					speed = 1;
					break;
				case 0x20:
					ps.println("424kbps");
					speed = 2;
					break;
				}
				byte[] nfcid3i = new byte[0];
				byte[] generalBytes = new byte[0];
				byte did = 0;
				boolean nad = false;
				if (!(response[2] == (byte) 0xd4 && response[3] == (byte) 0x00)) {
					throw new Exception(
							"We only support ATR_REQ command from initiator at this time");
				} else {
					/* ATR_REQ received (see ECMA-340) */
					nfcid3i = Utils.subByteArray(response, 4, 10);
					ps.println("NFCID3i                       : "
							+ Utils.byteArrayToString(nfcid3i));
					ps.println("DIDi (Device ID Initiator)    : "
							+ response[14]);
					did = response[14];
					ps.println("BSi  (Send Speed Initiator)   : "
							+ response[15]);
					ps.println("BRi  (Receive Speed Initiator): "
							+ response[16]);
					ps.print("PPi  (Protocol Parameters)    : ");
					if ((response[17] & (byte) 0x01) == 0x01) {
						ps.print("NAD");
						nad = true;
					} else {
						ps.print("NO NAD");
						nad = false;
					}
					if ((response[17] & (byte) 0x02) == 0x02)
						ps.println(", General Bytes");
					else
						ps.println(", NO General Bytes??");
					/* the rest is general bytes */
					generalBytes = Utils.subByteArray(response, 18,
							response.length - 18);
					ps.println("Gi   (General Bytes)          : "
							+ Utils.byteArrayToString(generalBytes));
				}

				/*
				 * Now that we found an initiator we will start our own
				 * initiator mode on the other reader and mimic the behavior of
				 * the initiator that activated our target...
				 */

				byte[] initiatorPayload = new byte[3];
				if (active)
					initiatorPayload[0] = 1;
				initiatorPayload[1] = speed;
				/* we always have NFCID3, copy from NFCID we received above */
				/* we may have general bytes */
				initiatorPayload[2] |= 0x02; /* NFCID3i */
				initiatorPayload[2] |= (generalBytes.length > 0) ? 0x04 : 0x00; /* generalBytes */
				initiatorPayload = Utils.appendToByteArray(initiatorPayload,
						Utils.appendToByteArray(nfcid3i, generalBytes));

				// ps.println(Utils.byteArrayToString(initiatorPayload));
				trace.addItoT(initiatorPayload);
				transmit(relay_initiator, IN_JUMP_FOR_DEP, initiatorPayload,
						null);
				/* Now we may, or may not need to set the NAD/DID usage */
				byte param = (byte) ((did > 0) ? 0x02 : 0x00); /* DID used? */
				param |= (byte) ((nad) ? 0x01 : 0x00); /* NAD used? */

				// ps.println("SET_PARAMETERS: " + Utils.byteToString(param));

				trace.addItoT(new byte[] { param });
				transmit(relay_initiator, SET_PARAMETERS, new byte[] { param },
						null);

				boolean mi = false;
				while (true) {
					byte[] statusByte = new byte[2];
					byte[] data = null;
					byte[] data2 = null;
					do {
						data = transmit(relay_target, TG_GET_DATA, null,
								statusByte);
						mi = (statusByte[0] & (byte) 0x40) == (byte) 0x40;
						ps.println("Relaying I->T: "
								+ Utils.byteArrayToString(data));
						if (mi)
							statusByte[1] = 0x01; /* tell transmit to add MI bit */
						else
							statusByte[1] = 0x00;
						trace.addItoT(data);
						data2 = transmit(relay_initiator, IN_DATA_EXCHANGE,
								data, statusByte);
						trace.addTtoI(data2);
					} while (mi);
					do {
						ps.println("Relaying T->I: "
								+ Utils.byteArrayToString(data2));
						mi = (statusByte[0] & (byte) 0x40) == (byte) 0x40;
						if (mi) {
							transmit(relay_target, TG_SET_META_DATA, data2,
									null);
							data2 = transmit(relay_initiator, IN_DATA_EXCHANGE,
									null, statusByte);

						} else {
							transmit(relay_target, TG_SET_DATA, data2, null);
						}
					} while (mi);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private CardChannel connectToTerminal(CardTerminal terminal)
			throws Exception {
		if (terminal == null)
			throw new Exception("need to set terminal device first");
		try {
			if (terminal.isCardPresent()) {
				Card c = terminal.connect("*");
				return c.getBasicChannel();
			} else {
				throw new Exception("unsupported device");
			}
		} catch (CardException e) {
			throw new Exception("problem with connecting to reader");
		}
	}

	public CardTerminal setTerminal(int terminalNumber) throws Exception {
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
			return terminals.get(terminalNumber);
		return null;
	}

	private byte[] transmit(CardChannel ch, byte instr, byte[] payload,
			byte[] statusByte) throws Exception {
		if (ch == null)
			throw new Exception("channel not open");

		// ps.println(instructionToString(instr));

		int payloadLength = (payload != null) ? payload.length : 0;
		byte[] instruction = { (byte) 0xd4, instr };

		/* ACR122 header */
		byte[] header = { (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) (instruction.length + payloadLength) };

		/* construct the command */
		byte[] cmd = Utils.appendToByteArray(header, instruction, 0,
				instruction.length);
		/*
		 * if we are initiator and we want to send data to a target we need to
		 * add the target, either 0x01 or 0x02 because the PN53x supports 2
		 * targets at once. IN_JUMP_FOR_DEP handles only one target, so we use
		 * 0x01 here
		 */
		if (instr == IN_DATA_EXCHANGE) {
			byte targetByte = 0x01;
			if (statusByte[1] == 0x01)
				targetByte |= (byte) 0x40; /* set MI bit */
			cmd = Utils.appendToByteArray(cmd, new byte[] { targetByte });
			/* increase APDU length byte */
			cmd[4]++;
		}

		cmd = Utils.appendToByteArray(cmd, payload);

		try {

			CommandAPDU c = new CommandAPDU(cmd);
			ResponseAPDU r = ch.transmit(c);

			byte[] ra = r.getBytes();

			/* check whether APDU command was accepted by the ACS ACR122 */
			if (r.getSW1() == 0x63 && r.getSW2() == 0x27) {
				throw new CardException(
						"wrong checksum from contactless response (0x63 0x27");
			} else if (r.getSW1() == 0x63 && r.getSW2() == 0x7f) {
				throw new CardException("wrong PN53x command (0x63 0x7f)");
			} else if (r.getSW1() != 0x90 && r.getSW2() != 0x00) {
				throw new CardException("unknown error ("
						+ Utils.byteToString(r.getSW1()) + " "
						+ Utils.byteToString(r.getSW2()));
			}

			/*
			 * some responses to commands have a status field, we check that
			 * here, this applies for TgSetData, TgGetData and InDataExchange
			 * and InRelease.
			 */
			if ((instr == TG_SET_DATA || instr == TG_GET_DATA
					|| instr == IN_DATA_EXCHANGE || instr == IN_RELEASE)
					&& ra[2] != (byte) 0x00 && ra[2] != 0x40) {
				throw new Exception("communication error (0x"
						+ Utils.byteToString(ra[2]) + ")");
			}
			/* strip of the response command codes and the status field */
			ra = Utils.subByteArray(ra, 2, ra.length - 4);

			/*
			 * remove status byte from result, but place it in method parameter
			 */
			if (instr == TG_GET_DATA || instr == IN_DATA_EXCHANGE) {
				statusByte[0] = ra[0];
				ra = Utils.subByteArray(ra, 1, ra.length - 1);
			}
			return ra;
		} catch (CardException e) {
			throw new Exception("problem with transmitting data ("
					+ e.getMessage() + ")");
		}
	}

	public class Cleanup extends Thread {
		public void run() {
			try {
				if (!replay) {
					if (trace.sizeItoT() > 0) {
						System.out.println("Saving trace to file...");
						File f = new File("trace.obj");
						FileOutputStream fos = new FileOutputStream(f);
						ObjectOutputStream oos = new ObjectOutputStream(fos);
						oos.writeObject(trace);
						oos.flush();
						oos.close();
					} else {
						System.out
								.println("No data available in trace, so we won't save anything...");
					}
				}
			} catch (IOException e) {
				System.out.println("Error writing trace!");
			}
		}
	}

	public void setTrace(Trace t) {
		trace = t;
	}

	public void setReplayInitiator() {
		replay = true;
		replayInitiator = true;
	}

	public void setReplayTarget() {
		replay = true;
		replayInitiator = false;
	}

}
