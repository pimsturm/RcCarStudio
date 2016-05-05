package com.github.pimsturm.commandmessenger;


import com.github.pimsturm.commandmessenger.Queue.CommandStrategy;
import com.github.pimsturm.commandmessenger.Queue.GeneralStrategy;
import com.github.pimsturm.commandmessenger.Queue.ReceiveCommandQueue;
import com.github.pimsturm.commandmessenger.Queue.SendCommandQueue;
import com.github.pimsturm.commandmessenger.Transport.Bluetooth.BluetoothConnectionManager;
import com.github.pimsturm.commandmessenger.Transport.ITransport;
import com.github.pimsturm.commandmessenger.Transport.ReceiveHandler;


/**
 * Command messenger main class
  */

//Idisposable
public class CmdMessenger {
    private Settings settings;
    private CommunicationManager communicationManager;                  // The communication manager
    private ITransport connectionManager;               // The connection manager
    private SendCommandQueue sendCommandQueue;                          // The queue of commands to be sent
    private ReceiveCommandQueue receiveCommandQueue;                    // The queue of commands to be processed
    private ReceiveHandler mReceiveHandler = new ReceiveHandler();      // Handles the received commands

    /**
     * Get an object with all the settings for cmdMessenger.
     * @return a settings object
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Gets the handler for received commands
     * @return mReceiveHandler
     */
    public ReceiveHandler getmReceiveHandler() {
        return mReceiveHandler;
    }

    public ITransport getConnectionManager() {
        return connectionManager;
    }
    /**
     * Event handler for one or more lines received
     */
    public IEventHandler newLineReceived;

    /**
     * Event handler for a new line sent
     */
    public IEventHandler newLineSent;

    /**
     * Constructor.
     */
    public CmdMessenger()
    {
        //Logger.open(@"sendCommands.txt");
        Logger.setDirectFlush(true);

        settings = Settings.getInstance();
        connectionManager = connectionManagerFactory();

        receiveCommandQueue = new ReceiveCommandQueue(mReceiveHandler);
        communicationManager = new CommunicationManager(connectionManager, receiveCommandQueue);
        sendCommandQueue = new SendCommandQueue(communicationManager, 255);

        receiveCommandQueue.NewLineReceived = new EventHandler <CommandEventArgs>() {
            @Override
            public void invokeEvent(Object sender, CommandEventArgs e) {
                invokeNewLineEvent(newLineReceived, e);
            }
        };
        sendCommandQueue.NewLineSent = new EventHandler <CommandEventArgs>() {
            @Override
            public void invokeEvent(Object sender, CommandEventArgs e) {
                invokeNewLineEvent(newLineSent, e);
            }

        };

        sendCommandQueue.Start();
        receiveCommandQueue.Start();
    }

    public ITransport connectionManagerFactory() {
        return new BluetoothConnectionManager(this);
    }
    /**
     * Disposal of CmdMessenger
     */
    public void dispose()
    {
        dispose(true);
        //GC.SuppressFinalize(this);
    }

    /**
     * Attaches default callback for unsupported commands.
     * @param newFunction The callback function.
     */
    public void attach(IMessengerCallbackFunction newFunction)
    {
        mReceiveHandler.attach(newFunction);
    }

    /**
     * Attaches default callback for certain Message ID.
     * @param messageId Command ID.
     * @param newFunction The callback function.
     */
    public void attach(int messageId, IMessengerCallbackFunction newFunction)
    {
        mReceiveHandler.attach(messageId, newFunction);
    }

    /**
     * Sends a command.
     *	  If no command acknowledge is requested, the command will be send asynchronously: it will be put on the top of the send queue
     * 	  If a  command acknowledge is requested, the command will be send synchronously:  the program will block until the acknowledge command
     * 	  has been received or the timeout has expired.
     * 	  Based on ClearQueueState, the send- and receive-queues are left intact or are cleared
     * @param sendCommand The command to sent.
     * @param sendQueueState  Property to optionally clear/wait the send queue
     * @param receiveQueueState Property to optionally clear/wait the receive queue
     * @return A received command. The received command will only be valid if the ReqAc of the command is true.
     */
    public ReceivedCommand sendCommand(SendCommand sendCommand, SendQueue sendQueueState, ReceiveQueue receiveQueueState)
    {
        return sendCommand(sendCommand, sendQueueState, receiveQueueState, UseQueue.UseQueue);
    }

