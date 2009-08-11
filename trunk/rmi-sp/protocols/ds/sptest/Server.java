package ds.sptest;

import java.math.BigInteger;
import ds.sp.IServer;

public class Server extends RMIServer implements IServer, ByteArrayInterface,
		PingInterface, ShortInterface, BigIntegerInterface {

	/************************************************************************
	 * Ping Test
	 ************************************************************************/
	public void pingMethod() {
		// we don't do anything here
	}

	/************************************************************************
	 * ByteArray Test
	 ************************************************************************/
	public byte[] addTwoByteArrays(byte[] ba, byte[] bb) {
		if (ba.length != bb.length) {
			// throw new RMIException("...");
		}
		byte[] bc = new byte[ba.length];
		for (int i = 0; i < ba.length; i++) {
			bc[i] = (byte) (ba[i] + bb[i]);
		}
		return bc;
	}

	public byte[] receiveBA() {
		int size = 500;
		byte[] ba = new byte[size];
		for (int i = 0; i < size; i++) {
			ba[i] = (byte) i;
		}
		return ba;
	}

	public void sendBA(byte[] ba) {
	}

	public byte[] sendReceiveBA(byte[] ba) {
		return ba;
	}

	public byte[] sendTwoReceiveBA(byte[] ba, byte[] bb) {
		byte[] bc = new byte[ba.length + bb.length];
		System.arraycopy(ba, 0, bc, 0, ba.length);
		System.arraycopy(bb, 0, bc, ba.length, bb.length);
		return bc;
	}

	/************************************************************************
	 * Short Test
	 ************************************************************************/
	public short addSomeShorts(short s0, short s1) {
		return (short) (s0 + s1);
	}

	public short multiplySomeShorts(short s0, short s1, short s2) {
		return (short) (s0 * s1 * s2);
	}

	public void setSomeShorts(short s0, short s1) {
		// do nothing
	}

	/************************************************************************
	 * BigInteger Test
	 ************************************************************************/
	public BigInteger modPow(BigInteger b0, BigInteger b1, BigInteger b2) {
		return b0.modPow(b1, b2);
	}

	public BigInteger[] arrayTest(BigInteger b0, BigInteger b1, BigInteger b2) {
		BigInteger[] a = new BigInteger[3];
		a[0] = b0;
		a[1] = b1;
		a[2] = b2;
		return a;
	}

	public BigInteger[] arrayParameterTest(BigInteger[] Ba0, BigInteger[] Ba1) {
		BigInteger[] b = new BigInteger[2];
		b[0] = Ba0[0].add(Ba1[0]);
		b[1] = Ba0[1].add(Ba1[1]);
		return b;
	}
}
