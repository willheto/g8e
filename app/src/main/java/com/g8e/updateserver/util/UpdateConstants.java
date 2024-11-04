package com.g8e.updateserver.util;

public class UpdateConstants {

    public static final int UPDATE_SERVER_PORT = 8887;
    public static final int CACHE_VERSION = 806;

    public static final int UPDATE_REQUEST_CHECK_FOR_UPDATES = 1;
    public static final int UPDATE_RESPONSE_CACHE_UP_TO_DATE = 1;
    public static final int UPDATE_RESPONSE_UPDATE_AVAILABLE = 2;

    // Prevent instantiation
    private UpdateConstants() {
    }

}
