package com.github.pimsturm.commandmessenger.Queue;

/// <summary> Base of general strategy.  </summary>
public class GeneralStrategy {
    /// <summary> Gets or sets the command queue. </summary>
    /// <value> A Queue of commands. </value>
    private ListQueue<CommandStrategy> CommandQueue;
    public ListQueue<CommandStrategy> getCommandQueue() {
        return CommandQueue;
    }
    public void setCommandQueue(ListQueue<CommandStrategy> commandQueue) {
        CommandQueue = commandQueue;
    }

    /// <summary> GenerAdd command (strategy) to command queue. </summary>
    public void OnEnqueue()
    {
    }

    /// <summary> Remove this command (strategy) from command queue. </summary>
    public void OnDequeue()
    {
    }
}
