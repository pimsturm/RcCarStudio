package com.github.pimsturm.commandmessenger.Queue;

import com.github.pimsturm.commandmessenger.Command;

/// <summary> Base command strategy.  </summary>
public class CommandStrategy {
    /// <summary> Base command strategy. </summary>
    /// <param name="command"> The command to be wrapped in a strategy. </param>
    public CommandStrategy(Command command)
    {
        Command = command;
    }

    /// <summary> Gets or sets the command queue. </summary>
    /// <value> A Queue of commands. </value>
    private ListQueue<CommandStrategy> CommandQueue;
    public ListQueue<CommandStrategy> getCommandQueue() {
        return CommandQueue;
    }
    public void setCommandQueue(ListQueue<CommandStrategy> commandQueue) {
        CommandQueue = commandQueue;
    }

    /// <summary> Gets or sets the command. </summary>
    /// <value> The command wrapped in the strategy. </value>
    private Command Command;
    public Command getCommand() {
        return Command;
    }

    /// <summary> Add command (strategy) to command queue. </summary>
    public  void Enqueue()
    {
        CommandQueue.Enqueue(this);
    }

    /// <summary> Remove this command (strategy) from command queue. </summary>
    public  void DeQueue()
    {
        CommandQueue.remove(this);
    }
}
