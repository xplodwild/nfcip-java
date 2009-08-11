package ds.sp;

import java.io.PrintStream;
import ds.nfcip.NFCIPException;

public class NFCIPConnection implements RMIConnection {
	ds.nfcip.NFCIPInterface c;

	public NFCIPConnection(ds.nfcip.NFCIPInterface ni) {
		c = ni;
	}

	public void close() throws RMIException {
		try {
			c.close();
		} catch (NFCIPException e) {
			throw new RMIException(e.toString());
		}
	}

	public byte[] receive() throws RMIException {
		try {
			return c.receive();
		} catch (NFCIPException e) {
			throw new RMIException(e.toString());
		}
	}

	public void send(byte[] data) throws RMIException {
		try {
			c.send(data);
		} catch (NFCIPException e) {
			throw new RMIException(e.toString());
		}
	}

	public void setMode(int mode) throws RMIException {
		try {
			if (mode == SERVER)
				c.setMode(ds.nfcip.NFCIPInterface.FAKE_TARGET);
			else
				c.setMode(ds.nfcip.NFCIPInterface.FAKE_INITIATOR);
		} catch (NFCIPException e) {
			throw new RMIException(e.toString());
		}
	}

	public void setLogging(PrintStream ps, int logLevel) throws RMIException {
		try {
			c.setLogging(ps, logLevel);
		} catch (NFCIPException e) {
			throw new RMIException(e.toString());
		}
	}

	public int getMode() throws RMIException {
		try {
			return c.getMode();
		} catch (NFCIPException e) {
			throw new RMIException(e.toString());
		}
	}

	public void setTerminal(int terminalNumber) throws RMIException {
		try {
			c.setTerminal(terminalNumber);
		} catch (NFCIPException e) {
			throw new RMIException(e.toString());
		}
	}
}
