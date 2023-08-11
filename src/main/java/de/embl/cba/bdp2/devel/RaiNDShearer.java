/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2023 EMBL
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
import net.imglib2.*;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.realtransform.AffineRandomAccessible;
import net.imglib2.realtransform.AffineTransform;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;


/*
 * This class is experimental.
 * It seems slow and thus not used.
 */
public class RaiNDShearer
{
	@Deprecated
	public static <T extends RealType<T> & NativeType<T> >
	RandomAccessibleInterval shear( RandomAccessibleInterval rai5D, ShearingSettings shearingSettings)
	{
		final AffineTransform affine5D = new AffineTransform( 5 );
		affine5D.set(shearingSettings.shearingFactorX, 0, 2);
		affine5D.set(shearingSettings.shearingFactorY, 1, 2);

		RealRandomAccessible rra = Views.interpolate(
				Views.extendZero( rai5D ),
				new NearestNeighborInterpolatorFactory());

		AffineRandomAccessible af = RealViews.affine( rra, affine5D );

		final FinalInterval interval5D = getInterval5DAfterShearing( rai5D, shearingSettings );

		RandomAccessibleInterval intervalView = Views.interval( af, interval5D );

		return intervalView;
	}

	private static FinalInterval getInterval5DAfterShearing(
			RandomAccessibleInterval rai5D,
			ShearingSettings shearingSettings )
	{
		AffineTransform3D affine3DtoEstimateBoundsAfterTransformation = new AffineTransform3D();
		affine3DtoEstimateBoundsAfterTransformation.set(shearingSettings.shearingFactorX, 0, 2);
		affine3DtoEstimateBoundsAfterTransformation.set(shearingSettings.shearingFactorY, 1, 2);


		final IntervalView intervalView3D = Views.hyperSlice( Views.hyperSlice( rai5D, DimensionOrder.T, 0 ), DimensionOrder.C, 0 );
		FinalRealInterval transformedRealInterval = affine3DtoEstimateBoundsAfterTransformation.estimateBounds( intervalView3D );

		final Interval interval = Intervals.largestContainedInterval( transformedRealInterval );
		final long[] min = new long[ 5 ];
		final long[] max = new long[ 5 ];
		rai5D.min( min );
		rai5D.max( max );
		interval.min( min );
		interval.max( max );
		return new FinalInterval( min, max );
	}

}
