package com.github.phillipkruger.stompee.socket;

import com.github.phillipkruger.stompee.ServiceFactory;
import com.github.phillipkruger.stompee.config.StompeeProperties;
import com.github.phillipkruger.stompee.config.ZolConfig;
import com.github.phillipkruger.stompee.config.ZolSessionStore;
import com.github.phillipkruger.stompee.json.Json;
import com.github.phillipkruger.stompee.log.ZolLogHandler;
import com.github.phillipkruger.stompee.util.StompeeUtil;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;

/**
 * Websocket server that can distribute log messages.
 * If there is no subscribers, the logging fall back to normal file.
 *
 * @author Phillip Kruger (stompee@phillip-kruger.com)
 */
@ServerEndpoint( "/socket/zol" )
public class StompeeSocket {

    private static final Logger LOGGER = Logger.getLogger( StompeeSocket.class.getName() );

    //private static final String ID = "uuid";
    //private static final String HANDLER = "handler";
    //private static final String LOGGER_NAME = "loggerName";

//    private static final String START = "start";
//    private static final String STOP = "stop";
//    private static final String SET_LOG_LEVEL = "setLogLevel";
//    private static final String SET_EXCEPTIONS_ONLY = "setExceptionsOnly";
//    private static final String SET_FILTER = "setFilter";
    //private static final String ACTION = "action";

    //private static final String LOGGER_PROP = "logger";

    //private static final String DOT = ".";
    public static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    private StompeeProperties stompeeProperties = ServiceFactory.getProperties();

    @OnOpen
    public void onOpen( Session session ) {
        String appName = StompeeUtil.getAppName();
        sendStartupMessage( appName, session );
    }

    @OnClose
    public void onClose( Session session ) {
        stop( session );
    }

    @OnMessage
    public void onMessage( String message, Session session ) {

        try {

            if ( message == null || message.isEmpty() ) {
                return;
            }

            //JsonObject jo = toJsonObject( message );
            Json jo = Json.read( message );
            SocketProtocol protocol = SocketProtocol.parseZolProtocol( jo );

            if ( protocol.isStart() ) {
                start( session, protocol );

            } else if ( protocol.isStop() ) {
                stop( session );

            } else if ( protocol.isSetLogLevel() ) {
                setLogLevel( session, protocol.getLogLevel() );

            } else if ( protocol.isSetExceptionsOnly() ) {
                Boolean exceptionsOnly = protocol.getExceptionsOnly();
                setExceptionsOnly( session, exceptionsOnly );

            } else if ( protocol.isSetFilter() ) {
                String filter = protocol.getFilter();
                setFilter( session, filter );
            }

        } catch ( Error err ) {
            LOGGER.log( Level.SEVERE, err.getMessage(), err );
            throw err;
        }
    }

    private void performStart( Session session, SocketProtocol protocol ) {

        setDefaultLogLevel( session );

        ZolSessionStore store = StompeeUtil.createSessionStore( session );
        String uuid = store.getUuid();

        if ( uuid == null ) {
            uuid = UUID.randomUUID().toString();
            registerHandler( session, uuid, protocol );
            SESSIONS.put( session.getId(), session );


            //Level level = StompeeUtil.parseLevel( levelName );
            //session.getUserProperties().put( Settings.LEVEL, level );

//            if ( levelName != null && !levelName.isEmpty() ) {
//                setLogLevel( session, levelName );
//            }
        }
    }

    private void stop( Session session ) {

        String id = null;

        if ( StompeeUtil.hasSessionStore( session ) ) {
            ZolSessionStore store = StompeeUtil.getSessionStore( session );
            id = store.getUuid();
        }

        if ( id != null ) {
            unregisterHandler( session );
            SESSIONS.remove( session.getId() );
        }
    }

//    private String getLogLevel( Session session ) {
//        Level level = StompeeUtil.getSessionLogLevel( session );
//        if (level == null) {
//            return null;
//        }
//        return level.getName();
//    }

    private void setLogLevel( Session session, Level level ) {
        ZolConfig config = StompeeUtil.getOrCreateConfig( session );
        config.setLogLevel( level );
    }

//    private void setLogLevel( Session session, String levelName ) {
//        String loggerName = ( String ) session.getUserProperties().get( LOGGER_NAME );
//
//        Level level = levelName == null ? null : Level.parse( levelName );
//        Logger logger = Logger.getLogger( loggerName );
//        logger.setLevel( level );
//
//        LogManager logManager = LogManager.getLogManager();
//        Enumeration<String> loggerNames = logManager.getLoggerNames();
//        String prefix = loggerName + DOT;
//
//        while ( loggerNames.hasMoreElements() ) {
//            String aLoggerName = loggerNames.nextElement();
//            if ( aLoggerName.startsWith( prefix ) ) {
//                Logger.getLogger( aLoggerName ).setLevel( level );
//            }
//        }
//    }

