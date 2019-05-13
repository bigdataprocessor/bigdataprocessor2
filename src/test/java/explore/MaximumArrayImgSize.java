package explore;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.integer.ByteType;

public class MaximumArrayImgSize
{
	public static void main( String[] args )
	{
		final int n = Integer.MAX_VALUE - 1;

		System.out.println( n );
		long nx = 2000;
		long ny = 2000;
		long nz = n / ( nx * ny );

		System.out.println( 1.0 * nx * ny * nz / n );

		final ArrayImg< ByteType, ByteArray > arrayImg
				= ArrayImgs.bytes( new long[]{ nx, ny, nz } );
	}
}
