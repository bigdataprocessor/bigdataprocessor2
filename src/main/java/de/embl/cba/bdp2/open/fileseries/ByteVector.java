package de.embl.cba.bdp2.open.fileseries;

/**
 * A growable array of bytes.
 */
public class ByteVector
{
	private byte[] data;
	private int size;

	public ByteVector()
	{
		data = new byte[ 10 ];
		size = 0;
	}

	public ByteVector( int initialSize )
	{
		data = new byte[ initialSize ];
		size = 0;
	}

	public ByteVector( byte[] byteBuffer )
	{
		data = byteBuffer;
		size = 0;
	}

	public void add( byte x )
	{
		if ( size >= data.length )
		{
			doubleCapacity();
			add( x );
		} else
			data[ size++ ] = x;
	}

	public int size()
	{
		return size;
	}

	public void add( byte[] array )
	{
		int length = array.length;
		while ( data.length - size < length )
			doubleCapacity();
		System.arraycopy( array, 0, data, size, length );
		size += length;
	}

	void doubleCapacity()
	{
		byte[] tmp = new byte[ data.length * 2 + 1 ];
		System.arraycopy( data, 0, tmp, 0, data.length );
		data = tmp;
	}

	public void clear()
	{
		size = 0;
	}

	public byte[] toByteArray()
	{
		byte[] bytes = new byte[ size ];
		System.arraycopy( data, 0, bytes, 0, size );
		return bytes;
	}
}
