package com.github.phillipkruger.stompee;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.logging.Level;

/**
 * To create a initial log level message
 *
 * @author Phillip Kruger (stompee@phillip-kruger.com)
 */
public class LogLevelMessage extends SystemMessage {

    private static final String INIT_LOG_LEVEL_MESSAGE = "logLevelMessage";
    private static final String LOG_LEVEL = "logLevel";

    private Level level;

    public Level getLevel() {
        return level;
    }

    public void setLevel( Level level ) {
        this.level = level;
    }

    @Override
    protected JsonObject toJsonObject() {
        JsonObjectBuilder builder = getJsonObjectBuilder();
        builder.add( LOG_LEVEL, level.getName() );
        return builder.build();
    }

    @Override
    protected String getMessageType() {
        return INIT_LOG_LEVEL_MESSAGE;
    }

}
