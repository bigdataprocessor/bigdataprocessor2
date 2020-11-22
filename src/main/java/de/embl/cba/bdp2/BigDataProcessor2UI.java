package de.embl.cba.bdp2;

import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.image.Image;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public abstract class BigDataProcessor2UI
{
	public static final String SPEED = "Read [MByte/s]: ";
	private static JFrame frame;
	private static JLabel imageInfo;
	private static JLabel readInfo;
	private static JPanel panel;

	public static void showUI()
	{
		if ( frame != null && frame.isVisible() ) return;
		if ( frame != null && ! frame.isVisible() ) { frame.setVisible( true ); return; }

		System.setProperty("apple.laf.useScreenMenuBar", "false");

		final BigDataProcessor2MenuActions menuActions = new BigDataProcessor2MenuActions();
		final JMenuBar jMenuBar = new JMenuBar();
		frame = new JFrame( "BigDataProcessor2" );
		frame.setJMenuBar( jMenuBar );
		final List< JMenu > menus = menuActions.getMenus();
		for ( JMenu menu : menus )
		{
			jMenuBar.add( menu );
		}

		panel = new JPanel();
		panel.setLayout( new BoxLayout(panel, BoxLayout.Y_AXIS) );
		panel.setBorder( BorderFactory.createEmptyBorder(10,10,10,10) );
		imageInfo = new JLabel( wrapAsHtml( "Please open an image..." ) );
		readInfo = new JLabel( wrapAsHtml( SPEED + "NaN" ) );
		panel.add( imageInfo );
		panel.add( new JLabel( "  " ) );
		panel.add( readInfo );

		showFrame();
	}

	public static void setImageInformation( Image< ? > image )
	{
		if ( frame == null ) return; // UI not instantiated

		if ( image != null )
		{
			imageInfo.setText( wrapAsHtml( image.getInfo() ));
		}
		else
		{
			imageInfo.setText( wrapAsHtml( "Active Image: None" ) );
		}

		imageInfo.validate();
		refreshFrame();
	}

	public static String wrapAsHtml( final String text )
	{
		return "<html><pre>" + text + "</pre></html>";
	}

	private static void refreshFrame()
	{
		frame.validate();
		frame.pack();
		frame.repaint();
	}

	public static void setReadPerformanceInformation( double mbps, double averageMBPS )
	{
		if ( frame == null ) return; // UI not instantiated

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(0);

		readInfo.setText(  wrapAsHtml(SPEED + df.format( mbps ) + " Avg: " + df.format( averageMBPS )));
		readInfo.validate();
		refreshFrame();
	}

	private static void showFrame()
	{
		frame.getContentPane().setLayout( new GridLayout() );
		frame.getContentPane().add( panel );

		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.pack();
		SwingUtilities.invokeLater( () ->
		{
			frame.setVisible( true );
			DialogUtils.moveWindowToPosition( panel, 50, 200 );
		} );
	}
}
