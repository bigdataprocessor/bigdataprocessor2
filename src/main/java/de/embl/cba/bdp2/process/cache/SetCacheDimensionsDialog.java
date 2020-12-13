package de.embl.cba.bdp2.process.cache;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.process.rename.ImageRenameCommand;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.viewer.ImageViewer;
import ij.gui.GenericDialog;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import static de.embl.cba.bdp2.process.rename.ImageRenameCommand.CHANNEL_NAMES_PARAMETER;

public class SetCacheDimensionsDialog < R extends RealType< R > & NativeType< R > >
{
	private final Image< R > inputImage;
	private final ImageViewer< R > viewer;
	private String[] channelNames;

	public SetCacheDimensionsDialog( final ImageViewer< R > viewer  )
	{
		this.viewer = viewer;
		this.inputImage = viewer.getImage();
	}

	protected void recordMacro()
	{
//		final MacroRecorder recorder = new MacroRecorder( ImageRenameCommand.COMMAND_FULL_NAME, inputImage );
//		recorder.addCommandParameter( CHANNEL_NAMES_PARAMETER, String.join( ",", channelNames ) );
//
//		// Image< R > rename( Image< R > image, String name )
//		recorder.setAPIFunctionName( "rename" );
//		recorder.addAPIFunctionPrequel( "# " +  ImageRenameCommand.COMMAND_NAME );
//		recorder.addAPIFunctionParameter( MacroRecorder.quote( inputImage.getName() ) );
//		recorder.addAPIFunctionParameter( inputImage.getChannelNames() );
//		recorder.record();
	}

	protected void showDialog()
	{
		final GenericDialog gd = new GenericDialog( "Set Cache Dimensions" );

		int[] cachedCellDims = inputImage.getCachedCellDims();

		gd.addNumericField( "Cache Size Y", cachedCellDims[ DimensionOrder.Y ] );

		gd.showDialog();

		if( gd.wasCanceled() ) return;

		cachedCellDims[ DimensionOrder.Y ] = (int) gd.getNextNumber();

		inputImage.setCache( cachedCellDims, DiskCachedCellImgOptions.CacheType.SOFTREF, 10000 );

		viewer.replaceImage( inputImage, false, true );

		recordMacro();
	}
}
