package com.github.pimsturm.commandmessenger;

import android.util.Log;

/**
 * This class will trigger the main thread when a specific command is received on the ReceiveCommandQueue thread
 * this is used when synchronously waiting for an acknowledge command in BlockedTillReply
 */
public class ReceivedCommandSignal {
    private static final String TAG = "ReceivedCommandSignal";
    private int cmdIdToMatch;
    private SendQueue sendQueueState;
    private ReceivedCommand receivedCommand;

    private final Object _lock = new Object();
    private final EventWaiter eventWaiter = new EventWaiter();

    /**
     * Wait function.
     * @param timeOut time-out in ms
     * @param cmdId
     * @param sendQueueState
     * @return
     */
    public ReceivedCommand waitForCmd(int timeOut, int cmdId, SendQueue sendQueueState)
    {
        synchronized (_lock)
        {
            receivedCommand = null;
            cmdIdToMatch = cmdId;
            this.sendQueueState = sendQueueState;
        }

        try {
            if (eventWaiter.waitOne(timeOut) == WaitState.TimeOut) {
                return null;
            }
        } catch (InterruptedException e) {
            Log.d(TAG, e.getMessage());
            return null;
        }

        return receivedCommand;
    }


    /**
     *  Process command.
     * @param receivedCommand
     * @return false if it needs to be sent to the main thread or true if it needs to used in a queue.
     */
    public boolean processCommand(ReceivedCommand receivedCommand)
    {
        synchronized (_lock)
        {
            if (receivedCommand.getCmdId() == cmdIdToMatch)
            {
                this.receivedCommand = receivedCommand;
                eventWaiter.set();
                return false;
            }

            return (sendQueueState != SendQueue.ClearQueue);
        }
    }
}
