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
package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.imglib2.LazyStackView;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.lazyalgorithm.converter.NeighborhoodAverageConverter;
import de.embl.cba.neighborhood.RectangleShape2;
import net.imglib2.EuclideanSpace;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.converter.Converter;
import net.imglib2.converter.read.ConvertedRandomAccessibleInterval;
import net.imglib2.interpolation.Interpolant;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineRandomAccessible;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.view.*;

import java.util.ArrayList;
import java.util.List;

/*
 * For view cascades ending in a {@link CachedCellImg},
 * the {@link CachedCellImg} will be replaced with the
 * provided one.
 *
 * This can be useful if different load strategies (e.g. cell sizes)
 * are more optimal for certain task than others.
 * For example, for interactive viewing of the data rather small cells
 * may be optimal. For processing of the entire data set
 * rather large cells may however yield a better performance.
 *
 * @author Christian Tischer
 */
public class CachedCellImgReplacer< T extends Type< T > & NativeType< T >, S extends Type< S > & NativeType< S > >
{
	private final RandomAccessible< T > ra;
	private final RandomAccessibleInterval< T > cachedCellImg;


	public CachedCellImgReplacer( final RandomAccessible< T > ra,
								  final RandomAccessibleInterval< T > cachedCellImg )
	{
		this.ra = ra;
		this.cachedCellImg = cachedCellImg;
	}

	/*
	 * TODO: Figure out why I need to have RandomAccessible inside
	 *   the replace function and then have to do the casting here...
	 *
	 * @return
	 * 		   The input RAI but now backed by the given CachedCellImg
	 */
	public RandomAccessibleInterval< T > get()
	{
		return ( RandomAccessibleInterval< T > ) replace( ra );
	}

