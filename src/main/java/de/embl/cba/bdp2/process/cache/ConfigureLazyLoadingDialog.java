package de.embl.cba.bdp2.process.cache;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.viewer.ImageViewer;
import ij.gui.GenericDialog;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class ConfigureLazyLoadingDialog< R extends RealType< R > & NativeType< R > >
{
	private final Image< R > inputImage;
	private final ImageViewer< R > viewer;
	private String[] channelNames;

	public ConfigureLazyLoadingDialog( final ImageViewer< R > viewer  )
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
//		recorder.addAPIFunctionPrequelComment(  ImageRenameCommand.COMMAND_NAME );
//		recorder.addAPIFunctionParameter( MacroRecorder.quote( inputImage.getName() ) );
//		recorder.addAPIFunctionParameter( inputImage.getChannelNames() );
//		recorder.record();
	}

	protected void showDialog()
	{
		final GenericDialog gd = new GenericDialog( "Set Cache Dimensions" );

		int[] cachedCellDims = inputImage.getCachedCellDims();

		gd.addNumericField( "Cache Size X", cachedCellDims[ DimensionOrder.X ] );
		gd.addNumericField( "Cache Size Y", cachedCellDims[ DimensionOrder.Y ] );
		gd.addNumericField( "Cache Size Z", cachedCellDims[ DimensionOrder.Z ] );

		gd.showDialog();

		if( gd.wasCanceled() ) return;

		cachedCellDims[ DimensionOrder.X ] = (int) gd.getNextNumber();
		cachedCellDims[ DimensionOrder.Y ] = (int) gd.getNextNumber();
		cachedCellDims[ DimensionOrder.Z ] = (int) gd.getNextNumber();

		// cache size is ignored for SOFTREF
		inputImage.setCache( cachedCellDims, DiskCachedCellImgOptions.CacheType.SOFTREF, 0 );

		viewer.replaceImage( inputImage, false, true );

		recordMacro();
	}
}
