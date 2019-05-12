package de.embl.cba.bdp2.ui;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Util;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

@Deprecated
public class EightBitConverterMenuDialog extends JDialog implements ActionListener {
    private final JTextField mapToZero= new JTextField("0", 5);
    private final JTextField mapTo255 = new JTextField("65535",5);
    private final String CONVERT_BUTTON_LABEL = "Convert";
    private final JButton convertButton =  new JButton(CONVERT_BUTTON_LABEL);
    private final JLabel warning = new JLabel("Image is already 8- bit");
    private final ImageViewer imageViewer;
    private static final String DIALOG_NAME = "8-Bit Converter";

    public EightBitConverterMenuDialog(ImageViewer imageViewer){
        this.imageViewer = imageViewer;
        JTabbedPane menu = new JTabbedPane();
        ArrayList<JPanel> mainPanels = new ArrayList<>();
        ArrayList<JPanel> panels = new ArrayList<>();
        int j = 0, k = 0;
        mainPanels.add(new JPanel());
        mainPanels.get(k).setLayout(new BoxLayout(mainPanels.get(k), BoxLayout.PAGE_AXIS));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("Map 0 \u21D0"));
        panels.get(j).add(mapToZero);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("Map 255 \u21D0" ));
        panels.get(j).add(mapTo255);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        convertButton.setActionCommand(CONVERT_BUTTON_LABEL);
        convertButton.addActionListener(this);
        panels.get(j).add(convertButton);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(warning);
        mainPanels.get(k).add(panels.get(j++));

        menu.add(DIALOG_NAME, mainPanels.get(k++));
        if (Util.getTypeFromInterval(imageViewer.getImage().getRai()) instanceof UnsignedByteType){
            warning.setVisible(true);
            convertButton.setEnabled(false);
        }else{
            warning.setVisible(false);
            convertButton.setEnabled(true);
        }
        add(menu);
        pack();
        setModalityType(ModalityType.DOCUMENT_MODAL);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int min = Integer.parseInt(mapToZero.getText());
        int max = Integer.parseInt(mapTo255.getText());
        DisplaySettings displaySettings= new DisplaySettings(min,max);
        final Image image = imageViewer.getImage();

        RandomAccessibleInterval<UnsignedByteType> unsignedByteTypeRAI =
                BigDataProcessor2.unsignedByteTypeConverter(
                        image.getRai(),displaySettings );

        ImageViewer newImageViewer = imageViewer.newImageViewer();

        newImageViewer.show( image.newImage( unsignedByteTypeRAI ), true );

        BdvMenus menus = new BdvMenus();
        newImageViewer.addMenus(menus);
        dispose();
    }
}
