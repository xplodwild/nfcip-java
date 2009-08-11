package ds.sp;

import java.math.BigInteger;

public class Util {

	/**
	 * Convert byte array to short
	 * 
	 * @param data
	 *            byte array of at least size 2 with the short
	 * @param offset
	 *            offset in the array
	 * @return converted short
	 */
	public static short byteArrayToShort(byte[] data, int offset) {
		return (short) (((data[offset] << 8)) | ((data[offset + 1] & 0xff)));
	}

	/**
	 * Convert a short to byte array
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] shortToByteArray(short s) {
		return new byte[] { (byte) ((s & 0xFF00) >> 8), (byte) (s & 0x00FF) };
	}

	/**
	 * Converts a byte array to readable string
	 * 
	 * @param a
	 *            array to print
	 * @return readable byte array string
	 */
	public static String byteArrayToString(byte[] a) {
		String result = "";
		String onebyte = null;
		for (int i = 0; i < a.length; i++) {
			onebyte = Integer.toHexString(a[i]);
			if (onebyte.length() == 1)
				onebyte = "0" + onebyte;
			else
				onebyte = onebyte.substring(onebyte.length() - 2);
			result = result + "0x" + onebyte.toUpperCase() + " ";
		}
		return result;
	}

	/**
	 * Join a list of Strings together
	 * 
	 * @param str
	 *            list of Strings
	 * @param delimiter
	 *            the delimiter for the join (e.g. ",")
	 * @return a joined String
	 */
	public static String joinString(Object[] str, String delimiter) {
		if (str == null || str.length == 0)
			return "";
		String output = "";
		for (int i = 0; i < str.length - 1; i++) {
			output += str[i] + ",";
		}
		output += str[str.length - 1];
		return output;
	}

	/**
	 * Append a byte array to another byte array
	 * 
	 * @param first
	 *            the byte array to append to
	 * @param second
	 *            the byte array to append
	 * @return the appended array
	 */
	public static byte[] appendToByteArray(byte[] first, byte[] second) {
		int secondLength = (second != null) ? second.length : 0;
		return appendToByteArray(first, second, 0, secondLength);
	}

	/**
	 * Append a byte array to another byte array specifying which part of the
	 * second byte array should be appended to the first
	 * 
	 * @param first
	 *            the byte array to append to
	 * @param second
	 *            the byte array to append
	 * @param offset
	 *            offset in second array to start appending from
	 * @param length
	 *            number of bytes to append from second to first array
	 * @return the appended array
	 */
	public static byte[] appendToByteArray(byte[] first, byte[] second,
			int offset, int length) {
		if (second == null || second.length == 0) {
			return first;
		}
		int firstLength = (first != null) ? first.length : 0;

		if (length < 0 || offset < 0 || second.length < length + offset)
			throw new ArrayIndexOutOfBoundsException();
		byte[] result = new byte[firstLength + length];
		if (firstLength > 0)
			System.arraycopy(first, 0, result, 0, firstLength);
		System.arraycopy(second, offset, result, firstLength, length);
		return result;
	}

	/**
	 * Return a specific part of a byte array starting from <em>offset</em> with
	 * <em>length</em>
	 * 
	 * @param array
	 *            the byte array
	 * @param offset
	 *            the offset in the array from where to start in bytes
	 * @param length
	 *            the number of bytes to get
	 * @return the sub byte array
	 */
	public static byte[] subByteArray(byte[] array, int offset, int length) {
		return appendToByteArray(null, array, offset, length);
	}

	/**
	 * Get the number of bytes of storage required to store all the BigIntegers
	 * in a byte array
	 * 
	 * @param ba
	 *            the BigInteger array
	 * @param b
	 *            whether or not to include space for storing the sizes of the
	 *            BigIntegers as well
	 * @return the total size required
	 */
	// public static short BigIntegerArraySize(BigInteger[] ba, boolean b) {
	// if (ba == null || ba.length == 0)
	// return 0;
	// short size = 0;
	// for (int i = 0; i < ba.length; i++) {
	// size += (ba[i] != null) ? ba[i].toByteArray().length : 0;
	// }
	// /*
	// * we need one byte to store the number of BigIntegers and for every
	// * BigInteger we need 2 bytes to store the size
	// */
	// if (b)
	// size += ba.length * 2 + 1;
	// return size;
	// }
	public static BigInteger[] fromByteArray(byte[] data, int offset) {
		/* data should always be of size 1 containing the number of BigInts! */
		if (data == null || data.length == 0)
			throw new IllegalArgumentException("null argument");
		/* preliminary offset verification */
		if (offset < 0 || offset > data.length - 1) {
			throw new IllegalArgumentException("invalid offset");
		}
		int noOfBigIntegers = data[offset];
		/* check whether this field indicates a postive number of BigIntegers */
		if (data[offset] < 0)
			throw new IllegalArgumentException(
					"negative number of big integers?");

		/*
		 * every BigInteger should have at least have two bytes indicating
		 * length
		 */
		if (data.length - offset < noOfBigIntegers * 2 + 1)
			throw new IllegalArgumentException(
					"invalid length, not enough shorts");
		offset++;
		int size = 0;
		int[] sizes = new int[noOfBigIntegers];
		/* now verify the total length of the data */
		for (int i = 0; i < noOfBigIntegers; i++) {
			size += Util.byteArrayToShort(data, offset);
			sizes[i] = Util.byteArrayToShort(data, offset);
			offset += 2;
		}
		if (size + offset > data.length)
			throw new IllegalArgumentException(
					"invalid length, not long enough to contain all specified bigints");
		/* now all should be ok */
		BigInteger[] ba = new BigInteger[noOfBigIntegers];
		for (int i = 0; i < noOfBigIntegers; i++) {
			byte[] tmp = new byte[sizes[i]];
			System.arraycopy(data, offset, tmp, 0, sizes[i]);
			if (tmp != null && tmp.length > 0)
				ba[i] = new BigInteger(tmp);
			offset += tmp.length;
		}
		return ba;
	}

