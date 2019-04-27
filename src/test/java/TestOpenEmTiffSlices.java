import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import de.embl.cba.bdp2.loading.CachedCellImageCreator;
import de.embl.cba.bdp2.files.FileInfoConstants;
import de.embl.cba.bdp2.files.FileInfos;
import net.imglib2.img.Img;

public class TestOpenEmTiffSlices {
    public static void main(String[] args) {
        final String directory = "src\\test\\resources\\em-tiff-slices\\";
        FileInfos fileInfos = new FileInfos(directory, FileInfoConstants.EM_TIFF_SLICES, ".*.tif", "");
        System.out.println( fileInfos.nZ);
        System.out.println( fileInfos.nT);
        System.out.println( fileInfos.nC);
        System.out.println( fileInfos.nX);
        Img myImg = new CachedCellImageCreator().create( fileInfos );
        BdvFunctions.show(myImg,"stream", BdvOptions.options().axisOrder( AxisOrder.XYCZT));

    }
}
