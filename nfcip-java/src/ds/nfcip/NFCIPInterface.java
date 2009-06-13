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
	 * Set the mode of operation (either INITIATOR or TARGET)
	 * 
	 * @param mode
	 *            the mode (INITIATOR or TARGET)
	 * @throws NFCIPException
	 *             if the operation fails
	 */
	public abstract void setMode(int mode) throws NFCIPException;

	/**
	 * Set block size for data transfer
	 * 
	 * @param bs
	 *            the block size: 2 <= block size <= 240
	 * @throws NFCIPException
	 *             if the block size is invalid
	 */
	public abstract void setBlockSize(int bs) throws NFCIPException;

	/**
	 * Set debug mode
	 * 
	 * @param p
	 *            the stream to write the logging to (can be System.out)
	 * @param b
	 *            the debug level
	 */
	public abstract void setDebugging(PrintStream p, int b);

	/**
	 * Send arbitrary amount of data
	 * 
	 * @param data
	 *            the data to send
	 * @throws NFCIPException
	 *             if the send operation is called while a receive is expected
	 */
	public abstract void send(byte[] data) throws NFCIPException;

	/**
	 * Receive arbitrary amount of data
	 * 
	 * @return the data received
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
	 * @return indicator for initiator or target
	 * @throws NFCIPException
	 *             if no mode currently set
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

	/**
	 * Returns true is the current mode is either initiator or "fake initiator"
	 * 
	 * @return whether or not the mode is initiator or "fake initiator"
	 */
	public boolean isInitiator();

	/**
	 * Returns true is the current mode is either target or "fake target"
	 * 
	 * @return whether or not the mode is target or "fake target"
	 */
	public boolean isTarget();

}