package de.embl.cba.bdp2.ui;

import de.embl.cba.bdp2.progress.LoggingProgressListener;
import de.embl.cba.bdp2.progress.ProgressListener;
import de.embl.cba.bdp2.saving.ImgSaver;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import ij.IJ;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

public class SaveMenuDialog extends JFrame implements ActionListener
{


    private static SavingSettings defaults = SavingSettings.getDefaults();

    private static final JCheckBox cbSaveVolume = new JCheckBox("Save Volume data");
    private static final JComboBox comboCompression = new JComboBox(
            new String[]{
                    SavingSettings.COMPRESSION_NONE,
                    SavingSettings.COMPRESSION_ZLIB,
                    SavingSettings.COMPRESSION_LZW
    } );private static final JCheckBox cbSaveProjection = new JCheckBox("Save Projections");
    private static final JTextField tfRowsPerStrip = new JTextField("10", 3);
    private static final JTextField tfNumIOThreads = new JTextField("" + defaults.numIOThreads, 2);
    private static final JTextField tfNumProcessingThreads = new JTextField( "" + defaults.numProcessingThreads, 2);
    private static final JTextField tfVolumesFilePath = new JTextField("", 50);
    private static final JTextField tfProjectionsFilePath = new JTextField("", 50);

    private final
    JComboBox comboFileTypeForSaving = new JComboBox(new SavingSettings.FileType[]{
            SavingSettings.FileType.TIFF_STACKS,
//            SavingSettings.FileType.HDF5_STACKS, //TODO: implement
            SavingSettings.FileType.IMARIS_STACKS,
            SavingSettings.FileType.TIFF_PLANES });

    private final String SAVE = "Save";
    protected final JButton save = new JButton(SAVE);
    private final String STOP_SAVING = "Stop Saving";
    private final JButton stopSaving = new JButton(STOP_SAVING);
    protected final JLabel MESSAGE = new JLabel("");
    protected final String MESSAGE_SAVE_INTERRUPTED ="Saving Interrupted!";
    protected final String MESSAGE_SAVE_FINISHED ="Saving Completed!";
    protected final JProgressBar progressBar;
    private final BdvImageViewer imageViewer;
    private ImgSaver saver;

    public SaveMenuDialog( BdvImageViewer imageViewer) {
        this.imageViewer = imageViewer;
        JTabbedPane menu = new JTabbedPane();
        ArrayList<JPanel> mainPanels = new ArrayList<>();
        ArrayList<JPanel> panels = new ArrayList<>();
        int j = 0, k = 0;
        mainPanels.add(new JPanel());
        mainPanels.get(k).setLayout(new BoxLayout(mainPanels.get(k), BoxLayout.PAGE_AXIS));
        cbSaveVolume.setSelected(true);
        panels.add(new JPanel());
        panels.get(j).add(new JLabel("File Type:"));
        panels.get(j).add(comboFileTypeForSaving);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(cbSaveVolume);
        panels.get(j).add(tfVolumesFilePath);
        final JButton volumesPathSelectionButton = new JButton( "Folder" );
        volumesPathSelectionButton.addActionListener( e ->
            tfVolumesFilePath.setText( IJ.getDirectory( "Volumes" ) ) );
        panels.get(j).add(volumesPathSelectionButton);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(cbSaveProjection);
        panels.get(j).add(tfProjectionsFilePath);
        final JButton projectionsPathSelectionButton = new JButton( "Folder" );
        projectionsPathSelectionButton.addActionListener( e ->
                tfProjectionsFilePath.setText( IJ.getDirectory( "Projections" ) ) );
        panels.get(j).add(projectionsPathSelectionButton);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel( "Tiff Compression" ));
        panels.get(j).add( comboCompression );
        //panels.get(j).add(new JLabel("Rows per Strip [ny]"));
        //panels.get(j).add(tfRowsPerStrip);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("I/O Threads"));
        panels.get(j).add( tfNumIOThreads );
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(new JLabel("Processing Threads"));
        panels.get(j).add( tfNumProcessingThreads );
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        save.setActionCommand(SAVE);
        save.addActionListener(this);
        panels.get(j).add(save);
        stopSaving.setActionCommand(STOP_SAVING);
        stopSaving.addActionListener(this);
        panels.get(j).add(stopSaving);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        panels.get(j).add(progressBar);
        mainPanels.get(k).add(panels.get(j++));

        panels.add(new JPanel());
        panels.get(j).add(MESSAGE);
        mainPanels.get(k).add(panels.get(j++));

        menu.add("Saving", mainPanels.get(k++));
        add(menu);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        SwingUtilities.invokeLater( () -> {

            if ( e.getActionCommand().equals( SAVE ) )
            {
                save();
            }
            else if ( e.getActionCommand().equals( STOP_SAVING ) )
            {
                stopSave();
            }
        });
    }

    public void stopSave()
    {
        saver.stopSave(); // Don't submit to thread pool. Let the main thread handle it.
        save.setEnabled( true );
        progressBar.setVisible( false );
        MESSAGE.setText( MESSAGE_SAVE_INTERRUPTED );
        pack();
    }

    public void save()
    {
        MESSAGE.setText( null );
        SavingSettings savingSettings = getSavingSettings();
        progressBar.setVisible( true );
        pack();
        save.setEnabled( false );
        BigDataProcessor2.generalThreadPool.submit( () -> {
            this.saver = BigDataProcessor2.saveImage(
                    imageViewer.getImage(),
                    savingSettings,
                    progressBar() );

            saver.addProgressListener( new LoggingProgressListener( "Frames saved" ) );
        } );
    }

    public SavingSettings getSavingSettings()
    {
        SavingSettings savingSettings = new SavingSettings();

        SavingSettings.FileType fileType
                = ( SavingSettings.FileType ) comboFileTypeForSaving.getSelectedItem();
        savingSettings.fileType = fileType;

        savingSettings.compression = ( String ) comboCompression.getSelectedItem();
        savingSettings.rowsPerStrip = Integer.parseInt( tfRowsPerStrip.getText() );

        savingSettings.saveVolumes = cbSaveVolume.isSelected();
        savingSettings.volumesFilePath = tfVolumesFilePath.getText() + File.separator + "volume";

        savingSettings.saveProjections = cbSaveProjection.isSelected();
        savingSettings.projectionsFilePath = tfProjectionsFilePath.getText() + File.separator + "projection";
        savingSettings.numIOThreads = Integer.parseInt( tfNumIOThreads.getText() );
        savingSettings.numProcessingThreads = Integer.parseInt( tfNumProcessingThreads.getText() );

        savingSettings.voxelSpacing = imageViewer.getImage().getVoxelSpacing();
        savingSettings.voxelUnit = imageViewer.getImage().getVoxelUnit();

        return savingSettings;
    }

    private ProgressListener progressBar()
    {
        return ( current, total ) -> {

            int progressPercent = ( int ) ( ( current * 100 ) / total );
            progressBar.setValue( progressPercent );
            if ( progressPercent >= 100 )
            {
                progressBar.setVisible( false );
                save.setEnabled( true );
                progressBar.setValue( 0 );
                pack();
                saver.stopSave();
            }
        };
    }
}

