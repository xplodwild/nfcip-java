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

package ds.nfcip.tests;

import java.util.Arrays;

import ds.nfcip.NFCIPConnection;
import ds.nfcip.NFCIPException;
import ds.nfcip.Util;

public class NFCIPTest extends Thread {
	private NFCIPConnection n;

	/**
	 * Instantiate the Test Class
	 * 
	 * @param n
	 *            the opened connection
	 */
	public NFCIPTest(NFCIPConnection n) {
		this.n = n;
	}

	/**
	 * This method tests the INITIATOR mode of NFCIP. It sets the device to
	 * INITIATOR mode and starts sending data to the TARGET
	 * 
	 * @param debugLevel
	 *            the debug level
	 * @param numberOfRuns
	 *            the number of runs of this test
	 * @param minDataLength
	 *            the minimum data length to test
	 * @param maxDataLength
	 *            the maximum data length to test
	 * @throws NFCIPException
	 *             when something in the test fails
	 */
	public void clientTest(int debugLevel, int numberOfRuns, int minDataLength,
			int maxDataLength) throws NFCIPException {
		long begin, end;

		for (int i = 0; i < numberOfRuns; i++) {
			n.setMode(NFCIPConnection.INITIATOR);
			begin = System.nanoTime();
			float reached = 0;
			try {
				for (int j = minDataLength; j < maxDataLength; j++) {
					byte[] data = new byte[j];
					for (int k = 0; k < data.length; k++)
						data[k] = (byte) (255 - k);
					Util.debugMessage(debugLevel, 1, "--> Sending  "
							+ data.length + " bytes");
					n.send(data);
					byte[] r = n.receive();
					Util.debugMessage(debugLevel, 1, "<-- Received "
							+ ((r != null) ? r.length : 0) + " bytes");

					if (!Arrays.equals(data, r)) {
						Util.debugMessage(debugLevel, 1, "We wanted: ("
								+ data.length + ") "
								+ Util.byteArrayToString(data));
						Util.debugMessage(debugLevel, 1, "We got:    ("
								+ ((r != null) ? r.length : 0) + ") "
								+ Util.byteArrayToString(r));
						throw new NFCIPException(
								"received different data from what we sent");
					}
					reached++;
				}
			} catch (NFCIPException e) {
				e.printStackTrace();
				if (n != null) {
					try {
						n.close();
					} catch (NFCIPException e1) {
						e.printStackTrace();
					}
				}
			}
			n.close();
			end = System.nanoTime();
			Util.debugMessage(debugLevel, 1, "Reached "
					+ (reached / (maxDataLength - minDataLength) * 100) + "% ");
			Util.debugMessage(debugLevel, 1, "(took " + ((end - begin) / 10E5)
					+ " ms)");
		}
	}

	/**
	 * This method tests the TARGET mode of NFCIP. It sets the device to TARGET
	 * mode and starts receiving data from the INITIATOR
	 * 
	 * @param debugLevel
	 *            the debug level
	 * @param numberOfRuns
	 *            the number of runs of this test
	 * @param minDataLength
	 *            the minimum data length to test
	 * @param maxDataLength
	 *            the maximum data length to test
	 * @throws NFCIPException
	 *             when something in the test fails
	 */
	public void serverTest(int debugLevel, int numberOfRuns, int minDataLength,
			int maxDataLength) throws NFCIPException {
		long begin, end;
		for (int i = 0; i < numberOfRuns; i++) {
			n.setMode(NFCIPConnection.TARGET);
			begin = System.nanoTime();
			float reached = 0;
			try {
				for (int j = minDataLength; j < maxDataLength; j++) {
					byte[] r = n.receive();
					Util.debugMessage(debugLevel, 1, "<-- Received " + r.length
							+ " bytes");

					byte[] data = new byte[j];
					for (int k = 0; k < data.length; k++)
						data[k] = (byte) (255 - k);
					if (!Arrays.equals(data, r)) {
						Util.debugMessage(debugLevel, 1, "We wanted: ("
								+ data.length + ") "
								+ Util.byteArrayToString(data));
						Util.debugMessage(debugLevel, 1, "We got:    ("
								+ ((r != null) ? r.length : 0) + ") "
								+ Util.byteArrayToString(r));
						throw new NFCIPException(
								"received data we do not expect to receive");
					}
					Util.debugMessage(debugLevel, 1, "--> Sending  "
							+ data.length + " bytes");
					n.send(r);
					reached++;
				}
			} catch (NFCIPException e) {
				e.printStackTrace();
				if (n != null) {
					try {
						n.close();
					} catch (NFCIPException e1) {
						e.printStackTrace();
					}
				}
			}
			n.close();
			end = System.nanoTime();
			Util.debugMessage(debugLevel, 1, "Reached "
					+ (reached / (maxDataLength - minDataLength) * 100) + "% ");
			Util.debugMessage(debugLevel, 1, "(took " + ((end - begin) / 10E5)
					+ " ms)");
		}
	}
}