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
public class CachedCellImgReplacer
		< T extends Type< T > & NativeType< T >, S extends Type< S > & NativeType< S > >
{


	private final RandomAccessible< T > rai;
	private final CachedCellImg< T, ? > cachedCellImg;


	public CachedCellImgReplacer( final RandomAccessible< T > rai,
								  final CachedCellImg< T, ? > cachedCellImg )
	{
		this.rai = rai;
		this.cachedCellImg = cachedCellImg;
	}


	public RandomAccessibleInterval< T > get()
	{
		return ( RandomAccessibleInterval< T > ) replace( rai );
	}

	private RandomAccessible< T > replace( RandomAccessible< T > rai )
	{
		if ( rai instanceof CachedCellImg )
		{
			// Replace the CachedCellImg with the given one
			return cachedCellImg;
		}
		else if ( rai instanceof IntervalView )
		{
			final IntervalView< T > view = ( IntervalView< T > ) rai;
			final RandomAccessible< T > replace = replace( view.getSource() );
			final IntervalView intervalView = new IntervalView( replace, view );
			return intervalView;
		}
		else if ( rai instanceof MixedTransformView )
		{
			final MixedTransformView< T > view = ( MixedTransformView< T > ) rai;

			final RandomAccessible< T > replace = replace( view.getSource() );

			final MixedTransformView< T > mixedTransformView =
					new MixedTransformView<>(
							replace,
							view.getTransformToSource() );

			return mixedTransformView;
		}
		else if ( rai instanceof SubsampleIntervalView )
		{
			final SubsampleIntervalView< T > view = ( SubsampleIntervalView< T > ) rai;

			final RandomAccessibleInterval< T > replace =
					( RandomAccessibleInterval< T > ) replace( view.getSource() );

			final SubsampleIntervalView subsampleIntervalView =
					new SubsampleIntervalView(
							replace,
							view.getSteps() );

			return subsampleIntervalView;
		}
		else if ( rai instanceof SubsampleView ) // TODO: do we need this ?
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
		else if ( rai instanceof ConvertedRandomAccessibleInterval )
		{
			final ConvertedRandomAccessibleInterval< T, S > view
					= ( ConvertedRandomAccessibleInterval< T, S > ) rai;

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
		else if ( rai instanceof StackView )
		{
			final StackView< T > view = ( StackView< T > ) rai;

			final List< RandomAccessibleInterval< T > > slices = view.getSourceSlices();

			final List< RandomAccessible< T > > replacedSlices = new ArrayList<>();
			for ( RandomAccessibleInterval< T > slice : slices )
				replacedSlices.add( replace( slice ) );

			final StackView stackView = new StackView( replacedSlices );

			return stackView;
		}
		else if ( rai instanceof RectangleShape2.NeighborhoodsAccessible )
		{
			// TODO: I did not manage to put the typing here
			final RectangleShape2.NeighborhoodsAccessible< T > view =
					( RectangleShape2.NeighborhoodsAccessible ) rai;

			final RandomAccessible< T > replace = replace( view.getSource() );

			final RectangleShape2.NeighborhoodsAccessible neighborhoodsAccessible
					= new RectangleShape2.NeighborhoodsAccessible(
							replace,
							view.getSpan(),
							view.getFactory() );

			return neighborhoodsAccessible;
		}
		else if ( rai instanceof ExtendedRandomAccessibleInterval )
		{
			// TODO: I did not manage to put the typing here
			final ExtendedRandomAccessibleInterval view =
					( ExtendedRandomAccessibleInterval ) rai;

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
