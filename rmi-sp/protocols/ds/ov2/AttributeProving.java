package ds.ov2;

import java.math.BigInteger;
import ds.sp.RMIException;

public interface AttributeProving {
	public final static int NO_OF_ATTRIBUTES = 3;
	public final static int BASE_BITS = 1280;
	public final static int EXP_BITS = 159;
	public static final int PRIME_CERTAINTY = 1000;

	public BigInteger[] getCommitment() throws RMIException;

	public void setChallenge(BigInteger challenge) throws RMIException;

	public BigInteger[] getProof() throws RMIException;

}
