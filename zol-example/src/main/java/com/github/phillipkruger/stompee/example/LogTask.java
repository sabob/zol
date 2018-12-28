package com.github.phillipkruger.stompee.example;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogTask implements Runnable {

    private static final Logger LOGGER = Logger.getLogger( LogTask.class.getName() );

    public boolean active = true;

    @Override
    public void run() {

        while ( active ) {

            try {
                LOGGER.log( Level.SEVERE, "Here some random severe {0}", UUID.randomUUID() );
                LOGGER.log( Level.INFO, "Here some random info {0}", UUID.randomUUID() );
                LOGGER.log( Level.WARNING, "Here some random warning {0}", UUID.randomUUID() );
                LOGGER.log( Level.FINE, "Here some random fine {0}", UUID.randomUUID() );
                LOGGER.log( Level.FINER, "Here some random finer {0}", UUID.randomUUID() );
                LOGGER.log( Level.FINEST, "Here some random finest {0}", UUID.randomUUID() );
                LOGGER.log( Level.SEVERE, "And here an exception", new Exception( "Something bad happened" ) );

                Thread.sleep( 5000 );

            } catch ( InterruptedException ex ) {
                LOGGER.log( Level.SEVERE, ex.getMessage(), ex );
            }
        }
    }
}
