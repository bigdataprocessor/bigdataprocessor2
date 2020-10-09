package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.lazyalgorithm.converter.NeighborhoodAverageConverter;
import de.embl.cba.neighborhood.RectangleShape2;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.converter.Converter;
import net.imglib2.converter.read.ConvertedRandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.view.*;

import java.util.ArrayList;
import java.util.List;

/**
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
	private final CachedCellImg< T, ? > cachedCellImg;


	public CachedCellImgReplacer( final RandomAccessible< T > ra,
								  final CachedCellImg< T, ? > cachedCellImg )
	{
		this.ra = ra;
		this.cachedCellImg = cachedCellImg;
	}

	/**
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

	private RandomAccessible< T > replace( RandomAccessible< T > ra )
	{
		if ( ra instanceof CachedCellImg )
		{
			// Replace the CachedCellImg with the given one
			return cachedCellImg;
		}
		else if ( ra instanceof IntervalView )
		{
			final IntervalView< T > view = ( IntervalView< T > ) ra;
			final RandomAccessible< T > replace = replace( view.getSource() );
			final IntervalView intervalView = new IntervalView( replace, view );
			return intervalView;
		}
		else if ( ra instanceof MixedTransformView )
		{
			final MixedTransformView< T > view = ( MixedTransformView< T > ) ra;

			final RandomAccessible< T > replace = replace( view.getSource() );

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
				replacedSlices.add( replace( slice ) );

			final StackView stackView = new StackView( replacedSlices );

			return stackView;
		}
		else if ( ra instanceof RectangleShape2.NeighborhoodsAccessible )
		{
			// TODO: I did not manage to put the typing here
			final RectangleShape2.NeighborhoodsAccessible< T > view =
					( RectangleShape2.NeighborhoodsAccessible ) ra;

			final RandomAccessible< T > replace = replace( view.getSource() );

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
		else
		{
			throw new IllegalArgumentException();
		}
	}


}
