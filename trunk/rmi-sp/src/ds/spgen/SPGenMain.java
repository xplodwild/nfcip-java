package ds.spgen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class SPGenMain {
	private static int protocolNumber = 0;
	private static String packageName = null;
	private static String protocolNames = null;
	private static String outputDirectory = null;

	public static void main(String[] args) {
		parseArguments(args);
		if (protocolNumber == 0 || packageName == null || protocolNames == null
				|| outputDirectory == null) {
			showHelp();
			System.exit(1);
		}

		BufferedWriter outputStream;
		SPGen s;
		try {
			s = new SPGen(protocolNumber, packageName, protocolNames);
			String server = s.generateSEServerStub("RMIServer");
			String client = s.generateNewClientStub("RMIClient");
			String path = outputDirectory + "/" + packageName.replace('.', '/');
			if (!new File(path).exists()) {
				boolean b = new File(path).mkdirs();
				if (!b) {
					System.out.println("Unable to create directory \"" + path
							+ "\"");
					System.exit(1);
				}
			}

			/* RMIServer */
			outputStream = new BufferedWriter(new FileWriter(path
					+ "/RMIServer.java"));
			outputStream.write(server);
			outputStream.close();

			/* RMIClient */
			outputStream = new BufferedWriter(new FileWriter(path
					+ "/RMIClient.java"));
			outputStream.write(client);
			outputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void parseArguments(String[] args) {
		/* parse command line arguments */
		for (int i = 0; i < args.length; i++) {

			if (args[i].equals("--help") || args[i].equals("-h")) {
				showHelp();
				System.exit(0);
			}

			if (args[i].equals("--protocol-number")) {
				i++;
				try {
					protocolNumber = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.err
							.println("--protocol-number should be followed by an integer");
					return;
				}
			}

			if (args[i].equals("--package")) {
				i++;
				packageName = args[i];
			}

			if (args[i].equals("--protocol-names")) {
				i++;
				protocolNames = args[i];
			}

			if (args[i].equals("--output-directory")) {
				i++;
				outputDirectory = args[i];
			}
		}
	}

	private static void showHelp() {
		System.out.println("Security Protocol Stub Generator");
		System.out.println("");
		System.out.println("  --help              Show this help message");
		System.out
				.println("  --protocol-number   Specify unique protocol number");
		System.out.println("  --package           Specify the package name");
		System.out
				.println("  --protocol-names    Specify the names of the protocols within the package");
		System.out
				.println("  --output-directory  Specify the directory where the stubs should be written");
		System.out.println("                      to");

	}
}
