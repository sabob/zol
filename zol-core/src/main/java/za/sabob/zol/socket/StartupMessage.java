package za.sabob.zol.socket;

import za.sabob.zol.json.Json;

/**
 * To create a startup message, to send system state
 * @author Phillip Kruger (stompee@phillip-kruger.com)
 */
public class StartupMessage {

    private String applicationName;

    private String logLevel;

    protected Json toJson(){
        Json json = Json.object( SocketProtocol.MESSAGE_TYPE, getMessageType());

        if(applicationName!=null) {
            json.set( APPLICATION_NAME, getApplicationName() );
        }

        if( logLevel !=null) {
            json.set( SocketProtocol.LOG_LEVEL, getLogLevel() );
        }

        return json;
    }

    protected String getMessageType() {
        return STARTUP_MESSAGE;
    }

    private static final String STARTUP_MESSAGE = "startupMessage";
    private static final String APPLICATION_NAME = "applicationName";

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName( String applicationName ) {
        this.applicationName = applicationName;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel( String logLevel ) {
        this.logLevel = logLevel;
    }

    public String toString() {
        return toJson().toString();
    }
}
