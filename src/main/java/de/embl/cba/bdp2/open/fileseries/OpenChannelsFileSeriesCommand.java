/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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
package de.embl.cba.bdp2.open.fileseries;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.record.ScriptRecorder;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import java.io.File;
import java.util.Arrays;

import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenFileSeriesCommand.COMMAND_OPEN_PATH + OpenChannelsFileSeriesCommand.COMMAND_FULL_NAME )
public class OpenChannelsFileSeriesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenFileSeriesCommand< R >
{
    public static final String COMMAND_NAME = "Open Position And Channel Subset...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Regular Expression"  )
    protected String regExp = "";
    protected static String REGEXP_PARAMETER = "regExp";

    @Parameter(label = "Channels")
    protected String channelSubset = "channel_0_Cam_Short,channel_0_Cam_Right";
    protected static String CHANNELS_PARAMETER = "channelSubset";

    private String[] channels;
    private String[] files;

    public OpenChannelsFileSeriesCommand( )
    {
    }

    public OpenChannelsFileSeriesCommand( File directory, String[] files, String channelSubset, String regExp  )
    {
        this.channelSubset = channelSubset;
        this.regExp = regExp;
        this.directory = directory;
        this.files = files;
    }

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {

            channels = Arrays.stream( channelSubset.split( "," ) ).map( String::trim ).toArray( String[]::new );

            if ( NamingSchemes.isLuxendoNamingScheme( regExp ) )
            {
                outputImage = BigDataProcessor2.openHDF5Series(
                                directory.toString(),
                                files, // @Nullable, e.g., if called via macro
                                regExp,
                               "Data",
                                channels );
            }
            else if ( regExp.contains( NamingSchemes.TIF ) )
            {
                outputImage = BigDataProcessor2.openTIFFSeries(
                                directory.toString(),
                                files, // @Nullable, e.g., if called via macro
                                regExp,
                                channels );
            }

            handleOutputImage( true, false );

            recordMacro();
        });
    }

    private void recordMacro()
    {
        if ( ScriptRecorder.isScriptMode() )
        {
            recordAPICall();
        }
        else
        {
            ScriptRecorder recorder = new ScriptRecorder( this.COMMAND_FULL_NAME, viewingModality, outputImage );
            recorder.addCommandParameter( AbstractOpenFileSeriesCommand.DIRECTORY_PARAMETER, directory.getAbsolutePath() );
            recorder.addCommandParameter( AbstractOpenFileSeriesCommand.ARBITRARY_PLANE_SLICING_PARAMETER, enableArbitraryPlaneSlicing );
            recorder.addCommandParameter( this.REGEXP_PARAMETER, regExp );
            recorder.addCommandParameter( this.CHANNELS_PARAMETER, channelSubset );
            recorder.record();
        }
    }

    @Override
    public void recordAPICall()
    {
        ScriptRecorder recorder = new ScriptRecorder( outputImage );
        recorder.recordImportStatements( true );
        recorder.recordShowImage( true );

        if ( regExp.contains( NamingSchemes.HDF5 ) )
        {
            recorder.setBDP2FunctionName( "openHDF5Series" );
            recorder.addAPIFunctionPrequelComment( this.COMMAND_NAME );
            recorder.addAPIFunctionParameter( recorder.quote( directory.toString() ) );
            recorder.addAPIFunctionParameter( recorder.quote( regExp ) );
            recorder.addAPIFunctionParameter( recorder.quote( "Data" ) );
            recorder.addAPIFunctionParameter( channels );
        }
        else if ( regExp.contains( NamingSchemes.TIF ) )
        {
            recorder.setBDP2FunctionName( "openTIFFSeries" );
            recorder.addAPIFunctionPrequelComment( this.COMMAND_NAME );
            recorder.addAPIFunctionParameter( recorder.quote( directory.toString() ) );
            recorder.addAPIFunctionParameter( recorder.quote( regExp ) );
            recorder.addAPIFunctionParameter( channels );
        }

        recorder.record();
    }
}
