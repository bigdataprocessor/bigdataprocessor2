package presentations;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.*;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import static net.imglib2.cache.img.ReadOnlyCachedCellImgOptions.*;

public class PresentationAugust2019
{

	public void traditionalImage()
	{
		final int[][][] image = new int[ 500 ][ 1000 ][ 1000];

		image[10][50][50] = 128;
		int value = image[10][50][50];
	}


	public void rai()
	{
		final RandomAccessibleInterval< UnsignedShortType > storage = ArrayImgs.unsignedShorts(  1000, 1000, 500 );

		final RandomAccess< UnsignedShortType > image = storage.randomAccess();

		image.setPosition( 50,0 );
		image.setPosition( 50,1 );
		image.setPosition( 10,2 );

		final UnsignedShortType unsignedShortType = image.get();
	}


	public void cachedCellImage()
	{
		final RandomAccessibleInterval image =
				new ReadOnlyCachedCellImgFactory().create(
						new long[]{ 1000, 1000, 500 },
						new UnsignedShortType(),
						cell -> {
							// load data and copy into cell
						},
						options().cellDimensions( 1000, 1000, 1 ));

		convert( image );

	}

	public void convert( RandomAccessibleInterval image )
	{
		final RandomAccessibleInterval processed = Converters.convert(
				image,
				( Converter< UnsignedShortType, UnsignedByteType > ) ( input, output ) ->
				{
					output.set( (int) ( input.get() / 65535.0 * 255 ) );
				},
				new UnsignedByteType() );

	}

	public static void main( String[] args )
	{

		final RandomAccessibleInterval< UnsignedShortType > img = ArrayImgs.unsignedShorts( 1000, 1000 );

		final RandomAccess< UnsignedShortType > access = img.randomAccess();
		access.setPosition( 50,0 );
		access.setPosition( 50,1 );
		final UnsignedShortType unsignedShortType = access.get();


	}
}
