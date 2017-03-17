package com.fractal.model;

public class Debouncer {

	private int lastvalue = 0;
	
	public int debounce(int value) {
		int ret = value < lastvalue ? 1 : 0;
		lastvalue = value;
		return ret;
	}
	
}
