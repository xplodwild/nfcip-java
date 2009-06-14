package ds.nfcip.tests.me;
/*
 * PersistentSettings - Persistent Settings for MIDlets
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

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

public class PersistentSettings {
	private RecordStore rs = null;

	PersistentSettings() {
		try {
			rs = RecordStore.openRecordStore("settings", true);
		} catch (RecordStoreFullException e) {
		} catch (RecordStoreNotFoundException e) {
		} catch (RecordStoreException e) {
		}
	}

	/**
	 * Add a new setting (at the first available position)
	 * 
	 * @param value
	 *            the value of the setting
	 */
	public void addSetting(int value) {
		if (rs == null)
			return;
		byte[] s = String.valueOf(value).getBytes();
		try {
			rs.addRecord(s, 0, s.length);
		} catch (RecordStoreNotOpenException e) {
		} catch (RecordStoreFullException e) {
		} catch (RecordStoreException e) {
		}
	}

	/**
	 * Update an existing setting with a new value
	 * 
	 * @param id
	 *            the position of the setting
	 * @param value
	 *            the value of the setting
	 */
	public void updateSetting(int id, int value) {
		if (rs == null)
			return;
		byte[] s = String.valueOf(value).getBytes();
		try {
			rs.setRecord(id, s, 0, s.length);
		} catch (RecordStoreNotOpenException e) {
		} catch (InvalidRecordIDException e) {
		} catch (RecordStoreFullException e) {
		} catch (RecordStoreException e) {
		}
	}

	/**
	 * Get a setting value
	 * 
	 * @param id
	 *            the position of the setting
	 * @return the value of the setting
	 */
	public int getSetting(int id) {
		if (rs == null)
			return 0;
		try {
			byte[] s = rs.getRecord(id);
			String t = new String(s);
			return Integer.parseInt(t);
		} catch (RecordStoreNotOpenException e) {
			return 0;
		} catch (InvalidRecordIDException e) {
			return 0;
		} catch (RecordStoreException e) {
			return 0;
		}
	}

	/**
	 * Get the number of settings in the store
	 * 
	 * @return the number of settings in the store
	 */
	public int getNumberOfSettings() {
		if (rs == null)
			return 0;
		try {
			return rs.getNumRecords();
		} catch (RecordStoreNotOpenException e) {
			return 0;
		}
	}
}