package com.cyr1en.voxx.commons.model;

public class Message {

    private final User sender;
    private String content;

    public Message(User sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    public User getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
