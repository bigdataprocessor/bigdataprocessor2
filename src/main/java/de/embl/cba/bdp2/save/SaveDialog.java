package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.log.progress.ProgressListener;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.viewer.ImageViewer;
import ij.IJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

public class SaveDialog< R extends RealType< R > & NativeType< R > > extends JFrame implements ActionListener
{
    private final ImageViewer viewer;
    private final Image< R > inputImage;
    private final SavingSettings.SaveFileType saveFileType;

    private static SavingSettings defaults = SavingSettings.getDefaults();

    private static final JCheckBox cbSaveVolume = new JCheckBox("Save Volume data");
    private static final JComboBox comboCompression = new JComboBox(
            new String[]{
                    SavingSettings.COMPRESSION_NONE,
                    SavingSettings.COMPRESSION_ZLIB,
                    SavingSettings.COMPRESSION_LZW
    } );
    private static final JCheckBox cbSaveProjection = new JCheckBox("Save Projections");

    private static final JComboBox comboChannelNames = new JComboBox(
            new String[]{
                    SavingSettings.CHANNEL_INDEXING,
                    SavingSettings.CHANNEL_NAMES
            }
    );
    private static final JTextField tfTStart = new JTextField("0", 3);
    private static final JTextField tfTEnd = new JTextField("0", 3);

    private static final JTextField tfRowsPerStrip = new JTextField("10", 3);
    private static final JTextField tfNumIOThreads = new JTextField("" + defaults.numIOThreads, 2);
    private static final JTextField tfNumProcessingThreads = new JTextField( "" + defaults.numProcessingThreads, 2);
    private static final JTextField tfDirectory = new JTextField("", 50);

    private final String SAVE = "Save";
    protected final JButton saveButton = new JButton(SAVE);

    private final String RECORD = "Record Only";
    protected final JButton recordButton = new JButton(RECORD);

    private final String STOP_SAVING = "Stop Saving";
    private final JButton stopSaving = new JButton(STOP_SAVING);
    protected final JLabel MESSAGE = new JLabel("");
    protected final String MESSAGE_SAVE_INTERRUPTED ="Saving Interrupted!";
    protected final String MESSAGE_SAVE_FINISHED ="Saving Completed!";
    protected JProgressBar progressBar;

    private ImageSaver saver;
    private SavingSettings savingSettings;
    private JPanel mainPanel;
    private ArrayList< JPanel > panels;

    public SaveDialog( ImageViewer viewer, SavingSettings.SaveFileType saveFileType )
    {
        this.viewer = viewer;
        this.inputImage = viewer.getImage();
        this.saveFileType = saveFileType;

        createDialog();
    }

    private void createDialog()
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
        volumesPathSelectionButton.addActionListener( e -> tfDirectory.setText( IJ.getDirectory( "Directory" ) ) );
        panels.get(panelIndex).add(volumesPathSelectionButton);
        mainPanel.add( panels.get(panelIndex++));

        panels.add(new JPanel());
        cbSaveVolume.setSelected(true);
        panels.get(panelIndex).add(cbSaveVolume);
        cbSaveProjection.setSelected(true);
        panels.get(panelIndex).add(cbSaveProjection);
        mainPanel.add( panels.get(panelIndex++));

        if ( saveFileType.equals( SavingSettings.SaveFileType.TIFF_VOLUMES ) )
        {
            panelIndex = addChannelNamingSchemeChoice( panelIndex );
        }

        panels.add( new JPanel() );
        panels.get( panelIndex ).add( new JLabel( "From time frame (zero-based) " ) );
        panels.get( panelIndex ).add( tfTStart );
        panels.get( panelIndex ).add( new JLabel( " to (inclusive) " ) );
        tfTEnd.setText( Long.toString( inputImage.getDimensionsXYZCT()[ DimensionOrder.T ] - 1 ) );
        panels.get( panelIndex ).add( tfTEnd );
        mainPanel.add( panels.get( panelIndex++ ) );


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

        panelIndex = addTiffCompressionPanel( panelIndex );

        if ( ( saveFileType.equals( SavingSettings.SaveFileType.TIFF_PLANES ) ||
                saveFileType.equals( SavingSettings.SaveFileType.TIFF_VOLUMES ) ) )
        {
            panels.add( new JPanel() );
            panels.get( panelIndex ).add( new JLabel( "I/O Threads" ) );
            panels.get( panelIndex ).add( tfNumIOThreads );
            mainPanel.add( panels.get( panelIndex++ ) );
        }
        else
        {
            // either input or output format are hdf5
            tfNumIOThreads.setText( "1" );
        }


        panels.add(new JPanel());
        panels.get(panelIndex).add(new JLabel("Processing Threads"));
        panels.get(panelIndex).add( tfNumProcessingThreads );
        mainPanel.add( panels.get(panelIndex++));

        panels.add(new JPanel());

        saveButton.setActionCommand(SAVE);
        saveButton.addActionListener(this);
        panels.get(panelIndex).add( saveButton );

        recordButton.setActionCommand(RECORD);
        recordButton.addActionListener(this);
        panels.get(panelIndex).add( recordButton );

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

