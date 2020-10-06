package de.embl.cba.bdp2.track;

import bdv.util.BdvHandle;
import bdv.util.BdvOverlay;
import net.imglib2.realtransform.AffineTransform3D;

import java.awt.*;
import java.util.ArrayList;


public class TrackOverlay extends BdvOverlay
{
	private final BdvHandle bdvHandle;
	private final Track track;
	private final double depthOfField;

	enum Shape
	{
		Filled,
		Empty
	}

	public TrackOverlay( BdvHandle bdvHandle, Track track, double depthOfField )
	{
		super();
		this.bdvHandle = bdvHandle;
		this.track = track;
		this.depthOfField = depthOfField;
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		final int numTimepoints = bdvHandle.getViewerPanel().getState().getNumTimepoints();
		final int currentTimepoint = bdvHandle.getViewerPanel().getState().getCurrentTimepoint();

		final ArrayList< Integer > timePoints = new ArrayList<>( track.getTimePoints() );

		for ( int t = 0; t < numTimepoints ; t++ )
		{
			drawShape( g, t );
		}
	}

	private void drawShape( Graphics2D g, int t )
	{
		if ( ! track.getTimePoints().contains( t ) ) return;

		final double[] position = track.getPosition( t );

		final AffineTransform3D viewerTransform = new AffineTransform3D();
		getCurrentTransform3D( viewerTransform );

		final double[] positionInViewer = new double[ 3 ];
		viewerTransform.apply( position, positionInViewer );
		final int size = getSize( positionInViewer[ 2 ] );
		final int x = ( int ) ( positionInViewer[ 0 ] - 0.5 * size );
		final int y = ( int ) ( positionInViewer[ 1 ] - 0.5 * size );

		final Color color = getColor( positionInViewer[ 2 ], track.getType( t ) );
		g.setColor( color );

		final Shape shape = getShape( t );

		if ( shape.equals( Shape.Filled ) )
			g.fillOval( x, y, size, size );
		else if ( shape.equals( Shape.Empty ))
			g.drawOval( x, y, size, size );

	}

	private Shape getShape( int t )
	{
		if ( t == bdvHandle.getViewerPanel().getState().getCurrentTimepoint() )
			return Shape.Filled;
		else
			return Shape.Empty;
	}

	private Color getColor( final double depth, TrackPosition.PositionType type )
	{
		int alpha = 255 - ( int ) Math.round( Math.abs( depth ) );

		if ( alpha < 64 )
			alpha = 64;

		if ( type.equals( TrackPosition.PositionType.Anchor ))
			return new Color( 255, 255, 0, alpha );
		else if ( type.equals( TrackPosition.PositionType.Interpolated ) )
			return new Color( 0, 0, 255, alpha );
		else
			throw new RuntimeException( "Cannot color type: " + type );
	}

	private int getSize( final double depth )
	{
		return ( int ) Math.max( 5, 20 - 1.0 / depthOfField * Math.round( Math.abs( depth ) ) );
	}

}
