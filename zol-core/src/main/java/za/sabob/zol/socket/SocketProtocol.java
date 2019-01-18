package za.sabob.zol.socket;

import za.sabob.zol.json.Json;
import za.sabob.zol.util.ZolUtil;

import java.util.logging.Level;

public class SocketProtocol {

    // Configurations that UI can specify
    private static final String FILTERS = "filters";
    private static final String FILTER_MESSAGE = "message";
    private static final String FILTER_THREAD_ID = "threadId";
    private static final String FILTER_USER = "user";
    private static final String FILTER_SOURCE_CLASS_NAME = "sourceClassName";
    private static final String FILTER_SOURCE_METHOD_NAME = "sourceMethodName";

    private static final String EXCEPTIONS_ONLY = "exceptionsOnly";
    public static final String LOG_LEVEL = "logLevel";


    //public static final String LOGGER = "logger";

    // Actions UI can invoke
    private static final String START = "start";
    private static final String STOP = "stop";
    private static final String SET_LOG_LEVEL = "setLogLevel";
    private static final String SET_EXCEPTIONS_ONLY = "setExceptionsOnly";
    private static final String SET_FILTERS = "setFilters";

    public static final String ACTION = "action";

    // Log Message format
    public static final String LOG = "log";
    public static final String MESSAGE_TYPE = "messageType";
    public static final String LOGGER_NAME = "loggerName";

    public static final String USER = "user";
    public static final String USER_UNKNOWN = "Unknown";

    //public static final String LEVEL = "level";
    public static final String MESSAGE = "message";
    public static final String SOURCE_CLASS_NAME_FULL = "sourceClassNameFull";
    public static final String SOURCE_CLASS_NAME = "sourceClassName";
    public static final String SOURCE_METHOD_NAME = "sourceMethodName";
    public static final String THREAD_ID = "threadId";
    public static final String TIMESTAMP = "timestamp";
    public static final String STACKTRACE = "stacktrace";
    public static final String SEQUENCE_NUMBER = "sequenceNumber";
    public static final String DOT = ".";

    // Variables
    public static final String ALL_LOGGERS = "ALL";

    private String action;

    private String loggerName;

    private LogFilter filter;

    private Boolean exceptionsOnly;

    private Level logLevel;

    public String getAction() {
        return action;
    }

    public void setAction( String action ) {
        this.action = action;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName( String loggerName ) {
        this.loggerName = loggerName;
    }

    public LogFilter getFilter() {
        return filter;
    }

    public void setFilter( LogFilter filter ) {
        this.filter = filter;
    }

    public Boolean getExceptionsOnly() {
        return exceptionsOnly;
    }

    public void setExceptionsOnly( Boolean exceptionsOnly ) {
        this.exceptionsOnly = exceptionsOnly;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel( Level logLevel ) {
        this.logLevel = logLevel;
    }

    public boolean isStart() {
        return START.equals( getAction() );
    }

    public boolean isStop() {
        return STOP.equals( getAction() );
    }

    public boolean isSetLogLevel() {
        return SET_LOG_LEVEL.equals( getAction() );
    }

    public boolean isSetExceptionsOnly() {
        return SET_EXCEPTIONS_ONLY.equals( getAction() );
    }

    public boolean isSetFilters() {
        return SET_FILTERS.equals( getAction() );
    }

    public static SocketProtocol parseZolProtocol( Json jo ) {
        SocketProtocol protocol = new SocketProtocol();

        if ( jo.has( ACTION ) ) {
            String action = jo.at( ACTION ).asString();
            protocol.setAction( action );
        }

        if ( jo.has( LOGGER_NAME ) ) {
            String loggerName = jo.at( LOGGER_NAME ).asString();
            protocol.setLoggerName( loggerName );
        }

        if ( jo.has( EXCEPTIONS_ONLY ) && jo.at( EXCEPTIONS_ONLY ).isBoolean() ) {
            Boolean exceptionsOnly = jo.at( EXCEPTIONS_ONLY ).asBoolean();
            protocol.setExceptionsOnly( exceptionsOnly );
        }

        setFilter( jo, protocol );

        if ( jo.has( LOG_LEVEL ) ) {
            String levelName = jo.at( LOG_LEVEL ).asString();
            Level level = ZolUtil.parseLevel( levelName );
            protocol.setLogLevel( level );
        }

        return protocol;
    }

    private static void setFilter( Json jo, SocketProtocol protocol ) {

        if ( jo.has( FILTERS ) ) {
            LogFilter filter = new LogFilter();
            protocol.setFilter( filter );

            Json joFilter = jo.at( FILTERS );

            if ( joFilter.has( FILTER_MESSAGE ) ) {
                String message = joFilter.at( FILTER_MESSAGE ).asString();
                filter.setMessage( message );
            }

            if ( joFilter.has( FILTER_SOURCE_CLASS_NAME ) ) {
                String value = joFilter.at( FILTER_SOURCE_CLASS_NAME ).asString();
                filter.setSourceClassName( value );
            }

            if ( joFilter.has( FILTER_SOURCE_METHOD_NAME ) ) {
                String value = joFilter.at( FILTER_SOURCE_METHOD_NAME ).asString();
                filter.setSourceMethodName( value );
            }

            if ( joFilter.has( FILTER_USER ) ) {
                String value = joFilter.at( FILTER_USER ).asString();
                filter.setUser( value );
            }

            if ( joFilter.has( FILTER_THREAD_ID ) ) {
                String value = joFilter.at( FILTER_THREAD_ID ).asString();
                long threadId = ZolUtil.parseLong( value );
                filter.setThreadId( threadId );

            }
        }
    }
}

