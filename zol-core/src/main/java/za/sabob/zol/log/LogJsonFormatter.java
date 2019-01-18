package za.sabob.zol.log;

import za.sabob.zol.json.Json;
import za.sabob.zol.socket.SocketProtocol;
import za.sabob.zol.filter.Context;
import za.sabob.zol.filter.ZolContextFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Formatting log records into a json format
 *
 * @author Phillip Kruger (stompee@phillip-kruger.com)
 */
public class LogJsonFormatter extends Formatter {

    private static final Logger LOGGER = Logger.getLogger( LogJsonFormatter.class.getName() );

    private String loggerName = "defaultLoggerName";

    public LogJsonFormatter( String loggerName ) {
        this.loggerName = loggerName;
    }


    @Override
    public String format( final LogRecord logRecord ) {
        Json json = toJson( logRecord );
        return json.toString();
    }

    private Json toJson( LogRecord logRecord ) {


        String formattedMessage = formatMessage( logRecord );
        Json json = Json.object( SocketProtocol.MESSAGE_TYPE, SocketProtocol.LOG );

        if ( ZolContextFilter.hasThreadLocalContext() ) {
            Context ctx = ZolContextFilter.getThreadLocalContext();
            String remoteUser = ctx.getRemoteUser();

            if (remoteUser == null || remoteUser.trim().isEmpty()) {
                // don't set user

            } else {
                json.set( SocketProtocol.USER, remoteUser );
            }
        }

        if ( logRecord.getLoggerName() != null ) {
            json.set( SocketProtocol.LOGGER_NAME, loggerName );
        }

        if ( logRecord.getLevel() != null ) {
            json.set( SocketProtocol.LOG_LEVEL, logRecord.getLevel().getName() );
        }

        if ( logRecord.getMessage() != null ) {
            json.set( SocketProtocol.MESSAGE, formattedMessage );
        }

        if ( logRecord.getSourceClassName() != null ) {
            json.set( SocketProtocol.SOURCE_CLASS_NAME_FULL, logRecord.getSourceClassName() );
            json.set( SocketProtocol.SOURCE_CLASS_NAME, getJustClassName( logRecord.getSourceClassName() ) );
        }
        if ( logRecord.getSourceMethodName() != null ) {
            json.set( SocketProtocol.SOURCE_METHOD_NAME, logRecord.getSourceMethodName() );
        }

        if ( logRecord.getThrown() != null ) {
            json.set( SocketProtocol.STACKTRACE, getStacktraces( logRecord.getThrown() ) );
        }

        json.set( SocketProtocol.THREAD_ID, logRecord.getThreadID() );
        json.set( SocketProtocol.TIMESTAMP, logRecord.getMillis() );
        json.set( SocketProtocol.SEQUENCE_NUMBER, logRecord.getSequenceNumber() );
        return json;
    }

    private Json getStacktraces( Throwable t ) {
        List<String> traces = new LinkedList<>();
        addStacktrace( traces, t );

        Json json = Json.array();
        traces.forEach( ( trace ) -> {
            json.add( trace );
        } );
        return json;
    }

    private void addStacktrace( List<String> traces, Throwable t ) {
        traces.add( getStacktrace( t ) );
        if ( t.getCause() != null ) addStacktrace( traces, t.getCause() );
    }

    private String getStacktrace( Throwable t ) {
        try ( StringWriter sw = new StringWriter();
              PrintWriter pw = new PrintWriter( sw ) ) {
            t.printStackTrace( pw );
            return sw.toString();
        } catch ( IOException ex ) {
            LOGGER.log( Level.WARNING, "Can not create stacktrace [{0}]", ex.getMessage() );
            return null;
        }
    }

    private String getJustClassName( String fullName ) {
        int lastDot = fullName.lastIndexOf( SocketProtocol.DOT ) + 1;
        return fullName.substring( lastDot );
    }
}
