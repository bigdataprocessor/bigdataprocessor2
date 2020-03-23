package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.log.progress.ProgressListener;
import de.embl.cba.bdp2.record.MacroRecorder;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import ij.IJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

public class SaveDialog< R extends RealType< R > & NativeType< R > >  extends JFrame implements ActionListener
{
    private final BdvImageViewer viewer;
    private final Image< R > inputImage;
    private final SavingSettings.FileType fileType;

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
    private static final JTextField tfDirectory = new JTextField("", 50);

    private final String SAVE = "Save";
    protected final JButton save = new JButton(SAVE);
    private final String STOP_SAVING = "Stop Saving";
    private final JButton stopSaving = new JButton(STOP_SAVING);
    protected final JLabel MESSAGE = new JLabel("");
    protected final String MESSAGE_SAVE_INTERRUPTED ="Saving Interrupted!";
    protected final String MESSAGE_SAVE_FINISHED ="Saving Completed!";
    protected JProgressBar progressBar;

    private ImgSaver saver;
    private SavingSettings savingSettings;
    private JPanel mainPanel;
    private ArrayList< JPanel > panels;

    public SaveDialog( BdvImageViewer viewer, SavingSettings.FileType fileType )
    {
        this.viewer = viewer;
        this.inputImage = viewer.getImage();
        this.fileType = fileType;

        createDialog();
    }


    public void createDialog()
    {
        JTabbedPane menu = new JTabbedPane();
        mainPanel = new JPanel();
        panels = new ArrayList<>();
        int panelIndex = 0;

        mainPanel.add(new JPanel());
        mainPanel.setLayout(new BoxLayout( mainPanel, BoxLayout.PAGE_AXIS));

        panels.add(new JPanel());
        panels.get(panelIndex).add( tfDirectory );
        final JButton volumesPathSelectionButton = new JButton( "Folder" );
        volumesPathSelectionButton.addActionListener( e ->
                tfDirectory.setText( IJ.getDirectory( "Directory" ) ) );
        panels.get(panelIndex).add(volumesPathSelectionButton);
        mainPanel.add( panels.get(panelIndex++));

        panels.add(new JPanel());
        cbSaveVolume.setSelected(true);
        panels.get(panelIndex).add(cbSaveVolume);
        cbSaveProjection.setSelected(true);
        panels.get(panelIndex).add(cbSaveProjection);

//        panels.get(j).add(tfVolumesFilePath);
//        final JButton volumesPathSelectionButton = new JButton( "Folder" );
//        volumesPathSelectionButton.addActionListener( e ->
//            tfVolumesFilePath.setText( IJ.getDirectory( "Volumes" ) ) );
//        panels.get(j).add(volumesPathSelectionButton);
//        mainPanels.get(k).add(panels.get(j++));

//        panels.add(new JPanel());
//        panels.get(j).add(cbSaveProjection);
//        panels.get(j).add(tfProjectionsFilePath);
//        final JButton projectionsPathSelectionButton = new JButton( "Folder" );
//        projectionsPathSelectionButton.addActionListener( e ->
//                tfProjectionsFilePath.setText( IJ.getDirectory( "Projections" ) ) );
//        panels.get(j).add(projectionsPathSelectionButton);
//        mainPanels.get(k).add(panels.get(j++));
        mainPanel.add( panels.get(panelIndex++));

        panelIndex = addTiffCompressionPanel( panelIndex );

        panels.add(new JPanel());
        panels.get(panelIndex).add(new JLabel("I/O Threads"));
        panels.get(panelIndex).add( tfNumIOThreads );
        mainPanel.add( panels.get(panelIndex++));

        panels.add(new JPanel());
        panels.get(panelIndex).add(new JLabel("Processing Threads"));
        panels.get(panelIndex).add( tfNumProcessingThreads );
        mainPanel.add( panels.get(panelIndex++));

        panels.add(new JPanel());
        save.setActionCommand(SAVE);
        save.addActionListener(this);
        panels.get(panelIndex).add(save);
        stopSaving.setActionCommand(STOP_SAVING);
        stopSaving.addActionListener(this);
        panels.get(panelIndex).add(stopSaving);
        mainPanel.add( panels.get(panelIndex++));

        panels.add(new JPanel());
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        panels.get(panelIndex).add(progressBar);
        mainPanel.add( panels.get(panelIndex++));

        panels.add(new JPanel());
        panels.get(panelIndex).add(MESSAGE);
        mainPanel.add( panels.get(panelIndex++));

        menu.add( "Save as " + fileType.toString(), mainPanel);
        add(menu);
        pack();
    }

