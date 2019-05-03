package de.embl.cba.bdp2.saving;

import java.util.concurrent.ExecutorService;

public class ImgSaverFactory  {

    public AbstractImgSaver getSaver(SavingSettings savingSettings, ExecutorService es, Integer saveId){
        if (savingSettings.fileType.equals( SavingSettings.FileType.TIFF_PLANES )) {
           return new SaveTiffAsPlanes(savingSettings, es, saveId);
        } else if (savingSettings.fileType.equals( SavingSettings.FileType.TIFF_STACKS )) {
            return new SaveTiffAsStacks(savingSettings, es, saveId);
//        } else if (savingSettings.fileType.equals( SavingSettings.FileType.HDF5_STACKS )) {
//            saveHDFStacks(savingSettings, es, saveId);
//        } else if (savingSettings.fileType.equals( SavingSettings.FileType.IMARIS_STACKS )) {
//            saveIMARIStacks(savingSettings, es, saveId);
        }
        else{
            return null;
        }
    }
}
