package de.embl.cba.bigDataTools2.dataStreamingGUI;

import de.embl.cba.bigDataTools2.bigDataTrackerGUI.BigDataTrackerGUI;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.viewers.ImageViewer;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class BdvMenus extends JMenu implements ActionListener { //TODO: change name as Menus --ashis

    private final SaveSelectMenu saveSelectMenu;
    private final CropSelectMenu cropSelectMenu;
    private final OthersMenu othersMenu;
    private ImageViewer imageViewer;

    public BdvMenus(){
        saveSelectMenu = new SaveSelectMenu(this);
        cropSelectMenu = new CropSelectMenu(this);
        othersMenu = new OthersMenu(this);
    }

    public void setImageViewer(ImageViewer viewer){
        this.imageViewer = viewer;
    }

    public List< JMenu > getMenus(){ //Add new menu items here.
        List<JMenu> jMenuList = new ArrayList<>();
        jMenuList.add(saveSelectMenu);
        jMenuList.add(cropSelectMenu);
        jMenuList.add(othersMenu);
        return jMenuList;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("Save As")) {
            BigDataConverter.executorService.submit(() -> {
                SaveMenuDialog saveMenuDialog = new SaveMenuDialog(imageViewer);
                saveMenuDialog.setVisible(true);
                saveMenuDialog.pack();
            });
        }else if (e.getActionCommand().equalsIgnoreCase("Oblique View")) {
            BigDataConverter.executorService.submit(() -> {
                ObliqueMenuDialog obliqueMenuDialog = new ObliqueMenuDialog(imageViewer);
                obliqueMenuDialog.setVisible(true);
            });
        }else if(e.getActionCommand().equalsIgnoreCase("Begin Crop")){
            final RandomAccessibleInterval rai = imageViewer.getRai();
            BigDataConverter.executorService.submit(() -> {
                FinalInterval interval = imageViewer.get5DIntervalFromUser();
                RandomAccessibleInterval croppedRAI = BigDataConverter.crop(rai,interval);
                ImageViewer newImageView = imageViewer.newImageViewer( croppedRAI,FileInfoConstants.CROPPED_STREAM_NAME);
                newImageView.show();
                BdvMenus menus = new BdvMenus();
                menus.cropSelectMenu.cropSection = interval;
                newImageView.addMenus(menus);
                imageViewer.replicateViewerContrast(newImageView);
            });
        }else if(e.getActionCommand().equalsIgnoreCase("Show in ImageJ Viewer")){
            ImageJFunctions.show(imageViewer.getRai(), BigDataConverter.executorService);
        }else if(e.getActionCommand().equalsIgnoreCase("Big Data Tracker")){
           BigDataConverter.executorService.submit(() -> {
                BigDataTrackerGUI bigDataTrackerGUI = new BigDataTrackerGUI(imageViewer);
                bigDataTrackerGUI.showDialog();
//            CommandService commandService = LazyLoadingCommand.uiService.getContext().service(CommandService.class);
//            commandService.run( BigDataTrackerUICommand.class, true, "imageViewer", imageViewer );

            });
        }
    }
}
