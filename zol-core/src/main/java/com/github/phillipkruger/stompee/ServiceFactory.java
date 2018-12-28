package com.github.phillipkruger.stompee;

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
