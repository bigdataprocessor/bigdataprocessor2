package de.embl.cba.bdp2.open.bioformats;

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
import java.util.concurrent.*;

import static de.embl.cba.bdp2.BigDataProcessor2Menu.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenBdvBioFormatsCommand.COMMAND_FULL_NAME, initializer = "iniMessage")
public class OpenBdvBioFormatsCommand< R extends RealType< R > & NativeType< R >> extends AbstractOpenFileCommand< R >
{
    public static final String COMMAND_NAME = "Open Bio-Formats...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;
    public static final String FILE_INFO = "File info: ";

    @Parameter( visibility = ItemVisibility.MESSAGE, persist = false )
    String infoFile;

    @Parameter( label = "File parse time limit (s)", required = false)
    int parseTimeLimit = 5;

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

    boolean successfulParse = false;

    @Override
    public void setFileCallBack()
    {
        successfulParse = false;
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

            ExecutorService executor = Executors.newSingleThreadExecutor();

            Future<Boolean> future = executor.submit(() -> {
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
                    return true;
                }
                catch ( Exception e )
                {
                    infoFile = FILE_INFO + " File could not be parsed!";
                    return false;
                }
            });

            try {
                //System.out.println("Started..");
                //System.out.println(future.get(parseTimeLimit, TimeUnit.SECONDS));
                //System.out.println("Finished!");
                successfulParse = future.get(parseTimeLimit, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                //System.out.println("Terminated!");
                successfulParse = false;
                infoFile = FILE_INFO + " Parse time out.";
            } catch(Exception e) {
                successfulParse = false;
                infoFile = FILE_INFO + " File could not be parsed!";
            } finally {
                try {
                    readerIdx.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
            executor.shutdownNow();
        }
        else
        {
            infoFile = FILE_INFO + " select a file"; // Default value on initialization
        }
        setSeriesCallBack();
    }

    public void setSeriesCallBack() {
        if ((successfulParse)&&(seriesIndex>=numberOfSeries)) {
            infoSeries = "ERROR : This series does not exists";
        } else {
            if (successfulParse) {
                infoSeries = "Series " + seriesIndex + ": " + seriesInfoMap.get(seriesIndex);
            } else {
                infoSeries = "";
            }
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