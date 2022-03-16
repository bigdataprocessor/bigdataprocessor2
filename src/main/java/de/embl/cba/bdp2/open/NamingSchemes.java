/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
	public static final String LUXENDO_STACK = "[sS]tack_";
	public static final String LUXENDO_CHANNEL = "[cC]hannel_";

	// I really don't remember why I need "\\/" and not just "/"...
	// https://stackoverflow.com/questions/9575116/forward-slash-in-java-regex
	public static final String LUXENDO = ".*\\/" + LUXENDO_STACK + P + "_(?<C1>" + LUXENDO_CHANNEL + ".*)\\/(?<C2>Cam_.*)_(" + T + "\\d+)(|.lux).h5";
	public static final String LUXENDO_ID = "(?<C2>Cam_.*)_(" + T + "\\d+)(|.lux).h5";
	public static final String LUXENDO_STACKINDEX = ".*" + LUXENDO_STACK + "("+ P + "\\d+)_" + LUXENDO_CHANNEL + ".*";
	public static final String CHANNEL_ID_DELIMITER = "_";
	public static final String LEICA_DSL_TIFF_PLANES = ".*" + "--t(" + T + "\\d+)" + "--Z(" + Z + "\\d+)" + "--C(" + C + "\\d+).*";
	public static final String VIVENTIS = ".*\\/t(?<T>\\d+)_(?<C>.+)" + TIF;

	public static final String NONRECURSIVE = "_NONRECURSIVE";

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

	public static boolean isTIFF( String path )
	{
		return path.contains( TIF );
	}

	public static boolean isHDF5( String path )
	{
		return path.endsWith( HDF5 );
	}
}
