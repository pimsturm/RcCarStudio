package com.github.pimsturm.commandmessenger.Queue;

import com.github.pimsturm.commandmessenger.CommandEventArgs;
import com.github.pimsturm.commandmessenger.IAsyncWorkerJob;
import com.github.pimsturm.commandmessenger.IEventHandler;
import com.github.pimsturm.commandmessenger.SendQueue;
import com.github.pimsturm.commandmessenger.ReceivedCommand;
import com.github.pimsturm.commandmessenger.ReceivedCommandSignal;
import com.github.pimsturm.commandmessenger.Transport.ReceiveHandler;

/// <summary> Queue of received commands.  </summary>
public class ReceiveCommandQueue extends CommandQueue {

    //public event handleMessage<CommandEventArgs> newLineReceived;
    public IEventHandler NewLineReceived;

    private ReceiveHandler _receivedCommandHandler;
    private final ReceivedCommandSignal _receivedCommandSignal = new ReceivedCommandSignal();

    public ReceiveCommandQueue(ReceiveHandler receivedCommandHandler)
    {
        super();
        _receivedCommandHandler = receivedCommandHandler;
    }

    /// <summary> Dequeue the received command. </summary>
    /// <returns> The received command. </returns>
    public ReceivedCommand DequeueCommand()
    {
        synchronized (Queue)
        {
            return DequeueCommandInternal();
        }
    }

    @Override
    protected void set_processQueue()
    {
        _processQueue = new ProcessQueue();
    }

    public class ProcessQueue implements IAsyncWorkerJob {
        @Override
        public boolean execute() {
            ReceivedCommand dequeueCommand;
            boolean hasMoreWork;

            synchronized (Queue) {
                dequeueCommand = DequeueCommandInternal();
                hasMoreWork = !getIsEmpty();
            }

            if (dequeueCommand != null) {
                //_receivedCommandHandler.processCommand(dequeueCommand);
            }

            return hasMoreWork;
        }
    }

    public ReceivedCommand WaitForCmd(int timeOut, int cmdId, SendQueue sendQueueState)
    {
        return _receivedCommandSignal.waitForCmd(timeOut, cmdId, sendQueueState);
    }

    /// <summary> Queue the received command. </summary>
    /// <param name="receivedCommand"> The received command. </param>
    public void QueueCommand(ReceivedCommand receivedCommand)
    {
        QueueCommand(new CommandStrategy(receivedCommand));
    }

    /// <summary> Queue the command wrapped in a command strategy. </summary>
    /// <param name="commandStrategy"> The command strategy. </param>
    public void QueueCommand(CommandStrategy commandStrategy)
    {
        if (getIsSuspended())
        {
            // Directly send this command to waiting thread
            boolean addToQueue = _receivedCommandSignal.processCommand((ReceivedCommand) commandStrategy.getCommand());
            // check if the item needs to be added to the queue for later processing. If not return directly
            if (!addToQueue) return;
        }

        synchronized (Queue)
        {
            // Process all generic enqueue strategies
            Queue.Enqueue(commandStrategy);
            for (GeneralStrategy generalStrategy : GeneralStrategies) { generalStrategy.OnEnqueue(); }
        }

        if (!getIsSuspended())
        {
            // Give a signal to indicate that a new item has been queued
            SignalWorker();
            if (NewLineReceived != null) NewLineReceived.invokeEvent(this, new CommandEventArgs(commandStrategy.getCommand()));
        }
    }

    private ReceivedCommand DequeueCommandInternal()
    {
        ReceivedCommand receivedCommand = null;
        if (!getIsEmpty())
        {
            for (GeneralStrategy generalStrategy : GeneralStrategies) { generalStrategy.OnDequeue(); }
            CommandStrategy commandStrategy = Queue.Dequeue();
            receivedCommand = (ReceivedCommand)commandStrategy.getCommand();
        }
        return receivedCommand;
    }
}
