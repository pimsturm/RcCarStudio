package com.github.pimsturm.commandmessenger;

import com.github.pimsturm.commandmessenger.Queue.CommandStrategy;
import com.github.pimsturm.commandmessenger.Queue.GeneralStrategy;
import com.github.pimsturm.commandmessenger.Queue.ReceiveCommandQueue;
import com.github.pimsturm.commandmessenger.Queue.SendCommandQueue;
import com.github.pimsturm.commandmessenger.Transport.ITransport;

import java.util.HashMap;


/**
 * Command messenger main class
  */

//Idisposable
public class CmdMessenger implements IMessengerCallbackFunction {
    private CommunicationManager communicationManager;                 // The communication manager
    private IMessengerCallbackFunction defaultCallback;                 // The default callback
    private HashMap<Integer, IMessengerCallbackFunction> callbackFunctionHashMap;   // List of callbacks
    private SendCommandQueue sendCommandQueue;                         // The queue of commands to be sent
    private ReceiveCommandQueue receiveCommandQueue;                   // The queue of commands to be processed

    /**
     * Definition of the messenger callback function.
     * @param receivedCommand The received command.
     */
    public IMessengerCallbackFunction messengerCallbackFunction;

    /**
     * Event handler for one or more lines received
     */
    public IEventHandler newLineReceived;

    /**
     * Event handler for a new line sent
     */
    public IEventHandler newLineSent;

    /**
     * Gets or sets a flag whether to print a line feed carriage return after each command.
     * @return true if print line feed carriage return, false if not.
     */
    public boolean getPrintLfCr() { return communicationManager.getPrintLfCr(); }
    public void setPrintLfCr(boolean printLfCr) { communicationManager.setPrintLfCr(printLfCr); }

    /**
     * Constructor.
     * @param transport The transport layer.
     * @param boardType Embedded Processor type. Needed to translate variables between sides.
     */
    public CmdMessenger(ITransport transport, BoardType boardType)
    {
        init(transport, boardType, ',', ';', '/', 60);
    }

    /**
     * Constructor.
     * @param transport The transport layer.
     */
    public CmdMessenger(ITransport transport)
    {
        this(transport, BoardType.Bit16);
    }

    /**
     * Constructor.
     * @param transport The transport layer.
     * @param sendBufferMaxLength The maximum size of the send buffer
     * @param boardType Embedded Processor type. Needed to translate variables between sides.
     */
    public CmdMessenger(ITransport transport, int sendBufferMaxLength, BoardType boardType)
    {
        init(transport, boardType, ',', ';', '/', sendBufferMaxLength);
    }

    /**
     * Constructor.
     * @param transport The transport layer.
     * @param sendBufferMaxLength The maximum size of the send buffer
     */
    public CmdMessenger(ITransport transport, int sendBufferMaxLength)
    {
        this(transport, sendBufferMaxLength, BoardType.Bit16);
    }

    /**
     * Constructor.
     * @param transport The transport layer.
     * @param boardType Embedded Processor type. Needed to translate variables between sides.
     * @param fieldSeparator The field separator.
     */
    public CmdMessenger(ITransport transport, BoardType boardType, char fieldSeparator)
    {
        init(transport, boardType, fieldSeparator, ';', '/', 60);
    }

    /**
     * Constructor.
     * @param transport The transport layer.
     * @param boardType Embedded Processor type. Needed to translate variables between sides.
     * @param fieldSeparator The field separator.
     * @param sendBufferMaxLength The maximum size of the send buffer
     */
    public CmdMessenger(ITransport transport, BoardType boardType, char fieldSeparator, int sendBufferMaxLength)
    {
        init(transport, boardType, fieldSeparator, ';', '/', sendBufferMaxLength);
    }

    /**
     * Constructor.
     * @param transport The transport layer.
     * @param boardType Embedded Processor type. Needed to translate variables between sides.
     * @param fieldSeparator The field separator.
     * @param commandSeparator The command separator.
     */
    public CmdMessenger(ITransport transport, BoardType boardType, char fieldSeparator, char commandSeparator)
    {
        init(transport, boardType, fieldSeparator, commandSeparator, commandSeparator, 60);
    }

    /**
     * Constructor.
     * @param transport The transport layer.
     * @param boardType Embedded Processor type. Needed to translate variables between sides.
     * @param fieldSeparator The field separator.
     * @param commandSeparator The command separator.
     * @param escapeCharacter The escape character.
     * @param sendBufferMaxLength The maximum size of the send buffer
     */
    public CmdMessenger(ITransport transport, BoardType boardType, char fieldSeparator, char commandSeparator,
                        char escapeCharacter, int sendBufferMaxLength)
    {
        init(transport, boardType, fieldSeparator, commandSeparator, escapeCharacter, sendBufferMaxLength);
    }