        menu.add( "Save as " + saveFileType.toString(), mainPanel);
        add(menu);
        pack();
    }

    public int addChannelNamingSchemeChoice( int panelIndex )
    {
        panels.add( new JPanel() );
        panels.get( panelIndex ).add( new JLabel( "Channel naming scheme" ) );
        panels.get( panelIndex ).add( comboChannelNames );
        mainPanel.add( panels.get( panelIndex++ ) );
        return panelIndex;
    }

    public int addTiffCompressionPanel( int panelIndex )
    {
        if ( saveFileType.equals( SavingSettings.SaveFileType.TIFF_VOLUMES ) ||
             saveFileType.equals( SavingSettings.SaveFileType.TIFF_PLANES ) )
        {
            panels.add( new JPanel() );
            panels.get( panelIndex ).add( new JLabel( "Tiff Compression" ) );
            panels.get( panelIndex ).add( comboCompression );
            //panels.get(j).add(new JLabel("Rows per Strip [ny]"));
            //panels.get(j).add(tfRowsPerStrip);
            mainPanel.add( panels.get( panelIndex++ ) );
        }
        return panelIndex;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {

        SwingUtilities.invokeLater( () -> {

            if ( e.getActionCommand().equals( SAVE ) )
            {
                save();
                recordMacro();
            }
            else if ( e.getActionCommand().equals( RECORD ) )
            {
                // only record the macro command, useful for batch analysis
                savingSettings = getSavingSettings();
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
        saveButton.setEnabled( true );
        progressBar.setVisible( false );
        MESSAGE.setText( MESSAGE_SAVE_INTERRUPTED );
        pack();
    }

    public void save()
    {
        MESSAGE.setText( null );
        savingSettings = getSavingSettings();
        progressBar.setVisible( true );
        pack();
        saveButton.setEnabled( false );
        BigDataProcessor2.threadPool.submit( () -> {
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
        savingSettings.saveFileType = saveFileType;
        savingSettings.compression = (String) comboCompression.getSelectedItem();
        // compress plane wise
        savingSettings.rowsPerStrip = (int) viewer.getImage().getRai().dimension( DimensionOrder.Y );  //Integer.parseInt( tfRowsPerStrip.getText() );

        final String imageName = inputImage.getName();
        savingSettings.saveVolumes = cbSaveVolume.isSelected();
        savingSettings.volumesFilePathStump = tfDirectory.getText() + File.separator + "volumes" + File.separator + imageName;
        savingSettings.saveProjections = cbSaveProjection.isSelected();
        savingSettings.projectionsFilePathStump = tfDirectory.getText() + File.separator + "projections" + File.separator + imageName;
        savingSettings.numIOThreads = Integer.parseInt( tfNumIOThreads.getText() );
        savingSettings.numProcessingThreads = Integer.parseInt( tfNumProcessingThreads.getText() );
        savingSettings.voxelSize = viewer.getImage().getVoxelSize();
        savingSettings.voxelUnit = viewer.getImage().getVoxelUnit();
        savingSettings.channelNamesInSavedImages = (String) comboChannelNames.getSelectedItem();
        savingSettings.tStart = Integer.parseInt( tfTStart.getText() );
        savingSettings.tEnd = Integer.parseInt( tfTEnd.getText() );

        return savingSettings;
    }

    private ProgressListener progressBar()
    {
        return ( current, total ) ->
        {
            int progressPercent = ( int ) ( ( current * 100 ) / total );
            progressBar.setValue( progressPercent );
            if ( progressPercent >= 100 )
            {
                progressBar.setVisible( false );
                saveButton.setEnabled( true );
                progressBar.setValue( 0 );
                pack();
                saver.stopSave();
            }
        };
    }

    private void recordMacro()
    {
        final MacroRecorder recorder = new MacroRecorder( SaveAdvancedCommand.COMMAND_FULL_NAME, inputImage );

        recorder.addCommandParameter( SaveAdvancedCommand.DIRECTORY_PARAMETER, tfDirectory.getText() );
        recorder.addCommandParameter( SaveAdvancedCommand.NUM_IO_THREADS_PARAMETER, savingSettings.numIOThreads );
        recorder.addCommandParameter( SaveAdvancedCommand.NUM_PROCESSING_THREADS_PARAMETER, savingSettings.numProcessingThreads );
        recorder.addCommandParameter( SaveAdvancedCommand.SAVE_FILE_TYPE_PARAMETER, savingSettings.saveFileType.toString());
        recorder.addCommandParameter( SaveAdvancedCommand.SAVE_PROJECTIONS_PARAMETER, savingSettings.saveProjections);
        recorder.addCommandParameter( SaveAdvancedCommand.SAVE_VOLUMES_PARAMETER, savingSettings.saveVolumes);
        recorder.addCommandParameter( SaveAdvancedCommand.TIFF_COMPRESSION_PARAMETER, savingSettings.compression);

        recorder.addCommandParameter( SaveAdvancedCommand.T_START_PARAMETER, savingSettings.tStart);
        recorder.addCommandParameter( SaveAdvancedCommand.T_END_PARAMETER, savingSettings.tEnd);

        recorder.record();
    }
}

