/*
 * NFCIPTestMIDlet - Test MIDlet for NFCIPConnection
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

import java.io.DataOutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class NFCIPTestMIDlet extends MIDlet implements Runnable,
		CommandListener {
	private Display display;
	private Form form;

	private List menu;
	private List choose;
	private TextField minDataLengthField;
	private TextField maxDataLengthField;
	private TextField blockSizeField;

	private static final Command backCommand = new Command("Back",
			Command.BACK, 0);
	private static final Command exitCommand = new Command("Exit",
			Command.STOP, 2);
	private String currentMenu;

	public final static int INITIATOR = 0;
	public final static int TARGET = 1;

	private int mode = TARGET;
	private int numberOfRuns = 1;
	private int minDataLength = 200;
	private int maxDataLength = 300;
	private int blockSize = 240;

	private int debugLevel = 0;

	private static DataOutputStream dos = null;
	private NFCIPConnection m = null;

	public NFCIPTestMIDlet() {
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		notifyDestroyed();
	}

	protected void pauseApp() {
		display = null;
		choose = null;
		menu = null;
		form = null;
	}

	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);
		menu = new List("NFCIP MIDlet Parameters", Choice.IMPLICIT);
		menu.append("Set Mode", null);
		menu.append("Set Block Size", null);
		menu.append("Set Test Range", null);
		menu.append("Set Debugging", null);
		menu.append("Start", null);
		menu.addCommand(exitCommand);
		menu.setCommandListener(this);
		mainMenu();
	}

	private void mainMenu() {
		display.setCurrent(menu);
		currentMenu = "main";
	}

	private void chooseMode() {
		choose = new List("Set Mode", Choice.EXCLUSIVE);
		choose.addCommand(backCommand);
		choose.setCommandListener(this);
		choose.append("Initiator", null);
		choose.append("Target", null);
		choose.setSelectedIndex(mode, true);
		display.setCurrent(choose);
		currentMenu = "setMode";
	}

	private void chooseBlockSize() {
		blockSizeField = new TextField("Block Size:", Integer
				.toString(blockSize), 3, TextField.DECIMAL);
		form = new Form("Set Block Size");
		form.append(blockSizeField);
		form.addCommand(backCommand);
		form.setCommandListener(this);
		display.setCurrent(form);
		currentMenu = "setBlockSize";
	}

	private void chooseTestRange() {
		minDataLengthField = new TextField("Minimum Data Length:", Integer
				.toString(minDataLength), 5, TextField.DECIMAL);
		maxDataLengthField = new TextField("Maximum Data Length:", Integer
				.toString(maxDataLength), 5, TextField.DECIMAL);
		form = new Form("Set Test Range");
		form.append(minDataLengthField);
		form.append(maxDataLengthField);
		form.addCommand(backCommand);
		form.setCommandListener(this);
		display.setCurrent(form);
		currentMenu = "setTestRange";
	}

	private void chooseDebugging() {
		choose = new List("Set Debugging Level", Choice.EXCLUSIVE);
		choose.addCommand(backCommand);
		choose.setCommandListener(this);
		choose.append("Disabled", null);
		choose.append("Level 1", null);
		choose.append("Level 2", null);
		choose.append("Level 3", null);
		choose.append("Level 4", null);
		choose.append("Level 5", null);
		choose.setSelectedIndex(debugLevel, true);
		display.setCurrent(choose);
		currentMenu = "setDebugging";
	}

	private void startConnection() {
		form = new Form("NFCIPTest MIDlet");
		form.append("Running...\n");
		form.addCommand(backCommand);
		form.setCommandListener(this);
		display.setCurrent(form);
		currentMenu = "runningTest";
		Thread thread = new Thread(this);
		thread.start();
	}

	public void run() {
		if (debugLevel > 0) {
			try {
				FileConnection filecon = (FileConnection) Connector
						.open("file:///E:/NFCIP-logfile.txt");
				if (!filecon.exists()) {
					filecon.create();
				} else {
					filecon.delete();
					filecon.create();
				}
				dos = filecon.openDataOutputStream();
			} catch (Exception e) {
			}
		}
		try {
			if (mode == INITIATOR) {
				initiatorMode();
			} else {
				targetMode();
			}
		} catch (NFCIPException e) {
		}
	}

	private void targetMode() throws NFCIPException {
		long begin, end;
		for (int i = 0; i < numberOfRuns; i++) {
			m = new NFCIPConnection();
			m.setDebugging(debugLevel);
			m.setMode(NFCIPConnection.TARGET);
			m.setBlockSize(blockSize);
			begin = System.currentTimeMillis();
			float reached = 0;
			try {
				for (int j = minDataLength; j < maxDataLength; j++) {
					form.append(".");
					byte[] r = m.receive();
					Util.debugMessage(debugLevel, 1, "<-- Received "
							+ ((r != null) ? r.length : 0) + " bytes");

					byte[] data = new byte[j];
					for (int k = 0; k < data.length; k++)
						data[k] = (byte) (255 - k);
					if (!Util.arrayCompare(data, r)) {
						Util.debugMessage(debugLevel, 1, "We wanted: ("
								+ data.length + ") "
								+ Util.byteArrayToString(data));
						Util.debugMessage(debugLevel, 1, "We got:    ("
								+ ((r != null) ? r.length : 0) + ") "
								+ Util.byteArrayToString(r));
						throw new NFCIPException(
								"received data we don't expect to receive");
					}
					Util.debugMessage(debugLevel, 1, "--> Sending  "
							+ data.length + " bytes");
					m.send(r);
					reached++;
				}
			} catch (NFCIPException e) {
				Util.debugMessage(debugLevel, 1, e.toString());
				if (m != null) {
					try {
						m.close();
					} catch (NFCIPException e1) {
						Util.debugMessage(debugLevel, 1, e1.toString());
					}
				}
			}
			m.close();
			end = System.currentTimeMillis();
			Util.debugMessage(debugLevel, 1, "Reached "
					+ (reached / (maxDataLength - minDataLength) * 100) + "% ");
			Util.debugMessage(debugLevel, 1, "(took " + (end - begin) + " ms)");
		}
	}

	private void initiatorMode() throws NFCIPException {
		long begin, end;
		for (int i = 0; i < numberOfRuns; i++) {
			m = new NFCIPConnection();
			m.setDebugging(debugLevel);
			m.setMode(NFCIPConnection.INITIATOR);
			m.setBlockSize(blockSize);
			begin = System.currentTimeMillis();
			float reached = 0;
			try {
				for (int j = minDataLength; j < maxDataLength; j++) {
					form.append(".");
					byte[] data = new byte[j];
					for (int k = 0; k < data.length; k++)
						data[k] = (byte) (255 - k);
					Util.debugMessage(debugLevel, 1, "--> Sending  "
							+ data.length + " bytes");
					m.send(data);
					byte[] r = m.receive();
					Util.debugMessage(debugLevel, 1, "<-- Received "
							+ ((r != null) ? r.length : 0) + " bytes");

					if (!Util.arrayCompare(data, r)) {
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
				Util.debugMessage(debugLevel, 1, e.toString());
				if (m != null) {
					try {
						m.close();
					} catch (NFCIPException e1) {
						Util.debugMessage(debugLevel, 1, e1.toString());
					}
				}
			}
			m.close();
			end = System.currentTimeMillis();
			Util.debugMessage(debugLevel, 1, "Reached "
					+ (reached / (maxDataLength - minDataLength) * 100) + "% ");
			Util.debugMessage(debugLevel, 1, "(took " + (end - begin) + " ms)");
		}
	}

	public void commandAction(Command c, Displayable d) {
		String label = c.getLabel();
		if (label.equals("Exit")) {
			try {
				destroyApp(true);
			} catch (MIDletStateChangeException e) {
			}
		} else if (label.equals("Back")) {
			if (currentMenu.equals("setMode")) {
				mode = choose.getSelectedIndex();
			}
			if (currentMenu.equals("setBlockSize")) {
				blockSize = Integer.parseInt(blockSizeField.getString());
			}
			if (currentMenu.equals("setTestRange")) {
				minDataLength = Integer
						.parseInt(minDataLengthField.getString());
				maxDataLength = Integer
						.parseInt(maxDataLengthField.getString());
			}
			if (currentMenu.equals("setDebugging")) {
				debugLevel = choose.getSelectedIndex();
			}
			if (currentMenu.equals("runningTest")) {
				if (m != null) {
					try {
						m.close();
					} catch (NFCIPException e) {
					}
				}
			}
			if (currentMenu.equals("setMode")
					|| currentMenu.equals("setBlockSize")
					|| currentMenu.equals("setTestRange")
					|| currentMenu.equals("runningTest")
					|| currentMenu.equals("setDebugging")) {
				mainMenu();
			}
		} else {
			List down = (List) display.getCurrent();
			switch (down.getSelectedIndex()) {
			case 0:
				chooseMode();
				break;
			case 1:
				chooseBlockSize();
				break;

			case 2:
				chooseTestRange();
				break;

			case 3:
				chooseDebugging();
				break;

			case 4:
				startConnection();
				break;
			}
		}
	}

	public static DataOutputStream getLogStream() {
		return dos;
	}
}