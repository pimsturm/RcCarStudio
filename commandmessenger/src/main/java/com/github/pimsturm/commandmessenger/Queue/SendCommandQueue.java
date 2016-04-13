package com.github.pimsturm.commandmessenger.Queue;

import com.github.pimsturm.commandmessenger.Command;
import com.github.pimsturm.commandmessenger.CommandEventArgs;
import com.github.pimsturm.commandmessenger.CommunicationManager;
import com.github.pimsturm.commandmessenger.IAsyncWorkerJob;
import com.github.pimsturm.commandmessenger.IEventHandler;
import com.github.pimsturm.commandmessenger.SendCommand;
import com.github.pimsturm.commandmessenger.SendQueue;

public class SendCommandQueue extends CommandQueue {
    public IEventHandler NewLineSent;

    private CommunicationManager _communicationManager;
    private int _sendBufferMaxLength = 62;
    private String _sendBuffer = "";
    private int _commandCount;

    private int MaxQueueLength; //uint
    public int getMaxQueueLength() { return MaxQueueLength; }
    public void setMaxQueueLength(int maxQueueLength) {MaxQueueLength = maxQueueLength;}

    /// <summary> send command queue constructor. </summary>
    /// <param name="communicationManager">The communication manager instance</param>
    /// <param name="sendBufferMaxLength">Length of the send buffer</param>
    public SendCommandQueue(CommunicationManager communicationManager, int sendBufferMaxLength)
    {
        super();
        MaxQueueLength = 5000;

        _communicationManager = communicationManager;
        _sendBufferMaxLength = sendBufferMaxLength;
    }

    @Override
    protected void set_processQueue()
    {
        _processQueue = new ProcessQueue();
    }

    public class ProcessQueue implements IAsyncWorkerJob {
        @Override
        public boolean execute() {
            SendCommandsFromQueue();
            synchronized (Queue) {
                return !getIsEmpty();
            }
        }
    }

    /// <summary> Sends the commands from queue. All commands will be combined until either
    /// 		   the SendBufferMaxLength  has been reached or if a command requires an acknowledge
    /// 		   </summary>
    private void SendCommandsFromQueue()
    {
        _commandCount = 0;
        _sendBuffer = "";
        CommandStrategy eventCommandStrategy = null;

        // while maximum buffer string is not reached, and command in queue
        while (_sendBuffer.length() < _sendBufferMaxLength && Queue.size() > 0)
        {
            synchronized (Queue)
            {
                CommandStrategy commandStrategy = !getIsEmpty() ? Queue.Peek() : null;
                if (commandStrategy != null)
                {
                    if (commandStrategy.getCommand() != null)
                    {
                        SendCommand sendCommand = (SendCommand)commandStrategy.getCommand();

                        if (sendCommand.getReqAc())
                        {
                            if (_commandCount > 0)
                            {
                                break;
                            }
                            SendSingleCommandFromQueue(commandStrategy);
                        }
                        else
                        {
                            eventCommandStrategy = commandStrategy;
                            AddToCommandString(commandStrategy);
                        }
                    }
                }
            }
            // event callback outside lock for performance
            if (eventCommandStrategy != null)
            {
                if (NewLineSent != null) NewLineSent.invokeEvent(this, new CommandEventArgs(eventCommandStrategy.getCommand()));
                eventCommandStrategy = null;
            }
        }

        // Now check if a command string has been filled
        if (_sendBuffer.length() > 0)
        {
            _communicationManager.executeSendString(_sendBuffer, SendQueue.InFrontQueue);
        }
    }

    /// <summary> Sends a float command from the queue. </summary>
    /// <param name="commandStrategy"> The command strategy to send. </param>
    private void SendSingleCommandFromQueue(CommandStrategy commandStrategy)
    {
        // Dequeue
        synchronized (Queue)
        {
            commandStrategy.DeQueue();
            // Process all generic dequeue strategies
            for (GeneralStrategy generalStrategy : GeneralStrategies) { generalStrategy.OnDequeue(); }
        }
        // Send command
        if (commandStrategy.getCommand() != null)
            _communicationManager.executeSendCommand((SendCommand) commandStrategy.getCommand(), SendQueue.InFrontQueue);
    }

    /// <summary> Adds a commandStrategy to the commands string.  </summary>
    /// <param name="commandStrategy"> The command strategy to add. </param>
    private void AddToCommandString(CommandStrategy commandStrategy)
    {
        // Dequeue
        synchronized (Queue)
        {
            commandStrategy.DeQueue();
            // Process all generic dequeue strategies
            for (GeneralStrategy generalStrategy : GeneralStrategies) { generalStrategy.OnDequeue(); }
        }
        // Add command
        if (commandStrategy.getCommand() != null)
        {
            _commandCount++;
            _sendBuffer += commandStrategy.getCommand().commandString();
            if (_communicationManager.getPrintLfCr()) { _sendBuffer += "\r\n"; }
        }
    }

    /// <summary> Sends a command. Note that the command is put at the front of the queue </summary>
    /// <param name="sendCommand"> The command to sent. </param>
    public void SendCommand(SendCommand sendCommand)
    {
        // Add command to front of queue
        QueueCommand(new TopCommandStrategy(sendCommand));
    }

    /// <summary> Queue the send command. </summary>
    /// <param name="sendCommand"> The command to sent. </param>
    public void QueueCommand(SendCommand sendCommand)
    {
        QueueCommand(new CommandStrategy(sendCommand));
    }

    /// <summary> Queue the send command wrapped in a command strategy. </summary>
    /// <param name="commandStrategy"> The command strategy. </param>
    public void QueueCommand(CommandStrategy commandStrategy)
    {
        while (Queue.size() > MaxQueueLength)
        {
            Thread.yield();
        }

        synchronized (Queue)
        {
            // Process commandStrategy enqueue associated with command
            commandStrategy.setCommandQueue(Queue);
            Command command = commandStrategy.getCommand();
            command.setCommunicationManager(_communicationManager);
            ((SendCommand)commandStrategy.getCommand()).initArguments();

            commandStrategy.Enqueue();

            // Process all generic enqueue strategies
            for (GeneralStrategy generalStrategy : GeneralStrategies) { generalStrategy.OnEnqueue(); }
        }

        SignalWorker();
    }
}
