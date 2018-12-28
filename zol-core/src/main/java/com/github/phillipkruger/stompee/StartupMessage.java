package com.github.phillipkruger.stompee;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * To create a startup message, to send system state
 * @author Phillip Kruger (stompee@phillip-kruger.com)
 */
public class StartupMessage extends SystemMessage {
    
    private String applicationName;

    @Override
    protected JsonObject toJsonObject(){
        JsonObjectBuilder builder = getJsonObjectBuilder();
        if(applicationName!=null)builder.add(APPLICATION_NAME, applicationName);
        return builder.build();
    }
    
    @Override
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
}
