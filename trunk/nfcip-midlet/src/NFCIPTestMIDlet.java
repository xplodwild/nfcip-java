import java.io.DataOutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class NFCIPTestMIDlet extends MIDlet implements Runnable,
		CommandListener {
	private Display display;
	private Form form;
	private Command exitCmd;

	private int numberOfRuns = 1;
	private int minDataLength = 200;
	private int maxDataLength = 300;

	private int debugLevel = 10;

	private static DataOutputStream dos;

	public NFCIPTestMIDlet() {
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
		} catch (Exception ioe) {
			Util.debugMessage(debugLevel, 1, ioe.toString());
		}
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}

	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);
		form = new Form("NFCIPConnection Test MIDlet");
		exitCmd = new Command("Exit", Command.EXIT, 1);
		form.addCommand(exitCmd);
		form.setCommandListener(this);
		display.setCurrent(form);
		Thread thread = new Thread(this);
		thread.start();
	}

	public void run() {
		try {
			targetMode();
		} catch (NFCIPException e) {
			form.append(e.toString() + "\n");
		}
	}

	public void targetMode() throws NFCIPException {
		long begin, end;
		for (int i = 0; i < numberOfRuns; i++) {
			NFCIPConnection m = new NFCIPConnection();
			m.setMode(NFCIPConnection.TARGET);
			m.setDebugging(debugLevel);
			begin = System.currentTimeMillis();
			float reached = 0;
			try {
				for (int j = minDataLength; j < maxDataLength; j++) {
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

	public void initiatorMode() throws NFCIPException {
		long begin, end;
		for (int i = 0; i < numberOfRuns; i++) {
			NFCIPConnection m = new NFCIPConnection();
			m.setMode(NFCIPConnection.INITIATOR);
			m.setDebugging(debugLevel);
			begin = System.currentTimeMillis();
			float reached = 0;
			try {
				for (int j = minDataLength; j < maxDataLength; j++) {
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

	public void commandAction(Command c, Displayable arg1) {
		if (c == exitCmd) {
			notifyDestroyed();
		}
	}

	public static DataOutputStream getLogStream() {
		return dos;
	}
}