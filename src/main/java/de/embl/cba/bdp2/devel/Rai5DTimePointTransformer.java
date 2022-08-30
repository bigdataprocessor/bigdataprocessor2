/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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
package de.embl.cba.bdp2.devel;

import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.FinalInterval;
import net.imglib2.FinalRealInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.realtransform.AffineRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class Rai5DTimePointTransformer< R extends RealType< R > & NativeType< R > > extends RecursiveTask< RandomAccessibleInterval >
{
	private RandomAccessibleInterval raiXYZCT;
	private int t;
	private final int numChannels;
	private final AffineTransform3D affine;
	private InterpolatorFactory interpolatorFactory;

	public Rai5DTimePointTransformer( RandomAccessibleInterval raiXYZCT, int timePoint, AffineTransform3D affine, InterpolatorFactory interpolatorFactory) {
		this.raiXYZCT = raiXYZCT;
		this.t = timePoint;
		this.numChannels = (int) raiXYZCT.dimension( DimensionOrder.C );
		this.affine = affine;
		this.interpolatorFactory = interpolatorFactory;
	}

	@Override
	protected RandomAccessibleInterval< R > compute()
	{
		RandomAccessibleInterval< R > timeSlice = Views.hyperSlice( raiXYZCT, DimensionOrder.T, t);
		List< RandomAccessibleInterval< R > > channels = new ArrayList<>();
		for ( int channel = 0; channel < numChannels; ++channel)
		{
			RandomAccessibleInterval< R > volume = Views.hyperSlice( timeSlice, DimensionOrder.C, channel);
			RealRandomAccessible< R > rra = Views.interpolate( Views.extendZero( volume ), this.interpolatorFactory);
			AffineRandomAccessible ara = RealViews.affine( rra, affine );
			FinalRealInterval bounds = affine.estimateBounds( volume );
			FinalInterval transformedInterval = Utils.asIntegerInterval( bounds );
			RandomAccessibleInterval intervalView = Views.interval( ara, transformedInterval );
			channels.add( intervalView );
		}
		return Views.stack( channels );
	}
}
