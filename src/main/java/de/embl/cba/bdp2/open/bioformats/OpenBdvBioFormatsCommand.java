package de.embl.cba.bdp2.open.bioformats;

import ch.epfl.biop.bdv.bioformats.BioFormatsMetaDataHelper;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.open.AbstractOpenCommand;
import de.embl.cba.bdp2.open.AbstractOpenFileCommand;
import loci.common.DebugTools;
import loci.formats.*;
import loci.formats.meta.IMetadata;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenBdvBioFormatsCommand.COMMAND_FULL_NAME, initializer = "iniMessage")
public class OpenBdvBioFormatsCommand< R extends RealType< R > & NativeType< R >> extends AbstractOpenFileCommand< R >
{
    public static final String COMMAND_NAME = "Open Bio-Formats...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;
    public static final String FILE_INFO = "File info: ";

    @Parameter( visibility = ItemVisibility.MESSAGE, persist = false )
    String infoFile;

    @Parameter( label = "Series index", min = "0", callback = "setSeriesCallBack", persist = false )
    private int seriesIndex = 0;

    private int numberOfSeries=1;

    @Parameter(visibility = ItemVisibility.MESSAGE, persist = false)
    String infoSeries;

    @Override
    public void run() {
        SwingUtilities.invokeLater( () ->  {
            String filePath = file.getAbsolutePath();
            Logger.info( "# " + COMMAND_NAME);
            Logger.info( "Opening file: " + filePath);

            outputImage = BigDataProcessor2.openBioFormats( filePath, seriesIndex );
            handleOutputImage( true, false );
            recordJythonCall();
        });
    }

    public void recordJythonCall()
    {
        MacroRecorder recorder = new MacroRecorder( outputImage );
        recorder.recordImportStatements( true );
        recorder.setAPIFunctionName( "openBioFormats" );
        recorder.addAPIFunctionParameter( recorder.quote( file.getAbsolutePath() ) );
        recorder.addAPIFunctionParameter( String.valueOf( seriesIndex ) );
        recorder.record();
    }

    Map<Integer, SeriesInfo> seriesInfoMap = new HashMap<>();

    @Override
    public void setFileCallBack()
    {
        new Thread( () ->
        {
            if ( file != null )
            {
                DebugTools.setRootLevel( "OFF" );

                // TODO: below line does not render, probably some UI thread stuff...
                infoFile = FILE_INFO + "Parsing file, please wait...";

                if ( ! file.exists() )
                {
                    infoFile = FILE_INFO + " File does not exist!";
                    setSeriesCallBack();
                    return;
                }

                IFormatReader readerIdx = new ImageReader();
                readerIdx.setFlattenedResolutions( false );
                final IMetadata omeMetaOmeXml = MetadataTools.createOMEXMLMetadata();
                readerIdx.setMetadataStore( omeMetaOmeXml );
                try
                {
                    readerIdx.setId( file.getAbsolutePath() );
                    this.numberOfSeries = readerIdx.getSeriesCount();
                    infoFile = FILE_INFO + "File contains " + numberOfSeries + " series.";
                    seriesInfoMap = new HashMap<>();
                    for ( int iSeries = 0; iSeries < numberOfSeries; iSeries++ )
                    {
                        SeriesInfo si = new SeriesInfo();
                        seriesInfoMap.put( iSeries, si );
                        readerIdx.setSeries( iSeries );
                        si.sizeX = readerIdx.getSizeX();
                        si.sizeY = readerIdx.getSizeY();
                        si.sizeZ = readerIdx.getSizeZ();
                        si.sizeT = readerIdx.getSizeT();
                        si.sizeC = readerIdx.getSizeC();
                    }
                    readerIdx.close();
                }
                catch ( Exception e )
                {
                    infoFile = FILE_INFO + "File could not be parsed!";
                }
            }
            else
            {
                infoFile = FILE_INFO + "Please select a file!"; // Default value on initialization
            }
            setSeriesCallBack();
        }).start();
    }

    public void setSeriesCallBack() {
        if (seriesIndex>=numberOfSeries) {
            infoSeries = "ERROR : This series does not exists";
        } else {
            infoSeries = "Series "+seriesIndex+": "+seriesInfoMap.get(seriesIndex);
        }
    }

    public void iniMessage() {
        setFileCallBack();
        setSeriesCallBack();
    }

    public static class SeriesInfo {

        int sizeX;
        int sizeY;
        int sizeZ;
        int sizeC;
        int sizeT;

        public String toString() {
            return " [X:"+sizeX+",Y:"+sizeY+",Z:"+sizeZ+"] "+sizeC+" channels, "+sizeT+" timepoints";
        }
    }
}