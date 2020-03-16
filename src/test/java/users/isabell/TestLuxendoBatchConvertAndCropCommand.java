package users.isabell;

import de.embl.cba.bdp2.scijava.command.LuxendoBatchConvertAndCropCommand;

import java.io.File;
import java.util.ArrayList;

public class TestLuxendoBatchConvertAndCropCommand
{
	public static void main( String[] args )
	{
		final LuxendoBatchConvertAndCropCommand command = new LuxendoBatchConvertAndCropCommand<>();

		final ArrayList< File > directories = new ArrayList<>();
		directories.add( new File( "/Volumes/t2ellenberg4/Isabell/inviSPIM/Trial_dual_color_live_20191106/stack_0_channel_0") );
		command.process( directories );
	}
}
