package com.cyr1en.voxx.server.protocol;

public enum ResponseID {
    INVALID(-1),
    VALID(1),
    INCORRECT_ARG(0);

    private final int id;

    ResponseID(int id) {
        this.id = id;
    }

    int asInt() {
        return id;
    }
}
