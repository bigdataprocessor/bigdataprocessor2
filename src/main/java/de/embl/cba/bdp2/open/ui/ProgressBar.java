package de.embl.cba.bdp2.open.ui;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.log.progress.ProgressListener;
import de.embl.cba.bdp2.open.OpenFileType;
import de.embl.cba.bdp2.record.MacroRecorder;
import de.embl.cba.bdp2.save.SaveAdvancedCommand;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.utils.DimensionOrder;
import ij.IJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ProgressBar implements ProgressListener
{
    private final String STOP = "Stop";
    private final JButton stopButton = new JButton( STOP );
    protected final JLabel MESSAGE = new JLabel("");
    protected JProgressBar progressBar;

    private JPanel panel;
    private JDialog dialog;
    private int previousProgress = 0;
    private JFrame frame;
    private String title;

    public ProgressBar( String title )
    {
        this.title = title;
        showDialog();
    }

    private void showDialog()
    {
        frame = new JFrame( title );
        progressBar = new JProgressBar( 0, 100 );
        progressBar.setValue( 0 );
        progressBar.setStringPainted( true );
        progressBar.setBounds(40,40,160,30);
        frame.add( progressBar );
        frame.setLocation(
                MouseInfo.getPointerInfo().getLocation().x - 50,
                MouseInfo.getPointerInfo().getLocation().y - 50
                );
        frame.setSize( 250,150 );
        frame.setLayout( null );
        frame.setVisible( true );
    }

    public void close()
    {
        frame.setVisible( false );
    }

    @Override
    public void progress( final long current, final long total )
    {
        SwingUtilities.invokeLater( () -> {
            int progressPercent = ( int ) ( ( current * 100 ) / total );
            if ( progressPercent > previousProgress )
            {
                progressBar.setValue( progressPercent );
                previousProgress = progressPercent;
            }
        } );
    }
}

