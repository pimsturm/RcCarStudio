package com.github.pimsturm.commandmessenger.Queue;

import com.github.pimsturm.commandmessenger.Command;

/// <summary>  Top strategy. The command is added to the front of the queue</summary>
public class TopCommandStrategy extends CommandStrategy {
    /// <summary>  Top strategy. The command is added to the front of the queue</summary>
    /// <param name="command"> The command to add to the front of the queue. </param>
    public TopCommandStrategy(Command command)
    {
        super(command);
    }

    /// <summary> Add command (strategy) to command queue. </summary>
    public void Enqueue()
    {
        getCommandQueue().EnqueueFront(this);
    }
}
