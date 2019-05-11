import net.imglib2.RandomAccess;
import net.imglib2.util.Util;

public class TestGenericArray
{
	public static < T extends Object > void main( String[] args )
	{
		final RandomAccess< T >[] objects = Util.genericArray( 2 );
	}
}
