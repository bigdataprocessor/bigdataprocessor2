/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
package de.embl.cba.bdp2.devel.register;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.AffineGet;

public class AffineGetNull implements AffineGet
{

	@Override
	public double get( int i, int i1 )
	{
		return 0;
	}

	@Override
	public double[] getRowPackedCopy()
	{
		return new double[ 0 ];
	}

	@Override
	public RealLocalizable d( int i )
	{
		return null;
	}

	@Override
	public void applyInverse( double[] doubles, double[] doubles1 )
	{

	}

	@Override
	public void applyInverse( RealPositionable realPositionable, RealLocalizable realLocalizable )
	{

	}

	@Override
	public AffineGet inverse()
	{
		return null;
	}

	@Override
	public int numSourceDimensions()
	{
		return 0;
	}

	@Override
	public int numTargetDimensions()
	{
		return 0;
	}

	@Override
	public void apply( double[] doubles, double[] doubles1 )
	{

	}

	@Override
	public void apply( RealLocalizable realLocalizable, RealPositionable realPositionable )
	{

	}

	@Override
	public AffineGet copy()
	{
		return null;
	}

	@Override
	public int numDimensions()
	{
		return 0;
	}
}
