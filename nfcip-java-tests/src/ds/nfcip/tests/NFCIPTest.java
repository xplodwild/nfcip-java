/*
 * NFCIPTest - Test class for NFCIPConnection
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

import java.io.PrintStream;
import java.util.Vector;
import ds.nfcip.NFCIPException;
import ds.nfcip.NFCIPInterface;
import ds.nfcip.NFCIPUtils;

/**
 * Test Class for NFCIPConnection
 * 
 * @author F. Kooman <F.Kooman@student.science.ru.nl>
 * 
 */
public class NFCIPTest extends Thread {
	private NFCIPInterface n;
	private PrintStream ps;

	/**
	 * Instantiate the NFCIPConnection Test Class
	 * 
	 * @param connection
	 *            the open NFCIPConnection
	 * @param p
	 * @param printTiming
	 *            whether or not to print timing information of the performed
	 *            tests
	 */
	public NFCIPTest(NFCIPInterface connection, PrintStream p,
			boolean printTiming) {
		this.n = connection;
		ps = p;
	}

	public void runTest(int numberOfRuns, int minDataLength, int maxDataLength)
			throws NFCIPException {
		/* contains the results for the tests within one run */
		Vector timingResultsTests = new Vector();

		long testBegin, testEnd;
		byte[] received;
		printMessage("### Starting test: numberOfRuns = " + numberOfRuns
				+ ", minDataLength = " + minDataLength + ", maxDataLength = "
				+ maxDataLength);
		for (int runNumber = 1; runNumber <= numberOfRuns; runNumber++) {
			printMessage("### Starting run " + runNumber);
			for (int testNumber = minDataLength; testNumber <= maxDataLength; testNumber++) {
				testBegin = System.currentTimeMillis();

				/*
				 * initiator will send this data and verify response, target
				 * will receive this data and verify response
				 */
				byte[] data = new byte[testNumber];
				for (int k = 0; k < data.length; k++)
					data[k] = (byte) (255 - k);

				/*
				 * the initiator will send data and then receive data, the
				 * target will just receive data at this point, the response
				 * will be sent only after comparison
				 */
				if (n.getMode() == NFCIPInterface.INITIATOR
						|| n.getMode() == NFCIPInterface.FAKE_INITIATOR) {
					printMessage("### --> Sending  " + data.length + " bytes");
					n.send(data);
				}
				received = n.receive();

				printMessage("### <-- Received " + received.length + " bytes");

				/*
				 * we verify the received data with the data we expected (in
				 * case of target) or data we sent (in case of initiator)
				 */
				if (!NFCIPUtils.arrayCompare(data, received)) {
					printMessage("we wanted: (" + data.length + " bytes) "
							+ NFCIPUtils.byteArrayToString(data));
					printMessage("we got:    (" + received.length + " bytes) "
							+ NFCIPUtils.byteArrayToString(received));
					throw new NFCIPException("unexpected data received");
				}

				/*
				 * if we are in target mode we have to send back the data we
				 * received
				 */
				if (n.getMode() == NFCIPInterface.TARGET
						|| n.getMode() == NFCIPInterface.FAKE_TARGET) {
					printMessage("### --> Sending  " + data.length + " bytes");
					n.send(received);
				}
				testEnd = System.currentTimeMillis();
				Measurement m = new Measurement(runNumber, testEnd - testBegin,
						n.getNumberOfResets());
				timingResultsTests.addElement(m);
			}
		}
		printMessage("### === END OF TEST ===");
		printMessage("### #messages sent     = " + n.getNumberOfSentMessages());
		printMessage("### #messages received = "
				+ n.getNumberOfReceivedMessages());
		printMessage("### #blocks sent       = " + n.getNumberOfSentBlocks());
		printMessage("### #blocks received   = "
				+ n.getNumberOfReceivedBlocks());
		printMessage("### #bytes sent        = " + n.getNumberOfSentBytes());
		printMessage("### #bytes received    = " + n.getNumberOfReceivedBytes());
		printMessage("### #resets            = " + n.getNumberOfResets());

		float packetLoss = (float) n.getNumberOfResets()
				/ (n.getNumberOfSentMessages() + n
						.getNumberOfReceivedMessages()) * 100;
		printMessage("### packet loss        = " + packetLoss + "%");
		printMessage("### \trun\ttime(ms)\t#numberOfResets");
		for (int i = 0; i < timingResultsTests.size(); i++) {
			Measurement m = (Measurement) timingResultsTests.elementAt(i);
			printMessage("\t" + m.runNumber + "\t" + m.elapsedTime + "\t\t"
					+ m.numberOfResets);
		}
	}

	private void printMessage(String m) {
		if (ps != null)
			ps.println(m);
	}

	private class Measurement {
		public int runNumber;
		public long elapsedTime;
		public int numberOfResets;

		public Measurement(int run, long time, int resets) {
			runNumber = run;
			elapsedTime = time;
			numberOfResets = resets;
		}
	}

}