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
logLevel=default log level - default: INFO
```

### Security
Secure the path **/zol** in your application otherwise the logs will be viewable publicly.

Zol ships with a poor-man security filter that will redirect to 
the root path ("/") if the user is not authenticated.

You can enable the filter in web.xml:
```
<filter>
    <filter-name>zolSecurityFilter</filter-name>
    <filter-class>ZolSecurityFilter</filter-class>
</filter>

<filter-mapping>
    <filter-name>zolSecurityFilter</filter-name>
    <url-pattern>/zol/*</url-pattern>
</filter-mapping> 
```