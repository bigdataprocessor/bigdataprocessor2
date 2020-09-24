package de.embl.cba.bdp2.dialog;

import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdv.utils.BdvUtils;
import fiji.util.gui.GenericDialogPlus;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Utils
{
	public static final String BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT = "Plugins>BigDataTools>BigDataProcessor2>";

	public static ArrayList< File > selectDirectories()
	{

		final ArrayList< File > dirs = new ArrayList<>(  );
		final AtomicBoolean isDone = new AtomicBoolean( false );

		Runnable r = new Runnable() {

			@Override
			public void run() {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int showOpenDialog = fileChooser.showOpenDialog(null);
				if ( showOpenDialog == JFileChooser.APPROVE_OPTION )
				{
					final File[] selectedFiles = fileChooser.getSelectedFiles();

					for ( File file : selectedFiles )
						dirs.add( file );
				}
				isDone.set( true );
			}
		};
		SwingUtilities.invokeLater(r);

		while ( ! isDone.get() ){
			try
			{
				Thread.sleep( 100 );
			} catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
		};

		return dirs;
	}

	public static Integer getChannel( BdvImageViewer imageViewer )
	{
		final GenericDialogPlus gd = new GenericDialogPlus( "Select channel" );
		final String[] channels = new String[ ( int ) imageViewer.getImage().getRai().dimension( DimensionOrder.C ) ];
		for ( int i = 0; i < channels.length; i++ )
			channels[ i ] = "" + i;
		gd.addChoice( "Channel", channels, "0" );
		gd.showDialog();
		int channel;
		if ( gd.wasCanceled() ) return null;
		channel = gd.getNextChoiceIndex();
		return channel;
	}

	public static void setOutputViewerPosition( BdvImageViewer viewer, BdvImageViewer outputViewer )
	{
		JFrame inputViewerFrame = ( JFrame ) SwingUtilities.getWindowAncestor( viewer.getBdvHandle().getViewerPanel() );
		final int x = inputViewerFrame.getLocationOnScreen().x + inputViewerFrame.getWidth() + 10;
		final int y = inputViewerFrame.getLocationOnScreen().y;

		JFrame outputViewerFrame = ( JFrame ) SwingUtilities.getWindowAncestor( outputViewer.getBdvHandle().getViewerPanel() );
		outputViewerFrame.setLocation( x, y );
	}

	public static void moveWindowToCurrentMousePosition( JComponent component )
	{
		final Window window = SwingUtilities.getWindowAncestor( component );
		window.setLocation( MouseInfo.getPointerInfo().getLocation().x - 50, MouseInfo.getPointerInfo().getLocation().y - 50 );
	}

	public static void moveWindowToPosition( JComponent component, int x, int y )
	{
		final Window window = SwingUtilities.getWindowAncestor( component );
		window.setLocation( x, y );
	}

	public static void centerWindowToPosition( JComponent component )
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final Window window = SwingUtilities.getWindowAncestor( component );
		window.setLocation( screenSize.width / 2 - window.getWidth() / 2, screenSize.height / 2 - window.getHeight() / 2 );
	}

}
