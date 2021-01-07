package de.embl.cba.bdp2.open;

public abstract class NamingSchemes
{
	// Groups
	public static final String P = "?<P>";
	public static final String T = "?<T>";
	public static final String Z = "?<Z>";
	public static final String C = "?<C>";

	// File extensions
	public static final String TIF = ".tif";
	public static final String OME_TIF = ".ome.tif";
	public static final String TIFF = ".tiff";
	public static final String HDF5 = ".h5";

	// File series regular expressions
	public static final String SINGLE_CHANNEL_VOLUMES = "(" + T + ".*)";
	public static final String SINGLE_CHANNEL_VOLUMES_WITH_TIME_INDEX = ".*T(" + T + "\\d+)";
	public static final String SINGLE_CHANNEL_VOLUMES_2 = ".*--T(" + T + "\\d+)";
	public static final String MULTI_CHANNEL_VOLUMES_FROM_SUBFOLDERS = "(" + C + ".*)/.*T(" + T + "\\d+)";
	public static final String MULTI_CHANNEL_VOLUMES = ".*--C(" + C + ".*)--T(" + T + "\\d+)";
	public static final String LUXENDO = ".*stack_"+ P +"_(?<C1>channel_.*)/(?<C2>Cam_.*)_(" + T + "\\d+)" + HDF5;
	public static final String LUXENDO_ID = "(?<C2>Cam_.*)_(" + T + "\\d+)" + HDF5;
	public static final String LUXENDO_STACKINDEX = ".*stack_("+ P +"\\d+)_channel_.*";
	public static final String CHANNEL_ID_DELIMITER = "_";
	public static final String LEICA_DSL_TIFF_PLANES = ".*" + "--t(" + T + "\\d+)" + "--Z(" + Z + "\\d+)" + "--C(" + C + "\\d+).*";

	@Deprecated
	public static final String PATTERN_6= "Cam_<c>_<t>" + HDF5;
	@Deprecated
	public static final String PATTERN_LUXENDO = "Cam_.*_(\\d)+" + HDF5 + "$";
	@Deprecated
	public static final String LOAD_CHANNELS_FROM_FOLDERS = "Channels from Subfolders";
	@Deprecated
	public static final String TIFF_SLICES = "TIFF Slices";
	@Deprecated
	public static final String SINGLE_CHANNEL_TIMELAPSE = "Single Channel Movie";


	public static boolean isLuxendoNamingScheme( String namingScheme )
	{
		return namingScheme.contains( LUXENDO_ID );
	}
}
