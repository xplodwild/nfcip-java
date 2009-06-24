/*
 * NFCIPTestMain - Test class for NFCIPConnection
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

package ds.nfcip.tests.se;

import java.io.PrintStream;
import java.util.List;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import ds.nfcip.NFCIPException;
import ds.nfcip.NFCIPInterface;
import ds.nfcip.se.NFCIPConnection;
import ds.nfcip.tests.NFCIPTest;
import ds.nfcip.NFCIPUtils;

public class TestApp {

	private static int minDataLength = 200;
	private static int maxDataLength = 300;

	public static void main(String[] args) {
		NFCIPTest t;
		int testMode = -1;
		int numberOfRuns = 1;
		int logLevel = 0; /* we don't print anything by default */
		int terminalNumber = -1;
		boolean printTiming = false;

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
				testMode = NFCIPInterface.INITIATOR;
			}

			if (args[i].equals("--fake-initiator")) {
				testMode = NFCIPInterface.FAKE_INITIATOR;
			}

			if (args[i].equals("--target") || args[i].equals("-t")) {
				testMode = NFCIPInterface.TARGET;
			}

			if (args[i].equals("--fake-target")) {
				testMode = NFCIPInterface.FAKE_TARGET;
			}

			if (args[i].equals("--log")) {
				i++;
				try {
					logLevel = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.err
							.println("--log should be followed by an integer log level");
					return;
				}
			}

			if (args[i].equals("--time")) {
				printTiming = true;
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
				if (numberOfRuns <= 0) {
					throw new IllegalArgumentException(
							"number of runs should be positive");
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

		PrintStream ps = new PrintStream(System.out);
		NFCIPConnection n = null;
		try {
			n = new NFCIPConnection();
			n.setLogging(ps, logLevel);
			n.setTerminal(terminalNumber);
			n.setMode(testMode);
			System.out.println("terminal number: " + terminalNumber);

			System.out.println("mode: " + NFCIPUtils.modeToString(testMode));
			System.out.println("using minimum data length of " + minDataLength);
			System.out.println("using maximum data length of " + maxDataLength);
			t = new NFCIPTest(n, ps, printTiming);
			t.runTest(numberOfRuns, minDataLength, maxDataLength);
			n.close();
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
		System.out
				.println("  --fake-initiator           Set as \"fake\" initiator");
		System.out
				.println("  --fake-target              Set as \"fake\" target");

		System.out.println("Options:");
		System.out
				.println("  --list-terminals [-l]      List the connected terminals");
		System.out
				.println("  --terminal i [-d i]        Specify the terminal to use, see --list-terminals");
		System.out
				.println("  --log i                    Log level, specify a number between 0 and 5");
		System.out
				.println("  --time                     Enable printing timing information for tests");
		System.out
				.println("  --runs i [-r i]            Number of runs of the test to perform");
		System.out
				.println("  --mindatalength i [-m i]   The minimum length of the data to use for testing\n"
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
