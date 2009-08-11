package ds.sp;

import java.io.PrintStream;

public interface RMIConnection {

	int CLIENT = 0;
	int SERVER = 1;

	void send(byte[] data) throws RMIException;

	void close() throws RMIException;

	byte[] receive() throws RMIException;

	void setMode(int connectionMode) throws RMIException;

	void setLogging(PrintStream ps, int logLevel) throws RMIException;

	int getMode() throws RMIException;

	void setTerminal(int terminalNumber) throws RMIException;

}
