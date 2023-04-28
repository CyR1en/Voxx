package com.cyr1en.voxx.commons.model;

public class User {

    private final UID uid;
    private String username;

    public User(UID uid, String username) {
        this.uid = uid;
        this.username = username;
    }

    public UID getUID() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return username + ":" + uid.asLong();
    }
}
