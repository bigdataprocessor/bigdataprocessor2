/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.dialog;

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
