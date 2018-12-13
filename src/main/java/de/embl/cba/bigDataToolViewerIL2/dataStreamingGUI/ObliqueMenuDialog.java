package de.embl.cba.bigDataToolViewerIL2.dataStreamingGUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ObliqueMenuDialog extends JDialog implements ActionListener {
    JCheckBox cbUseObliqueAngle = new JCheckBox("");
    JCheckBox cbUseYshear = new JCheckBox("");
    JCheckBox cbViewLeft = new JCheckBox("");
    JCheckBox cbBackwardStackAcquisition = new JCheckBox("");
    JTextField tfCameraPixelsize= new JTextField("6.5", 2);
    JTextField tfMagnification = new JTextField("40",2);
    JTextField tfStepsize = new JTextField("0.707",2);
    JTextField tfObjectiveAngle = new JTextField("45",3);

    final String USEObliqueButton = "Use Oblique?";
    JButton useObliqueButton =  new JButton(USEObliqueButton);
    final String ObliqueUpdate = "Update!";
    JButton obliqueUpdate =  new JButton(ObliqueUpdate);
    public int NumberOfTimesCalled=1;
    public ShearingSettings shearingSettings = new ShearingSettings();

    public ObliqueMenuDialog() {
        JTabbedPane menu = new JTabbedPane();
        ArrayList<JPanel> mainPanels = new ArrayList();
        ArrayList<JPanel> panels = new ArrayList();
        int j = 0, k = 0;
        mainPanels.add(new JPanel());
        mainPanels.get(k).setLayout(new BoxLayout(mainPanels.get(k), BoxLayout.PAGE_AXIS));



        //mainPanels.get(k).add(panels.get(j++));

//        panels.add(new JPanel());
//        panels.get(j).add(new JLabel("Use Oblique  ( false Default)"));
//        panels.get(j).add(cbUseObliqueAngle);
//        //cbUseObliqueAngle.addActionListener(this);
//        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("along Y shearing? ( false Default)"));
        panels.get(j).add(cbUseYshear);
        //cbUseYshear.addActionListener(this);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("Magnification =   (40x Default)"));
        panels.get(j).add(tfMagnification);
        //tfMagnification.addActionListener(this);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("Pixelsize on Camera in micrometers = (6.5 Default)"));
        panels.get(j).add(tfCameraPixelsize);
        //tfCameraPixelsize.addActionListener(this);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("View left? (Right default)"));
        panels.get(j).add(cbViewLeft);
        //cbViewLeft.addActionListener(this);
        mainPanels.get(k).add(panels.get(j++));

//        panels.add(new JPanel());
//        panels.get(j).add(new JLabel("BackwardStackAcquisition? (Forward default)"));
//        panels.get(j).add(cbBackwardStackAcquisition);
//        //cbBackwardStackAcquisition.addActionListener(this);
//        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("Stack stepsize (in micrometers) "));
        panels.get(j).add(tfStepsize);
        //tfStepsize.addActionListener(this);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("tfobjectiveAngle (in degrees ) "));
        panels.get(j).add(tfObjectiveAngle);
        //tfobjectiveAngle.addActionListener(this);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        useObliqueButton.setActionCommand(USEObliqueButton);
        useObliqueButton.addActionListener(this);
        panels.get(j).add(useObliqueButton);

        panels.add(new JPanel());
        obliqueUpdate.setActionCommand(ObliqueUpdate);
        obliqueUpdate.addActionListener(this);
        panels.get(j).add(obliqueUpdate);
        mainPanels.get(k).add(panels.get(j++));

        menu.add("Oblique", mainPanels.get(k++));
        add(menu);
        //setModal(true);
        setSize(345, 500); //TODO: reset size --ashis
        setModalityType(ModalityType.DOCUMENT_MODAL);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        getShearingSettings(shearingSettings);
        shearingSettings.useObliqueAngle = true;
        DataStreamingTools.shearImage(shearingSettings);
    }

    public void getShearingSettings(ShearingSettings shearingSettings) {
        // read into all shearinfo settings into shearingSettings of type Shearingsettings..
        //if(Math.floorMod( NumberOfTimesCalled ,2)==0) {   // check if UseOBlique is set  otherwise just define a default shearingsetting.

            shearingSettings.magnification = new Double(tfMagnification.getText());
            shearingSettings.cameraPixelsize = new Double(tfCameraPixelsize.getText());
            shearingSettings.stepSize = new Double(tfStepsize.getText());
            shearingSettings.backwardStackAcquisition = cbBackwardStackAcquisition.isSelected();
            //shearingSettings.viewLeft = cbViewLeft.isSelected();
            shearingSettings.objectiveAngle = new Double(tfObjectiveAngle.getText());
            shearingSettings.useYshear = cbUseYshear.isSelected();

            //shearingSettings.shearingFactorX = (-1.0)*shearingSettings.stepSize * shearingSettings.magnification *(1.0/shearingSettings.cameraPixelsize)* sin(shearingSettings.objectiveAngle*PI/180.0);
        // Calculate shearingFactor in X and Y :
        double shearFactor =  calculateShearFactor(shearingSettings);
        if (shearingSettings.useYshear) {
                shearingSettings.shearingFactorY = shearFactor;
                shearingSettings.shearingFactorX = 0.0;
        }else {
            shearingSettings.shearingFactorX = -shearFactor;
            shearingSettings.shearingFactorY = 0.0;
        }
        if (shearingSettings.backwardStackAcquisition) {
                shearingSettings.shearingFactorX = -shearFactor;
                shearingSettings.shearingFactorY = -shearFactor;
        }

//        }
//        else {
//            shearingSettings = new ShearingSettings();
//        }
    }

    private double calculateShearFactor(ShearingSettings settings){
        double shearFactor = shearingSettings.stepSize * shearingSettings.magnification *(1.0/shearingSettings.cameraPixelsize)* Math.sin(shearingSettings.objectiveAngle* Math.PI/180.0);
        return shearFactor;
    }
}
