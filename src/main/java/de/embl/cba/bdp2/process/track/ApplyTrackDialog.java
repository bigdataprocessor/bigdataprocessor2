package de.embl.cba.bdp2.process.track;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdv.utils.BdvUtils;
import fiji.util.gui.GenericDialogPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;

public class ApplyTrackDialog< R extends RealType< R > & NativeType< R > >
{
	private final Image< R > inputImage;
	private Image< R > outputImage;
	private static File trackFile = new File( "" );
	private static boolean centerImage = false;

	public ApplyTrackDialog( final BdvImageViewer< R > viewer )
	{
		this.inputImage = viewer.getImage();
		showGenericDialog();
	}

	private void showGenericDialog()
	{
		final GenericDialogPlus genericDialog = new GenericDialogPlus( "Apply Track" );
		genericDialog.addFileField( "Track", trackFile.getAbsolutePath(), 50 );
		genericDialog.addCheckbox("Center image on track positions", centerImage );

		genericDialog.showDialog();
		if ( genericDialog.wasCanceled() ) return;

		trackFile = new File ( genericDialog.getNextString() );
		centerImage = genericDialog.getNextBoolean();
		outputImage = BigDataProcessor2.applyTrack( trackFile, inputImage, centerImage );

		final BdvImageViewer viewer = BigDataProcessor2.showImage( outputImage );
		if ( centerImage )
			BdvUtils.moveToPosition( viewer.getBdvHandle(), new double[]{ 0, 0, 0 }, 0 , 0);

		recordMacro();
	}

	protected void recordMacro()
	{
		final MacroRecorder recorder = new MacroRecorder( ApplyTrackCommand.COMMAND_FULL_NAME, inputImage, outputImage );

		recorder.addOption( ApplyTrackCommand.TRACK_FILE_PARAMETER, trackFile );
		recorder.addOption( ApplyTrackCommand.CENTER_IMAGE_PARAMETER, centerImage);

		recorder.record();
	}
}
