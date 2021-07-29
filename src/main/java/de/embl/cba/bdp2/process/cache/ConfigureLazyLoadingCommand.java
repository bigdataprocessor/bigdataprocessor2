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

import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.cache.img.optional.CacheOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;

import static de.embl.cba.bdp2.process.cache.ConfigureLazyLoadingCommand.COMMAND_NAME;

@Plugin(type = AbstractImageProcessingCommand.class, name = COMMAND_NAME,  menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PATH + ConfigureLazyLoadingCommand.COMMAND_FULL_NAME )
public class ConfigureLazyLoadingCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Configure Lazy Loading...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Cache dimensions x,y,z,c,t")
    String chacheDimensions = "100,100,1,1,1"; // TODO: pre-fill with current
    public static String CACHE_DIMENSIONS = "chacheDimensions";

    // TODO: Make a choice
    private CacheOptions.CacheType cacheType = CacheOptions.CacheType.SOFTREF;

    @Override
    public void run()
    {
        process();
        handleOutputImage( false, true );
        ImageService.imageNameToImage.put( outputImage.getName(), outputImage );
    }

    private void process()
    {
        final int[] cacheDims = Arrays.stream( chacheDimensions.split( "," ) ).mapToInt( i -> Integer.parseInt( i ) ).toArray();

        outputImage = new Image<>( inputImage );
        outputImage.setCache( cacheDims, CacheOptions.CacheType.SOFTREF, 0  );
        outputImage.setName( outputImageName );

        Logger.info( "\n# " + ConfigureLazyLoadingCommand.COMMAND_NAME );
        Logger.info( "Image: " + inputImage.getName() );
        Logger.info( "Cell dimensions: " + Arrays.toString( cacheDims ) );
        Logger.info( "Type: " + cacheType.toString() );
    }

    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {
        new ConfigureLazyLoadingDialog<>( imageViewer ).showDialog();
    }
}
