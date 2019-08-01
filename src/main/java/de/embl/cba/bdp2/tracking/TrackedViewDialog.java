package de.embl.cba.bdp2.tracking;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdv.utils.BdvUtils;
import ij.IJ;
import ij.gui.NonBlockingGenericDialog;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.Map;

public class TrackedViewDialog< T extends RealType< T > & NativeType< T > >
{
	public TrackedViewDialog( BdvImageViewer< T > viewer )
	{

		final Map< String, Track > tracks = viewer.getTracks();

		if ( tracks.keySet().size() == 0 )
		{
			IJ.showMessage( "There are no tracks yet...");
			return;
		}

		final NonBlockingGenericDialog gd = new NonBlockingGenericDialog( "Tracked View" );

		gd.addChoice( "Track",
				tracks.keySet().toArray( new String[]{} ), tracks.keySet().iterator().next() );

		gd.showDialog();
		if( gd.wasCanceled() ) return;

		final String trackId = gd.getNextString();

		final Image< T > image = TrackViews.applyTrack( viewer.getImage(), tracks.get( trackId ) );

		final BdvImageViewer< T > newViewer = new BdvImageViewer<>( image );
		newViewer.setDisplaySettings( viewer.getDisplaySettings() );

		BdvUtils.moveToPosition( newViewer.getBdvHandle(), new double[]{0,0,0}, 0, 100);


	}
}
