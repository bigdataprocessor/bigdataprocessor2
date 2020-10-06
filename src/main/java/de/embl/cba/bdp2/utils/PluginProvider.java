package de.embl.cba.bdp2.utils;

import org.scijava.AbstractContextual;
import org.scijava.InstantiableException;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.SciJavaPlugin;

import java.util.*;


/**
 * Copied from Mastodon
 *
 *
 * @param <K>
 */
public class PluginProvider< K extends SciJavaPlugin > extends AbstractContextual
{
	private final Class< K > cl;

	private List< String > keys;

	private List< String > visibleKeys;

	private List< String > disabled;

	private Map< String, String > descriptions;

	private List< Class< ? extends K > > classes;

	private Map< String, K > instances;

	public PluginProvider( final Class< K > cl )
	{
		this.cl = cl;
	}

	private void register()
	{
		final PluginService pluginService = context().getService( PluginService.class );
		final List< PluginInfo< K > > infos = pluginService.getPluginsOfType( cl );

		this.keys = new ArrayList<>( infos.size() );
		this.visibleKeys = new ArrayList<>( infos.size() );
		this.disabled = new ArrayList<>( infos.size() );
		this.descriptions = new HashMap<>();
		this.classes = new ArrayList<>( infos.size() );
		this.instances = new HashMap<>( infos.size() );

		for ( final PluginInfo< K > info : infos )
		{
			final String key = info.getName();
			final String description = info.getDescription();
			try
			{
				info.loadClass();
				classes.add( info.getPluginClass() );
				descriptions.put( key, description );
				if ( !info.isEnabled() )
				{
					disabled.add( key );
					continue;
				}
				keys.add( key );
				if ( info.isVisible() )
					visibleKeys.add( key );
				instances.put( key, info.createInstance() );
			}
			catch ( final InstantiableException e )
			{
				e.printStackTrace();
			}
		}
	}

	public K getInstance(final String name)
	{
		if (null == instances)
			register();
		return instances.get( name );
	}

	public List< Class< ? extends K > > getClasses()
	{
		if ( null == classes )
			register();
		return Collections.unmodifiableList( classes );
	}

	public List< String > getNames()
	{
		if ( null == keys )
			register();
		return Collections.unmodifiableList( keys );
	}

	public List< String > getVisibleNames()
	{
		if ( null == visibleKeys )
			register();
		return Collections.unmodifiableList( visibleKeys );
	}

	public List< String > getDisabled()
	{
		if ( null == disabled )
			register();
		return Collections.unmodifiableList( disabled );
	}

	public Map< String, String > getDescriptions()
	{
		if ( null == descriptions )
			register();
		return Collections.unmodifiableMap( descriptions );
	}

	public String echo()
	{
		final StringBuilder str = new StringBuilder();
		str.append( "Discovered modules for " + cl.getSimpleName() + ":\n" );
		str.append( "  Enabled & visible:" );
		if ( getVisibleNames().isEmpty() )
		{
			str.append( " none.\n" );
		}
		else
		{
			str.append( '\n' );
			for ( final String key : getVisibleNames() )
			{
				str.append( "  - " + key + '\n' );
			}
		}
		str.append( "  Enabled & not visible:" );
		final List< String > invisibleKeys = getNames();
		invisibleKeys.removeAll( getVisibleNames() );
		if ( invisibleKeys.isEmpty() )
		{
			str.append( " none.\n" );
		}
		else
		{
			str.append( '\n' );
			for ( final String key : invisibleKeys )
			{
				str.append( "  - " + key + '\n' );
			}
		}
		str.append( "  Disabled:" );
		if ( getDisabled().isEmpty() )
		{
			str.append( " none.\n" );
		}
		else
		{
			str.append( '\n' );
			for ( final String cn : getDisabled() )
			{
				str.append( "  - " + cn + '\n' );
			}
		}
		return str.toString();
	}
}

