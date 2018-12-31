package com.github.phillipkruger.stompee.socket;

import com.github.phillipkruger.stompee.json.Json;

/**
 * To create a startup message, to send system state
 * @author Phillip Kruger (stompee@phillip-kruger.com)
 */
public class StartupMessage {

    private String applicationName;

    protected Json toJson(){
        Json json = Json.object( SocketProtocol.MESSAGE_TYPE, getMessageType());

        if(applicationName!=null) {
            json.set( APPLICATION_NAME, getApplicationName() );
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

    public String toString() {
        return toJson().toString();
    }
}
