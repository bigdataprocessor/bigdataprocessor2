package test.script;

import net.imagej.ImageJ;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;

public class TestMacros
{
	public static void main(String... args) throws ScriptException, FileNotFoundException
	{
		new TestMacros().runMacro00();
	}

	@Test
	public void runMacro00() throws FileNotFoundException, ScriptException
	{
		// start ImageJ
		ImageJ ij = new ImageJ();

		ij.script().run(new File("scripts/rename-channels-silent.ijm"), false);
	}
}
