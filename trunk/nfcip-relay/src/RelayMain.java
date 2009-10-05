/*
 * RelayMain - Class that starts the relaying and replaying
 *                     
 * Copyright (C) 2009  François Kooman <fkooman@tuxed.net>
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class RelayMain {
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("NFCIP Relay Tool");
			System.out.println("----------------");
			System.out
					.println("  -c [file]    Create a dump (requires two NFC readers)");
			System.out
					.println("  -i [file]    Replays the initiator from dump file (created with -c)");
			System.out
					.println("  -t [file]    Replays the target from dump file (created with -c)");
			System.out
					.println("  -d [file]    Shows the dump (created with -c)\n");
		} else {
			try {
				Relay r = new Relay();
				FileInputStream fis = new FileInputStream(new File(args[1]));
				ObjectInputStream ois = new ObjectInputStream(fis);
				Trace tr = (Trace) ois.readObject();
				if (args[0].equals("-i")) {
					r.setTrace(tr);
					r.setReplayInitiator();
				} else if (args[0].equals("-t")) {
					r.setTrace(tr);
					r.setReplayTarget();
				} else if (args[0].equals("-d")) {
					System.out.println(tr);
					System.exit(0);
				} else {
					// we run in relay mode...
				}
				Runtime.getRuntime().addShutdownHook(r.new Cleanup());
				r.run();				
			} catch (FileNotFoundException e) {
				System.out.println("Trace file \"" + args[1]
						+ "\" does not exist!");
			} catch (IOException e) {
				System.out.println("Error reading trace!");
			} catch (ClassNotFoundException e) {
				System.out.println("Error loading trace!");
			}
		}
	}
}
