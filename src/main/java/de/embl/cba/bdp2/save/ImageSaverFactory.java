package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.save.bdvhdf5.BigDataViewerXMLHDF5Saver;
import de.embl.cba.bdp2.save.imaris.ImarisImageSaver;
import de.embl.cba.bdp2.save.tiff.TIFFPlanesSaver;
import de.embl.cba.bdp2.save.tiff.TIFFFramesSaver;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.concurrent.ExecutorService;

public class ImageSaverFactory < R extends RealType< R > & NativeType< R > >
{
    public ImageSaver getSaver( Image< R > image, SavingSettings settings, ExecutorService es)
    {
        switch ( settings.fileType )
        {
            case TIFFPlanes:
                return new TIFFPlanesSaver( image, settings, es);
            case TIFFVolumes:
                return new TIFFFramesSaver( image, settings, es );
            case ImarisVolumes:
                return new ImarisImageSaver( image, settings, es);
            case BigDataViewerXMLHDF5:
                return new BigDataViewerXMLHDF5Saver<>( image, settings );
            default:
                throw new UnsupportedOperationException( settings.fileType.toString() );
        }
    }
}
