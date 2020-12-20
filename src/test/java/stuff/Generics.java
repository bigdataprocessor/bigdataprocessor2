package stuff;

import clojure.lang.Obj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Generics
{
	public static void main( String[] args )
	{
		List< Object > objects = new ArrayList<>();
		objects.add( "aaaa" );
		objects.add( 1 );

	}

	public < T extends Object > void add ( T a )
	{

    }
}
