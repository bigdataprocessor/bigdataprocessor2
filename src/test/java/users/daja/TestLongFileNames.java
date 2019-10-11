package users.daja;

import ij.IJ;
import ij.ImagePlus;

public class TestLongFileNames
{
	public static void main( String[] args )
	{

		final ImagePlus imagePlus = IJ.openImage( " /Users/tischer/Desktop/blobs-11111111111111112222222222222222333333333333333344444444444444445555555555555.tif" );

		imagePlus.show();

	}
}
