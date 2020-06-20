package de.embl.cba.bdp2.open.core;

public abstract class NamingScheme
{
	public static final String SINGLE_CHANNEL_TIMELAPSE = "Single Channel Movie"; // TODO: get rid of this and replace by regExp
	public static final String PATTERN_LUXENDO_LEFT_CAM = "Cam_Left_(\\d)+.h5$";
	public static final String PATTERN_LUXENDO_RIGHT_CAM = "Cam_Right_(\\d)+.h5$";
	public static final String PATTERN_LUXENDO_LONG_CAM = "Cam_long_(\\d)+.h5$";
	public static final String PATTERN_LUXENDO_SHORT_CAM = "Cam_short_(\\d)+.h5$";

	/**
	 * Use containing folder as the channel id.
	 * Users: Gustavo
	 */
//	public static final String SINGLE_CHANNEL_TIFF_VOLUMES = "(?<C>.*)/T(?<T>\\d+).tif";
	public static final String SINGLE_CHANNEL_VOLUMES = ".*T(?<T>\\d+)";
	public static final String MULTI_CHANNEL_VOLUMES_FROM_SUBFOLDERS = "(?<C>.*)/.*T(?<T>\\d+)";
	public static final String MULTI_CHANNEL_VOLUMES = ".*--C(?<C>.*)--T(?<T>\\d+)";

	public static final String LUXENDO_REGEXP = ".*stack_STACK_channel_(?<C1>\\d+)/Cam_(?<C2>.*)_(?<T>\\d+).h5";
	public static final String LUXENDO_STACKINDEX_REGEXP = ".*stack_(?<StackIndex>\\d+)_channel_.*";
	public static final String PATTERN_LUXENDO = "Cam_.*_(\\d)+.h5$";
	public static final String PATTERN_ALL= ".*";
	public static final String PATTERN_1= "<Z0000-0009>.tif"; // make pattern class
	public static final String PATTERN_2= ".*--C<c>--T<t>.tif";
	public static final String PATTERN_3= ".*--C<c>--T<t>.h5";
	public static final String PATTERN_4= ".*_C<c>_T<t>.tif";
	public static final String PATTERN_5= ".*--t<t>--Z<z>--C<c>.tif";
	public static final String PATTERN_6= "Cam_<c>_<t>.h5";
	@Deprecated
	public static final String LOAD_CHANNELS_FROM_FOLDERS = "Channels from Subfolders";
	public static final String TIFF_SLICES = "Tiff Slices";
	public static final String LEICA_LIGHT_SHEET_TIFF = "Leica Light Sheet Tiff";
	public static final String LUXENDO_REGEXP_ID = "+)/Cam_";

	// File extensions
	public static final String TIF = ".tif";
	public static final String OME_TIF = ".ome.tif";
	public static final String TIFF = ".tiff";
	public static final String H_5 = ".h5";
}
