package de.embl.cba.bdp2.log;

import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.type.NativeType;

import java.util.Arrays;

public class CellLoaderLogger< T extends NativeType< T > >
{
	public static final double MEGA = 1000000D;

	private final SingleCellArrayImg< T, ? > cell;
	private long startTimeNanos;
	private long durationNanos;

	public CellLoaderLogger( SingleCellArrayImg< T, ? > cell )
	{
		this.cell = cell;
	}

	public void start()
	{
		this.startTimeNanos = System.nanoTime();
	}

	public void stop()
	{
		durationNanos = System.nanoTime() - startTimeNanos;
	}

	public long getDurationNanos()
	{
		return durationNanos;
	}

	public String getBenchmarkLog()
	{
		long[] min = new long[ cell.numDimensions() ];
		long[] max = new long[ cell.numDimensions() ];
		cell.min( min );
		cell.max( max );

		StringBuilder builder = new StringBuilder( "Read " );
		builder.append( Arrays.toString( min ) );
		builder.append( " - "  );
		builder.append( Arrays.toString( max ) );
		builder.append( " in "  );
		builder.append( String.format("%.2f", durationNanos / Math.pow( 10, 6 ) ) );
		builder.append( " ms "  );
		builder.append( " ( "  );
		builder.append( String.format("%.2f", 1.0 * numBytes() / durationNanos * Math.pow( 10, 3 ) ) );
		builder.append( " MB/s )"  );

		return builder.toString();
	}

	private int numBytes()
	{
		Object array = cell.getStorageArray();
		if ( array instanceof byte[] ) return ( ( byte[] ) array ).length * 1;
		else if ( array instanceof short[] ) return ( ( short[] ) array ).length * 2;
		else if ( array instanceof int[] ) return ( ( int[] ) array ).length * 4;
		else if ( array instanceof long[] ) return ( ( long[] ) array ).length * 8;
		else if ( array instanceof float[] ) return ( ( float[] ) array ).length * 4;
		else if ( array instanceof double[] ) return ( ( double[] ) array ).length * 8;
		else throw new RuntimeException( "Unsupported array type: " + array.getClass() );
	}
}
