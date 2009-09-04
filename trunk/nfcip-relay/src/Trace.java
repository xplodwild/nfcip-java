import java.io.Serializable;
import java.util.ArrayList;

public class Trace implements Serializable {
	private static final long serialVersionUID = -9040240257893371851L;
	private ArrayList<byte[]> trace;

	Trace() {
		trace = new ArrayList<byte[]>();
	}

	public void add(byte[] data) {
		trace.add(data);
	}

	public byte[] get(int i) {
		return trace.get(i);
	}

	public String toString() {
		String output = "";
		for (int i = 0; i < trace.size(); i++) {
			output += ("[" + i + "] = " + Utils.byteArrayToString(trace.get(i)) + "\n");
		}
		return output;
	}

	public int size() {
		return trace.size();
	}
}