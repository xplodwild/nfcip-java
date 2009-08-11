package ds.sptest;

import ds.sp.RMIException;

public interface ShortInterface {

	public void setSomeShorts(short s1, short s2) throws RMIException;

	public short addSomeShorts(short s1, short s2) throws RMIException;

	public short multiplySomeShorts(short s1, short s2, short s3)
			throws RMIException;

}
