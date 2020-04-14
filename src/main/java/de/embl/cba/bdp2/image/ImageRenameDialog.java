package de.embl.cba.bdp2.image;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.record.MacroRecorder;
import de.embl.cba.bdp2.shift.ChromaticShiftCommand;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import ij.gui.GenericDialog;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.util.ArrayList;

public class ImageRenameDialog< R extends RealType< R > & NativeType< R > >
{
	private final Image< R > inputImage;
	private final BdvImageViewer< R > viewer;
	private Image< R > outputImage;

	public ImageRenameDialog( final BdvImageViewer< R > viewer  )
	{
		this.viewer = viewer;
		this.inputImage = viewer.getImage();
		showDialog();
	}

	protected void recordMacro()
	{
		final MacroRecorder recorder = new MacroRecorder( ImageRenameCommand.COMMAND_NAME, inputImage, outputImage );
		recorder.record();
	}

	protected void showDialog()
	{
		final GenericDialog gd = new GenericDialog( "Rename Image" );
		gd.addStringField( "Image name", inputImage.getName() );
		gd.showDialog();

		if( gd.wasCanceled() ) return;

		inputImage.setName( gd.getNextString() );
		outputImage = inputImage;
		viewer.replaceImage( outputImage, false, true );
		recordMacro();
	}
}
