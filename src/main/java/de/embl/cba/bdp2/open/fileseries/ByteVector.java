/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
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
package de.embl.cba.bdp2.open.fileseries;

/*
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
