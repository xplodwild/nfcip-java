/*
 * NFCIPInterface - Interface for NFCIP Communication
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

/**
 * Interface that specifies the public accessible methods for the
 * NFCIPConnections.
 * 
 * @author F. Kooman <F.Kooman@student.science.ru.nl>
 * 
 */
public interface NFCIPInterface {

	/**
	 * Initiator mode
	 */
	public final static int INITIATOR = 0;

	/**
	 * Target mode
	 */
	public final static int TARGET = 1;

	/**
	 * Fake initiator mode.
	 * 
	 * This is actually target mode, but acts like an initiator in that it first
	 * wants to send and then receive data.
	 */
	public final static int FAKE_INITIATOR = 2;

	/**
	 * Fake target mode.
	 * 
	 * This is actually initiator mode, but acts like a target in that it first
	 * wants to receive and then send data.
	 */
	public final static int FAKE_TARGET = 3;

	/**
	 * Set the mode of operation (either INITIATOR, TARGET, FAKE_INITIATOR or
	 * FAKE_TARGET)
	 * 
	 * @param mode
	 *            the mode (INITIATOR, TARGET, FAKE_INITIATOR or FAKE_TARGET)
	 * @throws NFCIPException
	 *             if the mode is invalid or setting the mode fails
	 */
	public abstract void setMode(int mode) throws NFCIPException;

	/**
	 * Set the debug level
	 * 
	 * @param p
	 *            the stream to write the logging to (can be System.out)
	 * @param b
	 *            the level on which debug messages become visible in the log (0
	 *            is quiet, 5 is maximum detail)
	 */
	public abstract void setDebugging(PrintStream p, int b);

	/**
	 * Send a message of arbitrary length
	 * 
	 * @param data
	 *            the message to send
	 * @throws NFCIPException
	 *             if the send operation is called while a receive is expected
	 */
	public abstract void send(byte[] data) throws NFCIPException;

	/**
	 * Receive a message of arbitrary length
	 * 
	 * @return the received message
	 * @throws NFCIPException
	 *             if the receive operation is called while a send is expected
	 */
	public abstract byte[] receive() throws NFCIPException;

	/**
	 * Close the connection
	 * 
	 * @throws NFCIPException
	 *             if closing the connection fails
	 */
	public abstract void close() throws NFCIPException;

	/**
	 * Get the current mode of the connection
	 * 
	 * @return the current mode of the connection (INITIATOR, TARGET,
	 *         FAKE_INITIATOR or FAKE_TARGET)
	 * @throws NFCIPException
	 *             if no mode is currently set
	 */
	public abstract int getMode() throws NFCIPException;

	/**
	 * Get the number of resets that were required during the transmission so
	 * far
	 * 
	 * @return the number of resets
	 */
	public abstract int getNumberOfResets();

	/**
	 * Get the number of received messages so far
	 * 
	 * @return the number of received messages
	 */
	public abstract int getNumberOfReceivedMessages();

	/**
	 * Get the number of sent messages so far
	 * 
	 * @return the number of sent messages
	 */
	public abstract int getNumberOfSentMessages();

	/**
	 * Get the number of received blocks so far
	 * 
	 * @return the number of received blocks
	 */
	public abstract int getNumberOfReceivedBlocks();

	/**
	 * Get the number of sent blocks so far
	 * 
	 * @return the number of sent blocks
	 */
	public abstract int getNumberOfSentBlocks();

	/**
	 * Get the number of sent bytes so far
	 * 
	 * @return the number of sent bytes
	 */
	public abstract int getNumberOfSentBytes();

	/**
	 * Get the number of received bytes so far
	 * 
	 * @return the number of received bytes
	 */
	public abstract int getNumberOfReceivedBytes();
}