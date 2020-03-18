package de.embl.cba.bdp2.ui;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public abstract class AbstractProcessingDialog< R extends RealType< R > & NativeType< R > > extends JDialog
{
	protected final AbstractProcessingDialog.OkCancelPanel buttons;
	protected BdvImageViewer viewer;
	protected Image< R > inputImage;
	protected Image< R > outputImage;
	protected JPanel panel;

	public AbstractProcessingDialog( )
	{
		buttons = new AbstractProcessingDialog.OkCancelPanel();
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

	protected abstract void ok();

	protected abstract void cancel();

	protected void showDialog( JPanel panel )
	{
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		getContentPane().add( panel, BorderLayout.CENTER );
		setBounds(
				MouseInfo.getPointerInfo().getLocation().x,
				MouseInfo.getPointerInfo().getLocation().y,
				120, 10);
		setResizable( false );
		pack();
		setVisible( true );
	}


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