	private EuclideanSpace replace( EuclideanSpace ra )
	{
		if ( ra instanceof CachedCellImg )
		{
			// Replace the CachedCellImg with the given one
			return cachedCellImg;
		}
		if ( ra instanceof Interpolant )
		{
			final Interpolant interpolant = ( Interpolant ) ra;
			final EuclideanSpace source = replace( ( EuclideanSpace ) interpolant.getSource() );
			final Interpolant replaced = new Interpolant<>( source, interpolant.getInterpolatorFactory(), source.numDimensions() );
			return replaced;
		}
		else if ( ra instanceof IntervalView )
		{
			final IntervalView< T > view = ( IntervalView< T > ) ra;

			final RandomAccessible< T > replace = ( RandomAccessible< T > ) replace( view.getSource() );

			final IntervalView intervalView = new IntervalView( replace, view );

			return intervalView;
		}
		else if ( ra instanceof MixedTransformView )
		{
			final MixedTransformView< T > view = ( MixedTransformView< T > ) ra;

			final RandomAccessible< T > replace = ( RandomAccessible< T > ) replace( view.getSource() );

			final MixedTransformView< T > mixedTransformView =
					new MixedTransformView<>(
							replace,
							view.getTransformToSource() );

			return mixedTransformView;
		}
		else if ( ra instanceof SubsampleIntervalView )
		{
			final SubsampleIntervalView< T > view = ( SubsampleIntervalView< T > ) ra;

			final RandomAccessibleInterval< T > replace =
					( RandomAccessibleInterval< T > ) replace( view.getSource() );

			final SubsampleIntervalView subsampleIntervalView =
					new SubsampleIntervalView(
							replace,
							view.getSteps() );

			return subsampleIntervalView;
		}
		else if ( ra instanceof SubsampleView ) // TODO: do we need this ?
		{
			Logger.error( "SubsampleView..." );
			return null;
//			final SubsampleView< T > view = ( SubsampleView< T > ) rai;
//			final VolatileViewData< T, V > sourceData =
//					wrapAsVolatileViewData( view.getSource(), queue, hints );
//
//			final VolatileViewData< T, V > volatileViewData = new VolatileViewData<>(
//					new SubsampleView<>( sourceData.getImg(), view.getSteps() ),
//						sourceData.getCacheControl(),
//						sourceData.getType(),
//						sourceData.getVolatileType() );
//
//			return volatileViewData;
		}
		else if ( ra instanceof ConvertedRandomAccessibleInterval )
		{
			final ConvertedRandomAccessibleInterval< T, S > view
					= ( ConvertedRandomAccessibleInterval< T, S > ) ra;

			final RandomAccessibleInterval< T > replace =
					( RandomAccessibleInterval< T > ) replace( view.getSource() );

			final S destinationType = view.getDestinationType();

			final Converter< ? super T, ? super S > converter = view.getConverter();

			if ( converter instanceof NeighborhoodAverageConverter )
			{
				// TODO: remove this if case?
				final ConvertedRandomAccessibleInterval converted
						= new ConvertedRandomAccessibleInterval(
								replace,
								converter,
								destinationType );

				return converted;
			}
			else
			{
				final ConvertedRandomAccessibleInterval converted
						= new ConvertedRandomAccessibleInterval(
								replace,
								converter,
								destinationType );

				return converted;
			}
		}
		else if ( ra instanceof StackView )
		{
			final StackView< T > view = ( StackView< T > ) ra;

			final List< RandomAccessibleInterval< T > > slices = view.getSourceSlices();

			final List< RandomAccessible< T > > replacedSlices = new ArrayList<>();
			for ( RandomAccessibleInterval< T > slice : slices )
			{
				replacedSlices.add( ( RandomAccessible< T > ) replace( slice ) );
			}

			final StackView stackView = new StackView( replacedSlices );

			return stackView;
		}
		else if ( ra instanceof LazyStackView )
		{
			final LazyStackView< T > view = ( LazyStackView< T > ) ra;

			final List< RandomAccessibleInterval< T > > slices = view.getSourceSlices();

			final List< RandomAccessible< T > > replacedSlices = new ArrayList<>();
			for ( RandomAccessibleInterval< T > slice : slices )
				replacedSlices.add( ( RandomAccessible< T > ) replace( slice ) );

			final LazyStackView stackView = new LazyStackView( replacedSlices );

			return stackView;
		}
		else if ( ra instanceof RectangleShape2.NeighborhoodsAccessible )
		{
			// TODO: I did not manage to put the typing here
			final RectangleShape2.NeighborhoodsAccessible< T > view =
					( RectangleShape2.NeighborhoodsAccessible ) ra;

			final RandomAccessible< T > replace = ( RandomAccessible< T > ) replace( view.getSource() );

			final RectangleShape2.NeighborhoodsAccessible neighborhoodsAccessible
					= new RectangleShape2.NeighborhoodsAccessible(
							replace,
							view.getSpan(),
							view.getFactory() );

			return neighborhoodsAccessible;
		}
		else if ( ra instanceof ExtendedRandomAccessibleInterval )
		{
			// TODO: I did not manage to put the typing here
			final ExtendedRandomAccessibleInterval view =
					( ExtendedRandomAccessibleInterval ) ra;

			final RandomAccessibleInterval< T > replace =
					( RandomAccessibleInterval< T > ) replace( view.getSource() );

			final ExtendedRandomAccessibleInterval extended
					= new ExtendedRandomAccessibleInterval(
							replace,
							view.getOutOfBoundsFactory() );

			return extended;
		}
		else if ( ra instanceof AffineRandomAccessible )
		{
			final AffineRandomAccessible view = ( AffineRandomAccessible ) ra;
			final RealRandomAccessible< T > replace = ( RealRandomAccessible< T > ) replace( view.getSource() );
			final AffineRandomAccessible affineRandomAccessible = new AffineRandomAccessible( replace, ( AffineGet ) view.getTransformToSource() );
			return affineRandomAccessible;
		}
		else
		{
			System.out.printf( "Cache cannot be replaced for image of class: " + ra.getClass().getSimpleName() + "\n" );
			throw new IllegalArgumentException();
		}
	}
}
