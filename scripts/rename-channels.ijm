run("BigDataProcessor2");
run("BDP2 Open Custom File Series...", "viewingmodality=[Show in new viewer] enablearbitraryplaneslicing=false directory=/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-nc2-nt6-subfolders fileextension=.tif regexp=(?<C>.*)/.*T(?<T>\\d+) hdf5datasetname=Data");
run("BDP2 Set Voxel Size...", "inputimage=[tiff-nc2-nt6-subfolders] outputimagename=[tiff-nc2-nt6-subfolders] viewingmodality=[Show in current viewer] unit=[µm] voxelsizex=1.0 voxelsizey=1.0 voxelsizez=1.0 ");
run("BDP2 Rename...", "inputimage=[tiff-nc2-nt6-subfolders] outputimagename=[im] viewingmodality=[Show in new viewer] channelnames=[a,b] ");
run("BDP2 Save As...", "inputimage=[im] directory=[/Users/tischer/Desktop/bdp2-out/] numiothreads=1 numprocessingthreads=4 filetype=[TIFFVolumes] saveprojections=true projectionmode=[sum] savevolumes=true channelnames=[Channel names] tiffcompression=[None] tstart=0 tend=5 ");
run("BDP2 Show as Hyperstack...", "inputimage=[tiff-nc2-nt2-16bit] ");
