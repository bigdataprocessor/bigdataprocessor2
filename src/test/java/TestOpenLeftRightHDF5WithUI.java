import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import de.embl.cba.bdp2.loading.CachedCellImgReader;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.viewers.BdvTransformEventHandler;
import net.imglib2.img.Img;

public class TestOpenLeftRightHDF5WithUI {

    /**
     * IMPORTANT NOTE: Adjust Max value to 2550 in the Big Data Viewer. (Settings>Brightness and Color>Max)
     */
    public static void main(String[] args) {


        final String directory = "Y:\\ashis\\movi\\stack_0_channel_2\\";
        FileInfos fileInfosLeft = new FileInfos(directory,"None",
                ".*Left.*.h5","Data");
        FileInfos fileInfosRight = new FileInfos(directory,"None",
                ".*Right.*.h5","Data");

        Img myImgLeft = new CachedCellImgReader().createCachedCellImg( fileInfosLeft );
        Img myImgRight = new CachedCellImgReader().createCachedCellImg( fileInfosRight );
        double [] voxelSpacing = new double[]{0,0};
        final BdvStackSource bdvss0 = BdvFunctions.show(myImgLeft, "left", BdvOptions.options().axisOrder(AxisOrder.XYCZT)
                .doubleBuffered(false)
                .transformEventHandlerFactory(new BdvTransformEventHandler.BehaviourTransformEventHandler3DFactory( voxelSpacing )));



        final BdvStackSource bdvss1 = BdvFunctions.show(myImgRight, "right", BdvOptions.options().axisOrder(AxisOrder.XYCZT)
                .doubleBuffered(false)
                .addTo(bdvss0)
                .transformEventHandlerFactory(new BdvTransformEventHandler.BehaviourTransformEventHandler3DFactory( voxelSpacing )));



    }

}
