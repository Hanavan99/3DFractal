package com.fractal.model.fractal;

import java.util.HashMap;
import java.util.Map;

import com.fractal.model.Complex;
import com.fractal.model.IFractal;

public abstract class AbstractFractal implements IFractal {

	private Map<String, Complex> options = new HashMap<String, Complex>();
	
	public void addOption(String name, Complex startValue) {
		options.put(name, startValue);
	}
	
	public Complex getOption(String name) {
		return options.get(name);
	}
	
}
