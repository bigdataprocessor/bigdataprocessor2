package explore;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.load.files.FileInfos;
import de.embl.cba.bdp2.load.CachedCellImgReader;
import de.embl.cba.bdp2.process.splitviewmerge.SplitViewMergingDialog;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class SplitChipMerging
{
	public static < R extends RealType< R > & NativeType< R > >
	void main( String[] args )
	{
		final Image< R > image = openImage();

		final BdvImageViewer< R > viewer = new BdvImageViewer<>( image );

		new SplitViewMergingDialog< R >( viewer );
	}

	public static < R extends RealType< R > & NativeType< R > > Image< R > openImage()
	{
		String imageDirectory = "/Users/tischer/Desktop/stack_0_channel_0";

		final FileInfos fileInfos = new FileInfos( imageDirectory,
				FileInfos.SINGLE_CHANNEL_TIMELAPSE,
				".*.h5", "Data" );

		fileInfos.voxelSpacing = new double[]{ 0.5, 0.5, 5.0 };

		return CachedCellImgReader.loadImage( fileInfos );
	}

}

