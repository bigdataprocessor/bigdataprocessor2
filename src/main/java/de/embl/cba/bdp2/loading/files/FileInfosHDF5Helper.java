package de.embl.cba.bdp2.loading.files;

import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5DataTypeInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdp2.logging.Logger;
import ij.gui.GenericDialog;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileInfosHDF5Helper
{

    public static boolean setImageDataInfoFromH5(
            FileInfos imageDataInfo,
            String directory,
            String fileName)
    {
        IHDF5Reader reader = HDF5Factory.openForReading(directory + "/" + fileName);

        StringBuilder hdf5DataSetSB = new StringBuilder();
        if (imageDataInfo.h5DataSetName != null && !imageDataInfo.h5DataSetName.isEmpty()
                && !imageDataInfo.h5DataSetName.trim().isEmpty()) {
            hdf5DataSetSB = new StringBuilder( imageDataInfo.h5DataSetName );
            if ( ! hdf5DataSetExists(reader, hdf5DataSetSB ) ) return false;
        } else {
            if( ! setHDF5DatasetViaUI(reader,hdf5DataSetSB) ) return false;
        }
        imageDataInfo.h5DataSetName = hdf5DataSetSB.toString();
        HDF5DataSetInformation dsInfo = reader.object().getDataSetInformation("/" + imageDataInfo.h5DataSetName);

        if (dsInfo.getDimensions().length == 3) {
            imageDataInfo.nZ = (int) dsInfo.getDimensions()[0];
            imageDataInfo.nY = (int) dsInfo.getDimensions()[1];
            imageDataInfo.nX = (int) dsInfo.getDimensions()[2];
        } else if (dsInfo.getDimensions().length == 2) {
            imageDataInfo.nZ = 1;
            imageDataInfo.nY = (int) dsInfo.getDimensions()[0];
            imageDataInfo.nX = (int) dsInfo.getDimensions()[1];
        }
        imageDataInfo.bitDepth = assignHDF5TypeToImagePlusBitdepth(dsInfo);

        // There is no standard way of retrieving voxelSpacings from h5 data....
        imageDataInfo.voxelSpacing = new double[]{1,1,1};
        imageDataInfo.voxelUnit = "micrometer";

        return true;

    }

    private static int assignHDF5TypeToImagePlusBitdepth(HDF5DataSetInformation dsInfo) {
        String type = dsInfoToTypeString(dsInfo);
        int nBits = 0;
        if (type.equals("uint8")) {
            nBits = Byte.SIZE;
        } else if (type.equals("uint16") || type.equals("int16")) {
            nBits = Short.SIZE;
        } else if (type.equals("float32") || type.equals("float64")) {
            nBits = Float.SIZE;
        } else {
            Logger.error("Type '" + type + "' Not handled yet!");
        }
        return nBits;
    }

    private static boolean hdf5DataSetExists(
            IHDF5Reader reader,
            StringBuilder hdf5DataSet) {
        String dataSets = "";
        boolean dataSetExists;
        if (reader.object().isDataSet(hdf5DataSet.toString())) {
            return true;
        } else {
            List<String> hdf5Header = reader.getGroupMembers("/");
            hdf5Header.replaceAll(String::toUpperCase);
            dataSetExists = Arrays.stream( FileInfos.POSSIBLE_HDF5_DATASETNAMES).parallel().anyMatch( x -> hdf5Header.contains(x.toUpperCase()));
            List<String> head = Arrays.stream( FileInfos.POSSIBLE_HDF5_DATASETNAMES).parallel().filter( x -> hdf5Header.contains(x.toUpperCase())).collect(Collectors.toList());
            hdf5DataSet.delete(0, hdf5DataSet.length());
            hdf5DataSet.append(head.get(0));
        }
        if (!dataSetExists) {
            Logger.error("The selected Hdf5 data set does not exist; " +
                    "please change to one of the following:\n\n" +
                    dataSets);
        }

        return dataSetExists;
    }

    private static boolean setHDF5DatasetViaUI( IHDF5Reader reader,
                                                StringBuilder hdf5DataSet) {

        List<String> hdf5Header = reader.getGroupMembers("/");

        final GenericDialog gd = new GenericDialog( "Choose Hdf5 Dataset" );

        gd.addChoice( "Hdf5 Dataset",
                hdf5Header.toArray(new String[0]),
                hdf5Header.get( 0 )  );

        gd.showDialog();
        if( gd.wasCanceled() ) return false;

        hdf5DataSet.delete(0, hdf5DataSet.length());
        hdf5DataSet.append( gd.getNextChoice() );
        return true;
    }

    public static String dsInfoToTypeString (HDF5DataSetInformation dsInfo ) {  //TODO : DUPLICATE CODE! Fix it! --ashis
        HDF5DataTypeInformation dsType = dsInfo.getTypeInformation();
        String typeText = "";

        if (dsType.isSigned() == false) {
            typeText += "u";
        }

        switch (dsType.getDataClass()) {
            case INTEGER:
                typeText += "int" + 8 * dsType.getElementSize();
                break;
            case FLOAT:
                typeText += "float" + 8 * dsType.getElementSize();
                break;
            default:
                typeText += dsInfo.toString();
        }
        return typeText;
    }
}
