package de.embl.cba.bdp2.crop;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.AbstractOpenCommand;
import de.embl.cba.bdp2.record.MacroRecorder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RealInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class CropDialog< R extends RealType< R > & NativeType< R > >
{
	public CropDialog( BdvImageViewer< R > viewer, boolean calibratedUnits )
	{
		final Image< R > inputImage = viewer.getImage();

		Interval voxelInterval = null;
		RealInterval realInterval = null;
		if ( calibratedUnits )
		{
			realInterval = viewer.getRealIntervalXYZCTViaDialog();
			if ( realInterval == null ) return;
			voxelInterval = toVoxelInterval( realInterval, inputImage.getVoxelSpacing() );
		}
		else
		{
			voxelInterval = viewer.getVoxelIntervalXYZCTViaDialog();
			if ( voxelInterval == null ) return;
		}

		Image< R > outputImage = BigDataProcessor2.crop( inputImage, voxelInterval );

		final BdvImageViewer< R > newViewer = new BdvImageViewer<>( outputImage );
		newViewer.setDisplaySettings( viewer.getDisplaySettings() );

		log( calibratedUnits, voxelInterval, realInterval, outputImage );

		recordMacro( inputImage, outputImage, voxelInterval );
	}

	public void log( boolean calibratedUnits, Interval voxelInterval, RealInterval realInterval, Image< R > outputImage )
	{
		Logger.info( "\n# Crop" );
		if ( calibratedUnits )
			Logger.info( "Crop interval [calibrated]: " + realInterval.toString() );
		Logger.info( "Crop interval [voxels]: " + voxelInterval.toString() );
		Logger.info( "Crop view size [GB]: " + Utils.getSizeGB( outputImage.getRai() ) );
	}

	public static Interval toVoxelInterval(
			RealInterval intervalXYZCT,
			double[] voxelSpacings )
	{
		final long[] min = new long[ intervalXYZCT.numDimensions() ];
		final long[] max = new long[ intervalXYZCT.numDimensions() ];

		// XYZ
		for ( int d = 0; d < 3; d++ )
		{
			min[ d ] = (long) ( intervalXYZCT.realMin( d ) / voxelSpacings[ d ] );
			max[ d ] = (long) ( intervalXYZCT.realMax( d ) / voxelSpacings[ d ] );
		}

		// CT
		for ( int d = 3; d < 5; d++ )
		{
			min[ d ] = (long) ( intervalXYZCT.realMin( d ) );
			max[ d ] = (long) ( intervalXYZCT.realMax( d ) );
		}

		return new FinalInterval( min, max );
	}


	private void recordMacro( Image< R > inputImage, Image< R > outputImage, Interval intervalXYZCT )
	{
		final MacroRecorder< R > recorder = new MacroRecorder<>( CropCommand.COMMAND_FULL_NAME, inputImage, outputImage, AbstractOpenCommand.SHOW_IN_NEW_VIEWER );
		
		recorder.addOption( "minX", intervalXYZCT.min(0 ) );
		recorder.addOption( "minY", intervalXYZCT.min(1 ) );
		recorder.addOption( "minZ", intervalXYZCT.min(2 ) );
		recorder.addOption( "minC", intervalXYZCT.min(3 ) );
		recorder.addOption( "minT", intervalXYZCT.min(4 ) );
		recorder.addOption( "maxX", intervalXYZCT.max(0 ) );
		recorder.addOption( "maxY", intervalXYZCT.max(1 ) );
		recorder.addOption( "maxZ", intervalXYZCT.max(2 ) );
		recorder.addOption( "maxC", intervalXYZCT.max(3 ) );
		recorder.addOption( "maxT", intervalXYZCT.max(4 ) );

		recorder.record();
	}
}
