import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class RelayMain {
	public static void main(String[] args) {
		Relay r = new Relay();
		Runtime.getRuntime().addShutdownHook(r.new Cleanup());

		if (args.length > 0
				&& (args[0].equals("-replay") || args[0].equals("-dump"))) {
			try {
				FileInputStream fis = new FileInputStream(new File("trace.obj"));
				ObjectInputStream ois = new ObjectInputStream(fis);
				Trace tr = (Trace) ois.readObject();
				if (args[0].equals("-replay")) {
					r.setTrace(tr);
					r.setReplay();
				} else {
					System.out.println(tr);
					System.exit(0);
				}
			} catch (IOException e) {
				System.out.println("Error reading trace!");
			} catch (ClassNotFoundException e) {
				System.out.println("Error loading trace!");
			}
		}
		r.run();
	}
}