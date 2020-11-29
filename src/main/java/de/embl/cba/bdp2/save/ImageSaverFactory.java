package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.save.imaris.ImarisImageSaver;
import de.embl.cba.bdp2.save.tiff.TiffPlanesSaver;
import de.embl.cba.bdp2.save.tiff.TiffFramesSaver;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.concurrent.ExecutorService;

public class ImageSaverFactory < R extends RealType< R > & NativeType< R > >
{
    public ImageSaver getSaver( Image< R > image, SavingSettings settings, ExecutorService es)
    {
        if ( settings.fileType.equals( SaveFileType.TiffPlanes ))
        {
            return new TiffPlanesSaver( image, settings, es);
        }
        else if ( settings.fileType.equals( SaveFileType.TiffVolumes ))
        {
            return new TiffFramesSaver( image, settings, es );
        }
//        else if (savingSettings.fileType.equals( SaveFileType.HDF5Volumes ))
//        {
//            return new HDF5StacksSaver(savingSettings, es);
//        }
        else if (settings.fileType.equals( SaveFileType.ImarisVolumes ))
        {
            return new ImarisImageSaver( image, settings, es);
        }
        else
        {
            return null;
        }
    }
}
