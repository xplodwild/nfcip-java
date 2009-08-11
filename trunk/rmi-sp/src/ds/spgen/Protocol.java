package ds.spgen;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Protocol {
	private byte protocolNumber;
	private String protocolName;
	private List<Step> protocolSteps;

	private int firstStepNumber;
	private int lastStepNumber;

	/**
	 * Convenient storage for all the methods from an interface
	 * 
	 * @param protocolNumber
	 * @param protocolName
	 */
	Protocol(byte protocolNumber, String protocolName, int firstStepNumber) {
		this.protocolNumber = protocolNumber;
		this.protocolName = protocolName;
		protocolSteps = new ArrayList<Step>();
		this.firstStepNumber = firstStepNumber;
	}

	/**
	 * Add method
	 * 
	 * @param mthds
	 *            the array of methods to store
	 * @throws Exception
	 */
	public void addSteps(Method[] mthds) throws Exception {
		lastStepNumber = firstStepNumber + mthds.length;
		for (int i = 0; i < mthds.length; i++) {
			Method m = mthds[i];
			Step pStep = new Step(m, (i + firstStepNumber));
			protocolSteps.add(pStep);
		}
	}

	/**
	 * Get a specific method by number
	 * 
	 * @param i
	 *            the position of the method
	 * @return the method
	 */
	Step getStep(int i) {
		return protocolSteps.get(i);
	}

	/**
	 * Get all the methods
	 * 
	 * @return the methods
	 */
	List<Step> getSteps() {
		return protocolSteps;
	}

	public byte getProtocolNumber() {
		return protocolNumber;
	}

	public int getLastStepNumber() {
		return lastStepNumber;
	}

	public int getFirstStepNumber() {
		return firstStepNumber;
	}

	public String getProtocolName() {
		return protocolName;
	}
}
