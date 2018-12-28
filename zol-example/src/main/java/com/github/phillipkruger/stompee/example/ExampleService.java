package com.github.phillipkruger.stompee.example;

import com.github.phillipkruger.stompee.StompeeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExampleService {

    private ExecutorService executor = Executors.newFixedThreadPool( 5 );

    private LogTask task = new LogTask();

    public void start() {
        new StompeeUtil();
        System.out.println( "LOG SPAWNER STARTED!" );

        executor.execute( task );
        executor.execute( task );
        executor.execute( task );
    }

    public void stop() {
        executor.shutdown();
        task.active = false;
    }
}