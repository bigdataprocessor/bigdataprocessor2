package users.isabell;

import de.embl.cba.bdp2.batch.LuxendoBatchMergeSplitChipCommand;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;
import java.util.ArrayList;

public class FixError02
{
	public static < R extends RealType< R > & NativeType< R > > void main( String[] args )
	{
		final LuxendoBatchMergeSplitChipCommand command = new LuxendoBatchMergeSplitChipCommand();
		final ArrayList< File > directories = new ArrayList< File >();
		directories.add( new File( "/Volumes/cba/exchange/Isabell_Schneider/2019_09_18_Fehlermeldung_2/stack_2_channel_0") );
		command.intervalsString = "896, 46, 1000, 1000, 0; 22, 630, 1000, 1000, 0";
		command.process( directories );
	}
}
