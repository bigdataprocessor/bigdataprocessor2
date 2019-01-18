package de.embl.cba.bigDataTools2.dataStreamingGUI;

import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.imaris.ImarisUtils;
import de.embl.cba.bigDataTools2.logging.IJLazySwingLogger;
import de.embl.cba.bigDataTools2.logging.Logger;
import de.embl.cba.bigDataTools2.utils.Utils;
import de.embl.cba.bigDataTools2.viewers.BdvImageViewer;
import de.embl.cba.bigDataTools2.viewers.IJ1ImageViewer;
import de.embl.cba.bigDataTools2.viewers.ImageViewer;
import ij.IJ;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.awt.Desktop.getDesktop;
import static java.awt.Desktop.isDesktopSupported;

@Deprecated
public class DataStreamingUI extends JFrame implements ActionListener, FocusListener, ItemListener {

    JCheckBox cbLog = new JCheckBox("Verbose Logging");
    JCheckBox cbBDV = new JCheckBox("Big Data Viewer");
//    JCheckBox cbLZW = new JCheckBox("LZW Compression (Tiff)");
//    JCheckBox cbSaveVolume = new JCheckBox("Save Volume data");
//    JCheckBox cbSaveProjection = new JCheckBox("Save Projections");
//    JCheckBox cbConvertTo8Bit = new JCheckBox("8-bit Conversion");
//    JCheckBox cbConvertTo16Bit = new JCheckBox("16-bit Conversion");
//    JCheckBox cbGating = new JCheckBox("Gate");
//
//    JTextField tfBinning = new JTextField("1,1,1", 10);
//    JTextField tfIOThreads = new JTextField("10", 2);
//    JTextField tfRowsPerStrip = new JTextField("10", 3);
//    JTextField tfMapTo255 = new JTextField("65535", 5);
//    JTextField tfMapTo0 = new JTextField("0", 5);
//    JTextField tfGateMin = new JTextField("0", 5);
//    JTextField tfGateMax = new JTextField("255", 5);
    JTextField tfChromaticShifts = new JTextField("0,0,0; 0,0,0", 20);


    JComboBox filterPatternComboBox = new JComboBox(new String[]{
            ".*", ".*--C.*", ".*Left.*", ".*Right.*", ".*short.*", ".*long.*", ".*Target.*", ".*LSEA00.*", ".*LSEA01.*"});

    JComboBox namingSchemeComboBox = new JComboBox(new String[]{
            FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
            FileInfoConstants.LEICA_SINGLE_TIFF,
            FileInfoConstants.LOAD_CHANNELS_FROM_FOLDERS,
            FileInfoConstants.EM_TIFF_SLICES,
            FileInfoConstants.PATTERN_1,
            FileInfoConstants.PATTERN_2,
            FileInfoConstants.PATTERN_3,
            FileInfoConstants.PATTERN_4,
            FileInfoConstants.PATTERN_5,
            FileInfoConstants.PATTERN_6
    });

    JComboBox hdf5DataSetComboBox = new JComboBox(new String[]{"None",
            "Data", "Data111",
            ImarisUtils.RESOLUTION_LEVEL + "0/Data",
            ImarisUtils.RESOLUTION_LEVEL + "1/Data",
            ImarisUtils.RESOLUTION_LEVEL + "2/Data",
            ImarisUtils.RESOLUTION_LEVEL + "3/Data",
            "ITKImage/0/VoxelData", "Data222", "Data444"});

//    JComboBox comboFileTypeForSaving = new JComboBox(new FileInfoConstants.FileType[]{
//            FileInfoConstants.FileType.TIFF_as_PLANES,
//            FileInfoConstants.FileType.TIFF_as_STACKS,
//            FileInfoConstants.FileType.HDF5,
//            FileInfoConstants.FileType.HDF5_IMARIS_BDV});

    final String SAVE = "Save";
    JButton save = new JButton(SAVE);

    final String STOP_SAVING = "Stop Saving";
    JButton stopSaving = new JButton(STOP_SAVING);

    final String STREAMfromFolder = "Stream from Folder";
    JButton streamFromFolder = new JButton(STREAMfromFolder);

    final String REPORT_ISSUE = "Report an issue";
    JButton reportIssue =  new JButton(REPORT_ISSUE);
    final ImageViewer imageViewer;
    ExecutorService uiActionThreadPool;

    Logger logger = new IJLazySwingLogger();

