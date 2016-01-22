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
    private final Object parseLinesLock = new Object();
    private final ReceiveCommandQueue receiveCommandQueue;

    private ITransport transport;
    private final IsEscaped isEscaped;                                       // The is escaped

    private String buffer = "";

    /**
     * The field separator
     */
    private char fieldSeparator;
    public char getFieldSeparator() {
        return fieldSeparator;
    }

    /**
     * The command separator
     */
    private char commandSeparator;
    public char getCommandSeparator() {
        return commandSeparator;
    }

    /**
     * The escape character
     */
    private char escapeCharacter;
    public char getEscapeCharacter() {
        return escapeCharacter;
    }

    private boolean printLfCr;
    public boolean getPrintLfCr() {
        return printLfCr;
    }

    /**
     * Gets or sets a whether to print a line feed carriage return after each command.
     * @param printLfCr true if print line feed carriage return, false if not.
     */
    public void setPrintLfCr(boolean printLfCr) {
        this.printLfCr = printLfCr;
    }

    private static BoardType boardType;
    public static void setBoardType(BoardType boardType) {
        CommunicationManager.boardType = boardType;
    }
    public static BoardType getBoardType() {
        return boardType;
    }

    private long lastLineTimeStamp;

    /**
     * Gets or sets the time stamp of the last received line.
     * @return time stamp of the last received line.
     */
    public long getLastLineTimeStamp() {
        return lastLineTimeStamp;
    }

    /**
     * Constructor
     * @param transport
     * @param receiveCommandQueue
     * @param boardType The Board Type.
     * @param commandSeparator The End-Of-Line separator.
     * @param fieldSeparator
     * @param escapeCharacter The escape character.
     */
    public CommunicationManager(ITransport transport, ReceiveCommandQueue receiveCommandQueue,
                                BoardType boardType, char commandSeparator,  char fieldSeparator, char escapeCharacter)
    {
        this.transport = transport;
        this.transport.setDataReceived(new newDataReceived());

        this.receiveCommandQueue = receiveCommandQueue;

        CommunicationManager.boardType = boardType;
        this.commandSeparator = commandSeparator;
        this.fieldSeparator = fieldSeparator;
        this.escapeCharacter = escapeCharacter;

        isEscaped = new IsEscaped();
    }

/*
    public void dispose()
    {
        dispose(true);
        //GC.SuppressFinalize(this);
    }
*/

    public class newDataReceived implements IEventHandler<CommandEventArgs>{
        @Override
        public void invokeEvent(Object o, CommandEventArgs e) //EventArgs
        {
            parseLines();
        }
    }

    /**
     * Connects to a transport layer defined through the current settings.
     * @return true if it succeeds, false if it fails.
     */
    public boolean connect()
    {
        return !transport.isConnected() && transport.connect();
    }

    /**
     * Stops listening to the transport layer
     * @return true if it succeeds, false if it fails.
     */
    public boolean disconnect()
    {
        return transport.isConnected() && transport.disconnect();
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
     * Writes a parameter to the transport layer.
     * @param value The value.
     * @param <T> Generic method parameter.
     */
    public <T> void write(T value)
    {
        write(value.toString());
    }

    /**
     * Writes a parameter to the transport layer followed by a NewLine.
     * @param value The value.
     * @param <T> Generic method parameter.
     */
    public <T>void writeLine(T value)
    {
        writeLine(value.toString());
    }

    /**
     * Writes a string to the transport layer.
     * @param value The string to write.
     */
    public void write(String value)
    {
        try {
            byte[] writeBytes = value.getBytes("ISO-8859-1");
            transport.write(writeBytes);
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "Unsupported character set");
        }
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
            sendCommand.communicationManager = this;
            sendCommand.initArguments();

            if (sendCommand.getReqAc())
            {
                // Stop processing receive queue before sending. Wait until receive queue is actually done
                receiveCommandQueue.Suspend();
            }

            if (printLfCr)
                writeLine(sendCommand.commandString());
            else
                write(sendCommand.commandString());

            ackCommand = sendCommand.getReqAc() ? blockedTillReply(sendCommand.getAckCmdId(), sendCommand.getTimeout(), sendQueueState) : new ReceivedCommand();
            ackCommand.communicationManager = this;
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
            if (printLfCr)
            {
                writeLine(commandString);
            }
            else
            {
                write(commandString);
            }
        }
        return new ReceivedCommand(this);
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

    private void parseLines()
    {
        synchronized(parseLinesLock)
        {
            byte[] data = transport.read();
            try {
                buffer += new String(data, "ISO-8859-1");
            } catch(UnsupportedEncodingException e) {
                Log.d(TAG, "Unsupported character set");
            }

            do
            {
                String currentLine = parseLine();
                if (currentLine == null || currentLine == "") break;

                lastLineTimeStamp = TimeUtils.millis;
                processLine(currentLine);
            }
            while (true);
        }
    }

    /**
     * Processes the byte message and add to queue.
     * @param line
     */
    private void processLine(String line)
    {
        // read line from raw buffer and make command
        ReceivedCommand currentReceivedCommand = parseMessage(line);
        currentReceivedCommand.setRawString(line);
        // set time stamp
        currentReceivedCommand.setTimeStamp(lastLineTimeStamp);
        // And put on queue
        receiveCommandQueue.QueueCommand(currentReceivedCommand);
    }

    /**
     * Parse message.
     * @param line The received command line.
     * @return The received command.
     */
    private ReceivedCommand parseMessage(String line)
    {
        // Trim and clean line
        String cleanedLine = line.replace('\r', ' ');
        cleanedLine = cleanedLine.replace('\n', ' ');
        cleanedLine = Escaping.remove(cleanedLine, commandSeparator, escapeCharacter);

        return new ReceivedCommand(
                Escaping.split(cleanedLine, fieldSeparator, escapeCharacter, true), this);
    }

    /**
     * Reads a float line from the buffer, if complete.
     * @return Whether a complete line was present in the buffer.
     */
    private String parseLine()
    {
        if (!(buffer == null || buffer == ""))
        {
            // Check if an End-Of-Line is present in the string, and split on first
            //var i = buffer.IndexOf(commandSeparator);
            int i = findNextEol();
            if (i >= 0 && i < buffer.length())
            {
                String line = buffer.substring(0, i + 1);
                if (!(line == null || line == ""))
                {
                    buffer = buffer.substring(i + 1);
                    return line;
                }
                buffer = buffer.substring(i + 1);
                return "";
            }
        }
        return "";
    }

    /**
     * Searches for the next End-Of-Line.
     * @return The location in the string of the next End-Of-Line.
     */
    private int findNextEol()
    {
        int pos = 0;
        while (pos < buffer.length())
        {
            boolean escaped = isEscaped.isEscapedChar(buffer.charAt(pos));
            if (buffer.charAt(pos) == commandSeparator && !escaped)
            {
                return pos;
            }
            pos++;
        }
        return pos;
    }

/*
    protected void dispose(boolean disposing)
    {
        if (disposing)
        {
            // Stop polling
            transport.DataReceived -= newDataReceived;
        }
    }
*/
}
