package de.embl.cba.bdp2.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.ui.MenuActions;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public abstract class BigDataProcessor2UI
{
	public static final String SPEED = "Reading speed [MBit/s]: ";
	private static JFrame frame;
	private static JLabel imageInfo;
	private static JLabel readInfo;
	private static JPanel panel;

	public static void showUI()
	{
		if ( frame != null && frame.isVisible() ) return;
		if ( frame != null && ! frame.isVisible() ) { frame.setVisible( true ); return; }

		System.setProperty("apple.laf.useScreenMenuBar", "false");

		final MenuActions menuActions = new MenuActions();
		final JMenuBar jMenuBar = new JMenuBar();
		frame = new JFrame( "BigDataProcessor2" );
		frame.setJMenuBar( jMenuBar );
		final List< JMenu > menus = menuActions.getMenus();
		for ( JMenu menu : menus )
		{
			jMenuBar.add( menu );
		}

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		imageInfo = new JLabel( "<html><pre>Please open an image...</pre></html>" );
		readInfo = new JLabel( SPEED + "NaN" );
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
			final ObjectMapper objectMapper = new ObjectMapper();

			try
			{
				DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
				prettyPrinter.indentArraysWith( DefaultIndenter.SYSTEM_LINEFEED_INSTANCE );
				objectMapper.setDefaultPrettyPrinter( prettyPrinter );
				String info = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString( image );
				imageInfo.setText( "<html><pre>Active image:\n" + info + "</pre></html>" );
			}
			catch ( JsonProcessingException e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			imageInfo.setText( "<html><pre>Active image: None</pre></html>" );
		}

		imageInfo.validate();
		refreshFrame();
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
		df.setMaximumFractionDigits(2);

		readInfo.setText(  SPEED + df.format( mbps ) + " <" + df.format( averageMBPS )+ ">");
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
			Utils.moveWindowToPosition( panel, 50, 200 );
		} );
	}
}
