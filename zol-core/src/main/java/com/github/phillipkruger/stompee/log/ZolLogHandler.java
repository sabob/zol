package com.github.phillipkruger.stompee.log;

import com.github.phillipkruger.stompee.util.JsonFormatter;
import com.github.phillipkruger.stompee.util.ZolUtil;
import com.github.phillipkruger.stompee.config.ZolConfig;

import javax.websocket.Session;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Log handler for Stompee
 *
 * @author Phillip Kruger (stompee@phillip-kruger.com)
 */
public class ZolLogHandler extends Handler {

    private static final Logger LOGGER = Logger.getLogger( ZolLogHandler.class.getName() );

    private final Session session;

    public ZolLogHandler( Session session, String loggerName ) {
        this.session = session;
        setFormatter( new JsonFormatter( loggerName ) );
    }

    @Override
    public void publish( LogRecord logRecord ) {

        if ( session != null && shouldLog( logRecord ) && filter( logRecord ) ) {

            String message = getFormatter().format( logRecord );
            try {
                session.getBasicRemote().sendText( message );

            } catch ( Throwable ex ) {
                LOGGER.log( Level.SEVERE, ex.getMessage(), ex );

                try {
                    session.close();
                } catch ( IOException ex1 ) {
                    LOGGER.log( Level.SEVERE, ex1.getMessage(), ex1 );
                }
            }
        }
    }

    private boolean shouldLog( LogRecord logRecord ) {

        if ( isLogExceptionOnly() ) {
            return isException( logRecord );
        }

        if ( canLogAtLevel( logRecord ) ) {
            return true;
        }

        return false;
    }

    private boolean canLogAtLevel( LogRecord logRecord ) {

        ZolConfig config = ZolUtil.getConfig( session );
        Level activeLevel = config.getLogLevel();

        int activeLevelInt = activeLevel.intValue();
        int recordLevelInt = logRecord.getLevel().intValue();

        if ( recordLevelInt >= activeLevelInt ) {
            return true;
        }

        return false;
    }

    private boolean isLogExceptionOnly() {
        ZolConfig config = ZolUtil.getConfig( session );
        boolean exceptionsOnly = config.isExceptionsOnly();
        return exceptionsOnly;
    }

    private boolean isException( LogRecord logRecord ) {
        if ( logRecord.getThrown() == null ) {
            return false;
        }
        return true;
    }

    private boolean filter( LogRecord logRecord ) {
        ZolConfig config = ZolUtil.getConfig( session );
        String filter = config.getFilter();

        if ( filter == null || filter.isEmpty() ) {
            return true;
        }

        return logRecord.getMessage().contains( filter );
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

}