package de.embl.cba.bdp2.dialog;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public abstract class AbstractProcessingDialog< R extends RealType< R > & NativeType< R > > extends AbstractOkCancelDialog< R >
{
	protected BdvImageViewer< R > viewer;
	protected Image< R > inputImage;
	protected Image< R > outputImage;
	protected JPanel panel;

	public AbstractProcessingDialog( )
	{
		super();
	}


	@Override
	protected void ok()
	{
		recordMacro();
		setVisible( false );
	}

	protected abstract void recordMacro();

	@Override
	protected void cancel()
	{
		viewer.replaceImage( inputImage, true, true );
		setVisible( false );
	}
}
