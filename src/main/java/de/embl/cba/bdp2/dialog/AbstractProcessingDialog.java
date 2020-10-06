package de.embl.cba.bdp2.dialog;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;

public abstract class AbstractProcessingDialog< R extends RealType< R > & NativeType< R > > extends AbstractOkCancelDialog
{
	protected ImageViewer< R > viewer;
	protected Image< R > inputImage;
	protected Image< R > outputImage;

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

	@Override
	protected void cancel()
	{
		viewer.replaceImage( inputImage, true, true );
		setVisible( false );
	}

	protected abstract void recordMacro();
}