    private void setExceptionsOnly( Session session, Boolean exceptionsOnly ) {
        ZolConfig config = StompeeUtil.getOrCreateConfig( session );

        if ( exceptionsOnly == null ) {
            exceptionsOnly = false;
        }

        config.setExceptionsOnly( exceptionsOnly );
    }

    private void setFilter( Session session, String filter ) {

        ZolConfig config = StompeeUtil.getOrCreateConfig( session );
        config.setFilter( filter );
    }

    private void sendStartupMessage( String appName, Session session ) {
        StartupMessage msg = new StartupMessage();
        msg.setApplicationName( appName );
        String startupMessage = msg.toString();
        try {
            session.getBasicRemote().sendText( startupMessage );
        } catch ( IllegalStateException | IOException ex ) {
            LOGGER.severe( ex.getMessage() );
        }
    }

    private void registerHandler( Session session, String uuid, SocketProtocol protocol ) {

        Handler handler = new MemoryHandler( new ZolLogHandler( session, protocol.getLoggerName() ), 1000, Level.FINEST );

        Logger logger = StompeeUtil.getLogger( protocol.getLoggerName() );
        if ( logger != null ) {
            logger.addHandler( handler );

            ZolSessionStore store = StompeeUtil.getOrCreateSessionStore( session );
            store.setHandler( handler );
            store.setUuid( uuid );
            store.setLoggerName( protocol.getLoggerName() );
            Level origLevel = StompeeUtil.getLevel( logger );
            store.setOrigLogLevel( origLevel );
        }
    }

    private void unregisterHandler( Session session ) {

        ZolSessionStore store = StompeeUtil.getSessionStore( session );

        Handler handler = store.getHandler();
        String loggerName = store.getLoggerName();
        //String loggerName = ( String ) session.getUserProperties().get( LOGGER_NAME );
        if ( handler != null ) {
            Logger logger = StompeeUtil.getLogger( loggerName );
            if ( logger != null ) logger.removeHandler( handler );
        }

        // Restore original level
        Level originalLevel = store.getOrigLogLevel();
        setLogLevel( session, originalLevel );

        StompeeUtil.removeSessionStore( session );

//        session.getUserProperties().remove( ID );
//        session.getUserProperties().remove( HANDLER );
//        session.getUserProperties().remove( LOGGER_NAME );
        StompeeUtil.removeConfig( session );


//        session.getUserProperties().remove( Settings.EXCEPTIONS_ONLY );
//        session.getUserProperties().remove( Settings.FILTER );
    }

//    private Handler getHandler( Session session ) {
//        Object o = session.getUserProperties().get( HANDLER );
//        if ( o != null ) {
//            return ( Handler ) o;
//        }
//        return null;
//    }

//    private String getUuid( Session session ) {
//        Object o = session.getUserProperties().get( ID );
//        if ( o == null ) return null;
//        return ( String ) o;
//    }

//    private JsonObject toJsonObject( String message ) {
//        try ( StringReader sr = new StringReader( message );
//              JsonReader reader = Json.createReader( sr ) ) {
//            return reader.readObject();
//        }
//    }

    private String getDefaultLogLevel() {
        String levelName = stompeeProperties.getProperty( SocketProtocol.LOG_LEVEL, null );
        return levelName;
    }

    private void setDefaultLogLevel( Session session ) {

        ZolConfig config = StompeeUtil.getConfig( session );
        Level activeLevel = config.getLogLevel();

        if ( activeLevel == null ) {
            // Set the default level
            String levelName = getDefaultLogLevel();
            Level level = StompeeUtil.parseLevel( levelName );
            setLogLevel( session, level );

        }
    }

    private void start( Session session, SocketProtocol protocol ) {

        String loggerName = protocol.getLoggerName();
        setExceptionsOnly( session, protocol.getExceptionsOnly() );
        setLogLevel( session, protocol.getLogLevel() );

        if ( StompeeUtil.validLogger( loggerName ) ) {
            performStart( session, protocol );
        }
    }
}