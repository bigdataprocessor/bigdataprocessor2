/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public abstract class AbstractOkCancelDialog extends JDialog
{
	protected final AbstractOkCancelDialog.OkCancelPanel buttons;
	protected JPanel panel;

	public AbstractOkCancelDialog( )
	{
		buttons = new AbstractOkCancelDialog.OkCancelPanel();
		getContentPane().add( buttons, BorderLayout.SOUTH );
		setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				cancel();
			}
		} );

		buttons.onOk( this::ok );
		buttons.onCancel( this::cancel );
	}

	public void showDialog()
	{
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		getContentPane().add( panel, BorderLayout.CENTER );
		this.setBounds(
				MouseInfo.getPointerInfo().getLocation().x - 50,
				MouseInfo.getPointerInfo().getLocation().y - 50,
				120, 10);
		setResizable( false );
		pack();
		SwingUtilities.invokeLater( () -> setVisible( true ) );
	}

	/**
	 * Fill JPanel panel field with content,
	 * such that it can be displayed by showDialog()
	 */
	protected abstract void createPanel();

	protected abstract void ok();

	protected abstract void cancel();

	public static class OkCancelPanel extends JPanel
	{
		private final ArrayList< Runnable > runOnOk = new ArrayList<>();

		private final ArrayList< Runnable > runOnCancel = new ArrayList<>();

		public OkCancelPanel()
		{
			final JButton cancelButton = new JButton( "Cancel" );
			cancelButton.addActionListener( e -> runOnCancel.forEach( Runnable::run ) );

			final JButton okButton = new JButton( "OK" );
			okButton.addActionListener( e -> runOnOk.forEach( Runnable::run ) );

			setLayout( new BoxLayout( this, BoxLayout.LINE_AXIS ) );
			add( Box.createHorizontalGlue() );
			add( cancelButton );
			add( okButton );
		}

		public synchronized void onOk( final Runnable runnable )
		{
			runOnOk.add( runnable );
		}

		public synchronized void onCancel( final Runnable runnable )
		{
			runOnCancel.add( runnable );
		}
	}

}
