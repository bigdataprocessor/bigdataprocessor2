package de.embl.cba.bigDataTools2.boundingBox;

import net.imglib2.AbstractInterval;
import net.imglib2.AbstractRealInterval;
import net.imglib2.Interval;
import net.imglib2.RealInterval;

public class ModifiableRealInterval extends AbstractRealInterval
{
	public ModifiableRealInterval( final int numDimensions )
	{
		super( numDimensions );
	}

	public ModifiableRealInterval( final RealInterval interval )
	{
		super( interval );
	}

	public void set( final RealInterval interval )
	{
		assert interval.numDimensions() == n;
		for ( int d = 0; d < n; ++d )
		{
			min[ d ] = interval.realMin( d );
			max[ d ] = interval.realMax( d );
		}
	}
}
