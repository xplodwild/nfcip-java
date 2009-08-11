package ds.ov2;

import java.math.BigInteger;
import ds.sp.RMIException;

public interface Issuance {
	/**
	 * Initialize the Server device
	 * 
	 * @param v
	 *            the public key of the verifier FIXME, move to verifier
	 *            protocol!
	 * @param n
	 *            the public modulus
	 * @param h
	 *            the public key of the issuer
	 * @param bases
	 *            the bases of the attribute expression
	 * @param attr
	 *            the attributes of the attribute expression
	 * @throws RMIException
	 */
	public void initialize(BigInteger v, BigInteger n, BigInteger h,
			BigInteger[] bases, BigInteger[] attr) throws RMIException;

	public void setCertificateChallenge(BigInteger alpha) throws RMIException;

	public BigInteger getCertificateRequest() throws RMIException;

	public void setCertificate(BigInteger sig) throws RMIException;

}
