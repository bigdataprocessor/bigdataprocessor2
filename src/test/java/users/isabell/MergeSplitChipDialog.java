package users.isabell;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.process.align.splitchip.SplitChipDialog;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.viewer.ImageViewer;
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

        final Image< R > image = BigDataProcessor2.openHdf5Series(
                "/Users/tischer/Desktop/stack_0_channel_0",
                ".*.h5",
                "Data");

        image.setVoxelUnit( "micrometer" );
        image.setVoxelSize( new double[]{0.13, 0.13, 1.04} );

        final ImageViewer viewer = BigDataProcessor2.showImage( image);
        new SplitChipDialog( viewer );
    }

}
