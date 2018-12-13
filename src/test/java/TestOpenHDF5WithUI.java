import bdv.BehaviourTransformEventHandler3D;
import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import de.embl.cba.bigDataTools2.CachedCellImageCreator;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoSource;
import net.imglib2.img.Img;

public class TestOpenHDF5WithUI {

    /**
     * IMPORTANT NOTE: Adjust Max value to 2550 in the Big Data Viewer. (Settings>Brightness and Color>Max)
     */
    public static void main(String[] args) {


        final String directory = "Y:\\ashis\\2018-07-19_17.34.07\\";
        //final String directory = "C:\\Users\\user\\Documents\\UNI_HEIDELBERG\\EMBL_internship\\";
        FileInfoSource fileInfoSource = new FileInfoSource(directory,
                FileInfoConstants.LOAD_CHANNELS_FROM_FOLDERS,
                ".*.h5",
                "Datawrong",
                true);
        //FileInfoSource fileInfoSource = new FileInfoSource(directory,"None",".*.h5","Datawrong",true,10);
        Img myImg = new CachedCellImageCreator().create(fileInfoSource,null);
        BdvSource bdvSource = BdvFunctions.show(myImg, "stream", BdvOptions.options().axisOrder(AxisOrder.XYCZT)
                .doubleBuffered(false)
                .transformEventHandlerFactory(new BehaviourTransformEventHandler3D.BehaviourTransformEventHandler3DFactory()));
        if(fileInfoSource.isAutoContrast()) {
            bdvSource.setDisplayRange(fileInfoSource.min_pixel_val, fileInfoSource.max_pixel_val);
        }

    }
}
