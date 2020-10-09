package develop;

import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import de.embl.cba.bdp2.open.fileseries.FileSeriesCachedCellImgCreator;
import de.embl.cba.bdp2.open.fileseries.FileInfos;
import de.embl.cba.bdp2.open.NamingSchemes;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;

public class TestTiffPlateLoader {


    public static void main(String[] args) {
        final String directory = "src\\test\\resources\\tiff-nc2-nt2\\";
        final FileInfos fileInfos = new FileInfos(directory, NamingSchemes.LOAD_CHANNELS_FROM_FOLDERS,".*","");

        Img myImg = new FileSeriesCachedCellImgCreator().createCachedCellImg( fileInfos, DiskCachedCellImgOptions.CacheType.BOUNDED, cacheSize );
        //ImgOpener imgOpener = new ImgOpener();
        //Img vsa = ( Img) imgOpener.openImgs( directory+fileName ).get( 0 );

        BdvFunctions.show(myImg,"stream", BdvOptions.options().axisOrder( AxisOrder.XYCZT));
    }
}
