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
package de.embl.cba.bdp2;

import de.embl.cba.bdp2.open.bioformats.OpenBDVBioFormatsCommand;
import de.embl.cba.bdp2.open.fileseries.*;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.open.samples.DownloadAndOpenSampleDataCommand;
import de.embl.cba.bdp2.process.cache.ConfigureLazyLoadingCommand;
import de.embl.cba.bdp2.track.ApplyTrackCommand;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.service.ImageViewerService;
import de.embl.cba.bdp2.utils.PluginProvider;
import de.embl.cba.bdp2.viewer.ImageViewer;
import ij.IJ;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BigDataProcessor2Menu extends JMenu
{
    public static final String COMMAND_BDP2_PREFIX = "BDP2 ";

    // Menu items
    public static final String RECORD = "Record...";
    public static final String ABOUT = "About";
    public static final String README = "User Guide";
    public static final String ISSUE = "Report an Issue";
    public static final String CITE = "Cite";
    public static final String LOG = "Configure Logging...";

    // Menu items
    public static final String IMAGEJ_VIEW_MENU_ITEM = "Show in Hyperstack Viewer";
    public static final String REGISTER_VOLUME_SIFT_MENU_ITEM = "Correct Lateral Slice Drift in Volume (SIFT)...";
    public static final String REGISTER_MOVIE_SIFT_MENU_ITEM = "Correct Lateral Frame Drift in Time-lapse (SIFT)...";
    public static final String REGISTER_MOVIE_PHASE_CORRELATION_MENU_ITEM = "Correct Lateral Frame Drift in Time-lapse (X-Corr)...";
    public static final String SAVE_AS_IMARIS_VOLUMES_MENU_ITEM = "Save as Imaris Volumes...";
    public static final String SAVE_AS_TIFF_VOLUMES_MENU_ITEM = "Save as TIFF Volumes...";
    public static final String SAVE_AS_TIFF_PLANES_MENU_ITEM = "Save as TIFF Planes...";
    public static final String SAVE_AS_BDV_XML_HDF5_MENU_ITEM = "Save as BigDataViewer XML/HDF5...";

    public static final String CREATE_TRACK = "Create Track...";

    private final BigDataProcessor2MenuActions menuActions;
    private final ArrayList< JMenu > menus;

    public BigDataProcessor2Menu( BigDataProcessor2MenuActions menuActions )
    {
        this.menuActions = menuActions;
        setText( "BigDataProcessor2" );

        menus = new ArrayList<>();

        final JMenu mainMenu = addMenu( "BDP2" );
        menus.add( mainMenu );
        addMenuItem( mainMenu, ABOUT );
        addMenuItem( mainMenu, README );
        addMenuItem( mainMenu, ISSUE );
        addMenuItem( mainMenu, CITE );

        final JMenu recordMenu = addMenu( "Record" );
        final JMenu openMenu = addMenu( "Open" );
        final JMenu processMenu = addMenu( "Process" );
        final JMenu saveMenu = addMenu( "Save" );
        final JMenu miscMenu = addMenu( "Misc" );

        menus.add( recordMenu );
        addMenuItem( recordMenu, RECORD );

        // TODO: auto-populate using SciJava annotation
        menus.add( openMenu );
        addMenuItem( openMenu, OpenHelpCommand.COMMAND_NAME );
        addMenuItem( openMenu, OpenBDVBioFormatsCommand.COMMAND_NAME );
        addMenuItem( openMenu, OpenFileSeriesCommand.COMMAND_NAME );
        JMenu openPredefinedFileSeriesMenu = new JMenu( "Open Predefined File Series" );
        openMenu.add( openPredefinedFileSeriesMenu );
        addMenuItem( openPredefinedFileSeriesMenu, OpenSingleTIFFVolumeCommand.COMMAND_NAME );
        addMenuItem( openPredefinedFileSeriesMenu, OpenSingleHDF5VolumeCommand.COMMAND_NAME );
        addMenuItem( openPredefinedFileSeriesMenu, OpenEMTIFFPlanesFileSeriesCommand.COMMAND_NAME );
        addMenuItem( openPredefinedFileSeriesMenu, OpenLeicaDSLTIFFPlaneSeriesCommand.COMMAND_NAME );
        addMenuItem( openPredefinedFileSeriesMenu, OpenLuxendoHDF5SeriesCommand.COMMAND_NAME );
        addMenuItem( openPredefinedFileSeriesMenu, OpenViventisTIFFSeriesCommand.COMMAND_NAME );
        addMenuItem( openMenu, DownloadAndOpenSampleDataCommand.COMMAND_NAME );

        menus.add( processMenu );
        final JMenu transformMenu = new JMenu( "Transform" );
        processMenu.add( transformMenu );
        populateProcessMenu( processMenu, miscMenu, transformMenu );

        final JMenu correctDriftMenu = new JMenu( "Correct Drift" );
        processMenu.add( correctDriftMenu );
        addMenuItem( correctDriftMenu, CREATE_TRACK );
        addMenuItem( correctDriftMenu, ApplyTrackCommand.COMMAND_NAME );

        menus.add( saveMenu );
        addMenuItem( saveMenu, SAVE_AS_IMARIS_VOLUMES_MENU_ITEM );
        addMenuItem( saveMenu, SAVE_AS_TIFF_VOLUMES_MENU_ITEM );
        addMenuItem( saveMenu, SAVE_AS_TIFF_PLANES_MENU_ITEM );
        addMenuItem( saveMenu, SAVE_AS_BDV_XML_HDF5_MENU_ITEM );


        menus.add( miscMenu );
        addMenuItem( miscMenu, IMAGEJ_VIEW_MENU_ITEM );
        addMenuItem( miscMenu, LOG );
    }

    private void populateProcessMenu( JMenu processMenu, JMenu miscMenu, JMenu transformMenu )
    {
        PluginProvider< AbstractImageProcessingCommand > pluginProvider = new PluginProvider<>( AbstractImageProcessingCommand.class );
        pluginProvider.setContext( Services.getContext() );
        List< String > names = new ArrayList<>( pluginProvider.getNames() );
        Collections.sort( names );

        for ( String name : names )
        {
            if ( name.contains( "Transform" ) )
            {
                addMenuItemAndProcessingAction( transformMenu, name, pluginProvider.getInstance( name ) );
            }
            else if ( name.equals( ConfigureLazyLoadingCommand.COMMAND_NAME ) )
            {
                addMenuItemAndProcessingAction( miscMenu, name, pluginProvider.getInstance( name ) );
            }
            else
            {
                addMenuItemAndProcessingAction( processMenu, name, pluginProvider.getInstance( name ) );
            }
        }
    }

    public ArrayList< JMenu > getMenus()
    {
        return menus;
    }

    private JMenu addMenu( String name )
    {
        final JMenu menu = new JMenu( name );
        this.add( menu );
        return menu;
    }

    private JMenuItem addMenuItem( String name )
    {
        JMenuItem menuItem = new JMenuItem( name );
        menuItem.addActionListener( menuActions );
        this.add( menuItem );
        return menuItem;
    }

    private JMenuItem addMenuItem( JMenu jMenu, String name )
    {
        JMenuItem jMenuItem = new JMenuItem( name );
        jMenuItem.addActionListener( menuActions );
        jMenu.add( jMenuItem );
        return jMenuItem;
    }

    private JMenuItem addMenuItemAndProcessingAction( JMenu jMenu, String name, AbstractImageProcessingCommand< ? > processingCommand )
    {
        JMenuItem jMenuItem = new JMenuItem( name );
        jMenuItem.addActionListener( e -> {
            new Thread( () ->
            {
                ImageViewer activeViewer = ImageViewerService.getActiveViewer();

                if ( activeViewer == null )
                {
                    IJ.showMessage( "No image selected.\n\nPlease select an image by either\n- clicking on an existing BigDataViewer window, or\n- open a new image using the [ BigDataProcessor2 > Open ] menu." );
                    return;
                }

                processingCommand.showDialog( activeViewer );
            }).start();
        } );
        jMenu.add( jMenuItem );
        return jMenuItem;
    }
}
