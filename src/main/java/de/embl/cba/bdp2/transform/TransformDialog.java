package de.embl.cba.bdp2.transform;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.AbstractProcessingDialog;
import de.embl.cba.bdp2.open.ui.AbstractOpenCommand;
import de.embl.cba.bdp2.record.MacroRecorder;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import ij.gui.GenericDialog;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class TransformDialog< T extends RealType< T > & NativeType< T > > extends AbstractProcessingDialog< T >
{
	private static String affine = "1,0,0,0,0,1,0,0,0,0,1,0";
	private static String interpolation = TransformCommand.NEAREST;

	public TransformDialog( final BdvImageViewer< T > viewer )
	{
		this.viewer = viewer;
		this.inputImage = viewer.getImage();
		showDialog();
	}

	private void showDialog()
	{
		final GenericDialog genericDialog = new GenericDialog( "Transform" );
		genericDialog.addStringField( TransformCommand.AFFINE_LABEL, affine, 30 );
		genericDialog.addChoice( "Interpolation", new String[]{ TransformCommand.NEAREST, TransformCommand.LINEAR }, interpolation );
		genericDialog.showDialog();
		if ( genericDialog.wasCanceled() ) return;
		affine = genericDialog.getNextString();
		interpolation = genericDialog.getNextChoice();
		outputImage = BigDataProcessor2.transform( inputImage, TransformCommand.getAffineTransform3D( affine ), TransformCommand.getInterpolator( interpolation ) );
		outputImage.setName( inputImage.getName() + "-transformed" );
		BigDataProcessor2.showImageInheritingDisplaySettings( outputImage, inputImage );
		recordMacro();
	}

	@Override
	protected void recordMacro()
	{
		final MacroRecorder recorder = new MacroRecorder( TransformCommand.COMMAND_FULL_NAME, inputImage, outputImage, AbstractOpenCommand.SHOW_IN_NEW_VIEWER );
		recorder.addOption( TransformCommand.AFFINE_STRING_PARAMETER, affine );
		recorder.addOption( TransformCommand.INTERPOLATION_PARAMETER, interpolation );
		recorder.record();
	}
}
