/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
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
package users.isabell;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.process.align.splitchip.SplitChipMerger;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.BigDataProcessor2;
import net.imagej.ImageJ;
import net.imglib2.Interval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;
import java.util.ArrayList;

import static de.embl.cba.bdp2.dialog.DialogUtils.selectDirectories;

public class MergeSplitChipWorkflow
{
    public static < R extends RealType< R > & NativeType< R > >
    void main( String[] args )
    {
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();

        final ArrayList< File > directories = selectDirectories();

        final String voxelUnit = "micrometer";
        double voxelSpacingMicrometerX = 0.13;
        double voxelSpacingMicrometerY = 0.13;
        double voxelSpacingMicrometerZ = 1.04;

        final SavingSettings savingSettings = SavingSettings.getDefaults();
        savingSettings.fileType = SaveFileType.TIFFVolumes;
        savingSettings.numIOThreads = Runtime.getRuntime().availableProcessors();

        final SplitChipMerger merger = new SplitChipMerger();
        merger.addIntervalXYC( 86, 5, 1000, 1000, 0 );
        merger.addIntervalXYC( 1, 65, 1000, 1000, 0 );


        /*
         * Get cropping intervals from user
         */
        ArrayList< Interval > croppingIntervals = new ArrayList<>(  );

        for ( File directory : directories )
        {
            final Image< R > image = BigDataProcessor2.openHDF5Series(
                    directory.toString(),
                    NamingSchemes.SINGLE_CHANNEL_TIMELAPSE,
                    "Data");
            image.setVoxelUnit( voxelUnit );
            image.setVoxelDimensions(
                    voxelSpacingMicrometerX,
                    voxelSpacingMicrometerY,
                    voxelSpacingMicrometerZ );

//            final Image< R > merge = merger.mergeRegionsAandB( image );

//            final BdvImageViewer viewer = bdp.showImage( merge );
//
//            final FinalInterval interval = viewer.get5DIntervalFromUser( false );
//
//            Logger.log( "Data set: " + directory );
//            Logger.log( "Crop interval: " + interval.toString()   );
//            croppingIntervals.add( interval );
        }


        /*
         *
         * Save both the complete merged data
         * as well as the cropped data, with projections
         *
         */
        for ( int i = 0; i < directories.size(); i++ )
        {
            // open
            final String directory = directories.get( i ).toString();
            final Image< R > image = BigDataProcessor2.openHDF5Series(
                    directory,
                    NamingSchemes.SINGLE_CHANNEL_TIMELAPSE,
                    "Data");

            image.setVoxelUnit( voxelUnit );
            image.setVoxelDimensions(
                    voxelSpacingMicrometerX,
                    voxelSpacingMicrometerY,
                    voxelSpacingMicrometerZ );

//            // merge
//            final Image< R > merge = merger.mergeRegionsAandB( image );
//            savingSettings.saveVolumes = true;
//            savingSettings.volumesFilePath = directory + "-stacks/stack";
//            savingSettings.saveProjections = false;
//            Utils.saveImageAndWaitUntilDone( bdp, savingSettings, merge );
//
//            // crop
//            final Image< R > crop = Cropper.crop( merge, croppingIntervals.get( i ) );
//            savingSettings.saveVolumes = true;
//            savingSettings.volumesFilePath = directory + "-crop-stacks/stack";
//            savingSettings.saveProjections = true;
//            savingSettings.projectionsFilePath =
//                    directory + "-crop-projections/projection";
//            Utils.saveImageAndWaitUntilDone( bdp, savingSettings, crop );

        }

        Logger.log( "Done!" );
    }


}
