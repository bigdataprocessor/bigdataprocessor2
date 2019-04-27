package de.embl.cba.bdp2.ui;

import de.embl.cba.bdp2.files.FileInfoConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class HDF5DatasetDialog extends JDialog implements ActionListener {

    private JComboBox datasetCombo;
    private String selectedDataset;

    public HDF5DatasetDialog( List<String> hdf5Header, boolean isEditable){
        String[] datasetNames = hdf5Header.toArray(new String[0]);
        datasetCombo = new JComboBox(datasetNames);
        ArrayList<JPanel> mainPanels = new ArrayList<>();
        ArrayList<JPanel> panels = new ArrayList<>();
        int j = 0, k = 0;
        JTabbedPane menu = new JTabbedPane();
        mainPanels.add(new JPanel());
        mainPanels.get(k).setLayout(new BoxLayout(mainPanels.get(k), BoxLayout.PAGE_AXIS));
        panels.add(new JPanel((new java.awt.FlowLayout(java.awt.FlowLayout.CENTER))));
        panels.get(j).add(new JLabel("Dataset: "));
        panels.get(j).add(datasetCombo);
        datasetCombo.addActionListener(this);
        datasetCombo.setEditable(isEditable);
        datasetCombo.setMaximumSize(new Dimension(1,25));
        datasetCombo.setSelectedIndex(0);
        mainPanels.get(k).add(panels.get(j++));
        menu.add("Select Dataset Name: ", mainPanels.get(k++));
        add(menu);
        pack();
        setModalityType(ModalityType.APPLICATION_MODAL);
    }

    public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        selectedDataset = (String)cb.getSelectedItem();
    }

    public String getSelectedDataset() {
        return selectedDataset;
    }
}
