package com.g8e.loginserver.util;

public class LoginConstants {
    public static final int LOGIN_SERVER_PORT = 8886;

    // Login request constants
    public static final int LOGIN = 1;
    public static final int LOGOUT = 2;
    public static final int RESET_WORLD = 3;
    public static final int COUNT_PLAYERS = 4;
    public static final int REGISTER = 5;

    // Login response constants
    public static final int INVALID_CREDENTIALS = 3;
    public static final int ALREADY_LOGGED_IN = 4;
    public static final int LOGIN_SUCCESS = 6;
    public static final int WORLD_FULL = 7;

    // Register constants
    public static final int SUCCESS = 1;
    public static final int USERNAME_TAKEN = 2;

}
