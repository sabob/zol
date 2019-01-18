package com.github.zol.zol.log;

import com.github.zol.zol.socket.LogFilter;
import com.github.zol.zol.util.Context;
import com.github.zol.zol.util.ContextFilter;
import com.github.zol.zol.util.ZolUtil;

import java.util.logging.LogRecord;

public class FilterMatcher {

    public boolean matchFilter( LogFilter filter, LogRecord logRecord, String logMessage ) {

        String message = filter.getMessage();
        if ( !matchMessage( message, logMessage ) ) {
            return false;
        }

        String user = filter.getUser();
        if ( !matchUser( user, logRecord ) ) {
            return false;
        }

        String className = filter.getSourceClassName();
        if ( !matchSourceClassName( className, logRecord ) ) {
            return false;
        }

        String methodName = filter.getSourceMethodName();
        if ( !matchSourceMethodName( methodName, logRecord ) ) {
            return false;
        }

        long threadId = filter.getThreadId();
        if ( !matchThreadId( threadId, logRecord ) ) {
            return false;
        }

        return true;
    }

    public boolean matchMessage( String message, String logMessage ) {

        if ( ZolUtil.isBlank( message ) ) {
            return true;
        }

        message = message.toLowerCase();
        logMessage = logMessage.toLowerCase();

        if ( logMessage.contains( message ) ) {
            return true;
        }

        return false;
    }

    public boolean matchUser( String user, LogRecord logRecord ) {

        if ( ZolUtil.isBlank( user ) ) {
            return true;
        }

        Context ctx = ContextFilter.getThreadLocalContext();
        String remoteUser = ctx.getRemoteUser();

        if ( remoteUser == null ) {
            remoteUser = "";
        }

        user = user.toLowerCase();
        remoteUser = remoteUser.toLowerCase();

        if ( user.equals( remoteUser ) ) {
            return true;
        }

        return false;
    }

    public boolean matchSourceClassName( String className, LogRecord logRecord ) {
        if ( ZolUtil.isBlank( className ) ) {
            return true;
        }

        className = className.toLowerCase();
        String logMessage = logRecord.getSourceClassName().toLowerCase();

        if ( logMessage.contains( className ) ) {
            return true;
        }

        return false;
    }

    public boolean matchSourceMethodName( String methodName, LogRecord logRecord ) {

        if ( ZolUtil.isBlank( methodName ) ) {
            return true;
        }

        if ( ZolUtil.isNotBlank( methodName ) ) {

            methodName = methodName.toLowerCase();
            String logMessage = logRecord.getSourceMethodName().toLowerCase();

            if ( logMessage.contains( methodName ) ) {
                return true;
            }
        }

        return false;
    }

    public boolean matchThreadId( long threadId, LogRecord logRecord ) {

        if ( threadId == 0 ) {
            return true;
        }

        String threadIdStr = threadId + "";
        String logThreadIdStr = logRecord.getThreadID() + "";

        if ( logThreadIdStr.contains( threadIdStr ) ) {
            return true;
        }

        return false;
    }
}
