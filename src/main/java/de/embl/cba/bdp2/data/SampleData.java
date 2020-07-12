package de.embl.cba.bdp2.data;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import de.embl.cba.bdp2.viewers.BdvImageViewer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static de.embl.cba.bdp2.open.core.NamingSchemes.TIF;

public class SampleData
{
	public static final String MINIMAL_SYNTHETIC = "Minimal synthetic dual color Tiff volumes (1.6 MB)";
	public static final String DUAL_COLOR_MOUSE = "Dual color light-sheet mouse Tiff volume (64.8 MB)";

	private Map< String, String > datasetNameToURL =  new HashMap<>();
	private Map< String, String > datasetNameToRegExp =  new HashMap<>();


	public SampleData()
	{
		datasetNameToURL.put( MINIMAL_SYNTHETIC, "https://www.ebi.ac.uk/biostudies/files/S-BSST417/tiff-volumes-x50y50z50c2t6.zip" );
		datasetNameToURL.put( DUAL_COLOR_MOUSE, "https://www.ebi.ac.uk/biostudies/files/S-BSST417/mouse-volumes.zip" );

		datasetNameToRegExp.put( MINIMAL_SYNTHETIC, NamingSchemes.MULTI_CHANNEL_VOLUMES + TIF);
		datasetNameToRegExp.put( DUAL_COLOR_MOUSE, NamingSchemes.MULTI_CHANNEL_VOLUMES + TIF);

	}

	public File download( String datasetName, File outputDirectory )
	{
		final String url = datasetNameToURL.get( datasetName );

		try
		{
			BufferedInputStream inputStream = new BufferedInputStream( new URL( url ).openStream());
			final String fileName = new File( url ).getName();
			final File outputFile = new File( outputDirectory, fileName );
			FileOutputStream fileOS = new FileOutputStream( outputFile );
			byte data[] = new byte[1024];
			int byteContent;
			while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
				fileOS.write(data, 0, byteContent);
			}
			return outputFile;
		}
		catch ( IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException( e );
		}
	}

	public void downloadAndOpen( String datasetName, File outputDirectory, BdvImageViewer viewer )
	{
		final File download = download( datasetName, outputDirectory );

		if ( download.getName().contains( ".zip" ) )
		{
			outputDirectory = UnZipper.unzip( download );
		}

		final Image image = BigDataProcessor2.openImage(
				outputDirectory,
				datasetNameToRegExp.get( datasetName ),
				".*" );

		if ( viewer != null )
		{
			viewer.replaceImage( image, true, false );
		}
		else
		{
			BigDataProcessor2.showImage( image, true, false );
		}
	}


	public String getURL( String datasetName )
	{
		return datasetNameToURL.get( datasetName );
	}
}
