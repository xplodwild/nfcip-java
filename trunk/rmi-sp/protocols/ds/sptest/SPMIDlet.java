package ds.sptest;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import ds.sp.NFCIPConnection;
import ds.sp.RMIConnection;
import ds.sp.RMIException;

public class SPMIDlet extends MIDlet implements Runnable, CommandListener {
	private Display display;
	private Form form;
	private Command exitCmd;
	private RMIConnection c;

	public SPMIDlet() {
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}

	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);
		form = new Form("Test Security Protocol MIDlet");
		exitCmd = new Command("Exit", Command.EXIT, 1);
		form.addCommand(exitCmd);
		form.setCommandListener(this);
		display.setCurrent(form);
		Thread thread = new Thread(this);
		thread.start();
	}

	public void run() {
		try {
			form.append("Almost ready...\n");
			c = new NFCIPConnection(new ds.nfcip.me.NFCIPConnection());
			c.setMode(RMIConnection.SERVER);
			form.append("We are active, waiting for an initiator!\n");
			Server s = new Server();
			int i = 0;
			while (true) {
				form.append("Processing test " + i + "\n");
				byte[] receive = c.receive();
				if (receive != null)
					form.append("Received " + receive.length + " bytes\n");
				long t1 = System.currentTimeMillis();
				s.process(receive);
				long t2 = System.currentTimeMillis();
				form.append("Processing took " + (t2 - t1) + "ms\n");
				byte[] result = s.getResult();
				form.append("Sending " + result.length + " bytes\n");
				c.send(result);
				if (receive[0] == (byte) 0xff && result[0] == (byte) 0x00) {
					/* we want to stop */
					break;
				}
				i++;
			}
			c.close();
			notifyDestroyed();
		} catch (Exception ex) {
			form.append(ex.toString());
			try {
				if (c != null) {
					c.close();
				}
			} catch (RMIException e) {
			}
		}
	}

	public void commandAction(Command c, Displayable arg1) {
		if (c == exitCmd) {
			notifyDestroyed();
		}
	}
}