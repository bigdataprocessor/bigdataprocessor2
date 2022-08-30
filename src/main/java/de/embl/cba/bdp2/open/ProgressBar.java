/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.log.progress.ProgressListener;

import javax.swing.*;
import java.awt.*;

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

