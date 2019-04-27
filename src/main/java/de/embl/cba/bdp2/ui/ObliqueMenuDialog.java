package de.embl.cba.bdp2.ui;

import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.interpolation.InterpolatorFactory;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ObliqueMenuDialog extends JDialog implements ActionListener {
    private final JCheckBox cbUseYshear = new JCheckBox("");
    private final JCheckBox cbViewLeft = new JCheckBox("");
    private final JTextField tfCameraPixelsize= new JTextField("6.5", 2);
    private final JTextField tfMagnification = new JTextField("40",2);
    private final JTextField tfStepsize = new JTextField("0.707",2);
    private final JTextField tfObjectiveAngle = new JTextField("45",3);
    private final JComboBox namingSchemeComboBox = new JComboBox(new String[]{
            "Clamping n-linear","N-linear","Nearest Neighbour"
    });
    private final String ObliqueUpdate = "Update!";
    private final JButton obliqueUpdate =  new JButton(ObliqueUpdate);
    private final ShearingSettings shearingSettings = new ShearingSettings();
    private final ImageViewer imageViewer;
    private final RandomAccessibleInterval originalRAI;

    public ObliqueMenuDialog(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;
        this.originalRAI = imageViewer.getImage().getRai();
        JTabbedPane menu = new JTabbedPane();
        ArrayList<JPanel> mainPanels = new ArrayList<>();
        ArrayList<JPanel> panels = new ArrayList<>();
        int j = 0, k = 0;
        mainPanels.add(new JPanel());
        mainPanels.get(k).setLayout(new BoxLayout(mainPanels.get(k), BoxLayout.PAGE_AXIS));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("along Y shearing? ( false Default)"));
        panels.get(j).add(cbUseYshear);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("Magnification =   (40x Default)"));
        panels.get(j).add(tfMagnification);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("Pixelsize on Camera in micrometers = (6.5 Default)"));
        panels.get(j).add(tfCameraPixelsize);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("View left? (Right default)"));
        panels.get(j).add(cbViewLeft);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("Stack stepsize (in micrometers) "));
        panels.get(j).add(tfStepsize);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("tfobjectiveAngle (in degrees ) "));
        panels.get(j).add(tfObjectiveAngle);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel((new java.awt.FlowLayout(java.awt.FlowLayout.CENTER))));
        panels.get(j).add(new JLabel("Interpolation:"));
        panels.get(j).add(namingSchemeComboBox);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        obliqueUpdate.setActionCommand(ObliqueUpdate);
        obliqueUpdate.addActionListener(this);
        panels.get(j).add(obliqueUpdate);
        mainPanels.get(k).add(panels.get(j++));

        menu.add("Oblique", mainPanels.get(k++));
        add(menu);
        pack();
        setModalityType(ModalityType.DOCUMENT_MODAL);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        getShearingSettings(shearingSettings);
        shearingSettings.useObliqueAngle = true;
        RandomAccessibleInterval sheared = BigDataProcessor2.shearImage(originalRAI,shearingSettings);
        imageViewer.show(
                sheared,
                "Oblique View",
                imageViewer.getImage().getVoxelSpacing(),
                imageViewer.getImage().getVoxelUnit(),
                true);
        double[] centerCoordinates = {sheared.min( DimensionOrder.X ) / 2.0,
                sheared.max( DimensionOrder.Y ) / 2.0,
                (sheared.max( DimensionOrder.Z ) - sheared.min( DimensionOrder.Z )) / 2.0
                        + sheared.min( DimensionOrder.Z )};
        imageViewer.shiftImageToCenter(centerCoordinates);
    }

    public void getShearingSettings(ShearingSettings shearingSettings) {
        // read into all shearinfo settings into shearingSettings of type Shearingsettings..
        //if(Math.floorMod( NumberOfTimesCalled ,2)==0) {   // check if UseOBlique is set  otherwise just define a default shearingsetting.

            shearingSettings.magnification = new Double(tfMagnification.getText());
            shearingSettings.cameraPixelsize = new Double(tfCameraPixelsize.getText());
            shearingSettings.stepSize = new Double(tfStepsize.getText());
            shearingSettings.backwardStackAcquisition = cbViewLeft.isSelected();
            //shearingSettings.viewLeft = cbViewLeft.isSelected();
            shearingSettings.objectiveAngle = new Double(tfObjectiveAngle.getText());
            shearingSettings.useYshear = cbUseYshear.isSelected();

            //shearingSettings.shearingFactorX = (-1.0)*shearingSettings.stepSize * shearingSettings.magnification *(1.0/shearingSettings.cameraPixelsize)* sin(shearingSettings.objectiveAngle*PI/180.0);
        // Calculate shearingFactor in X and Y :
        double shearFactor =  calculateShearFactor(shearingSettings);
        if (shearingSettings.useYshear) {
                shearingSettings.shearingFactorY = -shearFactor;
                shearingSettings.shearingFactorX = 0.0;
        }else {
            shearingSettings.shearingFactorX = -shearFactor;
            shearingSettings.shearingFactorY = 0.0;
        }
        if (shearingSettings.backwardStackAcquisition) {
            if (shearingSettings.useYshear) {
                shearingSettings.shearingFactorY = shearFactor;
                shearingSettings.shearingFactorX = 0.0;
            }else {
                shearingSettings.shearingFactorX = shearFactor;
                shearingSettings.shearingFactorY = 0.0;
            }
        }

        shearingSettings.interpolationFactory = setInterpolatorFactory();
//        }
//        else {
//            shearingSettings = new ShearingSettings();
//        }
    }

    private double calculateShearFactor(ShearingSettings settings){
        return settings.stepSize * settings.magnification *(1.0/settings.cameraPixelsize)* Math.sin(settings.objectiveAngle* Math.PI/180.0);
    }

    private InterpolatorFactory setInterpolatorFactory(){
        InterpolatorFactory factory;
        String selectedOption =  (String) namingSchemeComboBox.getSelectedItem();
        if (java.util.Objects.requireNonNull(selectedOption).equalsIgnoreCase("Clamping n-linear")){
            factory = new net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory();
        }else if (selectedOption.equalsIgnoreCase("N-linear")){
            factory = new net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory();
        }else {
            factory = new net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory();
        }
        return factory;
    }
}
