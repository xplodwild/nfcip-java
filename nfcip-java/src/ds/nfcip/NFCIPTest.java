/*
 * NFCIPConnectionTest - Test class for NFCIPConnection
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

import java.io.PrintStream;
import java.util.Vector;

public class NFCIPTest extends Thread {
	private NFCIPInterface n;
	private PrintStream ps;
	private int debugLevel;

	/**
	 * Instantiate the Test Class
	 * 
	 * @param n
	 *            the opened connection
	 */
	public NFCIPTest(NFCIPInterface n, PrintStream p, int d) {
		this.n = n;
		ps = p;
		debugLevel = d;
	}

	public void runTest(int numberOfRuns, int minDataLength, int maxDataLength)
			throws NFCIPException {
		Vector timingResults = new Vector();
		long begin, end;
		byte[] received;
		NFCIPUtils.debugMessage(ps, debugLevel, 1,
				"Starting test: numberOfRuns = " + numberOfRuns
						+ ", minDataLength = " + minDataLength
						+ ", maxDataLength = " + maxDataLength);
		for (int i = 0; i < numberOfRuns; i++) {
			begin = System.currentTimeMillis();
			for (int j = minDataLength; j <= maxDataLength; j++) {
				/*
				 * initiator will send this data and verify response, target
				 * will receive this data and verify response
				 */
				byte[] data = new byte[j];
				for (int k = 0; k < data.length; k++)
					data[k] = (byte) (255 - k);

				/*
				 * the initiator will send data and then receive data, the
				 * target will just receive data at this point, the response
				 * will be sent only after comparison
				 */
				if (n.getMode() == NFCIPInterface.INITIATOR
						|| n.getMode() == NFCIPInterface.FAKE_INITIATOR) {
					NFCIPUtils.debugMessage(ps, debugLevel, 1, "--> Sending  "
							+ data.length + " bytes");
					n.send(data);
				}
				received = n.receive();

				NFCIPUtils.debugMessage(ps, debugLevel, 1, "<-- Received "
						+ received.length + " bytes");

				/*
				 * we verify the received data with the data we expected (in
				 * case of target) or data we sent (in case of initiator)
				 */
				if (!NFCIPUtils.arrayCompare(data, received)) {
					NFCIPUtils.debugMessage(ps, debugLevel, 0, "we wanted: ("
							+ data.length + " bytes) "
							+ NFCIPUtils.byteArrayToString(data));
					NFCIPUtils.debugMessage(ps, debugLevel, 0, "we got:    ("
							+ received.length + " bytes) "
							+ NFCIPUtils.byteArrayToString(received));
					throw new NFCIPException("unexpected data received");
				}

				/*
				 * if we are in target mode we have to send back the data we
				 * received
				 */
				if (n.getMode() == NFCIPInterface.TARGET
						|| n.getMode() == NFCIPInterface.FAKE_TARGET) {
					NFCIPUtils.debugMessage(ps, debugLevel, 1, "--> Sending  "
							+ data.length + " bytes");
					n.send(received);
				}
			}
			end = System.currentTimeMillis();
			timingResults.addElement(new Long(end - begin));
		}
		if (ps != null) {
			ps.println("=== END OF TEST ===");
			ps.println("#messages sent     = " + n.getNumberOfSentMessages());
			ps.println("#messages received = "
					+ n.getNumberOfReceivedMessages());
			ps.println("#blocks sent       = " + n.getNumberOfSentBlocks());
			ps.println("#blocks received   = " + n.getNumberOfReceivedBlocks());
			ps.println("#bytes sent        = " + n.getNumberOfSentBytes());
			ps.println("#bytes received    = " + n.getNumberOfReceivedBytes());
			ps.println("#resets            = " + n.getNumberOfResets());

			float packetLoss = (float) n.getNumberOfResets()
					/ (n.getNumberOfSentMessages() + n
							.getNumberOfReceivedMessages()) * 100;
			ps.println("packet loss        = " + packetLoss + "%");
			ps.println("run #\ttime(ms)");
			for (int i = 1; i <= numberOfRuns; i++) {
				ps.println(i + "\t" + timingResults.elementAt(i - 1));
			}
			ps.flush();
		}
	}
}