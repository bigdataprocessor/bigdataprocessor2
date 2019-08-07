package de.embl.cba.bdp2.registration;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.AffineGet;

public class AffineGetNull implements AffineGet
{

	@Override
	public double get( int i, int i1 )
	{
		return 0;
	}

	@Override
	public double[] getRowPackedCopy()
	{
		return new double[ 0 ];
	}

	@Override
	public RealLocalizable d( int i )
	{
		return null;
	}

	@Override
	public void applyInverse( double[] doubles, double[] doubles1 )
	{

	}

	@Override
	public void applyInverse( RealPositionable realPositionable, RealLocalizable realLocalizable )
	{

	}

	@Override
	public AffineGet inverse()
	{
		return null;
	}

	@Override
	public int numSourceDimensions()
	{
		return 0;
	}

	@Override
	public int numTargetDimensions()
	{
		return 0;
	}

	@Override
	public void apply( double[] doubles, double[] doubles1 )
	{

	}

	@Override
	public void apply( RealLocalizable realLocalizable, RealPositionable realPositionable )
	{

	}

	@Override
	public AffineGet copy()
	{
		return null;
	}

	@Override
	public int numDimensions()
	{
		return 0;
	}
}
