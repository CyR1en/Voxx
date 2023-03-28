package com.cyr1en.model;

public class User {

    private final UID uid;
    private String username;

    private User(UID uid, String username) {
        this.uid = uid;
        this.username = username;
    }

    public UID getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
