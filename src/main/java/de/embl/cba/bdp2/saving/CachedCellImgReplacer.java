package de.embl.cba.bdp2.saving;

import bdv.img.cache.CreateInvalidVolatileCell;
import bdv.img.cache.VolatileCachedCellImg;
import bdv.util.volatiles.*;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.lazyalgorithm.converter.NeighborhoodAverageConverter;
import de.embl.cba.neighborhood.RectangleShape2;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.algorithm.neighborhood.RectangleNeighborhoodRandomAccess;
import net.imglib2.cache.Cache;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.ref.WeakRefVolatileCache;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.CreateInvalid;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.cache.volatiles.VolatileCache;
import net.imglib2.converter.Converter;
import net.imglib2.converter.read.ConvertedRandomAccessibleInterval;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.img.basictypeaccess.volatiles.VolatileArrayDataAccess;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.view.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.imglib2.img.basictypeaccess.AccessFlags.DIRTY;
import static net.imglib2.img.basictypeaccess.AccessFlags.VOLATILE;

/**
 * For view cascades ending in a {@link CachedCellImg},
 * the {@link CachedCellImg} will be replaced with the
 * provided one.
 *
 * This can be useful if different loading strategies (e.g. cell sizes)
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


	public RandomAccessibleInterval< T > getReplaced()
	{
		return ( RandomAccessibleInterval< T > ) replace();
	}

	private RandomAccessible< T > replace()
	{
		if ( rai instanceof CachedCellImg )
		{
			// Replace the CachedCellImg with the given one
			return cachedCellImg;
		}
		else if ( rai instanceof IntervalView )
		{
			final IntervalView< T > view = ( IntervalView< T > ) rai;
			final RandomAccessible< T > replace = getReplaced( view.getSource() );
			final IntervalView intervalView = new IntervalView( replace, view );
			return intervalView;
		}
		else if ( rai instanceof MixedTransformView )
		{
			final MixedTransformView< T > view = ( MixedTransformView< T > ) rai;

			final RandomAccessible< T > replace = getReplaced( view.getSource() );

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
					( RandomAccessibleInterval< T > ) getReplaced( view.getSource() );

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
					( RandomAccessibleInterval< T > ) getReplaced( view.getSource() );

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
				replacedSlices.add( getReplaced( slice ) );

			final StackView stackView = new StackView( replacedSlices );

			return stackView;
		}
		else if ( rai instanceof RectangleShape2.NeighborhoodsAccessible )
		{
			// TODO: I did not manage to put the typing here
			final RectangleShape2.NeighborhoodsAccessible< T > view =
					( RectangleShape2.NeighborhoodsAccessible ) rai;

			final RandomAccessible< T > replace = getReplaced( view.getSource() );

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
					( RandomAccessibleInterval< T > ) getReplaced( view.getSource() );

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

	private static < T extends NativeType< T >, V extends Volatile< T > >
	boolean isNeighborhood( RandomAccessibleInterval< V > vRAI )
	{
		final RandomAccess< V > vRandomAccess = vRAI.randomAccess();

		// TODO: make more general
		if ( vRandomAccess instanceof RectangleNeighborhoodRandomAccess )
			return true;
		else
			return false;
	}

	@SuppressWarnings( "unchecked" )
	private static < T extends NativeType< T >, V extends Volatile< T > & NativeType< V >, A > VolatileViewData< T, V > wrapCachedCellImg(
			final CachedCellImg< T, A > cachedCellImg,
			SharedQueue queue,
			CacheHints hints )
	{
		final T type = cachedCellImg.createLinkedType();
		final CellGrid grid = cachedCellImg.getCellGrid();
		final Cache< Long, Cell< A > > cache = cachedCellImg.getCache();

		final Set< AccessFlags > flags = AccessFlags.ofAccess( cachedCellImg.getAccessType() );
		if ( !flags.contains( VOLATILE ) )
			throw new IllegalArgumentException( "underlying " + CachedCellImg.class.getSimpleName() + " must have volatile access type" );
		final boolean dirty = flags.contains( DIRTY );

		final V vtype = ( V ) VolatileTypeMatcher.getVolatileTypeForType( type );
		if ( queue == null )
			queue = new SharedQueue( 1, 1 );
		if ( hints == null )
			hints = new CacheHints( LoadingStrategy.VOLATILE, 0, false );
		@SuppressWarnings( "rawtypes" )
		final VolatileCachedCellImg< V, ? > img = createVolatileCachedCellImg( grid, vtype, dirty, ( Cache ) cache, queue, hints );

		return new VolatileViewData<>( img, queue, type, vtype );
	}

	private static < T extends NativeType< T >, A extends VolatileArrayDataAccess< A > > VolatileCachedCellImg< T, A > createVolatileCachedCellImg(
			final CellGrid grid,
			final T type,
			final boolean dirty,
			final Cache< Long, Cell< A > > cache,
			final SharedQueue queue,
			final CacheHints hints )
	{
		final CreateInvalid< Long, Cell< A > > createInvalid =
				CreateInvalidVolatileCell.get( grid, type, dirty );

		final VolatileCache< Long, Cell< A > > volatileCache =
				new WeakRefVolatileCache<>( cache, queue, createInvalid );

		final VolatileCachedCellImg< T, A > volatileImg =
				new VolatileCachedCellImg<>( grid, type, hints, volatileCache.unchecked()::get );
		return volatileImg;
	}
}
