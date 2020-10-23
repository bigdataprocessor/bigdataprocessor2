package de.embl.cba.bdp2.process.transform;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.AbstractOpenCommand;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import ij.gui.GenericDialog;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class TransformDialog< T extends RealType< T > & NativeType< T > >
{
	private static String affine = "1,0,0,0,0,1,0,0,0,0,1,0";
	private static String interpolation = TransformCommand.NEAREST;
	private final ImageViewer< T > viewer;
	private final Image< T > inputImage;
	private Image< T > outputImage;

	public TransformDialog( final ImageViewer< T > viewer )
	{
		this.viewer = viewer;
		this.inputImage = viewer.getImage();
	}

	public void showDialog()
	{
		final GenericDialog genericDialog = new GenericDialog( "Transform" );
		genericDialog.addStringField( TransformCommand.AFFINE_LABEL, affine, 30 );
		genericDialog.addChoice( "Interpolation", new String[]{ TransformCommand.NEAREST, TransformCommand.LINEAR }, interpolation );
		genericDialog.showDialog();
		if ( genericDialog.wasCanceled() ) return;
		affine = genericDialog.getNextString();
		interpolation = genericDialog.getNextChoice();
		outputImage = BigDataProcessor2.transform( inputImage, TransformCommand.getAffineTransform3D( affine ), Utils.getInterpolator( interpolation ) );
		outputImage.setName( inputImage.getName() + "-transformed" );
		BigDataProcessor2.showImageInheritingDisplaySettings( outputImage, inputImage );
		recordMacro();
	}

	private void recordMacro()
	{
		final MacroRecorder recorder = new MacroRecorder( TransformCommand.COMMAND_FULL_NAME, inputImage, outputImage, AbstractOpenCommand.SHOW_IN_NEW_VIEWER );
		recorder.addCommandParameter( TransformCommand.AFFINE_STRING_PARAMETER, affine );
		recorder.addCommandParameter( TransformCommand.INTERPOLATION_PARAMETER, interpolation );

		recorder.record();
	}
}
