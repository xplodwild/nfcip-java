package ds.ov2;

import java.math.BigInteger;
import java.util.Random;
import org.bouncycastle.crypto.digests.SHA1Digest;

import ds.sp.IServer;

public class Server extends RMIServer implements IServer, AttributeProving, Issuance {
	/* modulus and public key */
	BigInteger n;
	BigInteger v;

	/* the public bases */
	BigInteger[] bases;
	BigInteger[] attributes;

	BigInteger attributeExpression;
	BigInteger blindedAttributeExpression;

	/* blinding vector */
	BigInteger b;

	/* challenge */
	BigInteger challenge;

	/* random values */
	BigInteger[] alphas;

	/* random value */
	BigInteger beta;

	/* public key of issuer */
	BigInteger h;

	/* the signatureRequest */
	BigInteger c;

	/* partial signatureRequest */
	BigInteger cPrime;

	/* generate beta1, beta2 and beta3 */
	BigInteger beta1, beta2, beta3;

	/* part of the signature */
	BigInteger rPrime;

	/* storage for certificate (c', r') */
	BigInteger[] certificate;

	/* specify which attribute to disclose */
	private int DISCLOSED_ATTRIBUTE = 1;

	public void initialize(BigInteger v, BigInteger n, BigInteger h,
			BigInteger[] b, BigInteger[] attr) {
		//if (!v.isProbablePrime(PRIME_CERTAINTY))
		//	System.out.println("[WARNING] public key of V is not prime!");
		this.v = v;
		this.n = n;
		this.h = h;

		bases = b;
		//for (int i = 0; i < bases.length; i++) {
		//	if (bases[i].gcd(n).compareTo(BigInteger.ONE) != 0)
		//		System.out.println("[WARNING] base " + i
		//				+ " is not coprime with modulus!");
		//}
		attributes = attr;

		attributeExpression = BigInteger.ONE;
		for (int i = 0; i < NO_OF_ATTRIBUTES; i++) {
			attributeExpression = attributeExpression.multiply(
					bases[i].modPow(attributes[i], n)).mod(n);
		}
		certificate = new BigInteger[2];

		/* blindedAttributeExpression, only changed on recertify! */
		BigInteger bv;
		do {
			bv = new BigInteger(BASE_BITS, new Random()).mod(n);
		} while (bv.gcd(n).compareTo(BigInteger.ONE) != 0);
		this.b = bv;
		blindedAttributeExpression = attributeExpression.multiply(
				bv.modPow(v, n)).mod(n);

	}

	public BigInteger[] getCommitment() {
		BigInteger[] ret = new BigInteger[3];
		ret[0] = attributes[DISCLOSED_ATTRIBUTE]; /* we return the ticket type */
		ret[1] = blindedAttributeExpression;

		alphas = new BigInteger[NO_OF_ATTRIBUTES];
		for (int i = 0; i < NO_OF_ATTRIBUTES; i++) {
			alphas[i] = new BigInteger(BASE_BITS, new Random()).mod(v);
		}
		/*
		 * we disclose this specific attribute, so we don't want it in the
		 * commitment
		 */
		alphas[DISCLOSED_ATTRIBUTE] = BigInteger.ZERO;

		do {
			beta = new BigInteger(BASE_BITS, new Random()).mod(n);
		} while (beta.gcd(n).compareTo(BigInteger.ONE) != 0);

		BigInteger commitment = BigInteger.ONE;
		for (int i = 0; i < NO_OF_ATTRIBUTES; i++) {
			commitment = commitment.multiply(bases[i].modPow(alphas[i], n))
					.mod(n);
		}
		commitment = commitment.multiply(beta.modPow(v, n)).mod(n);
		ret[2] = commitment;
		return ret;
	}

	public void setChallenge(BigInteger chal) {
		challenge = chal;
	}

	public BigInteger[] getProof() {
		BigInteger s = BigInteger.ONE;
		BigInteger[] rs = new BigInteger[NO_OF_ATTRIBUTES + 1];
		BigInteger[] ds = new BigInteger[NO_OF_ATTRIBUTES];
		BigInteger[] dr;
		for (int i = 0; i < NO_OF_ATTRIBUTES; i++) {
			dr = challenge.multiply(attributes[i]).add(alphas[i])
					.divideAndRemainder(v);
			rs[i + 1] = dr[1];
			ds[i] = dr[0];
		}
		/* we disclose this attribute */
		rs[DISCLOSED_ATTRIBUTE + 1] = BigInteger.ZERO;
		ds[DISCLOSED_ATTRIBUTE] = BigInteger.ZERO;

		for (int i = 0; i < NO_OF_ATTRIBUTES; i++) {
			s = s.multiply(bases[i].modPow(ds[i], n)).mod(n);
		}
		s = s.multiply(b.modPow(challenge, n)).mod(n);
		s = s.multiply(beta).mod(n);
		rs[0] = s;
		return rs;
	}

	public BigInteger getCertificateRequest() {
		do {
			beta1 = new BigInteger(BASE_BITS, new Random()).mod(n);
		} while (beta1.gcd(n).compareTo(BigInteger.ONE) != 0);
		do {
			beta2 = new BigInteger(BASE_BITS, new Random()).mod(n);
		} while (beta2.gcd(n).compareTo(BigInteger.ONE) != 0);
		beta3 = new BigInteger(BASE_BITS, new Random()).mod(v);

		BigInteger APrime = beta1.modPow(v, n).multiply(attributeExpression)
				.mod(n);
		cPrime = h.multiply(attributeExpression).mod(n).modPow(beta3, n)
				.multiply(challenge).mod(n).multiply(beta2.modPow(v, n)).mod(n);
		certificate[0] = cPrime;

		byte[] byteHash = null;
		SHA1Digest md = new SHA1Digest();
		md.update(APrime.toByteArray(), 0, APrime.toByteArray().length);
		md.update(cPrime.toByteArray(), 0, cPrime.toByteArray().length);
		byteHash = new byte[md.getByteLength()];
		md.doFinal(byteHash, 0);
		BigInteger hash = new BigInteger(byteHash);
		c = hash.multiply(beta3).mod(v);
		return c;
	}

	public void setCertificateChallenge(BigInteger chal) {
		challenge = chal;
	}

	public void setCertificate(BigInteger signature) {
		/* verify */
		BigInteger lhs = signature.modPow(v, n);
		BigInteger rhs = h.multiply(attributeExpression).mod(n).modPow(c, n)
				.multiply(challenge).mod(n);
		if (lhs.compareTo(rhs) != 0)
			System.out.println("[WARNING] failed signature check!");
		else {
			rPrime = h.multiply(attributeExpression).mod(n).modPow(
					cPrime.add(beta3).divide(v), n).multiply(signature).mod(n)
					.multiply(beta2).mod(n).multiply(beta1.modPow(cPrime, n))
					.mod(n);
			certificate[1] = rPrime;
		}
	}
}