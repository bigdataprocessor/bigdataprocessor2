package de.embl.cba.bdp2.open;

public abstract class NamingSchemes
{
	public static final String T = "?<T>";
	public static final String Z = "?<Z>";
	public static final String C = "?<C>";

	public static final String SINGLE_CHANNEL_VOLUMES = ".*T(" + T + "\\d+)";
	public static final String SINGLE_CHANNEL_VOLUMES_2 = ".*--T(" + T + "\\d+)";
	public static final String MULTI_CHANNEL_VOLUMES_FROM_SUBFOLDERS = "(" + C + ".*)/.*T(" + T + "\\d+)";
	public static final String MULTI_CHANNEL_VOLUMES = ".*--C(" + C + ".*)--T(" + T + "\\d+)";
	public static final String LUXENDO_REGEXP = ".*stack_STACK_(?<C1>channel_.*)/(?<C2>Cam_.*)_(" + T + "\\d+).h5";
	public static final String LUXENDO_REGEXP_ID = "(?<C1>channel_.*)/(?<C2>Cam_.*)_(" + T + "\\d+).h5";
	public static final String CHANNEL_ID_DELIMITER = "_";
	public static final String LEICA_DSL_TIFF_PLANES_REG_EXP = ".*" +
			"--t(" + T + "\\d+)" +
			"--Z(" + Z + "\\d+)" +
			"--C(" + C + "\\d+).*";
	public static final String LUXENDO_STACKINDEX_REGEXP = ".*stack_(?<StackIndex>\\d+)_channel_.*";
	public static final String PATTERN_LUXENDO = "Cam_.*_(\\d)+.h5$";
	public static final String PATTERN_ALL= ".*";
	public static final String PATTERN_6= "Cam_<c>_<t>.h5";

	@Deprecated
	public static final String LOAD_CHANNELS_FROM_FOLDERS = "Channels from Subfolders";
	@Deprecated
	public static final String TIFF_SLICES = "Tiff Slices";
	@Deprecated
	public static final String SINGLE_CHANNEL_TIMELAPSE = "Single Channel Movie";

	// File extensions
	public static final String TIF = ".tif";
	public static final String OME_TIF = ".ome.tif";
	public static final String TIFF = ".tiff";
	public static final String H_5 = ".h5";

	public static boolean isLuxendoNamingScheme( String namingScheme )
	{
		return namingScheme.contains( LUXENDO_REGEXP_ID );
	}
}
