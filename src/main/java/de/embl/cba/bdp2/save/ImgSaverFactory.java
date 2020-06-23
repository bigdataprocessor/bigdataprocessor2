package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.save.imaris.ImarisImageSaver;
import de.embl.cba.bdp2.save.tiff.TiffPlanesSaver;
import de.embl.cba.bdp2.save.tiff.TiffVolumesImageSaver;

import java.util.concurrent.ExecutorService;

public class ImgSaverFactory {

    public AbstractImageSaver getSaver( SavingSettings savingSettings, ExecutorService es)
    {
        if (savingSettings.saveFileType.equals( SavingSettings.SaveFileType.TIFF_PLANES))
        {
            return new TiffPlanesSaver(savingSettings, es);
        }
        else if (savingSettings.saveFileType.equals( SavingSettings.SaveFileType.TIFF_VOLUMES ))
        {
            return new TiffVolumesImageSaver(savingSettings, es);
        }
        else if (savingSettings.saveFileType.equals( SavingSettings.SaveFileType.HDF5_VOLUMES ))
        {
            return new HDF5StacksSaver(savingSettings, es);
        }
        else if (savingSettings.saveFileType.equals( SavingSettings.SaveFileType.IMARIS_VOLUMES ))
        {
            return new ImarisImageSaver(savingSettings, es);
        }
        else
            {
            return null;
        }
    }
}
