package com.github.pimsturm.commandmessenger;

import java.util.ArrayList;

/**
 * A command to be send by CmdMessenger
 */
public class Command {
    CommunicationManager communicationManager;
    public void setCommunicationManager(CommunicationManager communicationManager) {
        this.communicationManager = communicationManager;
    }

    protected ArrayList<String> cmdArgs = new ArrayList<String>(); // The argument list of the command, first one is the command ID

    private int cmdId;

    /**
     * Gets the command ID.
     * @return The command ID.
     */
    public int getCmdId() {
        return cmdId;
    }

    /**
     * Sets the command ID
     * @param cmdId The command ID.
     */
    public void setCmdId(int cmdId) {
        this.cmdId = cmdId;
    }

    /**
     * Gets the command arguments.
     * @return The arguments, first one is the command ID
     */
    public String[] getArguments()
    {
        return cmdArgs.toArray(new String[cmdArgs.size()]);
    }

    private long timeStamp;

    /**
     * Gets the time stamp.
     * @return The time stamp.
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the time stamp.
     * @param timeStamp The time stamp.
     */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Constructor.
     */
    public Command()
    {
        cmdId = -1;
        cmdArgs = new ArrayList<String>();
        timeStamp = TimeUtils.millis;
    }

    /**
     * Returns whether this is a valid & filled command.
     * @return true if ok, false if not.
     */
    public boolean getOk() {
        return (cmdId >= 0);
    }

    /**
     * Build a commandstring
     * @return command with arguments as a string.
     */
    public String commandString()
    {
        if (communicationManager == null)
            throw new IllegalStateException("communicationManager was not set for command.");

        StringBuilder commandString = new StringBuilder(Integer.toString(cmdId));

        for (String argument : getArguments())
        {
            commandString.append(communicationManager.getFieldSeparator()).append(argument);
        }
        commandString.append(communicationManager.getCommandSeparator());

        return commandString.toString();
    }
}
