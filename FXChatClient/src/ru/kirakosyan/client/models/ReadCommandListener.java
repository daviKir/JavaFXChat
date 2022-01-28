package ru.kirakosyan.client.models;

import ru.kirakosyan.clientserver.Command;

public interface ReadCommandListener {

    void processReceivedCommand(Command command);
}
