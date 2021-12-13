/*
 * When you record a macro the viewingmodality will typically be: [Show in current viewer] or [Show in new viewer]
 * Replacing this with [Do not show] everywhere will prevents the image windows to be shown on the screen while running the macro.
 * This can be a good idea when processing many images in batch.
 *
 * Also note that the first line does *not* say run("BigDataProcessor2"),
 * as it typically does when you record a macro.
 * Removing this line prevents the BDP2 UI from showing up.
 *
 */
run("BDP2 Open Custom File Series...", "viewingmodality=[Do not show] enablearbitraryplaneslicing=false directory=/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-nc2-nt6-subfolders fileextension=.tif regexp=(?<C>.*)/.*T(?<T>\\d+) hdf5datasetname=Data");
run("BDP2 Set Voxel Size...", "inputimage=[tiff-nc2-nt6-subfolders] outputimagename=[tiff-nc2-nt6-subfolders] viewingmodality=[Show in current viewer] unit=[µm] voxelsizex=1.0 voxelsizey=1.0 voxelsizez=1.0 ");
run("BDP2 Rename...", "inputimage=[tiff-nc2-nt6-subfolders] outputimagename=[im] viewingmodality=[Do not show] channelnames=[a,b] ");
run("BDP2 Save As...", "inputimage=[im] directory=[/Users/tischer/Desktop/bdp2-out] numiothreads=1 numprocessingthreads=4 filetype=[TIFFVolumes] saveprojections=true projectionmode=[sum] savevolumes=true channelnames=[Channel names] tiffcompression=[None] tstart=0 tend=5 ");