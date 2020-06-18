package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.save.tiff.TiffPlanesSaver;
import de.embl.cba.bdp2.save.tiff.TiffStacksImageSaver;

import java.util.concurrent.ExecutorService;

public class ImgSaverFactory {

    public AbstractImageSaver getSaver( SavingSettings savingSettings, ExecutorService es)
    {
        if (savingSettings.fileType.equals(SavingSettings.FileType.TIFF_PLANES))
        {
            return new TiffPlanesSaver(savingSettings, es);
        }
        else if (savingSettings.fileType.equals(SavingSettings.FileType.TIFF_VOLUMES ))
        {
            return new TiffStacksImageSaver(savingSettings, es);
        }
        else if (savingSettings.fileType.equals(SavingSettings.FileType.HDF5_VOLUMES ))
        {
            return new HDF5StacksSaver(savingSettings, es);
        }
        else if (savingSettings.fileType.equals(SavingSettings.FileType.IMARIS_VOLUMES ))
        {
            return new ImarisStacksSaver(savingSettings, es);
        }
        else
            {
            return null;
        }
    }
}
