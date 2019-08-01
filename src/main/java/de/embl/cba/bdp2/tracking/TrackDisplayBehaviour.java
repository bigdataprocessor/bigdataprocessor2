package de.embl.cba.bdp2.tracking;

import bdv.util.BdvHandle;
import de.embl.cba.bdp2.logging.Logger;
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
				moveToTrackPosition( bdv, track, bdv.getViewerPanel().getState().getCurrentTimepoint() + 1 );
			} )).start();

		}, "Move forward along track" + track.getId(), "ctrl M"  ) ;


		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			(new Thread( () -> {
				moveToTrackPosition( bdv, track, bdv.getViewerPanel().getState().getCurrentTimepoint() - 1 );
			} )).start();

		}, "Move backward along track" + track.getId(), "ctrl N"  ) ;

	}

	private void moveToTrackPosition( BdvHandle bdv, Track track, int t )
	{
		double[] position = track.getCalibratedPosition( t );

		if ( position == null )
		{
			Logger.log( "Track " + track.getId() + ": Time-point" + t + ": Position not (yet) available." );
			return;
		}

		BdvUtils.moveToPosition( bdv, position, t, 200 );
	}
}