    /**
     * Initializes this object.
     * @param transport The transport layer.
     * @param boardType Embedded Processor type. Needed to translate variables between sides.
     * @param fieldSeparator The field separator.
     * @param commandSeparator The command separator.
     * @param escapeCharacter The escape character.
     * @param sendBufferMaxLength The maximum size of the send buffer
     */
    private void init(ITransport transport, BoardType boardType, char fieldSeparator, char commandSeparator,
                      char escapeCharacter, int sendBufferMaxLength)
    {
        //Logger.open(@"sendCommands.txt");
        Logger.setDirectFlush(true);

        receiveCommandQueue = new ReceiveCommandQueue(this); // this.handleMessage = Delegate
        communicationManager = new CommunicationManager(transport, receiveCommandQueue, boardType, commandSeparator, fieldSeparator, escapeCharacter);
        sendCommandQueue = new SendCommandQueue(communicationManager, sendBufferMaxLength);

        setPrintLfCr(false);

        //receiveCommandQueue.newLineReceived = (o, e) -> invokeNewLineEvent(newLineReceived, e);
        receiveCommandQueue.NewLineReceived = new EventHandler <CommandEventArgs>() {
            @Override
            public void invokeEvent(Object sender, CommandEventArgs e) {
                invokeNewLineEvent(newLineReceived, e);
            }
        };
        //sendCommandQueue.newLineSent        += (o, e) -> invokeNewLineEvent(newLineSent, e);
        sendCommandQueue.NewLineSent = new EventHandler <CommandEventArgs>() {
            @Override
            public void invokeEvent(Object sender, CommandEventArgs e) {
                invokeNewLineEvent(newLineSent, e);
            }

        };

        Escaping.setEscapeChars(fieldSeparator, commandSeparator, escapeCharacter);
        callbackFunctionHashMap = new HashMap<Integer, IMessengerCallbackFunction>();

        sendCommandQueue.Start();
        receiveCommandQueue.Start();
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
     * Stop listening and end serial port connection.
     * @return true if it succeeds, false if it fails.
     */
    public boolean disconnect()
    {
        return communicationManager.disconnect();
    }

    /**
     * Starts serial port connection and start listening.
     * @return true if it succeeds, false if it fails.
     */
    public boolean connect()
    {
        return communicationManager.connect();
    }

    /**
     * Attaches default callback for unsupported commands.
     * @param newFunction The callback function.
     */
    public void attach(IMessengerCallbackFunction newFunction)
    {
        defaultCallback = newFunction;
    }

    /**
     * Attaches default callback for certain Message ID.
     * @param messageId Command ID.
     * @param newFunction The callback function.
     */
    public void attach(int messageId, IMessengerCallbackFunction newFunction)
    {
        callbackFunctionHashMap.put(messageId, newFunction);
    }

    /**
     * Gets or sets the time stamp of the last command line received.
     * @return The last line time stamp.
     */
    public long getLastReceivedCommandTimeStamp()
    {
        return communicationManager.getLastLineTimeStamp();
    }

    /**
     * Handle message.
     * @param receivedCommand The received command.
     */
    public void handleMessage(ReceivedCommand receivedCommand)
    {
        IMessengerCallbackFunction callback = null;

        if (receivedCommand.getOk())
        {
            if (callbackFunctionHashMap.containsKey(receivedCommand.getCmdId()))
            {
                callback = callbackFunctionHashMap.get(receivedCommand.getCmdId());
            }
            else
            {
                if (defaultCallback != null) callback = defaultCallback;
            }
        }
        else
        {
            // Empty command
            receivedCommand = new ReceivedCommand(communicationManager);
        }

        invokeCallBack(callback, receivedCommand);
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
     * @return A received command. The received command will only be valid if the ReqAc of the command is true.
     */
    public ReceivedCommand sendCommand(SendCommand sendCommand)
    {
        return sendCommand(sendCommand, SendQueue.InFrontQueue, ReceiveQueue.Default, UseQueue.UseQueue);
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
        return new ReceivedCommand(communicationManager);
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
     * @param newLineArgs
     */
    private void invokeNewLineEvent(IEventHandler newLineHandler, CommandEventArgs newLineArgs)
    {
        if (newLineHandler == null) return;

        //Directly call
        newLineHandler.invokeEvent(this, newLineArgs);
    }

    /**
     * Helper function to Invoke or directly call callback function.
     * @param messengerCallbackFunction The messenger callback function.
     * @param command The command.
     */
    private void invokeCallBack(IMessengerCallbackFunction messengerCallbackFunction, ReceivedCommand command)
    {
        if (messengerCallbackFunction == null) return;

        //Directly call
        messengerCallbackFunction.handleMessage(command);
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
