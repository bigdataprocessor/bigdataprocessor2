package de.embl.cba.bigDataTools2.dataStreamingGUI;

import de.embl.cba.bigDataTools2.bigDataTrackerGUI.BigDataTrackerGUI;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.viewers.ImageViewer;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.view.Views;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class BdvMenus extends JMenu implements ActionListener { //TODO: change name as Menus --ashis

    private SaveSelectMenu saveSelectMenu;
    public CropSelectMenu cropSelectMenu;
    private OthersMenu othersMenu;
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
            DataStreamingTools.executorService.submit(() -> {
                DataStreamingTools.selectedImageViewer = imageViewer;
                SaveMenuDialog saveMenuDialog = new SaveMenuDialog();
                saveMenuDialog.setVisible(true);
                saveMenuDialog.pack();
            });
        }else if (e.getActionCommand().equalsIgnoreCase("Oblique View")) {
            DataStreamingTools.executorService.submit(() -> {
                DataStreamingTools.selectedImageViewer = imageViewer;
                ObliqueMenuDialog obliqueMenuDialog = new ObliqueMenuDialog();
                obliqueMenuDialog.setVisible(true);
                obliqueMenuDialog.pack();
            });
        }else if(e.getActionCommand().equalsIgnoreCase("Begin Crop")){
            final RandomAccessibleInterval rai = imageViewer.getRai();
            DataStreamingTools.executorService.submit(() -> {
                FinalInterval interval = imageViewer.get5DIntervalFromUser();
                RandomAccessibleInterval croppedRAI = Views.interval(rai, interval);
                ImageViewer newImageView =  imageViewer.newImageViewer( croppedRAI,FileInfoConstants.CROPPED_STREAM_NAME);
                newImageView.show();
                BdvMenus menus = new BdvMenus();
                menus.cropSelectMenu.cropSection = interval;
                newImageView.addMenus(menus);
                imageViewer.replicateViewerContrast(newImageView);
            });
        }else if(e.getActionCommand().equalsIgnoreCase("Show in ImageJ Viewer")){
            ImageJFunctions.show(imageViewer.getRai(), DataStreamingTools.executorService);
        }else if(e.getActionCommand().equalsIgnoreCase("Big Data Tracker")){
           DataStreamingTools.executorService.submit(() -> {
                BigDataTrackerGUI bigDataTrackerGUI = new BigDataTrackerGUI(imageViewer);
                bigDataTrackerGUI.showDialog();
//            CommandService commandService = LazyLoadingCommand.uiService.getContext().service(CommandService.class);
//            commandService.run( BigDataTrackerUICommand.class, true, "imageViewer", imageViewer );

            });
        }
    }
}
