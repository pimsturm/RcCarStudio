package com.github.pimsturm.commandmessenger;

import android.util.Log;

import com.github.pimsturm.commandmessenger.Queue.ReceiveCommandQueue;
import com.github.pimsturm.commandmessenger.Transport.ITransport;

import java.io.UnsupportedEncodingException;

/**
 * Manager for data over transport layer.
 */
public class CommunicationManager {
    private static final String TAG = "communicationManager";
    private final Object sendCommandDataLock = new Object();        // The process serial data lock
    private final ReceiveCommandQueue receiveCommandQueue;

    private ITransport transport;


    /**
     * Constructor
     * @param transport
     * @param receiveCommandQueue
     */
    public CommunicationManager(ITransport transport, ReceiveCommandQueue receiveCommandQueue)
    {
        this.transport = transport;

        this.receiveCommandQueue = receiveCommandQueue;

    }


    /**
     * Writes a string to the transport layer.
     * @param value The string to write.
     */
    public void writeLine(String value)
    {
        write(value + "\r\n");
    }


    /**
     * Writes a string to the transport layer.
     * @param value The string to write.
     */
    public void write(String value)
    {
        transport.write(value);
    }

    /**
     * Directly executes the send command operation.
     * @param sendCommand The command to send.
     * @param sendQueueState Property to optionally clear the send and receive queues.
     * @return The received command will only be valid if the ReqAc of the command is true.
     */
    public ReceivedCommand executeSendCommand(SendCommand sendCommand, SendQueue sendQueueState)
    {
        // Disable listening, all callbacks are disabled until after command was sent

        ReceivedCommand ackCommand;
        synchronized (sendCommandDataLock)
        {
            sendCommand.initArguments();

            if (sendCommand.getReqAc())
            {
                // Stop processing receive queue before sending. Wait until receive queue is actually done
                receiveCommandQueue.Suspend();
            }

//            if (printLfCr)
//                writeLine(sendCommand.commandString());
//            else
                write(sendCommand.commandString());

            ackCommand = sendCommand.getReqAc() ? blockedTillReply(sendCommand.getAckCmdId(), sendCommand.getTimeout(), sendQueueState) : new ReceivedCommand();
        }

        if (sendCommand.getReqAc())
        {
            // Stop processing receive queue before sending
            receiveCommandQueue.Resume();
        }

        return ackCommand;
    }

    /**
     * Directly executes the send string operation.
     * @param commandString The string to send.
     * @param sendQueueState Property to optionally clear the send and receive queues.
     * @return The received command is added for compatibility. It will not yield a response.
     */
    public ReceivedCommand executeSendString(String commandString, SendQueue sendQueueState)
    {
        synchronized (sendCommandDataLock)
        {
//            if (printLfCr)
//            {
//                writeLine(commandString);
//            }
//            else
//            {
                write(commandString);
//            }
        }
        return new ReceivedCommand();
    }

    /**
     * Blocks until acknowledgement reply has been received.
     * @param ackCmdId acknowledgement command ID
     * @param timeout Timeout on acknowledge command.
     * @param sendQueueState
     * @return A received command.
     */
    private ReceivedCommand blockedTillReply(int ackCmdId, int timeout, SendQueue sendQueueState)
    {
        // Wait for matching command
        ReceivedCommand receivedCommand = receiveCommandQueue.WaitForCmd(timeout, ackCmdId, sendQueueState);
        if (receivedCommand == null)
        {
            return new ReceivedCommand();
        } else {
            return receivedCommand;
        }
    }


}
