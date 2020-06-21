package develop;

import bdv.tools.boundingbox.BoxSelectionOptions;
import bdv.tools.boundingbox.TransformedBox;
import bdv.tools.boundingbox.TransformedBoxOverlay;
import bdv.tools.boundingbox.TransformedRealBoxSelectionDialog;
import bdv.util.BdvFunctions;
import bdv.util.ModifiableRealInterval;
import bdv.viewer.ViewerPanel;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.CachedCellImgReader;
import de.embl.cba.bdp2.open.core.FileInfos;
import de.embl.cba.bdp2.open.core.NamingScheme;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.FinalRealInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

public class BoundingBoxSelection
{
	public static < R extends RealType< R > & NativeType< R > >
	void main( String[] args )
	{

		final Image< R > image = openImage();
		final BdvImageViewer< R > viewer = new BdvImageViewer<>( image );

		final RandomAccessibleInterval rai = viewer.getImage().getRai();
		final double[] min = new double[ 3 ];
		final double[] max = new double[ 3 ];

		final double[] voxelSpacing = viewer.getImage().getVoxelSpacing();

		for (int d = 0; d < 3; d++) {
			min[d] = (int) (rai.min(d) * voxelSpacing[d]);
			max[d] = (int) (0.7 * rai.max(d) * voxelSpacing[d]);
		}

		final FinalRealInterval interval = new FinalRealInterval( min, max );

		ModifiableRealInterval modifiableRealInterval = new ModifiableRealInterval( interval );

		final TransformedBoxOverlay transformedBoxOverlay =
				new TransformedBoxOverlay( new TransformedBox()
		{
			@Override
			public void getTransform( final AffineTransform3D transform )
			{
				viewer.getSourceTransform( transform );

			}

			@Override
			public Interval getInterval()
			{
				return Intervals.largestContainedInterval( modifiableRealInterval );
			}
		} );

		transformedBoxOverlay.boxDisplayMode().set( TransformedBoxOverlay.BoxDisplayMode.SECTION );

		final ViewerPanel viewerPanel = viewer.getBdvHandle().getViewerPanel();
		viewerPanel.getDisplay().addOverlayRenderer(transformedBoxOverlay);
		viewerPanel.addRenderTransformListener(transformedBoxOverlay);

		// replaceImage two independent boxes which are fully adjustable.
//		new Thread( () -> showRealBox( viewer, interval, "Channel 0" ) ).start();
//		new Thread( () -> showRealBox( viewer, interval, "Channel 1" ) ).start();

	}

	public static void showBox( BdvImageViewer viewer, FinalRealInterval finalInterval, String title )
	{
		final TransformedRealBoxSelectionDialog.Result result = BdvFunctions.selectRealBox(
				viewer.getBdvHandle(),
				new AffineTransform3D(),
				finalInterval,
				finalInterval,
				BoxSelectionOptions.options().title( title )
		);


		if (result.isValid())
		{
			FinalRealInterval finalRealInterval = (FinalRealInterval) result.getInterval();
			final Interval interval = Intervals.largestContainedInterval( finalRealInterval );
		}
	}

	public static < R extends RealType< R > & NativeType< R > > Image< R > openImage()
	{
		String imageDirectory = "/Users/tischer/Desktop/stack_0_channel_0";

		final FileInfos fileInfos = new FileInfos( imageDirectory,
				NamingScheme.SINGLE_CHANNEL_TIMELAPSE,
				".*.h5", "Data" );

		fileInfos.voxelSpacing = new double[]{ 0.5, 0.5, 5.0};

		return CachedCellImgReader.loadImage( fileInfos );
	}
}
