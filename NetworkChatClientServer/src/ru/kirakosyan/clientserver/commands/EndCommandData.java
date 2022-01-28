package ru.kirakosyan.clientserver.commands;

import java.io.Serializable;

public class EndCommandData implements Serializable {
    private final int code;
    private final String message;

    public EndCommandData(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
