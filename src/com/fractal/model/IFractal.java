package com.fractal.model;

public interface IFractal {

	public EvalResult evaluate(FractalMode mode, int iterations, Object[] args);
	
}
