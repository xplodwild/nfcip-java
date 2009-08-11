package ds.ov2;

import java.math.BigInteger;
import java.util.Random;
import ds.sp.RMIException;

public class Gate {
	private BigInteger v;
	private BigInteger n;
	private BigInteger[] bases;
	private Random random;

	public Gate(Issuer i) {
		v = i.getPublicKey();
		n = i.getModulus();
		bases = i.getPublicBases();
		random = new Random();
	}

	public boolean isValidSecondClassTicket(AttributeProving r)
			throws RMIException {
		/* get the commitment */
		BigInteger[] com = r.getCommitment();
		BigInteger disclosedAttribute = com[0];
		BigInteger blindedAttributeExpression = com[1];
		BigInteger commitment = com[2];

		/* set the challenge */
		BigInteger challenge = new BigInteger(AttributeProving.BASE_BITS, random).mod(v);
		r.setChallenge(challenge);

		/* get the proof */
		BigInteger[] proof = r.getProof();

		/* verification of the responses by the prover */
		BigInteger s = proof[0];

		BigInteger lhs = BigInteger.ONE;
		for (int i = 0; i < AttributeProving.NO_OF_ATTRIBUTES; i++) {
			lhs = lhs.multiply(bases[i].modPow(proof[i + 1], n)).mod(n);
		}
		lhs = lhs.multiply(s.modPow(v, n)).mod(n);

		BigInteger rhs = blindedAttributeExpression.multiply(
				bases[1].modPow(disclosedAttribute.negate(), n)).mod(n).modPow(
				challenge, n).multiply(commitment).mod(n);

		/* for now when the card doesn't lie it's a valid card */
		return lhs.compareTo(rhs) == 0;

		// if (lhs.compareTo(rhs) == 0) {
		// /*
		// * proof is valid, now we want to check whether the card is a second
		// * class ticket, we check whether a certain bit is set in the
		// attribute
		// */
		// return disclosedAttribute.and(new BigInteger("2")).compareTo(
		// new BigInteger("2")) == 0;
		// } else {
		// return false;
		// }
	}
}
