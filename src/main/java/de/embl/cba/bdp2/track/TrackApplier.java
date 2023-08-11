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
package de.embl.cba.bdp2.track;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.imglib2.LazyStackView;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.RAISlicer;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Arrays;

public class TrackApplier< R extends RealType< R > & NativeType< R > >
{
	private final Image< R > image;

	public TrackApplier( Image< R > image )
	{
		this.image = image;
	}

	public Image< R > applyTrack( Track track )
	{
		final ArrayList< RandomAccessibleInterval< R > > timePoints = new ArrayList<>();
		RandomAccessibleInterval< R > volume = RAISlicer.getVolumeView( this.image.getRai(), 0, 0 );
		Interval union = createUnion( track, volume );
		final R zero = this.image.getType().createVariable();
		zero.setZero();
		final OutOfBoundsConstantValueFactory< R, RandomAccessibleInterval< R > > zeroValueFactory = new OutOfBoundsConstantValueFactory<>( zero );

		for (int t = track.tMin(); t <= track.tMax(); ++t)
		{
			final ArrayList< RandomAccessibleInterval< R > > channels = new ArrayList<>();
			// TODO: Can I shift the channels together?
			for ( int c = 0; c < this.image.getNumChannels(); c++ )
			{
				volume = RAISlicer.getVolumeView( this.image.getRai(), c, t );

				RandomAccessible< R > extendBorder = new ExtendedRandomAccessibleInterval<>( volume, zeroValueFactory );
				final long[] translation = getTranslation( track, t, volume.numDimensions() );
				if ( this.image.getDimensionsXYZCT()[ DimensionOrder.Z ] == 1 )
				{
					int a = 1;
				}
				RandomAccessible< R > translate = Views.translate( extendBorder, translation );
				final IntervalView< R > intervalView = Views.interval( translate, union );

				channels.add( intervalView );
			}

			timePoints.add( Views.stack( channels ) );
		}

		final RandomAccessibleInterval< R > trackView = new LazyStackView<>( timePoints );

		final Image< R > trackViewImage = new Image<>( this.image );
		trackViewImage.setRai( trackView );
		trackViewImage.setName( this.image.getName() + "-track" );
		return trackViewImage;
	}

	// create an interval that encompasses all translated images
	private static < R extends RealType< R > & NativeType< R > > Interval createUnion( Track track, RandomAccessibleInterval< R > image )
	{
		Interval union = null;
		for (int t = track.tMin(); t < track.tMax(); ++t)
		{
			Interval translateInterval = Views.translate( image, getTranslation( track, t, image.numDimensions() ) );
			if ( union == null )
				union = translateInterval;
			else
				union = Intervals.union( union, translateInterval );
		}
		return union;
	}

	private static long[] getTranslation( Track track, int t, int numDimensions )
	{
		final long[] voxelPosition = track.getVoxelPosition( t );
		final long[] shift = Arrays.stream( voxelPosition ).map( x -> -x ).limit( numDimensions ).toArray();
		return shift;
	}
}
