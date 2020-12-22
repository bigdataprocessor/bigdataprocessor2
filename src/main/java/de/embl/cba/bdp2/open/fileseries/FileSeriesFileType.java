package de.embl.cba.bdp2.open.fileseries;

public enum FileSeriesFileType
{
	LUXENDO("Luxendo HDF5"),
	IMARIS("Partitioned Imaris HDF5"),
	TIFF_STACKS("Tiff Stacks"),
	TIFF_PLANES("Tiff Planes");

	private final String text;
	private FileSeriesFileType( String s)
	{
		text = s;
	}

	@Override
	public String toString() {
return text;
}
}
