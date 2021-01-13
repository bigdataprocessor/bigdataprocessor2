package de.embl.cba.bdp2.open.samples;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.ProgressListener;
import de.embl.cba.bdp2.open.NamingSchemes;
import de.embl.cba.bdp2.process.calibrate.CalibrationChecker;
import de.embl.cba.bdp2.viewer.ImageViewer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static de.embl.cba.bdp2.open.NamingSchemes.TIF;

public class SampleDataDownloader
{
	public static final String MINIMAL_SYNTHETIC = "Minimal synthetic dual color TIFF volumes (1.6 MB)";
	public static final String DUAL_COLOR_MOUSE = "Dual color light-sheet mouse TIFF volume (64.8 MB)";
	public static final String NON_ORTHO = "Non-orthogonal acquisition TIFF volume (2.6 MB)";

	private Map< String, String > datasetNameToURL =  new HashMap<>();
	private Map< String, String > datasetNameToRegExp =  new HashMap<>();
	private ProgressListener progressListener;

	public SampleDataDownloader()
	{
		datasetNameToURL.put( MINIMAL_SYNTHETIC, "https://www.ebi.ac.uk/biostudies/files/S-BSST417/tiff-volumes-x50y50z50c2t6.zip" );
		datasetNameToURL.put( DUAL_COLOR_MOUSE, "https://www.ebi.ac.uk/biostudies/files/S-BSST417/mouse-volumes.zip" );
		datasetNameToURL.put( NON_ORTHO, "https://www.ebi.ac.uk/biostudies/files/S-BSST417/non-ortho.zip" );

		datasetNameToRegExp.put( MINIMAL_SYNTHETIC, NamingSchemes.MULTI_CHANNEL_VOLUMES + TIF);
		datasetNameToRegExp.put( DUAL_COLOR_MOUSE, NamingSchemes.MULTI_CHANNEL_VOLUMES + TIF);
		datasetNameToRegExp.put( NON_ORTHO, NamingSchemes.SINGLE_CHANNEL_VOLUMES_2 + TIF);
	}

	public File download( String datasetName, File outputDirectory )
	{
		try
		{
			final URL url = new URL( datasetNameToURL.get( datasetName ) );
			HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
			long completeFileSize = httpConnection.getContentLength();
			BufferedInputStream inputStream = new BufferedInputStream( url.openStream());
			final String fileName = new File( datasetNameToURL.get( datasetName ) ).getName();
			final File outputFile = new File( outputDirectory, fileName );
			FileOutputStream fileOS = new FileOutputStream( outputFile );
			byte data[] = new byte[1024];
			int byteContent;
			long downloadedFileSize = 0;
			while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
				downloadedFileSize += byteContent;
				fileOS.write(data, 0, byteContent);
				progressListener.progress( downloadedFileSize, completeFileSize );
			}
			return outputFile;
		}
		catch ( IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException( e );
		}
	}

	public void setProgressListener( ProgressListener progressListener )
	{
		this.progressListener = progressListener;
	}

	public void downloadAndOpen( String datasetName, File outputDirectory, ImageViewer viewer )
	{
		final File download = download( datasetName, outputDirectory );

		if ( download.getName().contains( ".zip" ) )
		{
			outputDirectory = UnZipper.unzip( download );
		}

		final Image image = BigDataProcessor2.openTIFFSeries( outputDirectory, datasetNameToRegExp.get( datasetName ) );

		if ( ! CalibrationChecker.checkImage( image ) )
		{
			image.setVoxelDimensions( new double[]{1,1,1} );
			image.setVoxelUnit( "pixel" );
		}

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
