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
