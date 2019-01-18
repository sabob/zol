package com.github.zol.config;

import com.github.zol.socket.LogFilter;

import java.util.logging.Level;

public class ZolConfig {

    private LogFilter filter;

    private boolean exceptionsOnly;

    private Level logLevel;

    public LogFilter getFilter() {
        return filter;
    }

    public void setFilter( LogFilter filter ) {
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
