package com.github.phillipkruger.stompee;

import com.github.phillipkruger.stompee.config.StompeeProperties;

public class ServiceFactory {

    private static StompeeProperties STOMPEE_PROPS;

    public static StompeeProperties getProperties() {

        if ( STOMPEE_PROPS != null ) {
            return STOMPEE_PROPS;
        }

        STOMPEE_PROPS = new StompeeProperties();
        STOMPEE_PROPS.init();
        return STOMPEE_PROPS;
    }
}
