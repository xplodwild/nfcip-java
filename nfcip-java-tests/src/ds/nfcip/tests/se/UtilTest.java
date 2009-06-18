/*
 * UtilTest - Simple test class that tests the Util class
 *                     
 * Copyright (C) 2009  François Kooman <F.Kooman@student.science.ru.nl>
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

package ds.nfcip.tests.se;

import java.util.Arrays;
import java.util.Vector;

import ds.nfcip.NFCIPUtils;

/**
 * Tests some methods in the Util class
 * 
 * @author F. Kooman <F.Kooman@student.science.ru.nl>
 * 
 */
public class UtilTest {

	public static void main(String[] args) {
		byte[] a = new byte[] { 0, 1, 2, 3, 4 };
		byte[] b = new byte[] { 4, 5, 6, 7, 8 };
		byte[] result;

		/* appendToByteArray tests */
		result = NFCIPUtils.appendToByteArray(a, b, 1, 4);
		testArrayCompare("a1", result, new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 });
		result = NFCIPUtils.appendToByteArray(a, null, 1, 4);
		testArrayCompare("a2", result, new byte[] { 0, 1, 2, 3, 4 });
		result = NFCIPUtils.appendToByteArray(null, b, 1, 4);
		testArrayCompare("a3", result, new byte[] { 5, 6, 7, 8 });
		result = NFCIPUtils.appendToByteArray(null, b, 0, 0);
		testArrayCompare("a4", result, new byte[0]);
		result = NFCIPUtils.appendToByteArray(null, null, 0, 0);
		testArrayCompare("a5", result, null);
		result = NFCIPUtils.appendToByteArray(null, null);
		testArrayCompare("a6", result, null);
		result = NFCIPUtils.appendToByteArray(a, b);
		testArrayCompare("a7", result, new byte[] { 0, 1, 2, 3, 4, 4, 5, 6, 7,
				8 });

		try {
			result = NFCIPUtils.appendToByteArray(a, b, -1, 0);
			System.out.println("TEST [a6] *FAILED*");
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("TEST [a6] SUCCESS");
		}
		try {
			result = NFCIPUtils.appendToByteArray(a, b, 0, -1);
			System.out.println("TEST [a7] *FAILED*");
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("TEST [a7] SUCCESS");
		}
		try {
			result = NFCIPUtils.appendToByteArray(a, b, 5, 1);
			System.out.println("TEST [a8] *FAILED*");
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("TEST [a8] SUCCESS");
		}

		/* subByteArray tests */
		result = NFCIPUtils.subByteArray(a, 1, a.length - 1);
		testArrayCompare("b1", result, new byte[] { 1, 2, 3, 4 });
		result = NFCIPUtils.subByteArray(b, 4, 1);
		testArrayCompare("b2", result, new byte[] { 8 });
		result = NFCIPUtils.subByteArray(a, 0, a.length);
		testArrayCompare("b3", result, new byte[] { 0, 1, 2, 3, 4 });
		try {
			result = NFCIPUtils.subByteArray(a, -1, a.length);
			System.out.println("TEST [b4] *FAILED*");
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("TEST [b4] SUCCESS");
		}

		/* byteToString tests */
		String s = NFCIPUtils.byteToString((byte) 0x3f);
		testStringCompare("bts0", s, "0x3F");
		s = NFCIPUtils.byteToString((byte) 0x00);
		testStringCompare("bts1", s, "0x00");

		/* padding tests */
		byte[] unpadded;
		byte[] padded;

		unpadded = null;
		padded = NFCIPUtils.addPadding(unpadded);
		testArrayCompare("p1", padded, new byte[] { 0x01, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 });

		unpadded = new byte[] { 0x00 };
		padded = NFCIPUtils.addPadding(unpadded);
		testArrayCompare("p2", padded, new byte[] { 0x01, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00 });

