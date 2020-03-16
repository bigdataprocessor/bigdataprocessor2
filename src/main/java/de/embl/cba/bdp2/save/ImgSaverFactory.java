package de.embl.cba.bdp2.save;

import java.util.concurrent.ExecutorService;

public class ImgSaverFactory {

    public AbstractImgSaver getSaver(SavingSettings savingSettings, ExecutorService es) {
        if (savingSettings.fileType.equals(SavingSettings.FileType.TIFF_PLANES))
        {
            return new SaveTiffAsPlanes(savingSettings, es);
        }
        else if (savingSettings.fileType.equals(SavingSettings.FileType.TIFF_STACKS))
        {
            return new SaveTiffAsStacks(savingSettings, es);
        }
        else if (savingSettings.fileType.equals(SavingSettings.FileType.HDF5_STACKS))
        {
            return new SaveHDF5AsStacks(savingSettings, es);
        }
        else if (savingSettings.fileType.equals(SavingSettings.FileType.IMARIS_STACKS))
        {
            return new SaveImarisAsStacks(savingSettings, es);
        }
        else
            {
            return null;
        }
    }
}
