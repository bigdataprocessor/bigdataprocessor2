package de.embl.cba.bigDataTools2.dataStreamingGUI;

import de.embl.cba.bigDataTools2.bigDataTrackerGUI.BigDataTrackerGUI;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.utils.Utils;
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
    private final OthersMenu othersMenu;
    private final ProcessMenu processMenu;
    private ImageViewer imageViewer;

    public BdvMenus(){
        saveSelectMenu = new SaveSelectMenu(this);
        othersMenu = new OthersMenu(this);
        processMenu = new ProcessMenu(this);
    }

    public void setImageViewer(ImageViewer viewer){
        this.imageViewer = viewer;
    }

    public List< JMenu > getMenus(){ //Add new menu items here.
        List<JMenu> jMenuList = new ArrayList<>();
        jMenuList.add(saveSelectMenu);
        jMenuList.add(processMenu);
        jMenuList.add(othersMenu);
        return jMenuList;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase(UIDisplayConstants.SAVE_AS_MENU_DISPLAY_TEXT)) {
            BigDataConverter.executorService.submit(() -> {
                SaveMenuDialog saveMenuDialog = new SaveMenuDialog(imageViewer);
                saveMenuDialog.setVisible(true);
            });
        }else if (e.getActionCommand().equalsIgnoreCase(UIDisplayConstants.OBLIQUE_MENU_DISPLAY_TEXT)) {
            BigDataConverter.executorService.submit(() -> {
                ObliqueMenuDialog obliqueMenuDialog = new ObliqueMenuDialog(imageViewer);
                obliqueMenuDialog.setVisible(true);
            });
        }else if(e.getActionCommand().equalsIgnoreCase(UIDisplayConstants.CROP_MENU_DISPLAY_TEXT)){
            final RandomAccessibleInterval rai = imageViewer.getRai();
            BigDataConverter.executorService.submit(() -> {
                FinalInterval interval = imageViewer.get5DIntervalFromUser();
                RandomAccessibleInterval croppedRAI = BigDataConverter.crop(rai,interval);
                ImageViewer newImageViewer = imageViewer.newImageViewer();
                newImageViewer.show(
                        croppedRAI,
                        FileInfoConstants.CROPPED_STREAM_NAME,
                        imageViewer.getVoxelSize(),
                        imageViewer.getCalibrationUnit(),
                        false );
                BdvMenus menus = new BdvMenus();
                newImageViewer.addMenus( menus );
                imageViewer.replicateViewerContrast( newImageViewer );
            });
        }else if(e.getActionCommand().equalsIgnoreCase(UIDisplayConstants.IMAGEJ_VIEW_MENU_DISPLAY_TEXT)){
            ImageJFunctions.show(imageViewer.getRai(), BigDataConverter.executorService);
        }else if(e.getActionCommand().equalsIgnoreCase(UIDisplayConstants.BIG_DATA_TRACKER_MENU_DISPLAY_TEXT)){
           BigDataConverter.executorService.submit(() -> {
                BigDataTrackerGUI bigDataTrackerGUI = new BigDataTrackerGUI(imageViewer);
                bigDataTrackerGUI.showDialog();
//            CommandService commandService = LazyLoadingCommand.uiService.getContext().service(CommandService.class);
//            commandService.run( BigDataTrackerUICommand.class, true, "imageViewer", imageViewer );

            });
        }else if(e.getActionCommand().equalsIgnoreCase(UIDisplayConstants.EIGHT_BIT_MENU_DISPLAY_TEXT)){
            BigDataConverter.executorService.submit(() -> {
                EightBitConverterMenuDialog menuDialog = new EightBitConverterMenuDialog(imageViewer);
                menuDialog.setVisible(true);
            });
        }
    }
}
