package za.sabob.zol.socket;

public class LogFilter {

    private String user;

    private long threadId;

    private String message;

    private String sourceClassName;

    private String sourceMethodName;

    public String getUser() {
        return user;
    }

    public void setUser( String user ) {
        this.user = user;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId( long threadId ) {
        this.threadId = threadId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage( String message ) {
        this.message = message;
    }

    public String getSourceClassName() {
        return sourceClassName;
    }

    public void setSourceClassName( String sourceClassName ) {
        this.sourceClassName = sourceClassName;
    }

    public String getSourceMethodName() {
        return sourceMethodName;
    }

    public void setSourceMethodName( String sourceMethodName ) {
        this.sourceMethodName = sourceMethodName;
    }
}
