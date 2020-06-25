package de.embl.cba.bdp2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.ui.MenuActions;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public abstract class BigDataProcessor2UI
{
	private static JFrame frame;
	private static JLabel imageInfo;
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
		panel.add( imageInfo );

		showFrame();
	}

	public static void setImageInformation( Image< ? > image )
	{
		final ObjectMapper objectMapper = new ObjectMapper();

		try
		{
			DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
			prettyPrinter.indentArraysWith( DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
			objectMapper.setDefaultPrettyPrinter(prettyPrinter);

			final String info = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString( image );
			imageInfo.setText( "<html><pre>Active image:\n" + info + "</pre></html>");
			imageInfo.validate();
			frame.validate();
			frame.pack();
			frame.repaint();
		}
		catch ( JsonProcessingException e )
		{
			e.printStackTrace();
		}
	}

	private static void showFrame()
	{
//		JSplitPane splitPane = new JSplitPane();
//		splitPane.setOrientation( JSplitPane.VERTICAL_SPLIT );
//		final int numModalities = actionPanel.getSortedModalities().size();
//		final int actionPanelHeight = ( numModalities + 7 ) * 40;
//		splitPane.setDividerLocation( actionPanelHeight );
//		splitPane.setTopComponent( actionPanel );
//		splitPane.setBottomComponent( sourcesPanel );
//		splitPane.setAutoscrolls( true );
//		frameWidth = 600;
//		frame.setPreferredSize( new Dimension( frameWidth, actionPanelHeight + 200 ) );
//		frame.getContentPane().setLayout( new GridLayout() );
//		frame.getContentPane().add( splitPane );

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
