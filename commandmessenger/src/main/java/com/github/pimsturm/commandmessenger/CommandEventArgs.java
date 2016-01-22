package com.github.pimsturm.commandmessenger;

public class CommandEventArgs {
    private Command command;
    public Command getCommand() {return command;}

    public CommandEventArgs(Command command)
    {
        this.command = command;
    }
}
