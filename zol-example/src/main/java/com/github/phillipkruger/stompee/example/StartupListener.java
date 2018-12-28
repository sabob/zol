package com.github.phillipkruger.stompee.example;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class StartupListener implements ServletContextListener {

    private ExampleService service = new ExampleService();

    @Override
    public void contextInitialized( ServletContextEvent sce ) {
        service.start();
    }

    @Override
    public void contextDestroyed( ServletContextEvent sce ) {
        service.stop();

    }
}
