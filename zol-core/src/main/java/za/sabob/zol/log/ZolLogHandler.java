package za.sabob.zol.log;

import za.sabob.zol.socket.LogFilter;
import za.sabob.zol.util.ZolUtil;
import za.sabob.zol.config.ZolConfig;

import javax.websocket.Session;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Log handler for Zol
 *
 * @author Phillip Kruger (stompee@phillip-kruger.com)
 */
public class ZolLogHandler extends Handler {

    private static final Logger LOGGER = Logger.getLogger( ZolLogHandler.class.getName() );

    private final Session session;

    public ZolLogHandler( Session session, String loggerName ) {
        this.session = session;
        setFormatter( new LogJsonFormatter( loggerName ) );
    }

    @Override
    public void publish( LogRecord logRecord ) {

        if ( session != null && canLog( logRecord ) && matchFilter( logRecord ) ) {

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

    private boolean canLog( LogRecord logRecord ) {

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

        // Guard against there not being config set, but shouldn't happen since we create a config
        // AND a Handler when Socket starts, and remove both when Socket stops
        if ( !ZolUtil.hasConfig( session ) ) {
            return false;

        }

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

    private boolean matchFilter( LogRecord logRecord ) {
        ZolConfig config = ZolUtil.getConfig( session );
        LogFilter filter = config.getFilter();

        if ( filter == null ) {
            return true;
        }

        FilterMatcher matcher = new FilterMatcher();

        String logMessage = getFormatter().formatMessage( logRecord );
        if (matcher.matchFilter( filter, logRecord, logMessage)) {
            return true;
        }

        return false;

    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

}