package de.embl.cba.bigDataToolViewerIL2.dataStreamingGUI;

import net.imglib2.FinalInterval;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class CropSelectMenu extends JMenu {

    public JMenuItem item;
    public FinalInterval cropSection;

    public CropSelectMenu(BdvMenus actionListener) {
        setText("Crop View");
        item = new JMenuItem("Begin Crop");
        item.addActionListener(actionListener);
        add(item);
    }
}
