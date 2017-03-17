package com.fractal.model;

public class Complex {

	private double re;
	private double im;
	
	public Complex(double re, double im) {
		this.re = re;
		this.im = im;
	}
	
	public double abs() {
		return Math.sqrt(re * re + im * im);
	}
	
	public void add(Complex b) {
		re += b.re;
		im += b.im;
	}
	
	public void multiply(Complex b) {
		double _re = (re * b.re) - (im * b.im);
		double _im = (re * b.im) + (im * b.re);
		re = _re;
		im = _im;
	}
	
	public double getRe() {
		return re;
	}
	
	public double getIm() {
		return im;
	}
	
	@Override
	public String toString() {
		return re + (im >= 0 ? "+" : "-") + Math.abs(im) + "i";
	}
	
}