    /**
     * Sends a command.
     *	  If no command acknowledge is requested, the command will be send asynchronously: it will be put on the top of the send queue
     * 	  If a  command acknowledge is requested, the command will be send synchronously:  the program will block until the acknowledge command
     * 	  has been received or the timeout has expired.
     * 	  Based on ClearQueueState, the send- and receive-queues are left intact or are cleared
     * @param sendCommand The command to sent.
     * @param sendQueueState  Property to optionally clear/wait the send queue
     * @return A received command. The received command will only be valid if the ReqAc of the command is true.
     */
    public ReceivedCommand sendCommand(SendCommand sendCommand, SendQueue sendQueueState)
    {
        return sendCommand(sendCommand, sendQueueState, ReceiveQueue.Default, UseQueue.UseQueue);
    }

    /**
     * Sends a command.
     *	  If no command acknowledge is requested, the command will be send asynchronously: it will be put on the top of the send queue
     * 	  If a  command acknowledge is requested, the command will be send synchronously:  the program will block until the acknowledge command
     * 	  has been received or the timeout has expired.
     * 	  Based on ClearQueueState, the send- and receive-queues are left intact or are cleared
     * @param sendCommand The command to sent.
     * @param receiveQueueState Property to optionally clear/wait the receive queue
     * @return A received command. The received command will only be valid if the ReqAc of the command is true.
     */
    public ReceivedCommand sendCommand(SendCommand sendCommand, ReceiveQueue receiveQueueState)
    {
        return sendCommand(sendCommand, SendQueue.InFrontQueue, receiveQueueState, UseQueue.UseQueue);
    }

    /**
     * Sends a command.
     *	  If no command acknowledge is requested, the command will be send asynchronously: it will be put on the top of the send queue
     * 	  If a  command acknowledge is requested, the command will be send synchronously:  the program will block until the acknowledge command
     * 	  has been received or the timeout has expired.
     * 	  Based on ClearQueueState, the send- and receive-queues are left intact or are cleared
     * @param sendCommand The command to sent.
     */
    public void sendCommand(SendCommand sendCommand)
    {
        sendCommand.initArguments();
        connectionManager.write(sendCommand.commandString());
    }

    /**
     * Sends a command.
     *	  If no command acknowledge is requested, the command will be send asynchronously: it will be put on the top of the send queue
     * 	  If a  command acknowledge is requested, the command will be send synchronously:  the program will block until the acknowledge command
     * 	  has been received or the timeout has expired.
     * 	  Based on ClearQueueState, the send- and receive-queues are left intact or are cleared
     * @param sendCommand The command to sent.
     * @param sendQueueState  Property to optionally clear/wait the send queue
     * @param receiveQueueState Property to optionally clear/wait the receive queue
     * @param useQueue Property to optionally bypass the queue
     * @return A received command. The received command will only be valid if the ReqAc of the command is true.
     */
    public ReceivedCommand sendCommand(SendCommand sendCommand, SendQueue sendQueueState, ReceiveQueue receiveQueueState, UseQueue useQueue)
    {
        boolean synchronizedSend = (sendCommand.getReqAc() || useQueue == UseQueue.BypassQueue);

        // When waiting for an acknowledge, it is typically best to wait for the ReceiveQueue to be empty
        // This is thus the default state
        if (sendCommand.getReqAc() && receiveQueueState == ReceiveQueue.Default)
        {
            receiveQueueState = ReceiveQueue.WaitForEmptyQueue;
        }

        if (sendQueueState == SendQueue.ClearQueue )
        {
            // Clear receive queue
            receiveCommandQueue.Clear();
        }

        if (receiveQueueState == ReceiveQueue.ClearQueue )
        {
            // Clear send queue
            sendCommandQueue.Clear();
        }

        // If synchronized sending, the only way to get command at end of queue is by waiting
        if (sendQueueState == SendQueue.WaitForEmptyQueue ||
                (synchronizedSend && sendQueueState == SendQueue.AtEndQueue)
                )
        {
            boolean isEmpty = false;
            synchronized (this){
               while (!isEmpty) {
                     try {
                        wait(100);
                       } catch(InterruptedException e){}
                   isEmpty = sendCommandQueue.getIsEmpty();
               }
            }
        }

        if (receiveQueueState == ReceiveQueue.WaitForEmptyQueue)
        {
            boolean isEmpty = false;
            synchronized (this){
                while (!isEmpty) {
                    try {
                        wait(100);
                    } catch(InterruptedException e){}
                    isEmpty = receiveCommandQueue.getIsEmpty();
                }
            }
        }

        if (synchronizedSend)
        {
            return sendCommandSync(sendCommand, sendQueueState);
        }

        if (sendQueueState != SendQueue.AtEndQueue)
        {
            // Put command at top of command queue
            sendCommandQueue.SendCommand(sendCommand);
        }
        else
        {
            // Put command at bottom of command queue
            sendCommandQueue.QueueCommand(sendCommand);
        }
        return new ReceivedCommand();
    }

