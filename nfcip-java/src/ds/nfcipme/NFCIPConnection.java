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

package ds.nfcipme;

import java.io.IOException;
import ds.nfcip.NFCIPAbstract;
import ds.nfcip.NFCIPInterface;
import ds.nfcip.NFCIPException;
import ds.nfcip.NFCIPUtils;

public class NFCIPConnection extends NFCIPAbstract implements NFCIPInterface {
	private com.nokia.nfc.p2p.NFCIPConnection c;
	private static final String INITIATOR_URL = "nfc:rf;type=nfcip;mode=initiator";
	private static final String TARGET_URL = "nfc:rf;type=nfcip;mode=target";

	/**
	 * Instantiate a new NFCIPConnection object
	 */
	public NFCIPConnection() {
		super();
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
	protected void setInitiatorMode() {
		this.transmissionMode = SEND;
		try {
			c = (com.nokia.nfc.p2p.NFCIPConnection) javax.microedition.io.Connector
					.open(INITIATOR_URL, -1, true);
			NFCIPUtils.debugMessage(ps, debugLevel, 1, "UID of peer: "
					+ NFCIPUtils.byteArrayToString(c.getUID()));
		} catch (Exception e) {
			setInitiatorMode();
		}
	}

	/**
	 * Set mode TARGET
	 * 
	 * @throws NFCIPException
	 *             if the operation fails
	 */
	protected void setTargetMode() {
		this.transmissionMode = RECEIVE;
		try {
			c = (com.nokia.nfc.p2p.NFCIPConnection) javax.microedition.io.Connector
					.open(TARGET_URL, -1, true);
			NFCIPUtils.debugMessage(ps, debugLevel, 1, "UID of peer: "
					+ NFCIPUtils.byteArrayToString(c.getUID()));
		} catch (Exception e) {
			setTargetMode();
		}
	}

	protected void sendCommand(byte[] data) throws NFCIPException {
		try {
			c.send(data);
		} catch (Exception e) {
			throw new NFCIPException("native send error: " + e.toString());
		}
	}

	protected byte[] receiveCommand() throws NFCIPException {
		try {
			return c.receive();
		} catch (Exception e) {
			throw new NFCIPException("native receive error: " + e.toString());
		}
	}
}