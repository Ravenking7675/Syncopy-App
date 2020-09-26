package com.avinash.syncopyproject.Model;

public class Commands {

    private String command_name;
    private String linux_command;
    private long command_code;

    public Commands() {
    }

    public Commands(String command_name, String linux_command, long command_code) {
        this.command_name = command_name;
        this.linux_command = linux_command;
        this.command_code = command_code;
    }

    public long getCommand_code() {
        return command_code;
    }

    public void setCommand_code(long command_code) {
        this.command_code = command_code;
    }

    public String getCommand_name() {
        return command_name;
    }

    public void setCommand_name(String command_name) {
        this.command_name = command_name;
    }

    public String getLinux_command() {
        return linux_command;
    }

    public void setLinux_command(String linux_command) {
        this.linux_command = linux_command;
    }
}
