package de.embl.cba.bdp2.save;

public enum SaveFileType
{
	TIFFPlanes,
	TIFFVolumes,
	ImarisVolumes,
	BigDataViewerXMLHDF5;

	public static boolean supportsMultiThreadedWriting( SaveFileType saveFileType )
	{
		switch ( saveFileType )
		{
			case TIFFVolumes:
				return true;
			case TIFFPlanes:
				return true;
			case ImarisVolumes:
				return false;
			case BigDataViewerXMLHDF5:
				return false;
			default:
				return false;
		}
	}
}
