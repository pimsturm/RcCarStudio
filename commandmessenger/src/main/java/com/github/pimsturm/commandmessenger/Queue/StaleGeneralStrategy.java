package com.github.pimsturm.commandmessenger.Queue;

import com.github.pimsturm.commandmessenger.TimeUtils;

/// <summary> Stale strategy. Any command older than the time-out is removed from the queue</summary>
public class StaleGeneralStrategy extends GeneralStrategy{
    private final long _commandTimeOut;

    /// <summary> Stale strategy. Any command older than the time-out is removed from the queue</summary>
    /// <param name="commandTimeOut"> The time-out for any commands on the queue. </param>
    public StaleGeneralStrategy(long commandTimeOut)
    {
        _commandTimeOut = commandTimeOut;
    }

    /// <summary> Remove this command (strategy) from command queue. </summary>
    public void OnDequeue()
    {
        // Remove commands that have gone stale
        long currentTime = TimeUtils.millis;
        // Work from oldest to newest
        for (int item = 0; item < getCommandQueue().size(); item++)
        {
            long age = currentTime - getCommandQueue().get(item).getCommand().getTimeStamp();
            if (age > _commandTimeOut && getCommandQueue().size() > 1 )
            {
                getCommandQueue().remove(item);
            }
            else
            {
                // From here on commands are newer, so we can stop
                break;
            }
        }
    }
}
