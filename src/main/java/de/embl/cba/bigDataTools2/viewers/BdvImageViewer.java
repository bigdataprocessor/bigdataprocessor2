package de.embl.cba.bigDataTools2.viewers;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.*;
import bdv.viewer.SourceAndConverter;
import de.embl.cba.bigDataTools2.boundingBox.ShowBoundingBoxDialog;
import de.embl.cba.bigDataTools2.dataStreamingGUI.BdvMenus;
import de.embl.cba.bigDataTools2.dataStreamingGUI.DisplaySettings;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import javax.swing.*;

public class BdvImageViewer<T extends RealType<T> & NativeType<T>> implements ImageViewer {

    private RandomAccessibleInterval<T> rai;
    private double[] voxelSize;
    private String imageName;

    private BdvStackSource< T > bdvSS;

    public BdvImageViewer()
    {

    }

    public BdvImageViewer( RandomAccessibleInterval<T> rai, String imageName, double[] voxelSize ) {
        this.imageName = imageName;
        this.rai = rai;
        this.voxelSize = voxelSize;
    }


    @Override
    public FinalInterval get5DIntervalFromUser() {
        ShowBoundingBoxDialog showBB = new ShowBoundingBoxDialog(this.bdvSS.getBdvHandle());
        showBB.show(rai, FileInfoConstants.BB_TRACK_BUTTON_LABEL,true);
        long[] minMax = {showBB.selectedMin[ShowBoundingBoxDialog.BB_X_POS], showBB.selectedMin[ShowBoundingBoxDialog.BB_Y_POS],
                rai.min(FileInfoConstants.C_AXIS_POSITION), showBB.selectedMin[ShowBoundingBoxDialog.BB_Z_POS], showBB.selectedMin[ShowBoundingBoxDialog.BB_T_POS],
                showBB.selectedMax[ShowBoundingBoxDialog.BB_X_POS], showBB.selectedMax[ShowBoundingBoxDialog.BB_Y_POS],
                rai.max(FileInfoConstants.C_AXIS_POSITION), showBB.selectedMax[ShowBoundingBoxDialog.BB_Z_POS],
                showBB.selectedMax[ShowBoundingBoxDialog.BB_T_POS]};
        return Intervals.createMinMax(minMax);
    }

    @Override
    public ImageViewer newImageViewer( ) {
        return new BdvImageViewer<T>( );
    }

    @Override
    public RandomAccessibleInterval<T> getRai() {
        return rai;
    }

    @Override
    public double[] getVoxelSize()
    {
        return new double[ 0 ];
    }

    @Override
    public String getImageName() {
        return imageName;
    }


    @Override
    public void repaint( AffineTransform3D viewerTransform )
    {
        this.bdvSS.getBdvHandle().getViewerPanel().setCurrentViewerTransform( viewerTransform );
    }

    @Override
    public void show( )
    {
        showImageInViewer( rai, voxelSize, imageName );
    }

    @Override
    public void show( RandomAccessibleInterval rai, double[] voxelSize, String imageName )
    {
        if ( this.bdvSS != null )
        {
            replaceImageInViewer( rai, voxelSize, imageName  );
        }
        else
        {
            showImageInViewer( rai, voxelSize ,imageName  );
        }
    }

    private void replaceImageInViewer( RandomAccessibleInterval rai, double[] voxelSize, String name )
    {
        showImageInViewer( rai, voxelSize ,name );
        SourceAndConverter scnv = this.bdvSS.getBdvHandle().getViewerPanel().getState().getSources().get(0);
        this.bdvSS.getBdvHandle().getViewerPanel().removeSource( scnv.getSpimSource() );
        int nChannels = (int) this.getRai().dimension( FileInfoConstants.C_AXIS_POSITION);
        for (int channel = 0; channel < nChannels; ++channel) {
			ConverterSetup converterSetup = this.getBdvSS().getBdvHandle().getSetupAssignments().getConverterSetups().get(channel);
			this.bdvSS.getBdvHandle().getSetupAssignments().removeSetup(converterSetup);
		}
    }

