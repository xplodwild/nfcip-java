/*
 * Trace - Class that stores traces.
 *                     
 * Copyright (C) 2009  François Kooman <fkooman@tuxed.net>
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

import java.io.Serializable;
import java.util.ArrayList;

public class Trace implements Serializable {
	private static final long serialVersionUID = 6389544221509736289L;
	/**
	 * Contains the trace of data coming from the original initiator which is
	 * being relayed to the target.
	 */
	private ArrayList<byte[]> traceInitToTarg;
	/**
	 * Contains the trace of data coming back from the original target which is
	 * being relayed to the initiator.
	 */
	private ArrayList<byte[]> traceTargToInit;

	Trace() {
		traceInitToTarg = new ArrayList<byte[]>();
		traceTargToInit = new ArrayList<byte[]>();
	}

	public void addItoT(byte[] data) {
		traceInitToTarg.add(data);
	}

	public void addTtoI(byte[] data) {
		traceTargToInit.add(data);
	}

	public byte[] getItoT(int i) {
		return traceInitToTarg.get(i);
	}

	public byte[] getTtoI(int i) {
		return traceTargToInit.get(i);
	}

	public String toString() {
		String output = "";
		/*
		 * we only show raw data, we don't care about initiator/target
		 * initialization stuff
		 */
		for (int i = 2; i < traceInitToTarg.size(); i++) {
			output += ("[" + i + "] I->T: "
					+ Utils.byteArrayToString(traceInitToTarg.get(i)) + "\n");
			output += ("[" + i + "] T->I: "
					+ Utils.byteArrayToString(traceTargToInit.get(i)) + "\n");
		}
		return output;
	}

	public int sizeItoT() {
		return traceInitToTarg.size();
	}
	
	public int sizeTtoI() {
		return traceTargToInit.size();
	}
}