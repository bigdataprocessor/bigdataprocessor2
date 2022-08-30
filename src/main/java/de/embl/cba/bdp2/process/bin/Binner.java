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
package de.embl.cba.bdp2.process.bin;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.lazyalgorithm.view.NeighborhoodViews;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class Binner
{
	public static double[] getBinnedVoxelSize( long[] span, double[] voxelSpacing )
	{
		final double[] newVoxelSize = new double[ voxelSpacing.length ];

		for ( int d = 0; d < 3; d++ )
			newVoxelSize[ d ] = voxelSpacing[ d ] * span[ d ];

		return newVoxelSize;
	}

	public static < T extends RealType< T > & NativeType< T > >
	Image< T > bin( Image< T > inputImage, long[] span )
	{
		RandomAccessibleInterval< T > binnedRai = binImageWithNonZeroSpatialOffset( inputImage, span );

		Image< T > binnedImage = new Image( inputImage );
		binnedImage.setRai( binnedRai );
		binnedImage.setVoxelDimensions( getBinnedVoxelSize( span, inputImage.getVoxelDimensions() ) );

		return binnedImage;
	}

	public static < T extends RealType< T > & NativeType< T > > RandomAccessibleInterval< T > binImageWithNonZeroSpatialOffset( Image< T > inputImage, long[] span )
	{
		RandomAccessibleInterval< T > rai = inputImage.getRai();

		final long[ ] min = new long[ 5 ];
		rai.min( min );
		rai = Views.zeroMin( rai );

		RandomAccessibleInterval< T > binnedRai = NeighborhoodViews.averageBinnedView( rai, span );

		for ( int d = 0; d < 3; d++ )
		{
			min[ d ] /= span[ d ];
		}
		binnedRai = Views.translate( binnedRai, min );
		return binnedRai;
	}
}
