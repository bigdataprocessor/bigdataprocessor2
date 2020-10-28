package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.save.imaris.ImarisImageSaver;
import de.embl.cba.bdp2.save.tiff.TiffPlanesSaver;
import de.embl.cba.bdp2.save.tiff.TiffFramesSaver;

import java.util.concurrent.ExecutorService;

public class ImgSaverFactory {

    public AbstractImageSaver getSaver( SavingSettings savingSettings, ExecutorService es)
    {
        if (savingSettings.fileType.equals( SaveFileType.TiffPlanes ))
        {
            return new TiffPlanesSaver(savingSettings, es);
        }
        else if (savingSettings.fileType.equals( SaveFileType.TiffVolumes ))
        {
            return new TiffFramesSaver( savingSettings, es );
        }
//        else if (savingSettings.fileType.equals( SaveFileType.Hdf5Volumes ))
//        {
//            return new HDF5StacksSaver(savingSettings, es);
//        }
        else if (savingSettings.fileType.equals( SaveFileType.ImarisVolumes ))
        {
            return new ImarisImageSaver(savingSettings, es);
        }
        else
            {
            return null;
        }
    }
}