		unpadded = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
				0x08, 0x09 };
		padded = NFCIPUtils.addPadding(unpadded);
		testArrayCompare("p3", padded, new byte[] { 0x01, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00,
				0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 });

		testArrayCompare("p4", unpadded, NFCIPUtils.removePadding(padded));

		// unpadded = null;
		// padded = Util.addPadding(unpadded);
		// System.out.println(Util.byteArrayToString(padded));
		// System.out.println(Util.byteArrayToString(Util.removePadding(padded)));

		// testArrayCompare("p5", unpadded, Util.removePadding(padded));

		unpadded = new byte[] { 0x00 };
		padded = NFCIPUtils.addPadding(unpadded);
		testArrayCompare("p6", unpadded, NFCIPUtils.removePadding(padded));

		/* testing 23 bytes */
		unpadded = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
				0x08, 0x09, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
				0x08, 0x09, 0x00, 0x01, 0x02 };
		padded = NFCIPUtils.addPadding(unpadded);
		// System.out.println(Util.byteArrayToString(padded));

		testArrayCompare("p7", unpadded, NFCIPUtils.removePadding(padded));

		/* testing 22 bytes */
		unpadded = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
				0x08, 0x09, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
				0x08, 0x09, 0x00, 0x01 };
		padded = NFCIPUtils.addPadding(unpadded);
		// System.out.println(Util.byteArrayToString(padded));
		testArrayCompare("p8", unpadded, NFCIPUtils.removePadding(padded));

		/* testing 21 bytes */
		unpadded = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
				0x08, 0x09, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
				0x08, 0x09, 0x00 };
		padded = NFCIPUtils.addPadding(unpadded);
		// System.out.println(Util.byteArrayToString(padded));

		testArrayCompare("p9", unpadded, NFCIPUtils.removePadding(padded));

		unpadded = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
				0x08, 0x09, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
				0x08, 0x09, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
				0x08, 0x09 };
		padded = NFCIPUtils.addPadding(unpadded);
		// System.out.println(Util.byteArrayToString(padded));

		testArrayCompare("p10", unpadded, NFCIPUtils.removePadding(padded));

		/* dataToBlockVector tests */
		byte[] dtbv, dtbv2;
		Vector<byte[]> v;

		/* this is supposed to fail */
		dtbv = null;
		v = NFCIPUtils.dataToBlockVector(dtbv, 20);
		dtbv2 = NFCIPUtils.blockVectorToData(v);
		testArrayCompare("dtbv1", dtbv, dtbv2);

		dtbv = new byte[0];
		v = NFCIPUtils.dataToBlockVector(dtbv, 20);
		dtbv2 = NFCIPUtils.blockVectorToData(v);
		testArrayCompare("dtbv2", dtbv, dtbv2);

		dtbv = new byte[] { 0x11 };
		v = NFCIPUtils.dataToBlockVector(dtbv, 20);
		dtbv2 = NFCIPUtils.blockVectorToData(v);
		testArrayCompare("dtbv3", dtbv, dtbv2);

		dtbv = new byte[] { 0x10, 0x11 };
		v = NFCIPUtils.dataToBlockVector(dtbv, 20);
		dtbv2 = NFCIPUtils.blockVectorToData(v);
		testArrayCompare("dtbv4", dtbv, dtbv2);

		dtbv = new byte[256];
		for (int i = 0; i < 256; i++)
			dtbv[i] = (byte) i;
		v = NFCIPUtils.dataToBlockVector(dtbv, 20);
		dtbv2 = NFCIPUtils.blockVectorToData(v);
		testArrayCompare("dtbv5", dtbv, dtbv2);

		dtbv = new byte[] { (byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc,
				(byte) 0xfb, (byte) 0xfa, (byte) 0xf9, (byte) 0xf8, (byte) 0xf7 };
		// v = Util.dataToBlockVector(dtbv, 4);

		for (int i = 0; i < v.size(); i++) {
			System.out.println(NFCIPUtils.byteArrayToString((byte[]) v
					.elementAt(i)));
		}

		/* chaining indicator tests */
		byte[] chainingTest;
		chainingTest = new byte[] { 0x00 };
		testBooleanCompare("ci1", NFCIPUtils.isChained(chainingTest), false);
		chainingTest = new byte[] { 0x01 };
		testBooleanCompare("ci2", NFCIPUtils.isChained(chainingTest), true);
		chainingTest = new byte[] { 0x02 };
		testBooleanCompare("ci3", NFCIPUtils.isChained(chainingTest), false);
		chainingTest = new byte[] { 0x03 };
		testBooleanCompare("ci4", NFCIPUtils.isChained(chainingTest), true);
		chainingTest = new byte[] { 0x04 };
		testBooleanCompare("ci5", NFCIPUtils.isChained(chainingTest), false);

		byte[] blockNumberTest;
		blockNumberTest = new byte[] { 0x00 };
		testIntegerCompare("bn1", NFCIPUtils.getBlockNumber(blockNumberTest), 0);
		blockNumberTest = new byte[] { 0x01 };
		testIntegerCompare("bn2", NFCIPUtils.getBlockNumber(blockNumberTest), 0);
		blockNumberTest = new byte[] { 0x02 };
		testIntegerCompare("bn3", NFCIPUtils.getBlockNumber(blockNumberTest), 1);
		blockNumberTest = new byte[] { 0x03 };
		testIntegerCompare("bn4", NFCIPUtils.getBlockNumber(blockNumberTest), 1);
		blockNumberTest = new byte[] { 0x04 };
		testIntegerCompare("bn5", NFCIPUtils.getBlockNumber(blockNumberTest), 0);

	}

	/**
	 * Compare two arrays and print the result of the comparison directly
	 * 
	 * @param name
	 *            name of the test
	 * @param first
	 *            the first array
	 * @param second
	 *            the second array
	 */
	private static void testArrayCompare(String name, byte[] first,
			byte[] second) {
		String output = "TEST [" + name + "] ";
		if (Arrays.equals(first, second)) {
			output += "SUCCESS";
		} else {
			output += "*FAILED*";
		}
		System.out.println(output);
	}

	private static void testStringCompare(String name, String a, String b) {
		String output = "TEST [" + name + "] ";
		if (a.equals(b)) {
			output += "SUCCESS";
		} else {
			output += "*FAILED*";
		}
		System.out.println(output);
	}

	private static void testIntegerCompare(String name, int a, int b) {
		String output = "TEST [" + name + "] ";
		if (a == b) {
			output += "SUCCESS";
		} else {
			output += "*FAILED*";
		}
		System.out.println(output);
	}

	private static void testBooleanCompare(String name, boolean a, boolean b) {
		String output = "TEST [" + name + "] ";
		if (a == b) {
			output += "SUCCESS";
		} else {
			output += "*FAILED*";
		}
		System.out.println(output);
	}
	// private static void testByteArrayVectorCompare(String name, Vector a,
	// Vector b) {
	// String output = "TEST [" + name + "] ";
	// if (a == null && b == null)
	// output += "SUCCESS";
	// if (a == null)
	// output += "FAILED";
	// if (b == null)
	// output += "FAILED";
	// if (a.size() != b.size())
	// output += "FAILED";
	// boolean failed = false;
	// for (int i = 0; i < a.size(); i++) {
	// if (!Arrays
	// .equals((byte[]) a.elementAt(i), (byte[]) b.elementAt(i))) {
	//
	// failed = true;
	// break;
	// }
	// }
	// if (failed)
	// output += "FAILED";
	// else
	// output += "SUCCESS";
	// System.out.println(output);
	// }
}
