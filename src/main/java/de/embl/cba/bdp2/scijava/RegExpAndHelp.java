package de.embl.cba.bdp2.scijava;

public class RegExpAndHelp implements StringAndHelp
{
	@Override
	public String getString()
	{
		return "Hello";
	}

	@Override
	public void showHelp()
	{
		System.out.println("Help");
	}
}
