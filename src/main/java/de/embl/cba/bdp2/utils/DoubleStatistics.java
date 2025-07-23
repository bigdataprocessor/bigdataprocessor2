/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.utils;

import java.util.DoubleSummaryStatistics;

public class DoubleStatistics extends DoubleSummaryStatistics
{

	private double sumOfSquare = 0.0d;
	private double sumOfSquareCompensation; // Low order bits of sum
	private double simpleSumOfSquare; // Used to compute right sum for non-finite inputs

	@Override
	public void accept(double value) {
		super.accept(value);
		double squareValue = value * value;
		simpleSumOfSquare += squareValue;
		sumOfSquareWithCompensation(squareValue);
	}

	public DoubleStatistics combine( DoubleStatistics other) {
		super.combine(other);
		simpleSumOfSquare += other.simpleSumOfSquare;
		sumOfSquareWithCompensation(other.sumOfSquare);
		sumOfSquareWithCompensation(other.sumOfSquareCompensation);
		return this;
	}

	private void sumOfSquareWithCompensation(double value) {
		double tmp = value - sumOfSquareCompensation;
		double velvel = sumOfSquare + tmp; // Little wolf of rounding error
		sumOfSquareCompensation = (velvel - sumOfSquare) - tmp;
		sumOfSquare = velvel;
	}

	public double getSumOfSquare() {
		double tmp =  sumOfSquare + sumOfSquareCompensation;
		if (Double.isNaN(tmp) && Double.isInfinite(simpleSumOfSquare)) {
			return simpleSumOfSquare;
		}
		return tmp;
	}

	public final double getStandardDeviation() {
		return getCount() > 0 ? Math.sqrt((getSumOfSquare() / getCount()) - Math.pow(getAverage(), 2)) : 0.0d;
	}

}