    /**
     * Synchronized send a command.
     * @param sendCommand The command to sent.
     * @param sendQueueState Property to optionally clear/wait the send queue.
     * @return .
     */
    public ReceivedCommand sendCommandSync(SendCommand sendCommand, SendQueue sendQueueState)
    {
        // Directly call execute command
        connectionManager.write(sendCommand.commandString());

        ReceivedCommand resultSendCommand = communicationManager.executeSendCommand(sendCommand, sendQueueState);
        invokeNewLineEvent(newLineSent, new CommandEventArgs(sendCommand));
        return resultSendCommand;
    }

    /**
     * Put the command at the back of the sent queue.
     * @param sendCommand The command to sent.
     */
    public void queueCommand(SendCommand sendCommand)
    {
        sendCommandQueue.QueueCommand(sendCommand);
    }

    /**
     * Put  a command wrapped in a strategy at the back of the sent queue.
     * @param commandStrategy The command strategy.
     */
    public void queueCommand(CommandStrategy commandStrategy)
    {
        sendCommandQueue.QueueCommand(commandStrategy);
    }

    /**
     * Adds a general command strategy to the receive queue. This will be executed on every enqueued and dequeued command.
     * @param generalStrategy The general strategy for the receive queue.
     */
    public void addReceiveCommandStrategy(GeneralStrategy generalStrategy)
    {
        receiveCommandQueue.AddGeneralStrategy(generalStrategy);
    }

    /**
     *  Adds a general command strategy to the send queue. This will be executed on every enqueued and dequeued command.
     * @param generalStrategy The general strategy for the send queue.
     */
    public void addSendCommandStrategy(GeneralStrategy generalStrategy)
    {
        sendCommandQueue.AddGeneralStrategy(generalStrategy);
    }

    /**
     * Clears the receive queue.
     */
    public void clearReceiveQueue()
    {
        receiveCommandQueue.Clear();
    }

    /**
     * Clears the send queue.
     */
    public void clearSendQueue()
    {
        sendCommandQueue.Clear();
    }

    /**
     * Helper function to call an event.
     * @param newLineHandler The event handler.
     * @param newLineArgs Argumnets of the event handler
     */
    private void invokeNewLineEvent(IEventHandler newLineHandler, CommandEventArgs newLineArgs)
    {
        if (newLineHandler == null) return;

        //Directly call
        newLineHandler.invokeEvent(this, newLineArgs);
    }

    protected void dispose(boolean disposing)
    {
        if (disposing)
        {
//            communicationManager.dispose();
            sendCommandQueue.Dispose();
            receiveCommandQueue.Dispose();
        }
    }
}
