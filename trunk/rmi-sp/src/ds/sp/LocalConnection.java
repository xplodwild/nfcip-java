package ds.sp;

import java.io.PrintStream;

public class LocalConnection implements RMIConnection {
	IServer server;

	LocalConnection(IServer s) {
		server = s;
	}

	public int getMode() {
		return 0;
	}

	public byte[] receive() {
		return server.getResult();
	}

	public void send(byte[] data) {
		server.process(data);
	}

	public void close() {
	}

	public void setMode(int mode) {
		
	}

	public void setLogging(PrintStream ps, int logLevel) throws RMIException {
		// do nothing for now
	}

	public void setTerminal(int terminalNumber) {
		// N/A
	}
}
