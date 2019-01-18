package com.github.zol.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Default Properties
 *
 * @author Phillip Kruger (zol@phillip-kruger.com)
 */
public class ZolProperties {

    private static final Logger LOGGER = Logger.getLogger( ZolProperties.class.getName() );

    private final Properties props = new Properties();
    private final String PROPERTIES_FILE_NAME = "zol.properties";

    public void init() {

        // Properties
        try ( InputStream propertiesStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( PROPERTIES_FILE_NAME ) ) {
            if ( propertiesStream != null ) {
                props.load( propertiesStream );
            } else {
                LOGGER.info( "Can not load zol properties [zol.properties]. Make sure you add he zol.properties file in the root of your classpath" );
            }
        } catch ( NullPointerException | IOException ex ) {
            LOGGER.info( "Can not load zol properties [zol.properties] - {0}" + ex.getMessage() );
        }
    }

    public boolean hasProperties() {
        return this.props != null && !this.props.isEmpty();
    }

    public String getProperty( String key, String defaultValue ) {
        if ( hasProperties() && props.containsKey( key ) ) {
            return props.getProperty( key );
        }
        return defaultValue;
    }

    public Map<String, String> getProperties() {
        if ( hasProperties() ) {
            return new HashMap<>( ( Map ) props );
        }
        return null;
    }
}
