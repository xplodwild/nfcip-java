/*
 * ContactlessConnection - ContactlessConnection Tests
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

package ds.sp;

import java.util.List;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

public class RunSP {
	/* type of connection */
	private final static int NFC = 0;
	private final static int LOCAL = 1;

	public static void main(String[] args) {
		// ContactlessConnectionTest t;
		int testMode = -1;
		int numberOfRuns = 1;
		int type = -1;
		String terminalNumber = null;
		String protocolSuite = null;

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

			if (args[i].equals("--nfc") || args[i].equals("-n")) {
				type = NFC;
			}

			if (args[i].equals("--local") || args[i].equals("-o")) {
				type = LOCAL;
			}

			if (args[i].equals("--protocol") || args[i].equals("-p")) {
				protocolSuite = args[i + 1];
			}

			if (args[i].equals("--client") || args[i].equals("-c")) {
				testMode = RMIConnection.CLIENT;
			}

			if (args[i].equals("--server") || args[i].equals("-s")) {
				testMode = RMIConnection.SERVER;
			}

			if (args[i].equals("--list-readers") || args[i].equals("-l")) {
				if (terminals != null) {
					System.out.println("Readers connected to this system:");
					for (int j = 0; j < terminals.size(); j++) {
						System.out.println("[" + j + "] " + terminals.get(j));
					}
					return;
				} else {
					System.err.println("no terminals found");
					return;
				}
			}

			if (args[i].equals("--reader") || args[i].equals("-z")) {
				i++;
				int tN;
				try {
					tN = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.out
							.println("--reader should be followed by a terminal number");
					return;
				}
				if (terminals == null || tN < 0 || tN >= terminals.size()) {
					System.err.println("specified reader does not exist");
					return;
				}
				terminalNumber = args[i];
			}

			if (args[i].equals("--runs") || args[i].equals("-r")) {
				i++;
				try {
					numberOfRuns = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.out.println("--runs should be followed by a number");
					return;
				}
				if (numberOfRuns <= 0 || numberOfRuns > 100) {
					throw new IllegalArgumentException(
							"number of runs out of range");
				}
			}
		}

		if (type == -1) {
			System.err.println("specify a type of connection (see --help)");
			System.exit(1);
		}
		if (testMode == -1 && type != LOCAL) {
			System.err.println("specify a mode, client or server (see --help)");
			System.exit(1);
		}
		if (type == NFC && terminalNumber == null) {
			System.err
					.println("specify a NFC reader to use for the connection (see --help)");
			System.exit(1);
		}
		if (protocolSuite == null) {
			System.err.println("specify a protocol to run (see --help)");
			System.exit(1);
		}

		RMIConnection n = null;
		try {
			if (type == NFC) {
				// Util.debugMessage(debugMode, "using NFC");
				System.out.println("NFC!");
				n = new NFCIPConnection(new ds.nfcip.se.NFCIPConnection());
				// n.setMode(RMIConnection.CLIENT);
				n.setTerminal(Integer.parseInt(terminalNumber));
			} else {
				/* local */
				// Util.debugMessage(debugMode, "using local connection");
				String serverClass = protocolSuite + ".Server";
				Class<?> server;
				IServer is = null;
				try {
					// Util.debugMessage(debugMode, "Loading class " +
					// serverClass);
					server = Class.forName(serverClass);
					is = (IServer) server.newInstance();
				} catch (ClassNotFoundException e) {
					System.err.println("Class " + serverClass + " not found");
					System.exit(1);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				n = new LocalConnection(is);
			}
			n.setMode(testMode);
			n.setLogging(System.out, 1);
		} catch (RMIException e) {
			e.printStackTrace();
			System.exit(1);
		}

		String clientClass = protocolSuite + ".Client";
		Class<?> client;
		try {
			System.out.println("Loading class " + clientClass);
			client = Class.forName(clientClass);
			IClient i = (IClient) client.newInstance();
			i.setConnection(n);
			i.runClient();
		} catch (ClassNotFoundException e) {
			System.err.println("Class " + clientClass + " not found");
			System.exit(1);
		} catch (IllegalAccessException e2) {

		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Print more information on how to run this program
	 */
	private static void showHelp() {
		System.out.println("ContactlessConnectionTest");
		System.out.println("");
		System.out.println("Connection type:");
		System.out.println("  --nfc [-n]           Use NFC connection");
		System.out
				.println("  --local [-o]         Use local direct method calls");

		System.out.println("Connection mode:");
		System.out.println("  --initiator [-c]        Test in initiator mode");
		System.out.println("  --target [-s]        Test in target mode");
		System.out.println("NFCIP options:");
		System.out.println("  --list-readers [-l]  List the connected readers");
		System.out
				.println("  --reader i [-z i]    Specify the reader to use, see --list-readers");
		System.out.println("General options:");
		System.out
				.println("  --protocol p [-p p]  Specify the protocol to run by name (i.e. ds.ov2 will run ds.ov2.Client)");
		System.out
				.println("  --debug              Prints the data being sent or received");
		System.out
				.println("  --runs i [-r i]      Number of runs of the test to perform");

	}

}
