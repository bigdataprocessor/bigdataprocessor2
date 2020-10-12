package de.embl.cba.bdp2.open.fileseries;

public enum FileSeriesFileType
{
	HDF5("Hdf5"),
	LUXENDO("Luxendo Hdf5"),
	IMARIS("Partitioned Imaris Hdf5"),
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