    JFileChooser fc;
    static final BigDataConverter BIG_DATA_CONVERTER = new BigDataConverter();
    public DataStreamingUI() {
        ImageIcon icon = new ImageIcon("src/main/resources/logo.png");
        setIconImage(icon.getImage());
        this.uiActionThreadPool = Executors.newFixedThreadPool(4);//TODO --ashis

        int n = JOptionPane.showConfirmDialog(
                this,
                "This application is now compatible with Big Data Viewer. Do you wish to continue?\n"+"Press 'No' if you wish to continue using the old viewer.",
                "Choose Viewer",
                JOptionPane.YES_NO_OPTION);

        if(n == 0){
            imageViewer = new BdvImageViewer();
            cbBDV.setSelected(true);
        }else{
            imageViewer = new IJ1ImageViewer();
            cbBDV.setSelected(false);
        }
        cbBDV.setEnabled(false);
    }

    public void showDialog() {

        JTabbedPane jtp = new JTabbedPane();
        //String[] toolTipTexts = getToolTipFile("DataStreamingHelp.html");
        //ToolTipManager.sharedInstance().setDismissDelay(10000000);

        // Checkboxes
        cbLog.setSelected(false);
        cbLog.addItemListener(this);
//        cbLZW.setSelected(false);
//        cbSaveVolume.setSelected(true);
//        cbSaveProjection.setSelected(false);
//        tfIOThreads.setEditable(false);

        int i = 0, j = 0, k = 0;

        ArrayList<JPanel> mainPanels = new ArrayList();
        ArrayList<JPanel> panels = new ArrayList();

        // Streaming
        //
        mainPanels.add(new JPanel());
        mainPanels.get(k).setLayout(new BoxLayout(mainPanels.get(k), BoxLayout.PAGE_AXIS));

        panels.add(new JPanel(new FlowLayout(FlowLayout.LEFT)));
        panels.get(j).add(new JLabel("STREAM FROM FOLDER"));
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel((new FlowLayout(FlowLayout.LEFT))));
        panels.get(j).add(new JLabel("Load Files Matching: "));
        panels.get(j).add(filterPatternComboBox);
        filterPatternComboBox.setEditable(true);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel((new FlowLayout(FlowLayout.LEFT))));
        panels.get(j).add(new JLabel("File Naming Scheme:"));
        namingSchemeComboBox.setEditable(true);
        panels.get(j).add(namingSchemeComboBox);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel((new FlowLayout(FlowLayout.LEFT))));
        panels.get(j).add(new JLabel("HDF5 Data Set:           "));
        panels.get(j).add(hdf5DataSetComboBox);
        hdf5DataSetComboBox.setEditable(true);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel((new FlowLayout(FlowLayout.CENTER))));
        streamFromFolder.setActionCommand(STREAMfromFolder);
        streamFromFolder.addActionListener(this);
        panels.get(j).add(streamFromFolder);
        mainPanels.get(k).add(panels.get(j++));

        jtp.add("Streaming", mainPanels.get(k++));

        // Misc
        //
        mainPanels.add( new JPanel() );
        mainPanels.get(k).setLayout(new BoxLayout(mainPanels.get(k), BoxLayout.PAGE_AXIS));
        /*
        panels.add(new JPanel());
        panels.get(j).add(new JLabel("I/O threads"));
        panels.get(j).add(tfIOThreads);
        mainPanels.get(k).add(panels.get(j++));
        */
        panels.add(new JPanel());
        panels.get(j).add(cbLog);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(cbBDV);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        reportIssue.setActionCommand(REPORT_ISSUE);
        reportIssue.addActionListener(this);
        panels.get(j).add(reportIssue);
        mainPanels.get(k).add(panels.get(j++));

        jtp.add("Misc.", mainPanels.get(k++));

        //jtp.setSize(800, 800);
        // Show the GUI
        setTitle("Data Streaming Tools");
        setLocationRelativeTo(null);
        setSize(485, 300);
        add(jtp);
