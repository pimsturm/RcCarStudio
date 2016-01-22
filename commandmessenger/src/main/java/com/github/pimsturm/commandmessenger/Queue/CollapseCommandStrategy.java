package com.github.pimsturm.commandmessenger.Queue;

import com.github.pimsturm.commandmessenger.Command;

/// <summary> Collapse command strategy.
/// 		  The purpose of the strategy is to avoid duplicates of a certain command on the queue
/// 		  to avoid lagging </summary>
public class CollapseCommandStrategy extends CommandStrategy{
    /// <summary>  Collapse strategy. </summary>
    /// <param name="command"> The command that will be collapsed on the queue. </param>
    public CollapseCommandStrategy(Command command) { super(command);}

    /// <summary> Add command (strategy) to command queue. </summary>
    public void Enqueue()
    {
        // find if there already is a command with the same CmdId
        int index = getIndex(getCommand().getCmdId());
        if (index < 0)
        {
            // if not, add to the back of the queue
            getCommandQueue().Enqueue(this);
        }
        else
        {
            // if on the queue, replace with new command
            getCommandQueue().set(index, this);
        }
    }

    private int getIndex(int cmdId)
    {
        for (int i = 0; i < getCommandQueue().size(); i++)
        {
            CommandStrategy commandStrategy = getCommandQueue().get(i);
            if (cmdId == commandStrategy.getCommand().getCmdId())
            {
                return i;
            }
        }

        return -1;
    }
}
