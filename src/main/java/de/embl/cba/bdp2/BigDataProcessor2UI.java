/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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
package de.embl.cba.bdp2;

import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.image.Image;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public abstract class BigDataProcessor2UI
{
	public static final String SPEED = "Data transfer rate [MByte/s]: ";
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

	public static void setReadPerformanceInformation( double mbps, double medianMPBS )
	{
		if ( frame == null ) return; // UI not instantiated

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		readInfo.setText(  wrapAsHtml(SPEED + df.format( mbps ) + " (Median: " + df.format( medianMPBS ) + ")") );
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
