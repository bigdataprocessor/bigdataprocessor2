/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.track;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import bdv.util.BdvOverlaySource;
import de.embl.cba.bdp2.viewer.ImageViewer;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

public class TrackCreator extends JFrame
{
	private Track track;
	private final BdvHandle bdvHandle;
	private final JPanel panel;
	private boolean automaticLinearInterpolation = true;
	private JTextField dt;
	private Behaviours behaviours;
	private static File recentTrackSavingDirectory = null;
	private BdvOverlaySource< TrackOverlay > bdvTrackOverlay;

	public TrackCreator( ImageViewer viewer, String trackName )
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
		this.panel.add( new JSeparator( SwingConstants.HORIZONTAL) );
		//addInterpolationCheckBoxPanel();
		addTrackNavigationPanel();
		addSaveTrackPanel();
		addHelpTextPanel();
		addHelpTextPanel2();
		showFrame();
	}

	private void addTrackNavigationPanel()
	{
		final JPanel panel = getPanel();
		final JLabel label = new JLabel( "Move" );
		dt = new JTextField( "1" );

		final JButton bwd = new JButton( "Bwd [ J ]" );
		//bwd.setFont( new Font(Font.MONOSPACED, Font.PLAIN, 12)  );
		bwd.addActionListener( e -> {
			moveBwd();
		} );

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> moveBwd(), "move bwd along track", "J" );

		final JButton fwd = new JButton( "Fwd [ K ]" );
		//fwd.setFont( new Font(Font.MONOSPACED, Font.PLAIN, 12)  );
		fwd.addActionListener( e -> {
			moveFwd();
		} );

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> moveFwd(), "move fwd along track", "K" );

		panel.add( label );
		panel.add( Box.createHorizontalGlue() );
		panel.add( dt );
		panel.add( Box.createHorizontalGlue() );
		panel.add( bwd );
		panel.add( Box.createHorizontalGlue() );
		panel.add( fwd );
		this.panel.add( panel );
	}

	private void moveFwd()
	{
		final int currentTimepoint = bdvHandle.getViewerPanel().state().getCurrentTimepoint();
		final int t = currentTimepoint + Integer.parseInt( dt.getText() );
		if ( t < bdvHandle.getViewerPanel().state().getNumTimepoints() )
			moveToTrackPosition( t );
	}

	private void moveBwd()
	{
		final int currentTimepoint = bdvHandle.getViewerPanel().state().getCurrentTimepoint();
		final int t = currentTimepoint - Integer.parseInt( dt.getText() );
		if ( t >= 0 )
			moveToTrackPosition( t );
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
		panel.setLayout( new BoxLayout( panel, BoxLayout.LINE_AXIS ) );
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

	public void initTrackAndOverlay( ImageViewer viewer, String trackName )
	{
		track = new Track( trackName, viewer.getImage().getVoxelDimensions() );
		final TrackOverlay trackOverlay = new TrackOverlay( bdvHandle, track, 20 );
		bdvTrackOverlay = BdvFunctions.showOverlay( trackOverlay, "drift-overlay", BdvOptions.options().addTo( bdvHandle ) );
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
		final JLabel label = new JLabel( "Add or change track point: Press [ A ]" );
		panel.add( label );
		panel.add( Box.createHorizontalGlue() );
		this.panel.add( panel );
	}

	private void addHelpTextPanel2()
	{
		final JPanel panel = getPanel();
		final JLabel label = new JLabel( "Correct drift: Process > Correct Drift > Apply Track..." );
		panel.add( label );
		panel.add( Box.createHorizontalGlue() );
		this.panel.add( panel );
	}

	private void addSaveTrackPanel()
	{
		final JPanel panel = getPanel();
		final JButton button = new JButton( "Save track to file" );
		panel.add( button );
		button.addActionListener( e -> {

			final JFileChooser jFileChooser = new JFileChooser();

			jFileChooser.setCurrentDirectory( recentTrackSavingDirectory );
			jFileChooser.setDialogTitle("Save track");

			if ( jFileChooser.showSaveDialog( this ) == JFileChooser.APPROVE_OPTION )
			{
				File file = jFileChooser.getSelectedFile();
				recentTrackSavingDirectory = new File( file.getParent() );

				Tracks.toJson( file, track );

				TrackManager.getTracks().put( track.getName(), track );
			}
		} );
		this.panel.add( panel );
	}

	private void showFrame()
	{
		this.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		panel.setOpaque( true ); //content panes must be opaque
		panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
		this.setContentPane( panel );
		this.setLocation( MouseInfo.getPointerInfo().getLocation().x - 50, MouseInfo.getPointerInfo().getLocation().y - 50 );
		this.pack();
		this.setVisible( true );
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing( WindowEvent e)
			{
				if ( bdvTrackOverlay != null )
					bdvTrackOverlay.removeFromBdv();
			}
		});
	}


	private void installBehaviours()
	{
		behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdvHandle.getTriggerbindings(), "behaviours" );

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			(new Thread( () -> {
				final RealPoint point = BdvUtils.getGlobalMouseCoordinates( bdvHandle );
				final int timepoint = bdvHandle.getViewerPanel().state().getCurrentTimepoint();
				track.setPosition( timepoint, point, TrackPosition.PositionType.Anchor );
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

	private void moveToTrackPosition( int t )
	{
		if ( track.getTimePoints().contains( t ) )
		{
			double[] position = track.getPosition( t );
			BdvUtils.moveToPosition( bdvHandle, position, t, 0 );
		}
		else
		{
			bdvHandle.getViewerPanel().setTimepoint( t );
		}
	}
}
