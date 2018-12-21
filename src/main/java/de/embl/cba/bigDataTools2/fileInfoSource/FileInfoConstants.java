package de.embl.cba.bigDataTools2.fileInfoSource;

import de.embl.cba.bigDataTools2.imaris.ImarisUtils;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;

public class FileInfoConstants {

    public static final String LOAD_CHANNELS_FROM_FOLDERS = "Channels from Sub-folders";
    public static final String EM_TIFF_SLICES = "EM Tiff Slices";
    public static final String LEICA_SINGLE_TIFF = "Leica Single Tiff";
    public static final String SINGLE_CHANNEL_TIMELAPSE = "Single Channel Time-lapse";

    public static final String PATTERN_1= "<Z0000-0009>.tif"; // make pattern class
    public static final String PATTERN_2= ".*--C<c>--T<t>.tif";
    public static final String PATTERN_3= ".*--C<c>--T<t>.h5";
    public static final String PATTERN_4= ".*_C<c>_T<t>.tif";
    public static final String PATTERN_5= ".*--t<t>--Z<z>--C<c>.tif";
    public static final String PATTERN_6= "Cam_<c>_<t>.h5";
    //public static final String PATTERN_7= "classified--C<C01-01>--T<T00001-00001>--Z<Z00001-01162>.tif";
    //public static final String PATTERN_8= "classified--C<C00-00>--T<T00000-00000>--Z<Z00001-01162>.tif";
//    public static final int UNSIGNED_BYTE_MAX_VAL = Byte.MAX_VALUE*2+1;
//    public static final int UNSIGNED_SHORT_MAX_VAL = Short.MAX_VALUE*2+1;
//    public static final long UNSIGNED_INT_MAX_VAL = 4294967295L;
    public static final int X_AXIS_POSITION=0;
    public static final int Y_AXIS_POSITION=1;
    public static final int C_AXIS_POSITION=2;
    public static final int Z_AXIS_POSITION=3;
    public static final int T_AXIS_POSITION=4;
    public static final int TOTAL_AXES = 5;
    public static final AxisType[] AXES_ORDER = {Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME};
    public static final String CROPPED_STREAM_NAME = "cropped";
    public static final String TRACKED_STREAM_NAME = "tracked";
    public static final String IMAGE_NAME = "stream";
    public static final String BB_CROP_BUTTON_LABEL= "Crop";
    public static final String BB_TRACK_BUTTON_LABEL= "Select";
    public static final int CELL_DIM_Y = 15;
    public static final String CENTER_OF_MASS = "Center of Mass";
    public static final String CROSS_CORRELATION = "Cross Correlation";



    public static final String[] POSSIBLE_HDF5_DATASETNAMES = new String[] {"None",
            "Data","Data111",
            ImarisUtils.RESOLUTION_LEVEL +"0/Data", //TODO . include IMARIS --ashis
            ImarisUtils.RESOLUTION_LEVEL +"1/Data",
            ImarisUtils.RESOLUTION_LEVEL +"2/Data",
            ImarisUtils.RESOLUTION_LEVEL +"3/Data",
            "ITKImage/0/VoxelData", "Data222", "Data444"};

}
