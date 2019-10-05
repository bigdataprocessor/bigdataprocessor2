package users.isabell;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.command.BatchMergeSplitChipCommand;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.saving.SavingSettings;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;
import java.util.ArrayList;

public class FixError01
{
	public static < R extends RealType< R > & NativeType< R > > void main( String[] args )
	{
		final BatchMergeSplitChipCommand command = new BatchMergeSplitChipCommand();
		final ArrayList< File > directories = new ArrayList< File >();
		directories.add( new File( "/Volumes/cba/exchange/Isabell_Schneider/2019_09_18_Fehlermeldung_1/stack_3_channel_0") );
		command.intervalsString = "896, 46, 1000, 1000, 0; 22, 630, 1000, 1000, 0";
		command.compression = SavingSettings.COMPRESSION_ZLIB;
		command.doCrop = false;
		command.process( directories );
	}
}
