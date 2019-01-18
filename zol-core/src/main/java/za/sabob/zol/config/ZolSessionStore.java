package za.sabob.zol.config;

import java.util.logging.Handler;
import java.util.logging.Level;

public class ZolSessionStore {

    private Handler handler;

    private String uuid;

    private String loggerName;

    private Level origLogLevel;

    public Handler getHandler() {
        return handler;
    }

    public void setHandler( Handler handler ) {
        this.handler = handler;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid( String uuid ) {
        this.uuid = uuid;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName( String loggerName ) {
        this.loggerName = loggerName;
    }

    public Level getOrigLogLevel() {
        return origLogLevel;
    }

    public void setOrigLogLevel( Level origLogLevel ) {
        this.origLogLevel = origLogLevel;
    }
}