    public int addTiffCompressionPanel( int j )
    {
        if ( fileType.equals( SavingSettings.FileType.TIFF_VOLUMES ) ||
             fileType.equals( SavingSettings.FileType.TIFF_PLANES ) )
        {
            panels.add( new JPanel() );
            panels.get( j ).add( new JLabel( "Tiff Compression" ) );
            panels.get( j ).add( comboCompression );
            //panels.get(j).add(new JLabel("Rows per Strip [ny]"));
            //panels.get(j).add(tfRowsPerStrip);
            mainPanel.add( panels.get( j++ ) );
        }
        return j;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        SwingUtilities.invokeLater( () -> {

            if ( e.getActionCommand().equals( SAVE ) )
            {
                save();
                recordMacro();
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
        savingSettings =  getSavingSettings();
        progressBar.setVisible( true );
        pack();
        save.setEnabled( false );
        BigDataProcessor2.generalThreadPool.submit( () -> {
            this.saver = BigDataProcessor2.saveImage(
                    viewer.getImage(),
                    savingSettings,
                    progressBar() );

            saver.addProgressListener( new LoggingProgressListener( "Frames saved" ) );
        } );
    }

    private SavingSettings getSavingSettings()
    {
        SavingSettings savingSettings = new SavingSettings();

        savingSettings.fileType = fileType;

        savingSettings.compression = ( String ) comboCompression.getSelectedItem();
        savingSettings.rowsPerStrip = Integer.parseInt( tfRowsPerStrip.getText() );
        savingSettings.saveVolumes = cbSaveVolume.isSelected();
        savingSettings.volumesFilePathStump = tfDirectory.getText() + File.separator + "volumes" + File.separator + "volume";

        savingSettings.saveProjections = cbSaveProjection.isSelected();
        savingSettings.projectionsFilePathStump = tfDirectory.getText() + File.separator + "projections" + File.separator + "projection";
        savingSettings.numIOThreads = Integer.parseInt( tfNumIOThreads.getText() );
        savingSettings.numProcessingThreads = Integer.parseInt( tfNumProcessingThreads.getText() );

        savingSettings.voxelSpacing = viewer.getImage().getVoxelSpacing();
        savingSettings.voxelUnit = viewer.getImage().getVoxelUnit();

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

    private void recordMacro()
    {
        final MacroRecorder recorder = new MacroRecorder( SaveAdvancedCommand.COMMAND_NAME, inputImage );

        recorder.addOption( SaveAdvancedCommand.DIRECTORY_PARAMETER, tfDirectory.getText() );
        recorder.addOption( SaveAdvancedCommand.NUM_IO_THREADS_PARAMETER, savingSettings.numIOThreads );
        recorder.addOption( SaveAdvancedCommand.NUM_PROCESSING_THREADS_PARAMETER, savingSettings.numProcessingThreads );
        recorder.addOption( SaveAdvancedCommand.SAVE_FILE_TYPE_PARAMETER, savingSettings.fileType.toString());
        recorder.addOption( SaveAdvancedCommand.SAVE_PROJECTIONS_PARAMETER, savingSettings.saveProjections);
        recorder.addOption( SaveAdvancedCommand.SAVE_VOLUMES_PARAMETER, savingSettings.saveVolumes);
        recorder.addOption( SaveAdvancedCommand.TIFF_COMPRESSION_PARAMETER, savingSettings.compression);

        recorder.record();
    }
}

