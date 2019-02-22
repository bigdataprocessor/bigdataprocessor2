import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import de.embl.cba.bdp2.CachedCellImageCreator;
import de.embl.cba.bdp2.fileinfosource.FileInfoConstants;
import de.embl.cba.bdp2.fileinfosource.FileInfoSource;
import net.imglib2.img.Img;

public class TestTiffPlateLoader {


    public static void main(String[] args) {
        final String directory = "src\\test\\resources\\tiff-nc2-nt2\\";
        final FileInfoSource fileInfoSource = new FileInfoSource(directory, FileInfoConstants.LOAD_CHANNELS_FROM_FOLDERS,".*","");

        Img myImg = new CachedCellImageCreator().create(fileInfoSource,null);
        //ImgOpener imgOpener = new ImgOpener();
        //Img vsa = ( Img) imgOpener.openImgs( directory+fileName ).get( 0 );

        BdvFunctions.show(myImg,"stream", BdvOptions.options().axisOrder( AxisOrder.XYCZT));
    }
}
