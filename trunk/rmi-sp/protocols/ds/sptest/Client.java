package ds.sptest;

import java.math.BigInteger;
import java.util.Arrays;
import ds.sp.IClient;
import ds.sp.RMIConnection;
import ds.sp.RMIException;
import ds.sp.Util;

public class Client implements IClient {
	private RMIConnection icc;

	public void setConnection(RMIConnection i) {
		this.icc = i;
	}

	public void runClient() {
		try {
			RMIClient r = new RMIClient(icc);

			/*****************************************************************
			 * Ping Test
			 *****************************************************************/
			r.pingMethod();
			System.out.println("TEST [pingMethod] SUCCESS");

			/*****************************************************************
			 * ByteArray Test
			 *****************************************************************/

			/* some test data */
			byte[] ba = { 0, 1, 2, 3, 4, 5, 6, 7 };
			byte[] bb = { 1, 2, 3, 4, 5, 6, 7, 8 };
			byte[] baplusbb = { 1, 3, 5, 7, 9, 11, 13, 15 };

			byte[] bigArrayTest = new byte[500];
			for (int i = 0; i < 500; i++) {
				bigArrayTest[i] = (byte) i;
			}

			byte[] concatbabb = new byte[ba.length + bb.length];
			System.arraycopy(ba, 0, concatbabb, 0, ba.length);
			System.arraycopy(bb, 0, concatbabb, ba.length, bb.length);

			short sa = (short) 5;
			short sb = (short) 7;
			short sc = (short) 3;

			byte[] baResult;
			short sResult;

			/* the actual tests */
			// sendBA
			r.sendBA(bigArrayTest);
			System.out.println("TEST [sendBA] SUCCESS");

			// receiveBA
			baResult = r.receiveBA();
			arrayCompare("receiveBA", baResult, bigArrayTest);

			// sendReceiveBA
			baResult = r.sendReceiveBA(bigArrayTest);
			arrayCompare("sendReceiveBA", baResult, bigArrayTest);

			// sendTwoReceiveBA
			baResult = r.sendTwoReceiveBA(ba, bb);
			arrayCompare("sendTwoReceiveBA", baResult, concatbabb);

			// addTwoByteArrays
			baResult = r.addTwoByteArrays(ba, bb);
			arrayCompare("addTwoByteArrays", baResult, baplusbb);

			/*****************************************************************
			 * Short Test
			 *****************************************************************/
			// setSomeShorts
			r.setSomeShorts((short) 5, (short) 7);
			System.out.println("TEST [setSomeShorts] SUCCESS");

			// addSomeShorts
			sResult = r.addSomeShorts(sa, sb);
			shortCompare("addSomeShorts", sResult, (short) (sa + sb));

			// multiplySomeShorts
			sResult = r.multiplySomeShorts(sa, sb, sc);
			shortCompare("multiplySomeShorts", sResult, (short) (sa * sb * sc));

			/*****************************************************************
			 * BigInteger Test
			 *****************************************************************/
			String base = "30849428153246678943383710834377546147361252507237488771952467649699526003127580673492204027596143820934993058971774135937892052413032063021058127042691796178246873321330889437326381246091174817436507225204850921717935447704881448715693715322283505976180013161891975454308466964633140453202688193550664717295942166499579329940808340899919768604130153878284870810562734601043055009883924815232818820879142292124967747540002117267832594444621620903155213275007706912873496749132052099379600833842642475681361010527633930664982116460015665463227794281340913406403228952730547659789257142952874038701578381127782870377663";
			String exp = "1671648377854561636191785821200615369951185074448022356413371639823087885966703682496944299";
			String mod = "19828226319739704775838206537915499698980057230007330140248937967285039912765260745431866414472034643228768263992106843029885414639231326693315967793801045401323561684586413167128378368581345678839489259987230943305055152850587252542586106515648966314335330594927485376257837432573663657895759705022447956394247398811109762688278846933398253340638308288404452348673664035602929309927888051709364340090291083728498945693614820162591895854345580220460854461260754422099211800051585175476605188209278569005450195755069458572521386631231663422333901809980427590377349711396822378319246433097256963834232716188353135851757";

			java.math.BigInteger b = new java.math.BigInteger(base);
			// System.out.println("base: " + b.bitLength() + " bits");
			java.math.BigInteger e = new java.math.BigInteger(exp);
			// System.out.println("exp:  " + e.bitLength() + " bits");
			java.math.BigInteger m = new java.math.BigInteger(mod);
			// System.out.println("mod:  " + m.bitLength() + " bits");

			// modPow
			java.math.BigInteger localResult = b.modPow(e, m);
			BigInteger remoteResult = r.modPow(b, e, m);
			arrayCompare("modPow", localResult.toByteArray(), remoteResult
					.toByteArray());

			// arrayTest
			// java.math.BigInteger[] remoteArrayResult = r.arrayTest(new
			// BigInteger("20"), new BigInteger("30"), new BigInteger("40"));
			java.math.BigInteger[] remoteArrayResult = r.arrayTest(b, e, m);
			arrayCompare("arrayTest", Util.appendToByteArray(
					remoteArrayResult[0].toByteArray(), Util.appendToByteArray(
							remoteArrayResult[1].toByteArray(),
							remoteArrayResult[2].toByteArray())), Util
					.appendToByteArray(b.toByteArray(), Util.appendToByteArray(
							e.toByteArray(), m.toByteArray())));

			// testBI
			java.math.BigInteger[] Ba = new java.math.BigInteger[2];
			java.math.BigInteger[] Bb = new java.math.BigInteger[2];
			Ba[0] = new java.math.BigInteger("47239479247");
			Ba[1] = new java.math.BigInteger("53972978344");
			Bb[0] = new java.math.BigInteger("58398093284");
			Bb[1] = new java.math.BigInteger("69378287482");

			java.math.BigInteger[] lR = { Ba[0].add(Bb[0]), Ba[1].add(Bb[1]) };
			java.math.BigInteger[] BBa = r.arrayParameterTest(Ba, Bb);
			testBigIntegerArrayCompare("arrayParameterTest", lR, BBa);

			// close connection
			System.out.println("Closing connection...");
			r.close();
		} catch (RMIException e) {
			System.err.println(e);
		}
	}

	/**
	 * Compare two arrays and print the result of the comparison directly
	 * 
	 * @param test
	 *            name of the test
	 * @param a
	 *            the first array
	 * @param b
	 *            the second array
	 */
	private static void arrayCompare(String test, byte[] a, byte[] b) {
		String output = "TEST [" + test + "] ";
		if (Arrays.equals(a, b)) {
			output += "SUCCESS";
		} else {
			output += "FAILED";
		}
		System.out.println(output);
	}

	/**
	 * Compare two shorts and print the result of the comparison directly
	 * 
	 * @param test
	 *            name of the test
	 * @param a
	 *            the first short
	 * @param b
	 *            the second short
	 */
	private static void shortCompare(String test, short a, short b) {
		String output = "TEST [" + test + "] ";
		if (a == b) {
			output += "SUCCESS";
		} else {
			output += "FAILED";
		}
		System.out.println(output);
	}

	private static void testBigIntegerArrayCompare(String test,
			BigInteger[] first, BigInteger second[]) {
		String output = "TEST [" + test + "] ";
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
				System.out.println(output);
				return;
			}
		}
		output += "SUCCESS";
		System.out.println(output);
	}

}
