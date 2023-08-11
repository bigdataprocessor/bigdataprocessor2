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
package de.embl.cba.bdp2.process.align.splitchip;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;

import static de.embl.cba.bdp2.process.align.splitchip.SplitChipMergeUtils.asIntervalXYC;
import static de.embl.cba.bdp2.utils.DimensionOrder.C;

public class SplitChipMerger
{
	private ArrayList< FinalInterval > intervalsXYC;

	public SplitChipMerger()
	{
		intervalsXYC = new ArrayList<>(  );
	}

	public void addIntervalXYC( int minX, int minY, int sizeX, int sizeY, int channel )
	{
		intervalsXYC.add( asIntervalXYC(
			new long[]{ minX, minY },
			new long[]{ sizeX, sizeY },
			channel ) );
	}

	public void addIntervalXYC( long[] intervalXYMinXYSpanC )
	{
		intervalsXYC.add( asIntervalXYC(
				new long[]{ intervalXYMinXYSpanC[ 0 ], intervalXYMinXYSpanC[ 1] },
				new long[]{ intervalXYMinXYSpanC[ 2 ], intervalXYMinXYSpanC[ 3 ] },
				intervalXYMinXYSpanC[ 4] ) );
	}

	public static < R extends RealType< R > & NativeType< R > >
	Image< R > mergeRegionsXYC( Image< R > image, List< long [] > regionsXminYminXdimYdimC )
	{
		final String[] newChannelNames = new String[ regionsXminYminXdimYdimC.size() ];
		ArrayList< FinalInterval > intervalsXYC = new ArrayList<>();

		for ( int outputChannel = 0; outputChannel < regionsXminYminXdimYdimC.size(); outputChannel++ )
		{
			final long[] region = regionsXminYminXdimYdimC.get( outputChannel );
			final long inputChannel = region[ 4 ];
			final long xMin = region[ 0 ];
			final long yMin = region[ 1 ];
			intervalsXYC.add( asIntervalXYC( new long[]{ xMin, yMin }, new long[]{ region[ 2 ], region[ 3 ] }, inputChannel ) );
			newChannelNames[ outputChannel ] = image.getChannelNames()[ (int) inputChannel ] + "_x" + xMin + "_y" + yMin;
		}

		final RandomAccessibleInterval< R > merge = mergeRegionsXYC( image.getRai(), intervalsXYC );

		final Image< R > mergeImage = new Image( image );
		mergeImage.setName( image.getName() + "_merged" );
		mergeImage.setChannelNames( newChannelNames );
		mergeImage.setRai( merge );

		return mergeImage;
	}

	public static < R extends RealType< R > & NativeType< R > >
	Image< R > mergeIntervalsXYZ(
			Image< R > image,
			ArrayList< ? extends Interval > intervalsXYZ,
			int channel )
	{
		RandomAccessibleInterval< R > raiXYZCT = image.getRai();

		final ArrayList< RandomAccessibleInterval< R > > crops
				= new ArrayList<>();

		for ( Interval intervalXYZ : intervalsXYZ )
		{
			final FinalInterval interval5D = intervalXYZasXYZCT( raiXYZCT, intervalXYZ );

			final IntervalView crop =
					Views.zeroMin(
							Views.interval(
									Views.extendZero( raiXYZCT ),
									interval5D ) );

			crops.add( Views.hyperSlice( crop, C, channel ) );
		}

		final RandomAccessibleInterval< R > merged = Views.stack( crops );

		final IntervalView< R > permute = Views.permute( merged, 3, 4 );

		Image< R > outputImage = new Image( image );
		outputImage.setRai( permute );
		outputImage.setName( image.getName() + "-splitchip" );

		return outputImage;
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > mergeRegionsXYC( RandomAccessibleInterval< R > raiXYZCT, ArrayList< ? extends Interval > intervalsXYC )
	{
		final ArrayList< RandomAccessibleInterval< R > > crops
				= new ArrayList<>();

		for ( Interval interval : intervalsXYC )
		{
			final FinalInterval intervalXYZCT = intervalXYCasXYZCT( raiXYZCT, interval );

			Logger.log( "Split Image Merging Interval [X, Y, Z, C, T]: " + intervalXYZCT );

			final FinalInterval union = Intervals.union( raiXYZCT, intervalXYZCT );

			if ( ! Intervals.equals( union, raiXYZCT ) )
			{
				System.err.println( "The region to be merged: " + intervalXYZCT
						+ "\nis outside the image bounds: " + raiXYZCT );
				throw new UnsupportedOperationException();
			};

			final IntervalView crop =
					Views.zeroMin(
							Views.interval(
									raiXYZCT,
									intervalXYZCT ) );

			// NOTE: below it is always channel 0, because of above Views.zeroMin
			crops.add( Views.hyperSlice( crop, C, 0 ) );
		}

		final RandomAccessibleInterval< R > merged = Views.stack( crops );

		final IntervalView< R > permute = Views.permute( merged, 3, 4 );

		return permute;
	}

	public static < R extends RealType< R > & NativeType< R > >
	FinalInterval intervalXYZasXYZCT( RandomAccessibleInterval< R > raiXYZCT,
									  Interval interval )
	{
		final long[] min = Intervals.minAsLongArray( raiXYZCT );
		final long[] max = Intervals.maxAsLongArray( raiXYZCT );

		for ( int d = 0; d < interval.numDimensions(); d++ )
		{
			min[ d ] = interval.min( d );
			max[ d ] = interval.max( d );
		}

		return new FinalInterval( min, max );
	}

	public static < R extends RealType< R > & NativeType< R > >
	FinalInterval intervalXYCasXYZCT( RandomAccessibleInterval< R > raiXYZCT, Interval interval )
	{
		final long[] min = Intervals.minAsLongArray( raiXYZCT );
		final long[] max = Intervals.maxAsLongArray( raiXYZCT );

		// XY
		for ( int d = 0; d < 2; d++ )
		{
			min[ d ] = interval.min( d );
			max[ d ] = interval.max( d );
		}

		// C
		min[ 3 ] = interval.min( 2 );
		max[ 3 ] = interval.max( 2 );

		return new FinalInterval( min, max );
	}
}