/*
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(statusPanel, BorderLayout.SOUTH);
        statusPanel.setPreferredSize(new Dimension(getWidth(), 16));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        JLabel statusLabel = new JLabel("status");
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(statusLabel);
*/

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Utils.shutdownThreadPack(uiActionThreadPool,2);
                BIG_DATA_CONVERTER.shutdownThreadPack();
            }
        });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
        pack();
    }


    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        JTextField tf = (JTextField) e.getSource();
        if (tf != null) {
            tf.postActionEvent();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        if (source == cbLog) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                logger.setShowDebug(false);
            } else {
                logger.setShowDebug(true);
            }
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        // Get values from GUI
        //

        final String h5DataSet = (String) hdf5DataSetComboBox.getSelectedItem();
        //final int rowsPerStrip = new Integer(tfRowsPerStrip.getText());
        final String filterPattern = (String) filterPatternComboBox.getSelectedItem();
        final String namingScheme = (String) namingSchemeComboBox.getSelectedItem();
        final String directory;
        if (e.getActionCommand().equals(STREAMfromFolder)) {

            directory = IJ.getDirectory("Select a Directory");
            if (null == directory) {
                return;
            }

            uiActionThreadPool.submit(() -> BIG_DATA_CONVERTER.openFromDirectory(
                    directory,
                    namingScheme,
                    filterPattern,
                    h5DataSet,
                    this.imageViewer
            ));
//        } else if (e.getActionCommand().equals(SAVE)) {
//            FileInfoConstants.FileType fileType = (FileInfoConstants.FileType) comboFileTypeForSaving.getSelectedItem();
//            fc = new JFileChooser(System.getProperty("user.dir"));
//
//            int returnVal = fc.showSaveDialog(DataStreamingUI.this);
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                final File file = fc.getSelectedFile();
//                SavingSettings savingSettings = new SavingSettings();
//                String compression = "";
//                if (cbLZW.isSelected()) {
//                    compression = "LZW";
//                }
//                savingSettings.compression = compression;
//                savingSettings.bin = tfBinning.getText();
//                savingSettings.saveVolume = cbSaveVolume.isSelected();
//                savingSettings.saveProjection = cbSaveProjection.isSelected();
//                savingSettings.convertTo8Bit = cbConvertTo8Bit.isSelected();
//                savingSettings.mapTo0 = Integer.parseInt(tfMapTo0.getText());
//                savingSettings.mapTo255 = Integer.parseInt(tfMapTo255.getText());
//
//                if (!(fileType.equals(FileInfoConstants.FileType.TIFF_as_PLANES))) {
//                    // TODO: implement below for planes
//                    savingSettings.convertTo16Bit = cbConvertTo16Bit.isSelected();
//                    savingSettings.gate = cbGating.isSelected();
//                    savingSettings.gateMin = Integer.parseInt(tfGateMin.getText());
//                    savingSettings.gateMax = Integer.parseInt(tfGateMax.getText());
//                }
//
//                //final int ioThreads = new Integer(tfIOThreads.getText());//TODO: implement below
//                // Check that there is enough memory to hold the data in RAM while saving
//                //
//                //if( ! Utils.checkMemoryRequirements(imp, Math.min(ioThreads, imp.getNFrames())) ) return;
//                //savingSettings.nThreads = ioThreads;
//                savingSettings.filePath = file.getAbsolutePath();
//                savingSettings.fileType = fileType;
//                if(fileType.equals(FileInfoConstants.FileType.HDF5_IMARIS_BDV)){
//                    savingSettings.fileBaseNameIMARIS = file.getName();
//                    savingSettings.parentDirectory = file.getParent();
//                }
//                savingSettings.rowsPerStrip = rowsPerStrip;
//                uiActionThreadPool.submit(() -> {
//                    BIG_DATA_CONVERTER.saveImage(savingSettings);
//                });
//            }
//        } else if (e.getActionCommand().equals(STOP_SAVING)) {
//            uiActionThreadPool.submit(() -> {
//                BIG_DATA_CONVERTER.stopSave();
//            });
        }  else if (e.getActionCommand().equals(REPORT_ISSUE)) {
            String url = "https://github.com/tischi/imagej-open-stacks-as-virtualstack/issues"; //TODO: change --ashis
            if (isDesktopSupported()) {
                try {
                    final URI uri = new URI(url);
                    getDesktop().browse(uri);
                } catch (URISyntaxException uriEx) {
                    logger.error(uriEx.toString());
                } catch (IOException ioEx) {
                    logger.error(ioEx.toString());
                }
            } else {
                logger.error("Could not open browser, please report issue here: \n" +       //TODO: change --ashis
                        "https://github.com/tischi/imagej-open-stacks-as-virtualstack/issues");
            }
    }

    }

}
