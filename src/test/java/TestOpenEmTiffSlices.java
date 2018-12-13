import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import de.embl.cba.bigDataTools2.CachedCellImageCreator;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoSource;
import net.imglib2.img.Img;

public class TestOpenEmTiffSlices {
    public static void main(String[] args) {
        final String directory = "src\\test\\resources\\em-tiff-slices\\";
        FileInfoSource fileInfoSource = new FileInfoSource(directory, FileInfoConstants.EM_TIFF_SLICES, ".*.tif", "");
        System.out.println(fileInfoSource.nZ);
        System.out.println(fileInfoSource.nT);
        System.out.println(fileInfoSource.nC);
        System.out.println(fileInfoSource.nX);
        Img myImg = new CachedCellImageCreator().create(fileInfoSource,null);
        BdvFunctions.show(myImg,"stream", BdvOptions.options().axisOrder( AxisOrder.XYCZT));

    }
}
