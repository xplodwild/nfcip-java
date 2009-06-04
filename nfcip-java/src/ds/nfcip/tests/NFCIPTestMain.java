/*
 * NFCIPConnectionTestMain - Test class for NFCIPConnection (Main)
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

package ds.nfcip.tests;

import java.util.List;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import ds.nfcip.NFCIPConnection;
import ds.nfcip.NFCIPException;
import ds.nfcip.Util;

public class NFCIPTestMain {

	private static int minDataLength = 200;
	private static int maxDataLength = 300;
	private static int blockSize = 240;

	public static void main(String[] args) {
		NFCIPTest t;
		int testMode = -1;
		int numberOfRuns = 1;
		int debugLevel = 0; /* we don't print anything by default */
		int terminalNumber = -1;

		/* the card terminals */
		List<CardTerminal> terminals;
		try {
			TerminalFactory factory = TerminalFactory.getDefault();
			terminals = factory.terminals().list();
			if (terminals.size() == 0)
				terminals = null;
		} catch (CardException c) {
			terminals = null;
		}

		/* parse command line arguments */
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--help") || args[i].equals("-h")) {
				showHelp();
				return;
			}

			if (args[i].equals("--initiator") || args[i].equals("-i")) {
				testMode = NFCIPConnection.INITIATOR;
			}

			if (args[i].equals("--target") || args[i].equals("-t")) {
				testMode = NFCIPConnection.TARGET;
			}

			if (args[i].equals("--debug")) {
				i++;
				try {
					debugLevel = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.err
							.println("--debug should be followed by a numerical debugging level");
					return;
				}
			}

			if (args[i].equals("--list-terminals") || args[i].equals("-l")) {
				if (terminals != null) {
					System.out.println("terminals connected to this system:");
					for (int j = 0; j < terminals.size(); j++) {
						System.out.println("[" + j + "] " + terminals.get(j));
					}
					return;
				} else {
					System.err.println("no terminals found");
					return;
				}
			}

			if (args[i].equals("--terminal") || args[i].equals("-d")) {
				i++;
				try {
					terminalNumber = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.out
							.println("--terminal should be followed by a terminal number");
					return;
				}
				if (terminals == null || terminalNumber < 0
						|| terminalNumber >= terminals.size()) {
					System.err.println("specified terminal does not exist");
					return;
				}
			}

			if (args[i].equals("--runs") || args[i].equals("-r")) {
				i++;
				try {
					numberOfRuns = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.out.println("--runs should be followed by a number");
					return;
				}
				if (numberOfRuns <= 0 ) {
					throw new IllegalArgumentException(
							"number of runs should be positive");
				}
			}

			if (args[i].equals("--blocksize") || args[i].equals("-b")) {
				i++;
				try {
					blockSize = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.out
							.println("--blocksize should be followed by a number >= 2");
					return;
				}
				if (blockSize < 2 || blockSize > 240) {
					throw new IllegalArgumentException(
							"block size out of range, 2 <= blocksize <= 240");
				}
			}

			if (args[i].equals("--mindatalength") || args[i].equals("-m")) {
				i++;
				try {
					minDataLength = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.out
							.println("--mindatalength should be followed by a number");
					return;
				}
				if (minDataLength < 0 || minDataLength > 65524) {
					throw new IllegalArgumentException(
							"data length out of range, 0 <= length <= 65224");
				}
			}

			if (args[i].equals("--maxdatalength") || args[i].equals("-M")) {
				i++;
				try {
					maxDataLength = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.out
							.println("--maxdatalength should be followed by a number");
					return;
				}
				if (maxDataLength < 0 || maxDataLength > 65524) {
					throw new IllegalArgumentException(
							"data length out of range, 0 <= length <= 65224");
				}
			}
		}

		if (testMode == -1) {
			System.err
					.println("specify a mode, initiator or target (see --help)");
			System.exit(1);
		}

		if (terminals == null) {
			System.err.println("no terminals found");
			return;
		} else {
			if (terminals.size() == 1) {
				terminalNumber = 0;
			} else if (terminalNumber == -1) {
				System.err
						.println("multiple terminals found, specify a terminal to use (see --help)");
				return;
			}
		}

		NFCIPConnection n = null;
		try {
			n = new NFCIPConnection();
			n.setDebugging(debugLevel);
			n.setBlockSize(blockSize);
			n.setTerminal(terminalNumber);
			Util.debugMessage(debugLevel, 2, "terminal number: "
					+ terminalNumber);

			Util.debugMessage(debugLevel, 2, "mode: "
					+ ((testMode == NFCIPConnection.INITIATOR) ? "INITIATOR"
							: "TARGET"));
			Util.debugMessage(debugLevel, 2, "using minumum data length of "
					+ minDataLength);
			Util.debugMessage(debugLevel, 2, "using maximum data length of "
					+ maxDataLength);
			t = new NFCIPTest(n);

			if (testMode == NFCIPConnection.INITIATOR) {
				t.clientTest(debugLevel, numberOfRuns, minDataLength,
						maxDataLength);
			} else {
				t.serverTest(debugLevel, numberOfRuns, minDataLength,
						maxDataLength);
			}
		} catch (NFCIPException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Print more information on how to run this program
	 */
	private static void showHelp() {
		System.out.println("NFCIPConnection Test");
		System.out.println("");
		System.out.println("Mode:");
		System.out.println("  --initiator [-i]           Set as initiator");
		System.out.println("  --target [-t]              Set as target");

		System.out.println("Options:");
		System.out
				.println("  --list-terminals [-l]      List the connected terminals");
		System.out
				.println("  --terminal i [-d i]        Specify the terminal to use, see --list-terminals");
		System.out
				.println("  --debug i                  Debug level, specify a number between 0 and 5");
		System.out
				.println("  --runs i [-r i]            Number of runs of the test to perform");
		System.out
				.println("  --blocksize i [-b i]       Block size used for transmission");
		System.out
				.println("  --mindatalength i [-m i]   The minumum length of the data to use for testing\n"
						+ "                             transmission, we test from this value to\n"
						+ "                             'maxdatalength' (default: "
						+ minDataLength + ")");
		System.out
				.println("  --maxdatalength i [-M i]   The maximum length of the data to use for testing\n"
						+ "                             transmission, we test from 'mindatalength' to this\n"
						+ "                             value (default: "
						+ maxDataLength + ")");
	}
}
