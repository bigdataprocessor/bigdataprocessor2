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

import static de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants.*;

public class BoundingBoxDialog
{

    private Bdv bdv;
    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;
    public static final int T = 3;
    public int[] selectedMin; // TODO: double?
    public int[] selectedMax; // TODO: double?


    public BoundingBoxDialog( Bdv bdv ) {
        this.bdv = bdv;
    }


    public void show( RandomAccessibleInterval rai, double[] voxelSize, final String buttonName, boolean includeTimeAxis ) {
        final int[] min, max;

        if (includeTimeAxis)
        {
            min = new int[ 4 ];
            min[ T ] = ( int ) rai.min( FileInfoConstants.T );
            max = new int[ 4 ];
            max[ T ] = ( int ) rai.max( FileInfoConstants.T );
        }
        else
        {
            min = new int[ 3 ];
            max = new int[ 3 ];
        }

        for ( int d = 0; d < 3; d++ )
        {
            min[ d ] = (int) ( rai.min( d ) * voxelSize[ d ] );
            max[ d ] = (int) ( rai.max( d ) * voxelSize[ d ] );
        }

        long[] size = new long[ MAX_ALLOWED_IMAGE_DIMS ];

        rai.dimensions(size);

        int[] center = new int[ 3 ];
        int[] width = new int[ 3 ];
        int[] initialBBSize = new int[ 3 ];
        for ( int d = 0; d < 3; d++ )
        {
            width[ d ] = ( max[ d ] - min[ d ] );
            center[ d ] = (int) ( ( min[ d ] + width[ d ] / 2.0 ) );
            initialBBSize[ d ] = width[ d ] / 4;
        }

        if (initialBBSize[ Z ] < 1) { // Check if Z goes below 1
            initialBBSize[ Z ] = 1;
        }

        int[] minBB = new int[]{ center[ X ] - initialBBSize[ X ] / 2, center[ Y ] - initialBBSize[ Y ] / 2, center[ Z ] - initialBBSize[ Z ] / 2}; //Positioning the new BB at the center of the image.
        int[] maxBB = new int[]{ center[ X ] + initialBBSize[ X ] / 2, center[ Y ] + initialBBSize[ Y ] / 2, center[ Z ] + initialBBSize[ Z ] / 2}; //Positioning the new BB at the center of the image.

        final Interval initialInterval, rangeInterval;
        final AtomicBoolean lock = new AtomicBoolean(false);
        final int boxSetupId = 8888; // some non-existing setup id TODO: make it static and increment for multiple use--ashis
        final String[] axesToCrop;
        String cropDialogTitleMessage;

        if (includeTimeAxis)
        {
            cropDialogTitleMessage = "Select 4D section to crop";

            initialInterval = Intervals.createMinMax( minBB[ X ], minBB[ Y ], minBB[ Z ], min[ T ],
                    maxBB[ X ], maxBB[ Y ], maxBB[ Z ], max[ T ]); // the initially selected bounding box

            rangeInterval = Intervals.createMinMax( min[ X ], min[ Y ], min[ Z ], min[ T ],
                    max[ X ], max[ Y ], max[ Z ], max[ T ]);// the range (bounding box of possible bounding boxes)

            axesToCrop = BOUNDING_BOX_AXES_4D;
        }
        else
        {
            cropDialogTitleMessage = "Select 3D section to crop";
            initialInterval = Intervals.createMinMax(
                    minBB[ X ], minBB[ Y ], minBB[ Z ],
                    maxBB[ X ], maxBB[ Y ], maxBB[ Z ]); // the initially selected bounding box
            rangeInterval = Intervals.createMinMax(
                    min[ X ], min[ Y ], min[ Z ],
                    max[ X ], max[ Y ], max[ Z ]);// the range (bounding box of possible bounding boxes)
            axesToCrop = BOUNDING_BOX_AXES_3D;
        }


        final CustomBoundingBoxDialog boundingBoxDialog =
                new CustomBoundingBoxDialog(
                        ((BdvHandleFrame) bdv).getBigDataViewer().getViewerFrame(),
                        cropDialogTitleMessage,
                        ((BdvHandleFrame) bdv).getBigDataViewer().getViewer(),
                        ((BdvHandleFrame) bdv).getBigDataViewer().getSetupAssignments(),
                        boxSetupId,
                        initialInterval,
                        rangeInterval,
                        axesToCrop) {

                    @Override
                    public void createContent() {
                        // button prints the bounding box realInterval
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
        //boundingBoxDialog.setModalityType(Dialog.ModalityType.MODELESS);
        boundingBoxDialog.setVisible(true);
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
