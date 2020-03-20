package users.isabell;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.load.files.FileInfos;
import de.embl.cba.bdp2.splitviewmerge.SplitViewMergeDialog;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class MergeSplitChipDialog
{
    public static < R extends RealType< R > & NativeType< R > >
    void main( String[] args )
    {
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();

        final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

        final Image< R > image = bdp.openHdf5Image(
                "/Users/tischer/Desktop/stack_0_channel_0",
                FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                ".*.h5",
                "Data" );

        image.setVoxelUnit( "micrometer" );
        image.setVoxelSpacing( new double[]{0.13, 0.13, 1.04} );

        final BdvImageViewer viewer = bdp.showImage( image );
        new SplitViewMergeDialog( viewer );
    }

}
