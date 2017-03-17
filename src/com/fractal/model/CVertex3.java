package com.fractal.model;

public class CVertex3 {

	private double x;
	private double y;
	private double z;
	private double r;
	private double g;
	private double b;

	public CVertex3(double x, double y, double z, double r, double g, double b) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @return the z
	 */
	public double getZ() {
		return z;
	}

	/**
	 * @return the r
	 */
	public double getR() {
		return r;
	}

	/**
	 * @return the g
	 */
	public double getG() {
		return g;
	}

	/**
	 * @return the b
	 */
	public double getB() {
		return b;
	}

}
