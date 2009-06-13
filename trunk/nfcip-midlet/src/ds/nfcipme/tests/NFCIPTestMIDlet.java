package ds.nfcipme.tests;

import java.io.PrintStream;
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
import ds.nfcipme.NFCIPConnection;
import ds.nfcip.NFCIPException;
import ds.nfcip.NFCIPInterface;
import ds.nfcip.NFCIPTest;
import ds.nfcip.NFCIPUtils;

public class NFCIPTestMIDlet extends MIDlet implements Runnable,
		CommandListener {
	private Display display;
	private Form form;

	private List menu;
	private List choose;
	private TextField numberOfRunsField;
	private TextField minDataLengthField;
	private TextField maxDataLengthField;
	private TextField blockSizeField;
	private TextField statusField;

	private static final Command backCommand = new Command("Back",
			Command.BACK, 0);
	private static final Command exitCommand = new Command("Exit",
			Command.STOP, 2);
	private String currentMenu;

	/* Settings */
	private int mode;
	private int numberOfRuns;
	private int minDataLength;
	private int maxDataLength;
	private int blockSize;
	private int debugLevel;

	/* Position of settings in RMS */
	private final static int S_MODE = 1;
	private final static int S_NUMBER_OF_RUNS = 2;
	private final static int S_MIN_DATA_LENGTH = 3;
	private final static int S_MAX_DATA_LENGTH = 4;
	private final static int S_BLOCK_SIZE = 5;
	private final static int S_DEBUG_LEVEL = 6;

	PersistentSettings ps = null;

	private static PrintStream printStream = null;
	private NFCIPConnection m = null;

	public NFCIPTestMIDlet() {
		ps = new PersistentSettings();
		if (ps.getNumberOfSettings() == 0) {
			/* first invocation of MIDlet, add default settings */
			ps.addSetting(NFCIPInterface.FAKE_TARGET); /* S_MODE */
			ps.addSetting(1); /* S_NUMBER_OF_RUNS */
			ps.addSetting(200); /* S_MIN_DATA_LENGTH */
			ps.addSetting(300); /* S_MAX_DATA_LENGTH */
			ps.addSetting(240); /* S_BLOCK_SIZE */
			ps.addSetting(0); /* S_DEBUG_LEVEL */
		}
		mode = ps.getSetting(S_MODE);
		numberOfRuns = ps.getSetting(S_NUMBER_OF_RUNS);
		minDataLength = ps.getSetting(S_MIN_DATA_LENGTH);
		maxDataLength = ps.getSetting(S_MAX_DATA_LENGTH);
		blockSize = ps.getSetting(S_BLOCK_SIZE);
		debugLevel = ps.getSetting(S_DEBUG_LEVEL);
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
		menu.append("Set Number Of Runs", null);
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
		choose.append("Fake Initiator", null);
		choose.append("Fake Target", null);
		choose.setSelectedIndex(mode, true);
		display.setCurrent(choose);
		currentMenu = "setMode";
	}

	private void chooseNumberOfRuns() {
		numberOfRunsField = new TextField("Number Of Runs:", Integer
				.toString(numberOfRuns), 3, TextField.DECIMAL);
		form = new Form("Set Number Of Runs");
		form.append(numberOfRunsField);
		form.addCommand(backCommand);
		form.setCommandListener(this);
		display.setCurrent(form);
		currentMenu = "setNumberOfRuns";
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
		TextField t = new TextField("Configuration", "(numberOfRuns = "
				+ numberOfRuns + ", minDataLength = " + minDataLength
				+ ", maxDataLength = " + maxDataLength + ", mode = "
				+ NFCIPUtils.modeToString(mode) + ")", 100, TextField.ANY
				| TextField.UNEDITABLE);
		statusField = new TextField("Status", "Waiting...", 50, TextField.ANY
				| TextField.UNEDITABLE);
		form.append(t);
		form.append(statusField);
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
				printStream = new PrintStream(filecon.openOutputStream());
			} catch (Exception e) {
			}
		}
		try {
			statusField.setString("Waiting...");
			m = new NFCIPConnection();
			m.setDebugging(printStream, debugLevel);
			m.setBlockSize(blockSize);
			m.setMode(mode);
			statusField.setString("Running...");
			NFCIPTest t = new NFCIPTest(m, printStream);
			t.runTest(numberOfRuns, minDataLength, maxDataLength);
			statusField.setString("Finished! (#resets = "
					+ m.getNumberOfResets() + ")");
			m.close();
		} catch (NFCIPException e) {
			statusField.setString("Error: " + e.getMessage());
		}
		if (printStream != null)
			printStream.close();
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
				ps.updateSetting(S_MODE, mode);
			}
			if (currentMenu.equals("setNumberOfRuns")) {
				numberOfRuns = Integer.parseInt(numberOfRunsField.getString());
				ps.updateSetting(S_NUMBER_OF_RUNS, numberOfRuns);
			}
			if (currentMenu.equals("setBlockSize")) {
				blockSize = Integer.parseInt(blockSizeField.getString());
				ps.updateSetting(S_BLOCK_SIZE, blockSize);
			}
			if (currentMenu.equals("setTestRange")) {
				minDataLength = Integer
						.parseInt(minDataLengthField.getString());
				ps.updateSetting(S_MIN_DATA_LENGTH, minDataLength);
				maxDataLength = Integer
						.parseInt(maxDataLengthField.getString());
				ps.updateSetting(S_MAX_DATA_LENGTH, maxDataLength);
			}
			if (currentMenu.equals("setDebugging")) {
				debugLevel = choose.getSelectedIndex();
				ps.updateSetting(S_DEBUG_LEVEL, debugLevel);
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
					|| currentMenu.equals("setNumberOfRuns")
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
				chooseNumberOfRuns();
				break;
			case 2:
				chooseBlockSize();
				break;
			case 3:
				chooseTestRange();
				break;
			case 4:
				chooseDebugging();
				break;
			case 5:
				startConnection();
				break;
			}
		}
	}
}
