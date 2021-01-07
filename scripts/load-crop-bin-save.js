
// To run this script, please select language: JavaScript
importClass(Packages.de.embl.cba.bdp2.BigDataProcessor2);
importClass(Packages.de.embl.cba.bdp2.save.SavingSettings);
importClass(Packages.de.embl.cba.bdp2.save.SaveFileType);

// Open Position And Channel Subset...
image = BigDataProcessor2.openHDF5Series( "/Volumes/Tischi/big-image-data/luxendo-publication-figure/mouse_2_Cam", ".*stack_6_(?<C1>channel_.*)/(?<C2>Cam_.*)_(?<T>\\d+).h5", "Data", ["channel_2_Cam_Long","channel_2_Cam_Short"] );
// BigDataProcessor2.showImage( image, true );

// Crop...
image = BigDataProcessor2.crop( image, [502,454,14,0,0,1802,1685,87,1,143] );
image.setName( "mouse_2_Cam-crop" );

// Bin...
image = BigDataProcessor2.bin( image, [3,3,1,1,1] );
image.setName( "mouse_2_Cam-crop-binned" );

// Save...
savingSettings = SavingSettings.getDefaults();
savingSettings.volumesFilePathStump = "/Volumes/Tischi/tmp/volumes/mouse_2_Cam-crop-binned";
savingSettings.projectionsFilePathStump = "/Volumes/Tischi/tmp/projections/mouse_2_Cam-crop-binned";
savingSettings.numIOThreads = 1;
savingSettings.numProcessingThreads = 4;
savingSettings.fileType = SaveFileType.TIFFVolumes;
savingSettings.saveProjections = true;
savingSettings.saveVolumes = true;
savingSettings.compression = "None";
savingSettings.tStart = 0;
savingSettings.tEnd = 1;
BigDataProcessor2.saveImageAndWaitUntilDone( image, savingSettings );

