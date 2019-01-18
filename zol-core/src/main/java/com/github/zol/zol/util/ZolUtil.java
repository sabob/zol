package com.github.zol.zol.util;

import com.github.zol.zol.ServiceFactory;
import com.github.zol.zol.config.ZolProperties;
import com.github.zol.zol.config.ZolConfig;
import com.github.zol.zol.config.ZolSessionStore;
import com.github.zol.zol.socket.SocketProtocol;

import javax.websocket.Session;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Some util to help with logger.
 *
 * @author Phillip Kruger (stompee@phillip-kruger.com)
 */
public class ZolUtil {

    private static final Logger LOGGER = Logger.getLogger( ZolUtil.class.getName() );

    private static final String UNKNOWN = "Unknown";

    private static final String APP_NAME_PROP = "appName";

    private static final List<String> LOGGERS = new ArrayList<>();

    static {

        LogManager manager = LogManager.getLogManager();
        Enumeration<String> names = manager.getLoggerNames();
        while ( names.hasMoreElements() ) {
            String name = names.nextElement();
            if ( name != null && !name.isEmpty() && name.contains( "." ) ) {
                LOGGERS.add( name );
                String[] parts = name.split( "\\." );
                LinkedList<String> partsList = new LinkedList<>( Arrays.asList( parts ) );
                if ( partsList.size() > 2 ) {
                    while ( partsList.size() > 1 ) {
                        partsList.remove( partsList.size() - 1 );
                        String parentLogger = String.join( ".", partsList );
                        if ( !LOGGERS.contains( parentLogger ) && parentLogger.contains( "." ) )
                            LOGGERS.add( parentLogger );
                    }
                }
            }
        }
    }

    public static ZolSessionStore createSessionStore( Session session ) {
        ZolSessionStore store = new ZolSessionStore();
        session.getUserProperties().put( ZolSessionStore.class.getName(), store );
        return store;
    }

    public static ZolSessionStore getOrCreateSessionStore( Session session ) {
        ZolSessionStore store = ( ZolSessionStore ) session.getUserProperties().get( ZolSessionStore.class.getName() );
        if ( store == null ) {
            store = createSessionStore( session );
        }
        return store;
    }

    public static boolean hasSessionStore( Session session ) {
        ZolSessionStore store = ( ZolSessionStore ) session.getUserProperties().get( ZolSessionStore.class.getName() );
        if ( store == null ) {
            return false;
        }
        return true;
    }

    public static ZolSessionStore getSessionStore( Session session ) {
        ZolSessionStore store = ( ZolSessionStore ) session.getUserProperties().get( ZolSessionStore.class.getName() );
        if ( store == null ) {
            throw new IllegalStateException( "ZolSessionStore is null, create session store first" );
        }
        return store;
    }

    public static void removeSessionStore( Session session ) {
        session.getUserProperties().remove( ZolSessionStore.class.getName() );
    }

    public static void removeConfig( Session session ) {
        session.getUserProperties().remove( ZolConfig.class.getName() );
    }

    public static ZolConfig createConfig( Session session ) {
        ZolConfig config = new ZolConfig();
        session.getUserProperties().put( ZolConfig.class.getName(), config );
        return config;
    }

    public static ZolConfig getOrCreateConfig( Session session ) {
        ZolConfig config = ( ZolConfig ) session.getUserProperties().get( ZolConfig.class.getName() );
        if ( config == null ) {
            config = createConfig( session );
        }
        return config;
    }

    public static boolean hasConfig( Session session ) {
        ZolConfig config = ( ZolConfig ) session.getUserProperties().get( ZolConfig.class.getName() );
        if ( config == null ) {
            return false;
        }
        return true;
    }

    public static ZolConfig getConfig( Session session ) {
        ZolConfig config = ( ZolConfig ) session.getUserProperties().get( ZolConfig.class.getName() );
        if ( config == null ) {
            throw new IllegalStateException( "ZolConfig is null, create config first" );
        }

        return config;
    }

    public static Logger getLogger( String loggerName ) {

        Objects.nonNull( loggerName );

        if ( useRootLogger( loggerName ) ) {
            return Logger.getLogger( "" );
        }

        if ( validLogger( loggerName ) ) {
            return Logger.getLogger( loggerName );
        }
        return null;
    }

//    public Level getLevel( String loggerName ) {
//        Logger logger = getLogger( loggerName );
//        if ( logger != null ) {
//            return getLevel( logger );
//        }
//        return null;
//    }

    public static Level getLevel( Logger logger ) {
        if ( logger == null || logger.getName().isEmpty() ) return Level.INFO; // Not sure about this
        Level level = logger.getLevel();
        if ( level == null && logger.getParent() != null ) return getLevel( logger.getParent() );
        return level;
    }

    public static Level parseLevel( String levelName ) {
        Level level = levelName == null ? null : Level.parse( levelName );
        return level;
    }

    public static long parseLong( String value ) {
        long result = 0;

        try {
            result = Long.parseLong( value );

        } catch ( Exception ignore ) {
        }
        return result;
    }

    public static boolean validLogger( String name ) {

        if ( SocketProtocol.ALL_LOGGERS.equals( name ) ) {
            return true;
        }

        return !name.isEmpty() && LOGGERS.contains( name );
    }

    public static List<String> getAllLoggerNames() {
        return LOGGERS;
    }

    public static String getAppName() {

        ZolProperties props = ServiceFactory.getProperties();

        String appName = props.getProperty( APP_NAME_PROP, null );

        if ( appName == null ) {
            return UNKNOWN;

        }
        return appName;
    }

    public static boolean isNotBlank( String val ) {
        return !isBlank( val );
    }

    public static boolean isBlank( String val ) {
        if ( val == null || val.trim().length() == 0 ) {
            return true;
        }
        return false;
    }

    private static boolean useRootLogger( String loggerName ) {
        if ( SocketProtocol.ALL_LOGGERS.equals( loggerName ) ) {
            return true;
        }
        return false;
    }
}
