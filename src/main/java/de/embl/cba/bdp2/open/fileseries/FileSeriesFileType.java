package de.embl.cba.bdp2.open.fileseries;

public enum FileSeriesFileType
{
	LUXENDO("Luxendo HDF5"),
	IMARIS("Partitioned Imaris HDF5"),
	TIFF_STACKS("TIFF Stacks"),
	TIFF_PLANES("TIFF Planes"),
	HDF5_VOLUMES("HDF5 Volumes");

	private final String text;
	private FileSeriesFileType( String s)
	{
		text = s;
	}

	public static boolean is2D( FileSeriesFileType fileType )
	{
		return fileType.equals( TIFF_PLANES );
	}

	public static boolean is3D( FileSeriesFileType fileType )
	{
		return fileType.equals( TIFF_STACKS ) || fileType.equals( LUXENDO ) || fileType.equals( HDF5_VOLUMES );
	}

	@Override
	public String toString() {
return text;
}

}
