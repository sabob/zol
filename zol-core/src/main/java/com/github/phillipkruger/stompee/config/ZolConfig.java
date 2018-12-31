package com.github.phillipkruger.stompee.config;

import java.util.logging.Level;

public class ZolConfig {

    private String filter;

    private boolean exceptionsOnly;

    private Level logLevel;

    public String getFilter() {
        return filter;
    }

    public void setFilter( String filter ) {
        this.filter = filter;
    }

    public boolean isExceptionsOnly() {
        return exceptionsOnly;
    }

    public void setExceptionsOnly( boolean exceptionsOnly ) {
        this.exceptionsOnly = exceptionsOnly;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel( Level logLevel ) {
        this.logLevel = logLevel;
    }
}
