package com.github.phillipkruger.stompee;

import com.github.phillipkruger.stompee.config.StompeeProperties;
import com.github.phillipkruger.stompee.json.Json;
import com.github.phillipkruger.stompee.socket.SocketProtocol;
import com.github.phillipkruger.stompee.util.StompeeUtil;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple Servlet for other data needed by the screen.
 *
 * @author Phillip Kruger (stompee@phillip-kruger.com)
 */
@WebServlet( value = "/servlet/zol", name = "StompeeServlet" )
public class StompeeServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger( StompeeUtil.class.getName() );

    private static final String ACTION = "action";

    private static final String GET_ALL_LOGGER_NAMES = "getAllLoggerNames";
    //private static final String GET_LOGGER_LEVEL = "getLoggerLevel";
    private static final String GET_DEFAULT_SETTINGS = "getDefaultSettings";
    private static final String LOGGER_PROP = "logger";
    //private static final String LEVEL = "level";
    private static final String CONTENT_TYPE = "application/json";

    private StompeeProperties stompeeProperties = ServiceFactory.getProperties();

    @Override
    public void service( ServletRequest req, ServletResponse res ) throws IOException, ServletException {
        res.setContentType( CONTENT_TYPE );

        String action = req.getParameter( ACTION );
        if ( GET_ALL_LOGGER_NAMES.equalsIgnoreCase( action ) ) {
            getAllLoggerNames( req, res );

//        } else if ( GET_LOGGER_LEVEL.equalsIgnoreCase( action ) ) {
//            getLoggerLevel( req, res );

        } else if ( GET_DEFAULT_SETTINGS.equalsIgnoreCase( action ) ) {
            getDefaultSettings( res );
        }
    }

    private void getDefaultSettings( ServletResponse res ) throws IOException, ServletException {
        Json json = Json.object();
        //JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        String loggerName = stompeeProperties.getProperty( SocketProtocol.LOGGER_NAME, null );
        if ( loggerName != null && !loggerName.isEmpty() ) {
            //objectBuilder.add( SocketProtocol.LOGGER, loggerName );
            json.set( SocketProtocol.LOGGER_NAME, loggerName );
        }

        String loggerLevel = stompeeProperties.getProperty( SocketProtocol.LOG_LEVEL, null );
        if ( loggerLevel != null && !loggerLevel.isEmpty() ) {
            //objectBuilder.add( SocketProtocol.LOG_LEVEL, loggerLevel );
            json.set( SocketProtocol.LOG_LEVEL, loggerLevel );
        }
        writeJson( res, json );
    }

//    private void getLoggerLevel( ServletRequest req, ServletResponse res ) throws IOException, ServletException {
//        //String name = req.getParameter( NAME );
//        //Level level = stompeeUtil.getLevel( name );
//
//        Level level = Level.INFO;
//        String levelName = stompeeProperties.getProperty( Settings.LOG_LEVEL, level.getName() );
//        level = StompeeUtil.parseLevel( levelName );
//
//        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
//        objectBuilder.add( LEVEL, level.getName() );
//        writeObject( res, objectBuilder.build() );
//    }

    private void getAllLoggerNames( ServletRequest req, ServletResponse res ) throws IOException, ServletException {

        //JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        Json json = Json.array();

        List<String> loggers = StompeeUtil.getAllLoggerNames();
        loggers.forEach( ( name ) -> {
            //arrayBuilder.add( name );
            json.add( name );
        } );
        writeJson( res, json );
    }

    private void writeJson( ServletResponse res, Json json ) throws IOException {

        try {
            res.getWriter().write( json.toString() );

        } catch ( IOException ex ) {
            LOGGER.log( Level.SEVERE, null, ex );
        }

        res.getWriter().flush();
    }
}
