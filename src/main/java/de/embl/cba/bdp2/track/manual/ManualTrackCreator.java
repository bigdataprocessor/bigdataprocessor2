package de.embl.cba.bdp2.track.manual;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.track.CorrectDriftWithTrackCommand;
import de.embl.cba.bdp2.track.Track;
import de.embl.cba.bdp2.track.TrackManager;
import de.embl.cba.bdp2.track.TrackOverlay;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdv.utils.BdvUtils;
import net.imglib2.RealPoint;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

public class ManualTrackCreator
{
	private final Track track;

	public ManualTrackCreator( BdvImageViewer viewer, String trackName )
	{
		final BdvHandle bdvHandle = viewer.getBdvHandle();

		track = new Track( trackName, viewer.getImage().getVoxelSpacing() );

		TrackManager.getTracks().put( trackName, track );

		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdvHandle.getTriggerbindings(), "behaviours" );

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			(new Thread( () -> {
				final RealPoint point = BdvUtils.getGlobalMouseCoordinates( bdvHandle );
				final int timepoint = bdvHandle.getViewerPanel().getState().getCurrentTimepoint();
				track.setPosition( timepoint, point );
			} )).start();
		}, "Add track position", "A"  ) ;

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			(new Thread( () -> {
				Services.commandService.run( CorrectDriftWithTrackCommand.class, true );
			} )).start();
		}, "Done", "D"  ) ;

		final TrackOverlay trackOverlay = new TrackOverlay( bdvHandle, track, 20 );
		BdvFunctions.showOverlay( trackOverlay, "track-overlay", BdvOptions.options().addTo( bdvHandle ) );
	}

	public Track getTrack()
	{
		return track;
	}

	private void moveToTrackPosition( BdvHandle bdv, Track track, int t )
	{
		double[] position = track.getPosition( t );

		if ( position == null )
		{
			Logger.log( "Track: " + track.getTrackName() + ": Time point: " + t + " => Position not (yet) available." );
			return;
		}

		BdvUtils.moveToPosition( bdv, position, t, 200 );
	}
}
