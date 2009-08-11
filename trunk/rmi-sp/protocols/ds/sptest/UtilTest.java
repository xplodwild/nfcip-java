/*
 * UtilTest - Simple test program that tests the Util static class
 *                     
 * Copyright (C) 2008  François Kooman <F.Kooman@student.science.ru.nl>
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

package ds.sptest;

import java.math.BigInteger;
import java.util.Arrays;
import ds.sp.Util;

/**
 * Tests some methods in the Util class
 * 
 * @author F. Kooman <F.Kooman@student.science.ru.nl>
 * 
 */
public class UtilTest {

	public static void main(String[] args) {
		BigInteger a = new BigInteger("123");
		BigInteger b = new BigInteger("456");
		BigInteger c = new BigInteger("789");
		BigInteger[] bia = new BigInteger[] { a, b, c };

		/* BigInteger Array to Byte Array Tests */
		byte[] result = Util.setBigIntegerArray(bia);
		testArrayCompare("bia2b 1", result, new byte[] { 0x03, 0x00, 0x01,
				0x00, 0x02, 0x00, 0x02, 0x7B, 0x01, (byte) 0xC8, 0x03, 0x15 });

		result = Util.setBigIntegerArray(null);
		testArrayCompare("bia2b 2", result, null);

		result = Util.setBigIntegerArray(new BigInteger[0]);
		testArrayCompare("bia2b 3", result, new byte[] { 0x00 });

		result = Util.setBigIntegerArray(new BigInteger[3]);
		testArrayCompare("bia2b 4", result, new byte[] { 0x03, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00 });

		result = Util.setBigIntegerArray(new BigInteger[] { a, null, c });
		testArrayCompare("bia2b 5", result, new byte[] { 0x03, 0x00, 0x01,
				0x00, 0x00, 0x00, 0x02, 0x7B, 0x03, 0x15 });

		/* Byte Array to BigInteger Array Tests */
		byte[] input = Util.setBigIntegerArray(bia);
		BigInteger[] res = Util.getBigIntegerArray(input, 0);
		testBigIntegerArrayCompare("b2bia 1", bia, res);

		try {
			res = Util.getBigIntegerArray(null, 0);
		} catch (IllegalArgumentException e) {
			System.out.println("TEST [b2bia 2] SUCCESS");
		}
		try {
			res = Util.getBigIntegerArray(new byte[1], -1);
		} catch (IllegalArgumentException e) {
			System.out.println("TEST [b2bia 3] SUCCESS");
		}

		res = Util.getBigIntegerArray(new byte[1], 0);
		testBigIntegerArrayCompare("b2bia 4", res, new BigInteger[0]);

		/* offset test */
		byte[] offsetTest = new byte[] { (byte) 0xff, (byte) 0xff, 0x03, 0x00,
				0x01, 0x00, 0x02, 0x00, 0x02, 0x7B, 0x01, (byte) 0xC8, 0x03,
				0x15, (byte) 0xff, (byte) 0xff };
		res = Util.getBigIntegerArray(offsetTest, 2);
		testBigIntegerArrayCompare("b2bia 5", res, bia);

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
		if ((first == null && second == null) || Arrays.equals(first, second)) {
			output += "SUCCESS";
		} else {
			output += "FAILED";
		}
		System.out.println(output);
	}

	private static void testBigIntegerArrayCompare(String name,
			BigInteger[] first, BigInteger second[]) {
		String output = "TEST [" + name + "] ";
		if (first == null && second == null) {
			output += "SUCCESS";
		} else if (first == null || second == null) {
			output += "FAILED";
		} else if (first.length != second.length) {
			output += "FAILED";
		}
		for (int i = 0; i < first.length; i++) {
			if (first[i].compareTo(second[i]) != 0) {
				output += "FAILED";
				return;
			}
		}
		output += "SUCCESS";
		System.out.println(output);
	}

}
