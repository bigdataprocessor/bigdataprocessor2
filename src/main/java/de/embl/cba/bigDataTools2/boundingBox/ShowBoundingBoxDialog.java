package de.embl.cba.bigDataTools2.boundingBox;

import bdv.util.Bdv;
import bdv.util.BdvHandleFrame;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.util.Intervals;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShowBoundingBoxDialog {

    private Bdv bdv;
    public static final int BB_X_POS = 0;
    public static final int BB_Y_POS = 1;
    public static final int BB_Z_POS = 2;
    public static final int BB_T_POS = 3;
    public int[] selectedMin;
    public int[] selectedMax;


    public ShowBoundingBoxDialog(Bdv bdv) {
        this.bdv = bdv;
    }



    public void show(RandomAccessibleInterval rai, final String buttonName, boolean includeTimeAxis) {
        final int[] min, max;
        if (includeTimeAxis) {
            min = new int[]{(int) rai.min(FileInfoConstants.X_AXIS_POSITION), (int) rai.min(FileInfoConstants.Y_AXIS_POSITION),
                    (int) rai.min(FileInfoConstants.Z_AXIS_POSITION),(int)rai.min(FileInfoConstants.T_AXIS_POSITION)};
            max = new int[]{(int) rai.max(FileInfoConstants.X_AXIS_POSITION), (int) rai.max(FileInfoConstants.Y_AXIS_POSITION),
                    (int) rai.max(FileInfoConstants.Z_AXIS_POSITION),(int)rai.max(FileInfoConstants.T_AXIS_POSITION)};
        }else {
            min = new int[]{(int) rai.min(FileInfoConstants.X_AXIS_POSITION), (int) rai.min(FileInfoConstants.Y_AXIS_POSITION),
                    (int) rai.min(FileInfoConstants.Z_AXIS_POSITION)};
            max = new int[]{(int) rai.max(FileInfoConstants.X_AXIS_POSITION), (int) rai.max(FileInfoConstants.Y_AXIS_POSITION),
                    (int) rai.max(FileInfoConstants.Z_AXIS_POSITION)};
        }
        long[] size= new long[FileInfoConstants.MAX_ALLOWED_IMAGE_DIMS];
        rai.dimensions(size);
        int[] center = new int[]{(int)size[FileInfoConstants.X_AXIS_POSITION]/2 + min[BB_X_POS],(int)size[FileInfoConstants.Y_AXIS_POSITION]/2 + min[BB_Y_POS],(int)size[FileInfoConstants.Z_AXIS_POSITION]/2 + min[BB_Z_POS]}; // Center of the image stack.
        int[] BBsize = new int[]{(int)size[FileInfoConstants.X_AXIS_POSITION]/4,(int)size[FileInfoConstants.X_AXIS_POSITION]/4,(int)size[FileInfoConstants.Z_AXIS_POSITION]/4}; //New BB 1/4th the size of the image.
        if(BBsize[BB_Z_POS]<1){ // Check if Z goes below 1
            BBsize[BB_Z_POS]=1;
        }
        int[] minBB = new int[]{center[BB_X_POS]-BBsize[BB_X_POS]/2,center[BB_Y_POS]-BBsize[BB_Y_POS]/2,center[BB_Z_POS]-BBsize[BB_Z_POS]/2}; //Positioning the new BB at the center of the image.
        int[] maxBB = new int[]{center[BB_X_POS]+BBsize[BB_X_POS]/2,center[BB_Y_POS]+BBsize[BB_Y_POS]/2,center[BB_Z_POS]+BBsize[BB_Z_POS]/2}; //Positioning the new BB at the center of the image.
        final Interval initialInterval,rangeInterval;
        final AtomicBoolean lock = new AtomicBoolean(false);
        final int boxSetupId = 8888; // some non-existing setup id TODO: make it static and increment for multiple use--ashis
        final String[] axesToCrop;
        if (includeTimeAxis){
            initialInterval = Intervals.createMinMax(minBB[BB_X_POS], minBB[BB_Y_POS], minBB[BB_Z_POS],min[BB_T_POS],
                        maxBB[BB_X_POS], maxBB[BB_Y_POS], maxBB[BB_Z_POS],max[BB_T_POS]); // the initially selected bounding box
            rangeInterval = Intervals.createMinMax(min[BB_X_POS], min[BB_Y_POS], min[BB_Z_POS],min[BB_T_POS],
                    max[BB_X_POS], max[BB_Y_POS], max[BB_Z_POS],max[BB_T_POS]);// the range (bounding box of possible bounding boxes)
            axesToCrop = FileInfoConstants.BOUNDING_BOX_AXES_4D;
        }else {
            initialInterval = Intervals.createMinMax(minBB[BB_X_POS], minBB[BB_Y_POS], minBB[BB_Z_POS],
                    maxBB[BB_X_POS], maxBB[BB_Y_POS], maxBB[BB_Z_POS]); // the initially selected bounding box
            rangeInterval = Intervals.createMinMax(min[BB_X_POS], min[BB_Y_POS], min[BB_Z_POS],
                    max[BB_X_POS], max[BB_Y_POS], max[BB_Z_POS]);// the range (bounding box of possible bounding boxes)
            axesToCrop = FileInfoConstants.BOUNDING_BOX_AXES_3D;
        }


        final CustomBoundingBoxDialog boundingBoxDialog =
                new CustomBoundingBoxDialog(((BdvHandleFrame) bdv).getBigDataViewer().getViewerFrame(), "Select 3D section to crop ",
                        ((BdvHandleFrame) bdv).getBigDataViewer().getViewer(),
                        ((BdvHandleFrame) bdv).getBigDataViewer().getSetupAssignments(), boxSetupId,
                        initialInterval, rangeInterval,axesToCrop) {

                    @Override
                    public void createContent() {
                        // button prints the bounding box interval
                        final JButton button = new JButton(buttonName);
                        button.addActionListener(new AbstractAction() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void actionPerformed(final ActionEvent e) {
                                setVisible(false);
                                    //System.out.println( "bounding box:" + BoundingBoxTools.printInterval( boxRealRandomAccessible.getInterval() ) );
                                for (int d = 0; d < min.length; ++d) {
                                    min[d] = (int) boxRealRandomAccessible.getInterval().realMin(d);
                                    max[d] = (int) boxRealRandomAccessible.getInterval().realMax(d);
                                }
                                lock.set(true);
                                try {
                                    synchronized (lock) {
                                        lock.notifyAll();
                                    }
                                } catch (Exception e1) {
                                }
                            }
                        });

                        getContentPane().add(boxSelectionPanel, BorderLayout.NORTH);
                        getContentPane().add(button, BorderLayout.SOUTH);
                        pack();
                    }

                    private static final long serialVersionUID = 1L;

                };
        boundingBoxDialog.setModal(true);
        boundingBoxDialog.setModalityType(Dialog.ModalityType.MODELESS);
        boundingBoxDialog.setVisible(true);
        //boundingBoxDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        do {
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (Exception e) {
            }
        }
        while (lock.get() == false);
        this.selectedMax = max;
        this.selectedMin = min;
    }
}
