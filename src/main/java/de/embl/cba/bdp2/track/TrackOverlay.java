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
		Circle,
		Rectangle
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
		final int currentTimepoint = bdvHandle.getViewerPanel().getState().getCurrentTimepoint();

		final ArrayList< Integer > timePoints = new ArrayList<>( track.getTimePoints() );

		if ( timePoints.contains( currentTimepoint ) )
		{
			drawShape( g, currentTimepoint, Shape.Circle );
		}

		for ( int t = currentTimepoint - 1; t >= 0 ; t-- )
		{
			if ( timePoints.contains( t ) )
			{
				drawShape( g, currentTimepoint, Shape.Rectangle );
				break;
			}
		}
	}

	private void drawShape( Graphics2D g, int currentTimepoint, Enum shape )
	{
		final double[] position = track.getPosition( currentTimepoint );

		final AffineTransform3D viewerTransform = new AffineTransform3D();
		getCurrentTransform3D( viewerTransform );

		final double[] positionInViewer = new double[ 3 ];
		viewerTransform.apply( position, positionInViewer );
		final int size = getSize( positionInViewer[ 2 ] );
		final int x = ( int ) ( positionInViewer[ 0 ] - 0.5 * size );
		final int y = ( int ) ( positionInViewer[ 1 ] - 0.5 * size );
		g.setColor( getColor( positionInViewer[ 2 ] ) );
		if ( shape.equals( Shape.Circle ) )
			g.fillOval( x, y, size, size );
		else if ( shape.equals( Shape.Rectangle ))
			g.drawRect( x, y, size, size );

	}

	private Color getColor( final double depth )
	{
		int alpha = 255 - ( int ) Math.round( Math.abs( depth ) );

		if ( alpha < 64 )
			alpha = 64;

		return new Color( 255, 0, 0, alpha );
	}

	private int getSize( final double depth )
	{
		return ( int ) Math.max( 5, 20 - 1.0 / depthOfField * Math.round( Math.abs( depth ) ) );
	}

}
