package de.embl.cba.bdp2.drift.track;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.tables.SwingUtils;
import net.imglib2.RealPoint;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import java.awt.*;

public class ManualTrackCreator extends JFrame
{
	private Track track;
	private final BdvHandle bdvHandle;
	private final JPanel panel;

	public ManualTrackCreator( BdvImageViewer viewer, String trackName )
	{
		bdvHandle = viewer.getBdvHandle();
		panel = new JPanel();

		initTrackAndOverlay( viewer, trackName );
		installBehaviours();
		addHelpTextPanel();
		addSaveTrackPanel();
		showFrame();
	}

	public void initTrackAndOverlay( BdvImageViewer viewer, String trackName )
	{
		track = new Track( trackName, viewer.getImage().getVoxelSpacing() );
		final TrackOverlay trackOverlay = new TrackOverlay( bdvHandle, track, 20 );
		BdvFunctions.showOverlay( trackOverlay, "drift-overlay", BdvOptions.options().addTo( bdvHandle ) );
	}

	private void addHelpTextPanel()
	{
		final JPanel panel = SwingUtils.horizontalLayoutPanel();
		final JLabel label = new JLabel( "- Press [A] to create or move a track point\n" +
				"- Pres ..." );
		this.panel.add( panel );
	}

	private void addSaveTrackPanel()
	{
		final JPanel panel = SwingUtils.horizontalLayoutPanel();
		final JButton button = new JButton( "Save track" );
		final JTextField textField = new JTextField( "Track 000" );
		panel.add( button );
		panel.add( textField );
		button.addActionListener( e -> {
			track.setName( textField.getText() );
			TrackManager.getTracks().put( track.getName(), track );
		} );
		this.panel.add( panel );
	}

	private void showFrame()
	{
		this.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		panel.setOpaque( true ); //content panes must be opaque
		panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
		this.setContentPane( panel );
		this.setLocation( MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y );
		this.pack();
		this.setVisible( true );
	}


	public void installBehaviours()
	{
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
			Logger.log( "Track: " + track.getName() + ": Time point: " + t + " => Position not (yet) available." );
			return;
		}

		BdvUtils.moveToPosition( bdv, position, t, 200 );
	}
}
