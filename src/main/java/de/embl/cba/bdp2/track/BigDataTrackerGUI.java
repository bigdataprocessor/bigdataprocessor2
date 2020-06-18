package de.embl.cba.bdp2.track;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdp2.utils.Point3D;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.LongStream;

public class BigDataTrackerGUI < R extends RealType< R > & NativeType< R > >
        extends JDialog implements ActionListener, FocusListener
{
    //JFrame frame;

    Point3D maxDisplacement = new Point3D(20,20,1);
    private String resizeFactor = "1.0";
    final BigDataTracker bigDataTracker= new BigDataTracker();
    //TrackTablePanel trackTablePanel;
    String[] defaults;
    TrackingSettings< R > trackingSettings = new TrackingSettings< >();
    private int hashcode = this.hashCode();
    String[] texts = {
            //"Region size: x,y,z [pixels]",
            //"Maximal displacement between subsequent frames: x,y,z [pixels]",
            //"dx(bin), dy(bin), dz(subsample), dt(subsample) [pixels, frames]",
            "Length [frames]",
            "Intensity gating [min, max]",
            //"Show (processed) track regions [Num]",
            "Resize regions by [factor]"
    };

    String[] buttonActions = {
            "Select ROI",
            //"Set z",
            "Track selected object",
            "Stop track",
            //"Show table",
            //"Save table",
            //"Clear all tracks",
            "View as new stream",
            "Report issue"
    };
    // Buttons
    //
    JButton[] buttons = new JButton[buttonActions.length];
    {
    for (int i = 0; i < buttons.length; i++) {
    buttons[i] = new JButton(buttonActions[i]);
    buttons[i].setActionCommand(buttonActions[i]);
    buttons[i].addActionListener(this);
    //buttons[i].setToolTipText(toolTipTexts[iToolTipText]);
    }}

    String[] comboNames = {
            //"Enhance image features",
            "Tracking method",
            "Select Channel to track"
    };

    String[][] comboChoices = new String[comboNames.length][];
    String[] channelChoices;
    JTextField[] textFields = new JTextField[texts.length];
    protected final JProgressBar progressBar = new JProgressBar(0, 100);
    JLabel[] labels = new JLabel[texts.length];
    protected final JLabel MESSAGE = new JLabel("");
    protected final String MESSAGE_TRACK_INTERRUPTED ="Tracking Interrupted!";
    protected final String MESSAGE_TRACK_FINISHED ="Tracking Completed!";
    private  AbstractObjectTracker tracker;
    private RandomAccessibleInterval image;
    public final BdvImageViewer imageViewer;

    public BigDataTrackerGUI(BdvImageViewer handle ){
        this.imageViewer= handle;
        this.image = imageViewer.getImage().getRai();
        long nChannels = this.image.dimension(DimensionOrder.C);
        channelChoices = new String[(int)nChannels];
        LongStream.range(0, nChannels).forEach( //Dynamically filling the array.
                n -> channelChoices[(int)n]=Long.toString(n)
        );
        String[] imageFilters = new String[ Utils.ImageFilterTypes.values().length];
        for ( int i = 0; i < imageFilters.length; i++ ){
            imageFilters[i] = Utils.ImageFilterTypes.values()[i].toString();
        }
        //comboChoices[0] = imageFilters;
        comboChoices[0] = new String[]{TrackingSettings.CENTER_OF_MASS,
                TrackingSettings.PHASE_CORRELATION };
        comboChoices[1] = channelChoices;

        trackingSettings.trackingMethod = TrackingSettings.CENTER_OF_MASS;
        trackingSettings.objectSize = new Point3D( 200, 200, 30);
        trackingSettings.maxDisplacement = maxDisplacement;
        trackingSettings.subSamplingXYZ = new Point3D( 3, 3, 1);
        trackingSettings.subSamplingT = 1;
        trackingSettings.intensityGate = new int[]{-1,-1};
        trackingSettings.viewFirstNProcessedRegions = 0;
        trackingSettings.imageFeatureEnhancement = Utils.ImageFilterTypes.NONE.toString();
        trackingSettings.nt = -1;
        trackingSettings.voxelSpacing = imageViewer.getImage().getVoxelSpacing();
        setDefaults();
    }

    public void setDefaults()
    {
        String[] defaults = {
//                "" + (int) trackingSettings.objectSize.getX() + "," +
//                        (int) trackingSettings.objectSize.getY() + "," +
//                        (int) trackingSettings.objectSize.getZ(),
//                "" + (int) trackingSettings.maxDisplacement.getX() + "," +
//                        (int) trackingSettings.maxDisplacement.getY() + "," +
//                        (int) trackingSettings.maxDisplacement.getZ(),
//                "" + (int) trackingSettings.subSamplingXYZ.getX() + "," +
//                        (int) trackingSettings.subSamplingXYZ.getY() + "," +
//                        (int) trackingSettings.subSamplingXYZ.getZ() + "," +
//                        trackingSettings.subSamplingT,
                "" + trackingSettings.nt,
                "" + trackingSettings.intensityGate[0] + "," +
                        trackingSettings.intensityGate[1],
                "" + trackingSettings.viewFirstNProcessedRegions,
                resizeFactor
        };

        this.defaults = defaults;
    }

    public void showDialog()
    {

        //frame = new JFrame("Big Data Tracker");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Container c = getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        //String[] toolTipTexts = getToolTipFile("TrackAndCropHelp.html");
        int iToolTipText = 0;

        // Configure all TextFields
        //
        for (int i = 0; i < textFields.length; i++, iToolTipText++)
        {
            textFields[i] = new JTextField(12);
            textFields[i].setActionCommand(texts[i]);
            textFields[i].addActionListener(this);
            textFields[i].addFocusListener(this);
            textFields[i].setText(defaults[i]);
            //textFields[i].setToolTipText(toolTipTexts[iToolTipText]);
            labels[i] = new JLabel(texts[i] + ": ");
            labels[i].setLabelFor(textFields[i]);
        }

        //
        // ComboBoxes
        //
        JComboBox[] comboBoxes = new JComboBox[comboNames.length];
        JLabel[] comboLabels = new JLabel[comboNames.length];

        for (int i = 0; i < comboChoices.length; i++, iToolTipText++) {
            comboBoxes[i] = new JComboBox(comboChoices[i]);
            comboBoxes[i].setActionCommand(comboNames[i]);
            comboBoxes[i].addActionListener(this);
            //comboBoxes[i].setToolTipText(toolTipTexts[iToolTipText]);
            comboLabels[i] = new JLabel(comboNames[i] + ": ");
            comboLabels[i].setLabelFor(comboBoxes[i]);
        }

        //
        // Panels
        //
        int i = 0;
        ArrayList<JPanel> panels = new ArrayList<>();
        int iPanel = 0;
        int k = 0;
        int iComboBox = 0;

        //
        // TRACKING
        //
        panels.add(new JPanel(new FlowLayout(FlowLayout.LEFT)));
        panels.get(iPanel).add(new JLabel("TRACKING"));
        c.add(panels.get(iPanel++));
        // Object size
        panels.add(new JPanel(new FlowLayout(FlowLayout.LEFT)));
        buttons[0].setFont(buttons[0].getFont().deriveFont(Font.BOLD));
        panels.get(iPanel).add(buttons[i++]);
        //panels.get(iPanel).add(buttons[i++]);
        //panels.get(iPanel).add(labels[k]);
        //panels.get(iPanel).add(textFields[k++]);
        c.add(panels.get(iPanel++));
        // Window size
//        panels.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
//        panels.get(iPanel).add(labels[k]);
//        panels.get(iPanel).add(textFields[k++]);
//        c.add(panels.get(iPanel++));
        // Subsampling
//        panels.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
//        panels.get(iPanel).add(labels[k]);
//        panels.get(iPanel).add(textFields[k++]);
//        c.add(panels.get(iPanel++));
        // Length
        panels.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
        panels.get(iPanel).add(labels[k]);
        panels.get(iPanel).add(textFields[k++]);
        c.add(panels.get(iPanel++));
        // Intensity gating
        panels.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
        panels.get(iPanel).add(labels[k]);
        panels.get(iPanel).add(textFields[k++]);
        c.add(panels.get(iPanel++));
        // Enhance features
//        panels.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
//        panels.get(iPanel).add(comboLabels[iComboBox]);
//        panels.get(iPanel).add(comboBoxes[iComboBox++]);
//        c.add(panels.get(iPanel++));
        // View processed tracked region
//        panels.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
//        panels.get(iPanel).add(labels[k]);
//        panels.get(iPanel).add(textFields[k++]);
//        c.add(panels.get(iPanel++));
        // Tracking Method
        panels.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
        panels.get(iPanel).add(comboLabels[iComboBox]);
        panels.get(iPanel).add(comboBoxes[iComboBox++]);
        c.add(panels.get(iPanel++));

        // Select Channel
        panels.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
        panels.get(iPanel).add(comboLabels[iComboBox]);
        panels.get(iPanel).add(comboBoxes[iComboBox++]);
        c.add(panels.get(iPanel++));

        // ObjectTracker button
        panels.add(new JPanel(new FlowLayout(FlowLayout.CENTER)));
        panels.get(iPanel).add(buttons[i++]);
        c.add(panels.get(iPanel++));

        // ObjectTracker cancel button
        panels.add(new JPanel(new FlowLayout(FlowLayout.CENTER)));
        buttons[2].setEnabled(false);
        panels.get(iPanel).add(buttons[i++]);
        c.add(panels.get(iPanel++));

        //
        // RESULTS TABLE
        //
//        c.add(new JSeparator(SwingConstants.HORIZONTAL));
//        panels.add(new JPanel(new FlowLayout(FlowLayout.LEFT)));
//        panels.get(iPanel).add(new JLabel("RESULTS TABLE"));
//        c.add(panels.get(iPanel++));
//        // Table buttons
//        panels.add(new JPanel(new FlowLayout(FlowLayout.CENTER)));
//        panels.get(iPanel).add(buttons[i++]);
//        panels.get(iPanel).add(buttons[i++]);
//        panels.get(iPanel).add(buttons[i++]);
//        c.add(panels.get(iPanel++));

        //
        // CROPPING
        //
        c.add(new JSeparator(SwingConstants.HORIZONTAL));
        panels.add(new JPanel(new FlowLayout(FlowLayout.LEFT)));
        panels.get(iPanel).add(new JLabel("VIEW TRACKED OBJECTS"));
        c.add(panels.get(iPanel++));

        panels.add(new JPanel(new FlowLayout(FlowLayout.CENTER)));
        panels.get(iPanel).add(labels[k]);
        panels.get(iPanel).add(textFields[k++]);
        panels.get(iPanel).add(buttons[i++]);
        c.add(panels.get(iPanel++));

        //
        // MISCELLANEOUS
        //
        c.add(new JSeparator(SwingConstants.HORIZONTAL));
        panels.add(new JPanel(new FlowLayout(FlowLayout.LEFT)));
        panels.get(iPanel).add(new JLabel("MISCELLANEOUS"));
        c.add(panels.get(iPanel++));

        panels.add(new JPanel());
        panels.get(iPanel).add(buttons[i++]);
        c.add(panels.get(iPanel++));

        panels.add(new JPanel(new FlowLayout(FlowLayout.CENTER)));
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        panels.get(iPanel).add(progressBar);
        c.add(panels.get(iPanel++));

        panels.add(new JPanel());
        panels.get(iPanel).add(MESSAGE);
        c.add(panels.get(iPanel++));


        //
        // Show the GUI
        //
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Shutdown pool");
                //Utils.shutdownThreadPack(uiActionThreadPool); //TODO: introduce UI thread pool if needed --ashis
                bigDataTracker.shutdownThreadPack();
            }
        });
        pack();
        setVisible(true);
    }

    public void changeTextField(int i, String text) {
        textFields[i].setText(text);
    }

    public void focusGained(FocusEvent e) {
        //
    }

    public void focusLost(FocusEvent e) {
        JTextField tf = (JTextField) e.getSource();
        if (!(tf == null)) {
            tf.postActionEvent();
        }
    }

    public void actionPerformed(ActionEvent e) {

        int i = 0, j = 0, k = 0;
        JFileChooser fc;

       if (e.getActionCommand().equals("Select ROI")) {
            System.out.println(e.getActionCommand());
           BigDataTracker.trackerThreadPool.submit(()-> {
               System.out.println(e.getActionCommand());
               FinalInterval interval = imageViewer.getVoxelIntervalXYZCTDialog( true );
               trackingSettings.pMin = new Point3D((int)interval.min( DimensionOrder.X ),
                                                   (int)interval.min( DimensionOrder.Y ),
                                                   (int)interval.min( DimensionOrder.Z ));

               trackingSettings.pMax = new Point3D((int)interval.max( DimensionOrder.X ),
                                                   (int)interval.max( DimensionOrder.Y ),
                                                   (int)interval.max( DimensionOrder.Z ));
           });
           trackingSettings.tStart= imageViewer.getCurrentTimePoint();
//        }
//        else if (e.getActionCommand().equals(buttonActions[i++]))
//        {
//
//            //
//            //  Set nz
//            //
//
//            int z =0;// imp.getZ()-1;
//            if (previouslySelectedZ==-1) {
//                // first time do nothing
//            } else {
//                int nz = Math.abs(z - previouslySelectedZ);
//                trackingSettings.objectSize = new Point3D(trackingSettings.objectSize.getX(),
//                        trackingSettings.objectSize.getY(), nz);
//
//                changeTextField(0, "" + (int) trackingSettings.objectSize.getX() + "," +
//                        (int) trackingSettings.objectSize.getY() + "," +
//                        (int) trackingSettings.objectSize.getZ());
//            }
//            previouslySelectedZ = z;
//
//        }
            //else if (e.getActionCommand().equals(buttonActions[i++]))
       }else if (e.getActionCommand().equals("Track selected object")) {

            System.out.println(e.getActionCommand());
            // track selected object

            //
            // configure track
            //
           MESSAGE.setText(null);
            trackingSettings.rai = image;
            // TODO: think about below:
            trackingSettings.trackingFactor = 1.0 + 2.0 * maxDisplacement.getX() /
                    trackingSettings.objectSize.getX() ;

            trackingSettings.iterationsCenterOfMass =
                     (int) Math.ceil(Math.pow(trackingSettings.trackingFactor, 2));

            // do it
            //
           progressBar.setVisible(true);
           buttons[1].setEnabled(false);
           buttons[2].setEnabled(true);
           pack();
           BigDataTracker.trackerThreadPool.submit(()-> {
               this.tracker =  bigDataTracker.trackObject(trackingSettings,imageViewer);
               this.tracker.setProgressListener( ( current, total ) ->{ //Progress bar
                   int progress = (int) ((current*100) / total);
                   progressBar.setValue(progress);
                   if (progress == 100){
                       progressBar.setVisible(false);
                       progressBar.setVisible(false);
                       buttons[1].setEnabled(true);
                       if (!MESSAGE_TRACK_INTERRUPTED.equals(MESSAGE.getText())) {
                           MESSAGE.setText(MESSAGE_TRACK_FINISHED);
                       }
                       pack();
                   }
               });
           });
       }
        else if ( e.getActionCommand().equals("Stop track") ) {
           tracker.stopTrack(); // Don't submit to thread pool. Let the main thread handle it.
           progressBar.setVisible(false);
           buttons[1].setEnabled(true);
           buttons[2].setEnabled(false);
           MESSAGE.setText(MESSAGE_TRACK_INTERRUPTED);
           pack();
        }

        else if ( e.getActionCommand().equals("Show table") ) {
            System.out.println(e.getActionCommand());
            //
            // Show Table
            //

           // showTrackTable();

        }
        else if (e.getActionCommand().equals("Save table"))
        {
            System.out.println(e.getActionCommand());
            //
            // Save Table
            //

//            TableModel model = bigDataTracker.getTrackTable().getTable().getModel();
//            if(model == null) {
//                Logger.error("There are no tracks yet.");
//                return;
//            }
//            fc = new JFileChooser();
//            if (fc.showSaveDialog(this.frame) == JFileChooser.APPROVE_OPTION) {
//                File file = fc.getSelectedFile();
//                bigDataTracker.getTrackTable().saveTrackTable(file);
//            }

        }
        else if ( e.getActionCommand().equals("Clear all tracks") )
        {
            System.out.println(e.getActionCommand());
            //bigDataTracker.clearAllTracks();


        } else if (e.getActionCommand().equals("View as new stream")) {
            // View Object tracks
            //
             System.out.println(e.getActionCommand());
            bigDataTracker.showTrackedObjects(imageViewer);


        } else if (e.getActionCommand().equals("Report issue")) {
            // Report issue
            //
             System.out.println(e.getActionCommand());
            if (Desktop.isDesktopSupported()) {
                try {
                    final URI uri = new URI("https://github.com/tischi/imagej-open-stacks-as-virtualstack/issues");
                    Desktop.getDesktop().browse(uri);
                } catch (URISyntaxException uriEx) {
                    Logger.error(uriEx.toString());
                } catch (IOException ioEx) {
                    Logger.error(ioEx.toString());
                }
            } else { /* TODO: error handling */ }

//        } else if (e.getActionCommand().equals(texts[k++])) {
//            //
//            // ObjectTracker object size
//            //
//            JTextField source = (JTextField) e.getSource();
//            String[] sA = source.getText().split(",");
//            trackingSettings.objectSize = new Point3D(new Integer(sA[0]), new Integer(sA[1]), new Integer(sA[2]));
//        }
//        else if (e.getActionCommand().equals(texts[k++]))
//        {
//            //
//            // ObjectTracker maximal displacements
//            //
//            JTextField source = (JTextField) e.getSource();
//            String[] sA = source.getText().split(",");
//            trackingSettings.maxDisplacement = new Point3D(new Integer(sA[0]), new Integer(sA[1]), new Integer(sA[2]));
//        }
//        else if (e.getActionCommand().equals(texts[k++]))
//        {
//            //
//            // ObjectTracker sub-sampling
//            //
//            JTextField source = (JTextField) e.getSource();
//            String[] sA = source.getText().split(",");
//            trackingSettings.subSamplingXYZ = new Point3D(new Integer(sA[0]), new Integer(sA[1]), new Integer(sA[2]));
//            trackingSettings.subSamplingT = new Integer(sA[3]);
        }else if ( e.getActionCommand().equals("Length [frames]") ){
           System.out.println(e.getActionCommand());
            // Track length
            //
            JTextField source = (JTextField) e.getSource();
            trackingSettings.nt = new Integer(source.getText());
        }
        else if ( e.getActionCommand().equals("Intensity gating [min, max]"))
        { System.out.println(e.getActionCommand());
            //
            // Image intensityGate value
            //
            JTextField source = (JTextField) e.getSource();
            trackingSettings.intensityGate = Utils.delimitedStringToIntegerArray( source.getText(), ",");
        }
        else if ( e.getActionCommand().equals("Show (processed) track regions [Num]") )
        {System.out.println(e.getActionCommand());
            //
            // Show processed image regions
            //
            JTextField source = (JTextField) e.getSource();
            //trackingSettings.viewFirstNProcessedRegions = new Integer(source.getText());;
        }else if (e.getActionCommand().equals("Resize regions by [factor]")){
           System.out.println(e.getActionCommand());
            // Cropping factor
            //
            JTextField source = (JTextField) e.getSource();
            resizeFactor = source.getText();
        }
//        else if ( e.getActionCommand().equals( comboNames[0]) )
//        {
//            //
//            // Image feature enhancement method
//            //
//            JComboBox cb = (JComboBox)e.getSource();
//            trackingSettings.imageFeatureEnhancement = (String) cb.getSelectedItem();
//            int a = 1;
//        }
        else if ( e.getActionCommand().equals("Tracking method") )
        {    // ObjectTracker method
            JComboBox cb = (JComboBox)e.getSource();
            trackingSettings.trackingMethod = (String)cb.getSelectedItem();
            System.out.println(trackingSettings.trackingMethod);
        }
       else if ( e.getActionCommand().equals("Select Channel to track") )
       {    // ObjectTracker method
           JComboBox cb = (JComboBox)e.getSource();
           trackingSettings.channel = Integer.parseInt((String)cb.getSelectedItem());
           System.out.println("channel selected "+trackingSettings.channel);
       }


    }

    private String[] getToolTipFile(String fileName) {
        ArrayList<String> toolTipTexts = new ArrayList<String>();

        //Get file from resources folder
        InputStream in = getClass().getResourceAsStream("/"+fileName);
        BufferedReader input = new BufferedReader(new InputStreamReader(in));
        Scanner scanner = new Scanner(input);
        StringBuilder sb = new StringBuilder("");

        while ( scanner.hasNextLine() )
        {
            String line = scanner.nextLine();
            if(line.equals("###")) {
                toolTipTexts.add(sb.toString());
                sb = new StringBuilder("");
            } else {
                sb.append(line);
            }

        }

        scanner.close();


        return(toolTipTexts.toArray(new String[0]));
    }

//     public void showTrackTable()
//    {
//        trackTablePanel.showTable();
//    }

//    public void showTrackedObjects() {
//
//        ArrayList<ImagePlus> imps = bigDataTracker.getViewsOnTrackedObjects( resizeFactor );
//
//        if( imps == null )
//        {
//            Logger.info("The cropping failed!");
//        }
//        else
//        {
//            for (ImagePlus imp : imps)
//            {
//                Utils.replaceImage(imp);
//            }
//        }
//    }

}
