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