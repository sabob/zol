package com.github.phillipkruger.stompee.log;

import com.github.phillipkruger.stompee.json.Json;
import com.github.phillipkruger.stompee.socket.SocketProtocol;
import com.github.phillipkruger.stompee.util.Context;
import com.github.phillipkruger.stompee.util.ContextFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static com.github.phillipkruger.stompee.socket.SocketProtocol.*;

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
        Json json = Json.object( MESSAGE_TYPE, LOG );

        if ( ContextFilter.hasThreadLocalContext() ) {
            Context ctx = ContextFilter.getThreadLocalContext();
            String remoteUser = ctx.getRemoteUser();

            if (remoteUser == null || remoteUser.trim().isEmpty()) {
                // don't set user

            } else {
                json.set( USER, remoteUser );
            }
        }

        if ( logRecord.getLoggerName() != null ) {
            json.set( LOGGER_NAME, loggerName );
        }

        if ( logRecord.getLevel() != null ) {
            json.set( SocketProtocol.LOG_LEVEL, logRecord.getLevel().getName() );
        }

        if ( logRecord.getMessage() != null ) {
            json.set( MESSAGE, formattedMessage );
        }

        if ( logRecord.getSourceClassName() != null ) {
            json.set( SOURCE_CLASS_NAME_FULL, logRecord.getSourceClassName() );
            json.set( SOURCE_CLASS_NAME, getJustClassName( logRecord.getSourceClassName() ) );
        }
        if ( logRecord.getSourceMethodName() != null ) {
            json.set( SOURCE_METHOD_NAME, logRecord.getSourceMethodName() );
        }

        if ( logRecord.getThrown() != null ) {
            json.set( STACKTRACE, getStacktraces( logRecord.getThrown() ) );
        }

        json.set( THREAD_ID, logRecord.getThreadID() );
        json.set( TIMESTAMP, logRecord.getMillis() );
        json.set( SEQUENCE_NUMBER, logRecord.getSequenceNumber() );
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
