package com.fractal.model;

public class EvalResult {

	private int iterations;
	private boolean valid;
	private byte[] color;

	public EvalResult(int iterations, boolean valid, byte[] color) {
		super();
		this.iterations = iterations;
		this.valid = valid;
		this.color = color;
	}

	/**
	 * @return the iterations
	 */
	public int getIterations() {
		return iterations;
	}

	/**
	 * @return the valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * @return the color
	 */
	public byte[] getColor() {
		return color;
	}

}
