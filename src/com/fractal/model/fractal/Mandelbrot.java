package com.fractal.model.fractal;

import com.fractal.model.Complex;
import com.fractal.model.EvalResult;
import com.fractal.model.FractalMode;

public class Mandelbrot extends AbstractFractal {

	@Override
	public EvalResult evaluate(FractalMode mode, int iterations, Object[] args) {
		Complex z = new Complex(0, 0);
		Complex c = new Complex((double) args[0], (double) args[1]);
		boolean inset = true;
		int i;
		for (i = 0; i < iterations; i++) {
			z.multiply(z);
			z.add(c);
			if (z.abs() > 2) {
				inset = false;
			}
		}
		return new EvalResult(i, inset, new byte[] { 127, 127, 127 });
	}

}
