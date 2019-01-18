# zol
Web Logviewer for Java Servlets

This project is a fork of [Stompee](https://github.com/phillip-kruger/stompee) but san be 
used in a Servlet Container instead of a JEE container.

Zol adds the following features:

* The logged in user is added to log table 
* More filters allow to filter logs on threadId and user
* Option to limit the number of log records in the table so browser won't lock up (default 500 records)    

 ### Usage
 
Add the zol.x.x.x.jar to your WAR project and access the log at http://hostname/zol.

If your application is deployed under the context path 'abc' you can access the logs at http://hostname/abc/zol

Add zol.properties to the classpath with the following properties:

```
appName=your app name - default: Unkonwn
loggerName=some.default.package - default: ALL 
logLevel=default log level - default: FINE

```