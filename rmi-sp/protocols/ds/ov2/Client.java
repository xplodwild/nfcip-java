package ds.ov2;

import ds.sp.IClient;
import ds.sp.RMIConnection;
import ds.sp.RMIException;

public class Client implements IClient {
	private RMIConnection icc;

	public void setConnection(RMIConnection i) {
		this.icc = i;
	}

	public void runClient() {
		try {
			RMIClient r = new RMIClient(icc);
			Issuer i = new Issuer();
			long begin, end;
			begin = System.nanoTime();
			i.initialize(r);
			end = System.nanoTime();
			System.out.println("Initialize: " + (end - begin) / 10E8);

			Gate g = new Gate(i);
			begin = System.nanoTime();
			boolean valid = g.isValidSecondClassTicket(r);
			end = System.nanoTime();
			System.out.println("Verify attribute: " + (end - begin) / 10E8);

			if (valid) {
				System.out.println("This is a valid Second Class ticket");
			} else {
				System.out.println("This is *NOT* a valid Second Class ticket");
			}

			// close connection
			System.out.println("Closing connection...");
			r.close();
		} catch (RMIException e) {
			System.err.println(e);
		}
	}

}