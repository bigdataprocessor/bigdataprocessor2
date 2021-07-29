/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.process.cache;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.viewer.ImageViewer;
import ij.gui.GenericDialog;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.optional.CacheOptions;
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
		inputImage.setCache( cachedCellDims, CacheOptions.CacheType.SOFTREF, 0 );

		viewer.replaceImage( inputImage, false, true );

		recordMacro();
	}
}
