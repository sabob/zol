package za.sabob.zol.zol.example;

import za.sabob.zol.util.ZolUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExampleService {

    private ExecutorService executor = Executors.newFixedThreadPool( 5 );

    private LogTask task = new LogTask();

    public void start() {
        new ZolUtil();
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