package de.embl.cba.bigDataTools2.dataStreamingGUI;

import de.embl.cba.bigDataTools2.saving.SavingSettings;
import de.embl.cba.bigDataTools2.viewers.ImageViewer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

public class SaveMenuDialog extends JFrame implements ActionListener {
    private final JCheckBox cbLZW = new JCheckBox("LZW Compression (Tiff)");
    private final JCheckBox cbSaveVolume = new JCheckBox("Save Volume data");
    private final JCheckBox cbSaveProjection = new JCheckBox("Save Projections");
    private final JCheckBox cbConvertTo8Bit = new JCheckBox("8-bit Conversion");
    private final JCheckBox cbConvertTo16Bit = new JCheckBox("16-bit Conversion");
    private final JCheckBox cbGating = new JCheckBox("Gate");

    private final JTextField tfBinning = new JTextField("1,1,1", 10);
    private final JTextField tfRowsPerStrip = new JTextField("10", 3);
    private final JTextField tfMapTo255 = new JTextField("65535", 5);
    private final JTextField tfMapTo0 = new JTextField("0", 5);
    private final JTextField tfGateMin = new JTextField("0", 5);
    private final JTextField tfGateMax = new JTextField("255", 5);

    @SuppressWarnings("unchecked")
    private final
    JComboBox comboFileTypeForSaving = new JComboBox(new SavingSettings.FileType[]{
            SavingSettings.FileType.TIFF_as_PLANES,
            SavingSettings.FileType.TIFF_as_STACKS,
            SavingSettings.FileType.HDF5,
            SavingSettings.FileType.HDF5_IMARIS_BDV});

    private final String SAVE = "Save";
    protected final JButton save = new JButton(SAVE);
    private final String STOP_SAVING = "Stop Saving";
    private final JButton stopSaving = new JButton(STOP_SAVING);
    private JFileChooser fc;
    protected final JProgressBar progressBar;
    private final ImageViewer imageViewer;

    public SaveMenuDialog(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;
        JTabbedPane menu = new JTabbedPane();
        ArrayList<JPanel> mainPanels = new ArrayList<>();
        ArrayList<JPanel> panels = new ArrayList<>();
        int j = 0, k = 0;
        mainPanels.add(new JPanel());
        mainPanels.get(k).setLayout(new BoxLayout(mainPanels.get(k), BoxLayout.PAGE_AXIS));
        cbSaveVolume.setSelected(true);
        panels.add(new JPanel());
        panels.get(j).add(new JLabel("File Type:"));
        panels.get(j).add(comboFileTypeForSaving);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("Binning [pixels]: x1,y1,z1; x2,y2,z2; ... "));
        panels.get(j).add(tfBinning);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(cbSaveVolume);
        panels.get(j).add(cbSaveProjection);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(cbLZW);
        panels.get(j).add(new JLabel("Chunks [ny]"));
        panels.get(j).add(tfRowsPerStrip);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(cbConvertTo8Bit);
        panels.get(j).add(new JLabel("0 ="));
        panels.get(j).add(tfMapTo0);
        panels.get(j).add(new JLabel("255 ="));
        panels.get(j).add(tfMapTo255);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(cbConvertTo16Bit);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(cbGating);
        panels.get(j).add(new JLabel("Min ="));
        panels.get(j).add(tfGateMin);
        panels.get(j).add(new JLabel("Max ="));
        panels.get(j).add(tfGateMax);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        save.setActionCommand(SAVE);
        save.addActionListener(this);
        panels.get(j).add(save);
        stopSaving.setActionCommand(STOP_SAVING);
        stopSaving.addActionListener(this);
        panels.get(j).add(stopSaving);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        panels.get(j).add(progressBar);
        mainPanels.get(k).add(panels.get(j++));
        menu.add("Saving", mainPanels.get(k++));

        add(menu);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(SAVE)) {
            SavingSettings.FileType fileType = (SavingSettings.FileType) comboFileTypeForSaving.getSelectedItem();
            fc = new JFileChooser(System.getProperty("user.dir"));
            int returnVal = fc.showSaveDialog(SaveMenuDialog.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                final File file = fc.getSelectedFile();
                SavingSettings savingSettings = new SavingSettings();
                String compression = SavingSettings.NONE;
                if (cbLZW.isSelected()) {
                    compression = SavingSettings.LZW;
                }
                savingSettings.compression = compression;
                savingSettings.rowsPerStrip = Integer.parseInt(tfRowsPerStrip.getText());
                savingSettings.bin = tfBinning.getText();
                savingSettings.saveVolume = cbSaveVolume.isSelected();
                savingSettings.saveProjection = cbSaveProjection.isSelected();
                savingSettings.convertTo8Bit = cbConvertTo8Bit.isSelected();
                savingSettings.mapTo0 = Integer.parseInt(tfMapTo0.getText());
                savingSettings.mapTo255 = Integer.parseInt(tfMapTo255.getText());

                if (!(fileType.equals(SavingSettings.FileType.TIFF_as_PLANES))) {
                    // TODO: implement below for planes
                    savingSettings.convertTo16Bit = cbConvertTo16Bit.isSelected();
                    savingSettings.gate = cbGating.isSelected();
                    savingSettings.gateMin = Integer.parseInt(tfGateMin.getText());
                    savingSettings.gateMax = Integer.parseInt(tfGateMax.getText());
                }

                //final int ioThreads = new Integer(tfIOThreads.getText());//TODO: implement below
                // Check that there is enough memory to hold the data in RAM while saving
                //
                //if( ! Utils.checkMemoryRequirements(imp, Math.min(ioThreads, imp.getNFrames())) ) return;
                //savingSettings.nThreads = ioThreads;
                savingSettings.filePath = file.getAbsolutePath();
                savingSettings.fileType = fileType;
                if (fileType.equals(SavingSettings.FileType.HDF5_IMARIS_BDV)) {
                    savingSettings.fileBaseNameIMARIS = file.getName();
                    savingSettings.parentDirectory = file.getParent();
                }

                progressBar.setVisible(true);
                pack();
                save.setEnabled(false);
                BigDataConverter.executorService.submit(() -> {
                    new ProgressBar(this).createGUIandRunMonitor();
                    BigDataConverter.saveImage(savingSettings, imageViewer);
                });

            }
        } else if (e.getActionCommand().equals(STOP_SAVING)) {
            BigDataConverter.stopSave(); // Don't submit to thread pool. Let the main thread handle it.
            save.setEnabled(true);
            progressBar.setVisible(false);
            pack();
        }
    }

}

