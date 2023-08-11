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
package de.embl.cba.bdp2.process.calibrate;

import ch.epfl.biop.bdv.img.legacy.bioformats.BioFormatsTools;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = AbstractImageProcessingCommand.class, name = SetVoxelSizeCommand.COMMAND_NAME, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PATH + SetVoxelSizeCommand.COMMAND_FULL_NAME )
public class SetVoxelSizeCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Set Voxel Size...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Unit", choices = { "micrometer", "nanometer" }, persist = false)
    public String unit = "micrometer";

    @Parameter(label = "Voxel size X", persist = false)
    public double voxelSizeX = 1.0;
    public static String VOXEL_SIZE_X_PARAMETER = "voxelSizeX";

    @Parameter(label = "Voxel size Y", persist = false)
    public double voxelSizeY = 1.0;
    public static String VOXEL_SIZE_Y_PARAMETER = "voxelSizeY";

    @Parameter(label = "Voxel size Z", persist = false)
    public double voxelSizeZ = 1.0;
    public static String VOXEL_SIZE_Z_PARAMETER = "voxelSizeZ";

    public void run()
    {
        outputImage = BigDataProcessor2.setVoxelSize( inputImage, new double[]{ voxelSizeX, voxelSizeY, voxelSizeZ }, BioFormatsTools.getUnitFromString( unit ) );
        log();

        handleOutputImage( false, false );
    }

    private void log()
    {
        Logger.log( COMMAND_FULL_NAME );
        double[] voxelSize = outputImage.getVoxelDimensions();
        for ( int d = 0; d < 3; d++ )
        {
            Logger.log( "Voxel size [" + d + "]: " + voxelSize[ d ] );
        }
    }

    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {
        new CalibrationDialog< R >( imageViewer ).showDialog();
    }
}
