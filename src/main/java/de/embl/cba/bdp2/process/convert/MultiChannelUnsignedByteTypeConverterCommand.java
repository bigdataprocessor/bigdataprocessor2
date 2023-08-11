/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2023 EMBL
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
package de.embl.cba.bdp2.process.convert;

import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;

import static de.embl.cba.bdp2.dialog.DialogUtils.*;

@Plugin(type = AbstractImageProcessingCommand.class, name = MultiChannelUnsignedByteTypeConverterCommand.COMMAND_NAME, menuPath = BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PATH + MultiChannelUnsignedByteTypeConverterCommand.COMMAND_FULL_NAME )
public class MultiChannelUnsignedByteTypeConverterCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Convert to 8-Bit...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Map to 0 [channel0, channel1, ...]")
    String mapTo0 = "0.0, 0.0";

    @Parameter(label = "Map to 255 [channel0, channel1, ...]")
    String mapTo255 = "65535.0, 65535.0";

    @Override
    public void run()
    {
        Logger.info( COMMAND_FULL_NAME );
        process();
        handleOutputImage( true, true );
    }

    private void process()
    {
        double[] mins = Utils.delimitedStringToDoubleArray( mapTo0, "," );
        double[] maxs = Utils.delimitedStringToDoubleArray( mapTo255, "," );

        long numChannels = inputImage.getNumChannels();

        if ( mins.length != maxs.length || mins.length != numChannels )
        {
            Logger.error( "The min and max mappings for the conversion must have the same length and correspond to the number of channels of the image.");
            return;
        }

        ArrayList< double[] > contrastLimits = new ArrayList<>();
        for ( int channel = 0; channel < numChannels; channel++ )
        {
            contrastLimits.add( new double[]{ mins[ channel ], maxs[ channel ] } );
        }

        MultiChannelUnsignedByteTypeConverter converter = new MultiChannelUnsignedByteTypeConverter<>( inputImage, contrastLimits );

        outputImage = converter.getConvertedImage();
    }

    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {
        new MultiChannelUnsignedByteTypeConverterDialog< R >( imageViewer ).showDialog();
    }
}
