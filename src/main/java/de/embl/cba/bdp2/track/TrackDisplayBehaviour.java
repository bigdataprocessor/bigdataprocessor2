package de.embl.cba.bdp2.track;

import bdv.util.BdvHandle;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdv.utils.BdvUtils;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

public class TrackDisplayBehaviour
{
	public TrackDisplayBehaviour( BdvHandle bdv, Track track )
	{
		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdv.getTriggerbindings(), "behaviours" );

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			(new Thread( () -> {
				moveToTrackPosition( bdv, track, bdv.getViewerPanel().state().getCurrentTimepoint() + 1 );
			} )).start();

		}, "Move forward along drift" + track.getName(), "ctrl M"  ) ;


		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			(new Thread( () -> {
				moveToTrackPosition( bdv, track, bdv.getViewerPanel().state().getCurrentTimepoint() - 1 );
			} )).start();

		}, "Move backward along drift" + track.getName(), "ctrl N"  ) ;
	}

	private void moveToTrackPosition( BdvHandle bdv, Track track, int t )
	{
		double[] position = track.getPosition( t );

		if ( position == null )
		{
			Logger.log( "Track: " + track.getName() + ": Time point: " + t + " => Position not (yet) available." );
			return;
		}

		BdvUtils.moveToPosition( bdv, position, t, 200 );
	}
}
