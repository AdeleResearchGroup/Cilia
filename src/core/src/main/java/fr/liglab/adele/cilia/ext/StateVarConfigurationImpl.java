package fr.liglab.adele.cilia.ext;

import java.util.HashMap;
import java.util.Map;

public class StateVarConfigurationImpl {

	private boolean enable;
	private String controlFlow;
	private int queueSize;
	private double veryLow, low, high, veryHigh;

	private Map setup;

	public StateVarConfigurationImpl(boolean enable) {
		this.enable = enable;
		this.controlFlow = null;
		this.queueSize = 1;
		this.veryLow = Double.NaN;
		this.low = Double.NaN;
		this.veryHigh = Double.NaN;
		this.high = Double.NaN;
		setup = new HashMap();

	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setControlFlow(String controlFlow) {
		this.controlFlow = controlFlow;
	}

	public String getControlFlow() {
		return controlFlow;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setVeryLow(double veryLow) {
		this.veryLow = veryLow;
	}

	public double getVeryLow() {
		return veryLow;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getLow() {
		return low;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getHigh() {
		return high;
	}

	public void setVeryHigh(double veryHigh) {
		this.veryHigh = veryHigh;
	}

	public double getVeryHigh() {
		return veryHigh;
	}

	public Map configuration() {
		HashMap config = new HashMap(7);
		config.put("enable", Boolean.toString(enable));
		config.put("flow-control", controlFlow);
		config.put("queue", Integer.toString(queueSize));
		config.put("very-low", Double.toString(veryLow));
		config.put("low", Double.toString(low));
		config.put("very-high", Double.toString(veryHigh));
		config.put("high", Double.toString(high));

		return config;
	}
}
