package ru.kirakosyan.clientserver;

public enum CommandType {
    AUTH,
    AUTH_OK,
    ERROR,
    PUBLIC_MESSAGE,
    PRIVATE_MESSAGE,
    CLIENT_MESSAGE,
    UPDATE_USERNAME,
    UPDATE_USER_LIST,
    END
}
