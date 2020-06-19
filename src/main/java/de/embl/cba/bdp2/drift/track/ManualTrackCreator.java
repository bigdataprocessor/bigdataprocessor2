package de.embl.cba.bdp2.drift.track;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.tables.SwingUtils;
import net.imglib2.RealPoint;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ManualTrackCreator extends JFrame
{
	private Track track;
	private final BdvHandle bdvHandle;
	private final JPanel panel;
	private boolean automaticLinearInterpolation = true;

	public ManualTrackCreator( BdvImageViewer viewer, String trackName )
	{
		bdvHandle = viewer.getBdvHandle();
		panel = new JPanel();

		initTrackAndOverlay( viewer, trackName );
		installBehaviours();
		createAndShowDialog();
	}

	public void createAndShowDialog()
	{
		addLegendPanels();
		addHelpTextPanel();
		//addInterpolationCheckBoxPanel();
		addSaveTrackPanel();
		showFrame();
	}

	private void addLegendPanels()
	{
		addCurrentAnchorPanel();
		addCurrentInterpolatedPanel();
		addOtherAnchorPanel();
		addOtherInterpolatedPanel();
	}

	private void addCurrentAnchorPanel()
	{
		final JPanel panel = getPanel();
		panel.add( new CurrentAnchor() );
		panel.add( Box.createHorizontalGlue() );
		panel.add( new JLabel( "Anchor, current time point" ) );
		this.panel.add( panel );
	}

	private void addOtherAnchorPanel()
	{
		final JPanel panel = getPanel();
		panel.add( new OtherAnchor() );
		panel.add( Box.createHorizontalGlue() );
		panel.add( new JLabel( "Anchor, other time point" ) );
		this.panel.add( panel );
	}

	private void addOtherInterpolatedPanel()
	{
		final JPanel panel = getPanel();
		panel.add( new OtherInterpolated() );
		panel.add( Box.createHorizontalGlue() );
		panel.add( new JLabel( "Interpolated, other time point" ) );
		this.panel.add( panel );
	}

	private void addCurrentInterpolatedPanel()
	{
		final JPanel panel = getPanel();
		panel.add( new CurrentInterpolated() );
		panel.add( Box.createHorizontalGlue() );
		panel.add( new JLabel( "Interpolated, current time point" ) );
		this.panel.add( panel );
	}

	private JPanel getPanel()
	{
		final JPanel panel = new JPanel(  );
		panel.setLayout( new BoxLayout(panel, BoxLayout.LINE_AXIS) );
		panel.setBorder( BorderFactory.createEmptyBorder(0, 10, 10, 10) );
		return panel;
	}

	class CurrentAnchor extends JPanel {
		@Override
		public void paintComponent(Graphics g) {
			g.setColor( Color.YELLOW );
			g.fillOval( 2, 2, 12, 12);
		}
	}

	class OtherAnchor extends JPanel {
		@Override
		public void paintComponent(Graphics g) {
			g.setColor( Color.YELLOW );
			g.drawOval(2, 2, 12, 12);
		}
	}

	class CurrentInterpolated extends JPanel {
		@Override
		public void paintComponent(Graphics g) {
			g.setColor( Color.BLUE );
			g.fillOval( 2, 2, 12, 12);
		}
	}

	class OtherInterpolated extends JPanel {
		@Override
		public void paintComponent(Graphics g) {
			g.setColor( Color.BLUE );
			g.drawOval( 2, 2, 12, 12);
		}
	}

	public void initTrackAndOverlay( BdvImageViewer viewer, String trackName )
	{
		track = new Track( trackName, viewer.getImage().getVoxelSpacing() );
		final TrackOverlay trackOverlay = new TrackOverlay( bdvHandle, track, 20 );
		BdvFunctions.showOverlay( trackOverlay, "drift-overlay", BdvOptions.options().addTo( bdvHandle ) );
	}

	private void addInterpolationCheckBoxPanel()
	{
		final JPanel panel = SwingUtils.horizontalLayoutPanel();
		final JCheckBox checkBox = new JCheckBox( "Automatically add missing points by linear interpolation");
		checkBox.setSelected( automaticLinearInterpolation );
		checkBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				automaticLinearInterpolation = checkBox.isSelected();
			}
		} );
		panel.add( checkBox );
		this.panel.add( panel );
	}

	private void addHelpTextPanel()
	{
		final JPanel panel = getPanel();
		final JLabel label = new JLabel( "Press [ A ] to add or move a track point" );
		panel.add( label );
		this.panel.add( panel );
	}

	private void addSaveTrackPanel()
	{
		final JPanel panel = getPanel();
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
				track.setPosition( timepoint, point, Track.PositionType.Anchor );
				if ( automaticLinearInterpolation )
				{
					final TrackInterpolator interpolator = new TrackInterpolator( track );
					interpolator.run();
				}
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
