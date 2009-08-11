package ds.ov2;

import java.math.BigInteger;
import java.util.Random;
import ds.sp.RMIException;

/**
 * The issuer is the organization that issues public transport document,
 * generates the public modulus, certifies cards
 * 
 * @author fkooman
 * 
 */
public class Issuer {
	private BigInteger n;
	private BigInteger v;
	private BigInteger[] bases;
	private BigInteger d;
	private BigInteger h;
	private BigInteger x;

	private Random random;

	public Issuer() {
		random = new Random();

		/* modulus */
		BigInteger p = new BigInteger(AttributeProving.BASE_BITS,
				AttributeProving.PRIME_CERTAINTY, random);
		BigInteger q = new BigInteger(AttributeProving.BASE_BITS,
				AttributeProving.PRIME_CERTAINTY, random);
		n = p.multiply(q);
		BigInteger EulerPhi = p.subtract(BigInteger.ONE).multiply(
				q.subtract(BigInteger.ONE));

		/* the public key (v) */
		do {
			v = new BigInteger(AttributeProving.BASE_BITS,
					AttributeProving.PRIME_CERTAINTY, random).mod(EulerPhi);
		} while (v.isProbablePrime(AttributeProving.PRIME_CERTAINTY) == false);

		/* the public bases */
		bases = new BigInteger[AttributeProving.NO_OF_ATTRIBUTES];
		for (int i = 0; i < AttributeProving.NO_OF_ATTRIBUTES; i++) {
			do {
				bases[i] = new BigInteger(AttributeProving.BASE_BITS, random)
						.mod(n);
			} while (bases[i].gcd(n).compareTo(BigInteger.ONE) != 0);
		}

		/* the private key, d */
		d = v.modPow(new BigInteger("-1"), EulerPhi);

		/* the private key, x, in Z_n */
		do {
			x = new BigInteger(AttributeProving.BASE_BITS, random).mod(n);
		} while (x.gcd(n).compareTo(BigInteger.ONE) != 0);

		/* the public key, h, in Z_n */
		h = x.modPow(v, n);
	}

	public BigInteger getPublicKey() {
		return v;
	}

	public BigInteger getModulus() {
		return n;
	}

	public BigInteger[] getPublicBases() {
		return bases;
	}

	/**
	 * Initializes the travel document
	 * 
	 * @param r
	 *            the communication channel
	 * @throws RMIException
	 */
	public void initialize(Issuance r) throws RMIException {
		/* the private attributes */
		BigInteger[] attributes = new BigInteger[AttributeProving.NO_OF_ATTRIBUTES];
		for (int i = 0; i < AttributeProving.NO_OF_ATTRIBUTES; i++) {
			attributes[i] = new BigInteger(AttributeProving.EXP_BITS, random)
					.mod(v);
		}

		/* initialize with public values and private attributes */
		r.initialize(v, n, h, bases, attributes);

		/* generate alpha for challenge */
		BigInteger alpha;
		do {
			alpha = new BigInteger(AttributeProving.BASE_BITS, random).mod(n);
		} while (alpha.gcd(n).compareTo(BigInteger.ONE) != 0);

		/* setCertificateChallenge */
		r.setCertificateChallenge(alpha);

		/* getCertificateRequest */
		BigInteger sigReq = r.getCertificateRequest();

		BigInteger attributeExpression = BigInteger.ONE;
		for (int i = 0; i < AttributeProving.NO_OF_ATTRIBUTES; i++) {
			attributeExpression = attributeExpression.multiply(
					bases[i].modPow(attributes[i], n)).mod(n);
		}

		BigInteger signature = h.multiply(attributeExpression).mod(n).modPow(
				sigReq, n).multiply(alpha).mod(n).modPow(d, n);

		/* setSignature */
		r.setCertificate(signature);
	}

}
