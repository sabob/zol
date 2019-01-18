package com.github.zol.zol;

import com.github.zol.zol.config.ZolProperties;

public class ServiceFactory {

    private static ZolProperties ZOL_PROPS;

    public static ZolProperties getProperties() {

        if ( ZOL_PROPS != null ) {
            return ZOL_PROPS;
        }

        ZOL_PROPS = new ZolProperties();
        ZOL_PROPS.init();
        return ZOL_PROPS;
    }
}
