package develop;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;

import java.nio.file.Paths;

public class DevelopJhdfReading
{
    public static void main( String[] args )
    {
        try ( HdfFile hdfFile = new HdfFile( Paths.get("/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/luxendo-different-stack-size/Stack_0_Channel_1/Cam_Fused_00001.h5"))) {
            Dataset dataset = hdfFile.getDatasetByPath("Data");
            // data will be a Java array with the dimensions of the HDF5 dataset
            Object data = dataset.getData();
            int a = 1;
        }
    }
}
