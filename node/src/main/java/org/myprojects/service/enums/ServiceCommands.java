package org.myprojects.service.enums;

public enum ServiceCommands {

    HELP("/help"),
    REGISTRATION("/register"),
    CANCEL("/cancel"),
    START("/start");

    private final String cmd;

    ServiceCommands(String cmd) {
        this.cmd = cmd;
    }

    public boolean equals (String cmd) {
        return this.toString().equals(cmd);
    }
}