    public void addMenus(BdvMenus menus) {
        menus.setImageViewer(this);
        for (JMenu menu : menus.getMenus()) {
            ((BdvHandleFrame) this.bdvSS.getBdvHandle()).getBigDataViewer().getViewerFrame().getJMenuBar().add((menu));
        }
        ((BdvHandleFrame) this.bdvSS.getBdvHandle()).getBigDataViewer().getViewerFrame().getJMenuBar().updateUI();
    }

    @Override
    public void setDisplayRange(double min, double max, int channel) {
        final ConverterSetup converterSetup = this.bdvSS.getBdvHandle().getSetupAssignments().getConverterSetups().get(channel);
        this.bdvSS.getBdvHandle().getSetupAssignments().removeSetup(converterSetup);
        converterSetup.setDisplayRange(min, max);
        this.bdvSS.getBdvHandle().getSetupAssignments().addSetup(converterSetup);

    }

    @Override
    public DisplaySettings getDisplaySettings(int channel) {
        RandomAccessibleInterval raiStack = this.bdvSS.getBdvHandle().getViewerPanel().getState().getSources().get(channel).getSpimSource().getSource(0, 0);
        IntervalView<T> ts = Views.hyperSlice(raiStack, 2, (raiStack.max(2) - raiStack.min(2)) / 2 + raiStack.min(2)); //z is 2 for this rai.
        Cursor<T> cursor = Views.iterable(ts).cursor();
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double value;
        while (cursor.hasNext()) {
            value = cursor.next().getRealDouble();
            if (value < min) min = value;
            if (value > max) max = value;
        }
        return new DisplaySettings(min, max);
    }


    public void replicateViewerContrast(ImageViewer newImageView) {
        int nChannels = (int) this.getRai().dimension(FileInfoConstants.C_AXIS_POSITION);
        for (int channel = 0; channel < nChannels; ++channel) {
            ConverterSetup converterSetup = this.getBdvSS().getBdvHandle().getSetupAssignments().getConverterSetups().get(channel);
            newImageView.setDisplayRange(converterSetup.getDisplayRangeMin(), converterSetup.getDisplayRangeMax(), 0);
            //channel is always 0 (zero) because converterSetup object gets removed and added at the end of bdvSS in setDisplayRange method.
            //Hence current channel is always at position 0 of the bdvSS.
        }
    }

    public int getCurrentTimePoint() {
        System.out.println("Time is" + this.bdvSS.getBdvHandle().getViewerPanel().getState().getCurrentTimepoint());
        return this.bdvSS.getBdvHandle().getViewerPanel().getState().getCurrentTimepoint();
    }

    @Override
    public void shiftImageToCenter(double[] centerCoordinates) {
        AffineTransform3D sourceTransform = new AffineTransform3D();
        int width = this.bdvSS.getBdvHandle().getViewerPanel().getWidth();
        int height = this.bdvSS.getBdvHandle().getViewerPanel().getHeight();
        centerCoordinates[0] = (width / 2.0 + centerCoordinates[0]);
        centerCoordinates[1] = (height / 2.0 - centerCoordinates[1]);
        centerCoordinates[2] = -centerCoordinates[2];
        sourceTransform.translate(centerCoordinates);
        repaint(sourceTransform);
    }

    public BdvStackSource getBdvSS() {
        return bdvSS;
    }

    private void showImageInViewer( RandomAccessibleInterval rai, double[] voxelSize, String imageName )
    {
        final AffineTransform3D scaling = new AffineTransform3D();

        for ( int d = 0; d < 3; d++ )
        {
            scaling.set( voxelSize[ d ], d, d );
        }

        bdvSS = BdvFunctions.show(
                rai,
                imageName,
                BdvOptions.options().axisOrder( AxisOrder.XYCZT )
                        .addTo( bdvSS ).sourceTransform( scaling ) );

        this.imageName = imageName;
        this.rai = rai;
        this.voxelSize = voxelSize;
    }

}
