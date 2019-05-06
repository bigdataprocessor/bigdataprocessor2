import de.embl.cba.bdp2.loading.files.FileInfos
import de.embl.cba.bdp2.process.splitviewmerge.RegionOptimiser
import de.embl.cba.bdp2.process.splitviewmerge.SplitImageMerger
import de.embl.cba.bdp2.saving.SavingSettings
import de.embl.cba.bdp2.ui.BigDataProcessor2

#@File[] (label="Select", style="both") inputDirs

print( "Number of dirs = " + inputDirs.size() + "\n" )

for ( i = 0; i < inputDirs.size(); i++ )
{
    print( i + " " + inputDirs[ i ] + "\n" )

    bdp = new BigDataProcessor2()

    /**
     * Open Data
     */

    image = bdp.openHdf5Data(
            inputDirs[ i ].toString(),
            FileInfos.SINGLE_CHANNEL_TIMELAPSE,
            ".*.h5",
            "Data" )

    image.setVoxelUnit( "micrometer" )
    image.setVoxelSpacing( [ 0.13, 0.13, 1.04 ] as double[] )

    /**
     * Merge Split
     */

    final ArrayList< long[] > centres = new ArrayList<>()
    centres.add( [ 522, 1143 ] as long[] )
    centres.add( [ 1396, 546 ] as long[] )
    spans = [ 1000 , 1000 ] as long[]

    //optimisedCentres = RegionOptimiser.optimiseCentres2D( image, centres, spans )

    merge = SplitImageMerger.merge( image, centres, spans )
    bdp.showImage( merge )

    // TODO: Shall we bin 3x3 in xy?


    /**
     * Save as Tiff Stacks
     */

    final SavingSettings savingSettings = SavingSettings.getDefaults()
    savingSettings.fileType = SavingSettings.FileType.TIFF_STACKS
    savingSettings.nThreads = Runtime.getRuntime().availableProcessors()
    savingSettings.saveVolumes = true
    savingSettings.volumesFilePath = inputDirs[ i ].toString() + "-stacks/stack"
    savingSettings.saveProjections = true
    savingSettings.projectionsFilePath = inputDirs[ i ].toString() + "-projections/projection"
    savingSettings.isotropicProjectionResampling = true
    savingSettings.isotropicProjectionVoxelSize = 0.5 // micrometer

    imgSaver = bdp.saveImage( merge, savingSettings )

}
