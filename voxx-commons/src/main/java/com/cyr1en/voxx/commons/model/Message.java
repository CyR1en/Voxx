package com.cyr1en.voxx.commons.model;

public class Message {

    private final User sender;
    private String content;
    private final UID messageUID;

    public Message(User sender, String content, UID messageUID) {
        this.sender = sender;
        this.content = content;
        this.messageUID = messageUID;
    }

    public User getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public UID getUID() {
        return messageUID;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
