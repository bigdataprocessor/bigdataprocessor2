package de.embl.cba.bdp2.read;

public abstract class NamingScheme
{
	public static final String SINGLE_CHANNEL_TIMELAPSE = "Single Channel Movie"; // TODO: get rid of this and replace by regExp
	public static final String PATTERN_LUXENDO_LEFT_CAM = "Cam_Left_(\\d)+.h5$";
	public static final String PATTERN_LUXENDO_RIGHT_CAM = "Cam_Right_(\\d)+.h5$";
	public static final String PATTERN_LUXENDO_LONG_CAM = "Cam_long_(\\d)+.h5$";
	public static final String PATTERN_LUXENDO_SHORT_CAM = "Cam_short_(\\d)+.h5$";
	public static final String LUXENDO_REGEXP = ".*stack_STACK_channel_(?<C1>\\d+)/Cam_(?<C2>.*)_(?<T>\\d+).h5";
	public static final String PATTERN_LUXENDO = "Cam_.*_(\\d)+.h5$";
	public static final String PATTERN_ALL= ".*";
	public static final String PATTERN_1= "<Z0000-0009>.tif"; // make pattern class
	public static final String PATTERN_2= ".*--C<c>--T<t>.tif";
	public static final String PATTERN_3= ".*--C<c>--T<t>.h5";
	public static final String PATTERN_4= ".*_C<c>_T<t>.tif";
	public static final String PATTERN_5= ".*--t<t>--Z<z>--C<c>.tif";
	public static final String PATTERN_6= "Cam_<c>_<t>.h5";
	public static final String LOAD_CHANNELS_FROM_FOLDERS = "Channels from Subfolders";
	public static final String TIFF_SLICES = "Tiff Slices";
	public static final String LEICA_LIGHT_SHEET_TIFF = "Leica Light Sheet Tiff";
	public static final String LUXENDO_REGEXP_ID = "+)/Cam_";
}
