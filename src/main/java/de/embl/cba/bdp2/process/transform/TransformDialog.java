package de.embl.cba.bdp2.process.transform;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import ij.gui.GenericDialog;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class TransformDialog< T extends RealType< T > & NativeType< T > >
{
	private static String affineTransform = "1,0,0,0,0,1,0,0,0,0,1,0";
	private static String interpolationMode = TransformCommand.NEAREST;
	private final ImageViewer< T > viewer;
	private final Image< T > inputImage;
	private Image< T > outputImage;

	public TransformDialog( final ImageViewer< T > viewer )
	{
		this.viewer = viewer;
		this.inputImage = viewer.getImage();
	}

	public void show()
	{
		final GenericDialog genericDialog = new GenericDialog( "Transform" );
		genericDialog.addStringField( TransformCommand.AFFINE_LABEL, affineTransform, 30 );
		genericDialog.addChoice( "Interpolation", new String[]{ TransformCommand.NEAREST, TransformCommand.LINEAR }, interpolationMode );

		genericDialog.showDialog();
		if ( genericDialog.wasCanceled() ) return;

		affineTransform = genericDialog.getNextString();
		interpolationMode = genericDialog.getNextChoice();
		outputImage = BigDataProcessor2.transform( inputImage, TransformCommand.getAffineTransform3D( affineTransform ), Utils.getInterpolator( interpolationMode ) );
		outputImage.setName( inputImage.getName() + "-transformed" );
		BigDataProcessor2.showImage( outputImage, inputImage );
		recordMacro();
	}

	private void recordMacro()
	{
		final MacroRecorder recorder = new MacroRecorder( TransformCommand.COMMAND_FULL_NAME, inputImage, outputImage, AbstractOpenFileSeriesCommand.SHOW_IN_NEW_VIEWER );
		recorder.addCommandParameter( TransformCommand.AFFINE_STRING_PARAMETER, affineTransform );
		recorder.addCommandParameter( TransformCommand.INTERPOLATION_PARAMETER, interpolationMode );

		recorder.setAPIFunctionName( "transform" );
		recorder.addAPIFunctionPrequel( "# " + TransformCommand.COMMAND_NAME );
		recorder.addAPIFunctionParameter( TransformCommand.getAffineTransform3D( affineTransform ).getRowPackedCopy() );
		recorder.addAPIFunctionParameter( interpolationMode );

		recorder.record();
	}
}
