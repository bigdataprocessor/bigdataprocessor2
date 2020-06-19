package de.embl.cba.bdp2.drift.track;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdv.utils.BdvUtils;
import ij.IJ;
import ij.gui.NonBlockingGenericDialog;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.Map;

public class ApplyTrackDialog< T extends RealType< T > & NativeType< T > >
{
	public ApplyTrackDialog( BdvImageViewer< T > viewer )
	{
		final Map< String, Track > tracks = viewer.getTracks();

		if ( tracks.keySet().size() == 0 )
		{
			IJ.showMessage( "There are no tracks yet...");
			return;
		}

		final NonBlockingGenericDialog gd = new NonBlockingGenericDialog( "Tracked View" );

		gd.addChoice( "Track", tracks.keySet().toArray( new String[]{} ), tracks.keySet().iterator().next() );

		gd.showDialog();
		if( gd.wasCanceled() ) return;

		final String trackId = gd.getNextString();

		final Image< T > image = new TrackApplier( viewer.getImage() ).applyTrack( tracks.get( trackId ) );

		final BdvImageViewer< T > newViewer = new BdvImageViewer<>( image );
		newViewer.setDisplaySettings( viewer.getDisplaySettings() );

		BdvUtils.moveToPosition( newViewer.getBdvHandle(), new double[]{0,0,0}, 0, 100);
	}
}
