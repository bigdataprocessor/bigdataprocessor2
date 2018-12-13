package de.embl.cba.bigDataTools2.dataStreamingGUI;

import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.saving.SavingSettings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

public class SaveMenuDialog extends JDialog implements ActionListener {
    JCheckBox cbLZW = new JCheckBox("LZW Compression (Tiff)");
    JCheckBox cbSaveVolume = new JCheckBox("Save Volume data");
    JCheckBox cbSaveProjection = new JCheckBox("Save Projections");
    JCheckBox cbConvertTo8Bit = new JCheckBox("8-bit Conversion");
    JCheckBox cbConvertTo16Bit = new JCheckBox("16-bit Conversion");
    JCheckBox cbGating = new JCheckBox("Gate");

    JTextField tfBinning = new JTextField("1,1,1", 10);
    JTextField tfRowsPerStrip = new JTextField("10", 3);
    JTextField tfMapTo255 = new JTextField("65535", 5);
    JTextField tfMapTo0 = new JTextField("0", 5);
    JTextField tfGateMin = new JTextField("0", 5);
    JTextField tfGateMax = new JTextField("255", 5);

    JComboBox comboFileTypeForSaving = new JComboBox(new FileInfoConstants.FileType[]{
            FileInfoConstants.FileType.TIFF_as_PLANES,
            FileInfoConstants.FileType.TIFF_as_STACKS,
            FileInfoConstants.FileType.HDF5,
            FileInfoConstants.FileType.HDF5_IMARIS_BDV});

    final String SAVE = "Save";
    JButton save = new JButton(SAVE);

    final String STOP_SAVING = "Stop Saving";
    JButton stopSaving = new JButton(STOP_SAVING);
    JFileChooser fc;

    public SaveMenuDialog() {

        JTabbedPane menu = new JTabbedPane();
        ArrayList<JPanel> mainPanels = new ArrayList();
        ArrayList<JPanel> panels = new ArrayList();
        int j = 0, k = 0;
        mainPanels.add(new JPanel());
        mainPanels.get(k).setLayout(new BoxLayout(mainPanels.get(k), BoxLayout.PAGE_AXIS));
        cbSaveVolume.setSelected(true);
        panels.add(new JPanel());
        panels.get(j).add(new JLabel("File Type:"));
        panels.get(j).add(comboFileTypeForSaving);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("Binnings [pixels]: x1,y1,z1; x2,y2,z2; ... "));
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
        menu.add("Saving", mainPanels.get(k++));

        add(menu);
        //setAlwaysOnTop (true);
        setModal(true);
        setSize(345, 500);
        setModalityType(ModalityType.APPLICATION_MODAL);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(Thread.currentThread().getId());
        if (e.getActionCommand().equals(SAVE)) {
            FileInfoConstants.FileType fileType = (FileInfoConstants.FileType) comboFileTypeForSaving.getSelectedItem();
            fc = new JFileChooser(System.getProperty("user.dir"));
            int returnVal = fc.showSaveDialog(SaveMenuDialog.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                final File file = fc.getSelectedFile();
                SavingSettings savingSettings = new SavingSettings();
                String compression = "";
                if (cbLZW.isSelected()) {
                    compression = "LZW";
                }
                savingSettings.compression = compression;
                savingSettings.bin = tfBinning.getText();
                savingSettings.saveVolume = cbSaveVolume.isSelected();
                savingSettings.saveProjection = cbSaveProjection.isSelected();
                savingSettings.convertTo8Bit = cbConvertTo8Bit.isSelected();
                savingSettings.mapTo0 = Integer.parseInt(tfMapTo0.getText());
                savingSettings.mapTo255 = Integer.parseInt(tfMapTo255.getText());

                if (!(fileType.equals(FileInfoConstants.FileType.TIFF_as_PLANES))) {
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
                if (fileType.equals(FileInfoConstants.FileType.HDF5_IMARIS_BDV)) {
                    savingSettings.fileBaseNameIMARIS = file.getName();
                    savingSettings.parentDirectory = file.getParent();
                }
                DataStreamingTools.executorService.submit(() -> {
                    DataStreamingUI.dataStreamingTools.saveImage(savingSettings);
                });

            }
        } else if (e.getActionCommand().equals(STOP_SAVING)) {
            DataStreamingTools.stopSave(); // Don't submit to thread pool. Let the main thread handle it.
        }
    }
}

