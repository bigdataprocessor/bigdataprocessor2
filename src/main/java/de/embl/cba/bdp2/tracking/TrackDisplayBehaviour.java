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

				final int nextTimePoint = bdv.getViewerPanel().getState().getCurrentTimepoint() + 1;

				double[] position = track.getCalibratedPosition( nextTimePoint );

				if ( position == null )
				{
					Logger.log( "Track " + track.getId() + ": Time-point" + nextTimePoint + ": Position not (yet) available."  );
					return;
				}

				BdvUtils.moveToPosition( bdv, position, nextTimePoint, 500 );

			} )).start();

		}, "Move forward along track" + track.getId(), "ctrl N"  ) ;


	}
}
