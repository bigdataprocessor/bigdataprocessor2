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
package de.embl.cba.bdp2.dialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class HelpWindow extends JFrame
{
	private static final long serialVersionUID = 1L;

	// Instantiates and displays a JFrame that lists the help file for the SPIM viewer UI.
	public HelpWindow( final URL helpFile )
	{
		if ( helpFile == null )
		{
			System.err.println( "helpFile url is null." );
			return;
		}
		try
		{
			final JEditorPane editor = new JEditorPane( );
			editor.setEditable( false );
			editor.setBorder( BorderFactory.createEmptyBorder( 10, 0, 10, 10 ) );
			editor.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
			editor.setPage( helpFile );

			editor.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate( HyperlinkEvent e) {
					if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						if( Desktop.isDesktopSupported()) {
							try
							{
								Desktop.getDesktop().browse(e.getURL().toURI());
							} catch ( IOException ex )
							{
								ex.printStackTrace();
							} catch ( URISyntaxException ex )
							{
								ex.printStackTrace();
							}
						}
					}
				}
			});

			final JScrollPane editorScrollPane = new JScrollPane( editor );
			editorScrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED );
			editorScrollPane.setPreferredSize( new Dimension( 800, 800 ) );

			getContentPane().add( editorScrollPane, BorderLayout.CENTER );

			final ActionMap am = getRootPane().getActionMap();
			final InputMap im = getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
			final Object hideKey = new Object();
			final Action hideAction = new AbstractAction()
			{
				private static final long serialVersionUID = 6288745091648466880L;

				@Override
				public void actionPerformed( final ActionEvent e )
				{
					setVisible( false );
				}
			};
			im.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), hideKey );
			am.put( hideKey, hideAction );

			pack();
			setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}
}

