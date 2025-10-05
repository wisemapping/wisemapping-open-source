package com.wisemapping.model;

import java.util.Calendar;

/**
 * Result class for inactive user queries with activity information
 */
public class InactiveUserResult {
    private final Account user;
    private final Calendar lastLogin;
    private final Calendar lastActivity;

    public InactiveUserResult(Account user, Calendar lastLogin, Calendar lastActivity) {
        this.user = user;
        this.lastLogin = lastLogin;
        this.lastActivity = lastActivity;
    }

    public Account getUser() {
        return user;
    }

    public Calendar getLastLogin() {
        return lastLogin;
    }

    public Calendar getLastActivity() {
        return lastActivity;
    }
}
