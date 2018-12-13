package de.embl.cba.bigDataTools2.viewers;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.*;
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

public class BdvImageViewer < T extends RealType< T > & NativeType< T > > implements ImageViewer {

    private RandomAccessibleInterval< T > rai;
    private double[] voxelSize;
    private String streamName;

    private BdvStackSource bdvSS;
    
    public BdvImageViewer( RandomAccessibleInterval< T > rai, String streamName, double[] voxelSize ) { // TODO: may be not needed. Check --ashis
        this.streamName = streamName;
        this.rai = rai;
        this.voxelSize = voxelSize;
    }

    @Override
    public void show() {
        this.bdvSS = BdvFunctions.show( rai, this.streamName, BdvOptions.options().axisOrder(AxisOrder.XYCZT)
                                        .transformEventHandlerFactory(new BdvTransformEventHandler.BehaviourTransformEventHandler3DFactory( voxelSize )));
    }
    @Override
    public void repaint(AffineTransform3D viewerTransform) {
       // RandomAccessibleIntervalSource4D raiSource = new RandomAccessibleIntervalSource4D(rai, Util.getTypeFromInterval(rai),viewerTransform,"sheared");
        ///this.bdvSS.getBdvHandle().getViewerPanel().getState().setCurrentSource(raiSource);
        //SourceAndConverter scnv = (SourceAndConverter) bdvSS.getSources().get(0);
       // this.bdvSS.getBdvHandle().getViewerPanel().removeSource(scnv.getSpimSource());
       // this.bdvSS.getBdvHandle().getViewerPanel().addSource(new SourceAndConverter<>(raiSource,scnv.getConverter()));
//        this.bdvSS.getBdvHandle().getViewerPanel().getState().setCurrentSource(raiSource);
//        this.bdvSS.getBdvHandle().getViewerPanel().getState().removeSource(scnv.getSpimSource());

        //this.bdvSS.getBdvHandle().getViewerPanel().getState().setViewerTransform(viewerTransform);
        //this.bdvSS.getBdvHandle().getViewerPanel().setInterpolation(Interpolation.NEARESTNEIGHBOR);
        //BdvHandleFrame bdvFrame=(BdvHandleFrame)(this.bdvSS.getBdvHandle());
        //Or this:
        //bdv.getBdvHandle().getViewerPanel().
        //viewerTransform.translate( new double[]{,,0});
        this.bdvSS.getBdvHandle().getViewerPanel().setCurrentViewerTransform(viewerTransform);
        //bdv.getViewerPanel().requestRepaint();
    }

    @Override
    public FinalInterval get5DIntervalFromUser() {
        ShowBoundingBoxDialog showBB = new ShowBoundingBoxDialog(this.bdvSS.getBdvHandle());
        showBB.show(rai, FileInfoConstants.BB_TRACK_BUTTON_LABEL);
        long[] minMax = {showBB.selectedMin[0], showBB.selectedMin[1], 0, showBB.selectedMin[2], 0,
                showBB.selectedMax[0], showBB.selectedMax[1], rai.dimension(FileInfoConstants.C_AXIS_POSITION) - 1, showBB.selectedMax[2], rai.dimension(FileInfoConstants.T_AXIS_POSITION) - 1};
        return Intervals.createMinMax(minMax);
    }

    @Override
    public ImageViewer newImageViewer(RandomAccessibleInterval rai,String streamName) {
        if(null == streamName|| streamName.isEmpty()){
            streamName = this.streamName;
        }
        return new BdvImageViewer<T>(rai,streamName, voxelSize );
    }
    @Override
    public RandomAccessibleInterval<T> getRai() {
        return rai;
    }

    public BdvStackSource getBdvSS() {
        return bdvSS;
    }
    @Override
    public String getStreamName() {
        return streamName;
    }

    @Override
    public void setRai(RandomAccessibleInterval rai) {
        this.rai = rai;
    }

    @Override
    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public void addMenus( BdvMenus menus ){
        menus.setImageViewer( this );
        for (JMenu menu: menus.getMenus()) {
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
        double max = - Double.MAX_VALUE;
        double value;
        while ( cursor.hasNext()){
            value = cursor.next().getRealDouble();
            if ( value < min ) min = value;
            if ( value > max ) max = value;
        }
        return new DisplaySettings(min,max);
    }


    public void replicateViewerContrast(ImageViewer newImageView){
        int nChannels = (int)this.getRai().dimension(FileInfoConstants.C_AXIS_POSITION);
        for (int channel=0; channel<nChannels; ++channel){
            ConverterSetup converterSetup = this.getBdvSS().getBdvHandle().getSetupAssignments().getConverterSetups().get( channel );
            newImageView.setDisplayRange(converterSetup.getDisplayRangeMin(),converterSetup.getDisplayRangeMax(),0);
            //channel is always 0 (zero) because converterSetup object gets removed and added at the end of bdvSS in setDisplayRange method.
            //Hence current channel is always at position 0 of the bdvSS.
        }
    }

    public int getCurrentTimePoint(){
        System.out.println("Time is"+this.bdvSS.getBdvHandle().getViewerPanel().getState().getCurrentTimepoint());
         return this.bdvSS.getBdvHandle().getViewerPanel().getState().getCurrentTimepoint();
    }
}