	public static byte[] toByteArray(BigInteger[] b) {
		if (b == null)
			return null;
		if (b.length == 0)
			return new byte[1];
		int noOfBigIntegers = b.length;
		int totalSize = 0;
		for (int i = 0; i < noOfBigIntegers; i++) {
			totalSize += (b[i] != null) ? b[i].toByteArray().length : 0;
		}
		byte[] data = new byte[totalSize + 1 + noOfBigIntegers * 2];
		int offset = 0;
		data[offset] = (byte) noOfBigIntegers;
		offset++;
		int dataOffset = 1 + noOfBigIntegers * 2;
		for (int i = 0; i < noOfBigIntegers; i++) {
			byte[] s = (b[i] != null) ? Util.shortToByteArray((short) b[i]
					.toByteArray().length) : new byte[] { 0x00, 0x00 };
			System.arraycopy(s, 0, data, offset, 2);
			byte[] tmp = (b[i] != null && b[i].toByteArray().length != 0) ? b[i]
					.toByteArray()
					: new byte[0];

			System.arraycopy(tmp, 0, data, dataOffset, tmp.length);
			offset += 2;
			dataOffset += tmp.length;
		}
		return data;
	}

	/**
	 * Get the number of bytes of storage required to store all the BigIntegers
	 * in a byte array
	 * 
	 * @param ba
	 *            the BigInteger array
	 * @param b
	 *            whether or not to include space for storing the sizes of the
	 *            BigIntegers as well
	 * @return the total size required
	 */
	public static short BigIntegerArraySize(BigInteger[] ba, boolean b) {
		if (ba == null || ba.length == 0)
			return 0;
		short size = 0;
		for (int i = 0; i < ba.length; i++) {
			size += (ba[i] != null) ? ba[i].toByteArray().length : 0;
		}
		/*
		 * we need one byte to store the number of BigIntegers and for every
		 * BigInteger we need 2 bytes to store the size
		 */
		if (b)
			size += ba.length * 2 + 1;
		return size;
	}

	public static BigInteger[] getBigIntegerArray(byte[] data, int offset) {
		/* data should always be of size 1 containing the number of BigInts! */
		if (data == null || data.length == 0)
			throw new IllegalArgumentException("null argument");
		/* preliminary offset verification */
		if (offset < 0 || offset > data.length - 1) {
			throw new IllegalArgumentException("invalid offset");
		}
		int noOfBigIntegers = data[offset];
		/* check whether this field indicates a postive number of BigIntegers */
		if (data[offset] < 0)
			throw new IllegalArgumentException(
					"negative number of big integers?");

		/*
		 * every BigInteger should have at least have two bytes indicating
		 * length
		 */
		if (data.length - offset < noOfBigIntegers * 2 + 1)
			throw new IllegalArgumentException(
					"invalid length, not enough shorts");
		offset++;
		int size = 0;
		int[] sizes = new int[noOfBigIntegers];
		/* now verify the total length of the data */
		for (int i = 0; i < noOfBigIntegers; i++) {
			size += Util.byteArrayToShort(data, offset);
			sizes[i] = Util.byteArrayToShort(data, offset);
			offset += 2;
		}
		if (size + offset > data.length)
			throw new IllegalArgumentException(
					"invalid length, not long enough to contain all specified bigints");
		/* now all should be ok */
		BigInteger[] ba = new BigInteger[noOfBigIntegers];
		for (int i = 0; i < noOfBigIntegers; i++) {
			byte[] tmp = new byte[sizes[i]];
			System.arraycopy(data, offset, tmp, 0, sizes[i]);
			if (tmp != null && tmp.length > 0)
				ba[i] = new BigInteger(tmp);
			offset += tmp.length;
		}
		return ba;
	}

	public static byte[] setBigIntegerArray(BigInteger[] b) {
		if (b == null)
			return null;
		if (b.length == 0)
			return new byte[1];
		int noOfBigIntegers = b.length;
		int totalSize = 0;
		for (int i = 0; i < noOfBigIntegers; i++) {
			totalSize += (b[i] != null) ? b[i].toByteArray().length : 0;
		}
		byte[] data = new byte[totalSize + 1 + noOfBigIntegers * 2];
		int offset = 0;
		data[offset] = (byte) noOfBigIntegers;
		offset++;
		int dataOffset = 1 + noOfBigIntegers * 2;
		for (int i = 0; i < noOfBigIntegers; i++) {
			byte[] s = (b[i] != null) ? Util.shortToByteArray((short) b[i]
					.toByteArray().length) : new byte[] { 0x00, 0x00 };
			System.arraycopy(s, 0, data, offset, 2);
			byte[] tmp = (b[i] != null && b[i].toByteArray().length != 0) ? b[i]
					.toByteArray()
					: new byte[0];

			System.arraycopy(tmp, 0, data, dataOffset, tmp.length);
			offset += 2;
			dataOffset += tmp.length;
		}
		return data;
	}
}
