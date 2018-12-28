package com.github.phillipkruger.stompee;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Some util to help with logger.
 *
 * @author Phillip Kruger (stompee@phillip-kruger.com)
 */
public class StompeeUtil {

    private static final Logger LOGGER = Logger.getLogger(StompeeUtil.class.getName());

    public Logger getLogger( String loggerName ) {

        Objects.nonNull( loggerName );

        if ( validLogger( loggerName ) ) {
            return Logger.getLogger( loggerName );
        }
        return null;
    }

    public Level getLevel( String loggerName ) {
        Logger logger = getLogger( loggerName );
        if ( logger != null ) {
            return getLevel( logger );
        }
        return null;
    }

    public Level getLevel( Logger logger ) {
        if ( logger == null || logger.getName().isEmpty() ) return Level.INFO; // Not sure about this
        Level level = logger.getLevel();
        if ( level == null && logger.getParent() != null ) return getLevel( logger.getParent() );
        return level;
    }

    public boolean validLogger( String name ) {
        return !name.isEmpty() && LOGGERS.contains( name );
    }

    public List<String> getAllLoggerNames() {
        return LOGGERS;
    }

    private static final List<String> LOGGERS = new ArrayList<>();

    static {

        LogManager manager = LogManager.getLogManager();
        Enumeration<String> names = manager.getLoggerNames();
        while ( names.hasMoreElements() ) {
            String name = names.nextElement();
            if ( name != null && !name.isEmpty() && name.contains( "." ) ) {
                LOGGERS.add( name );
                String[] parts = name.split( "\\." );
                LinkedList<String> l = new LinkedList<>( Arrays.asList( parts ) );
                if ( l.size() > 2 ) {
                    while ( l.size() > 1 ) {
                        l.remove( l.size() - 1 );
                        String parentLogger = String.join( ".", l );
                        if ( !LOGGERS.contains( parentLogger ) && parentLogger.contains( "." ) )
                            LOGGERS.add( parentLogger );
                    }
                }

            }
        }
    }
}
