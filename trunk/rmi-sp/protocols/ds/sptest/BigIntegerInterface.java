package ds.sptest;

import java.math.BigInteger;
import ds.sp.RMIException;

public interface BigIntegerInterface {

	/**
	 * Calculates base^power mod m
	 * 
	 * @param b
	 *            base
	 * @param p
	 *            power
	 * @param m
	 *            modulus
	 * @return the answer
	 */
	BigInteger modPow(BigInteger b, BigInteger p, BigInteger m)
			throws RMIException;

	BigInteger[] arrayTest(BigInteger a, BigInteger b, BigInteger c)
			throws RMIException;

	BigInteger[] arrayParameterTest(BigInteger[] a, BigInteger[] b)
			throws RMIException;
}
