package de.embl.cba.bdp2.ui;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.bin.Binner;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.record.MacroRecorder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class AbstractOkCancelDialog extends JDialog
{
	protected final AbstractOkCancelDialog.OkCancelPanel buttons;

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

	protected abstract void ok();

	protected abstract void cancel();

	/**
	 * A panel containing OK and Cancel buttons, and callback lists for both.
	 */
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
