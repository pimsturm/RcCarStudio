package com.github.pimsturm.commandmessenger.Transport;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import com.github.pimsturm.commandmessenger.CommunicationManager;
import com.github.pimsturm.commandmessenger.Escaping;
import com.github.pimsturm.commandmessenger.IMessengerCallbackFunction;
import com.github.pimsturm.commandmessenger.IsEscaped;
import com.github.pimsturm.commandmessenger.ReceivedCommand;
import com.github.pimsturm.commandmessenger.Settings;
import com.github.pimsturm.commandmessenger.TimeUtils;
import com.github.pimsturm.commandmessenger.Transport.Bluetooth.ConnectedThread;
import com.github.pimsturm.commandmessenger.Transport.Bluetooth.Constants;

import java.io.UnsupportedEncodingException;

/**
 * Handler for data received from the Arduino
 */
public class ReceiveHandler extends Handler {
    private SparseArray<IMessengerCallbackFunction> callbackFunctionHashMap = new SparseArray<>();   // List of callbacks
    private ConnectedThread mBluetoothConnection = null;
    private IMessengerCallbackFunction defaultCallback;                 // The default callback
    private String buffer = "";
    private final IsEscaped isEscaped;                                       // The is escaped
    private final Settings settings;

    /**
     * Creates an instance of the ReceiveHandler.
     */
    public ReceiveHandler() {
        super();
        isEscaped = new IsEscaped();
        settings = Settings.getInstance();
    }

    /**
     * Attaches default callback for unsupported commands.
     *
     * @param newFunction The callback function.
     */
    public void attach(IMessengerCallbackFunction newFunction) {
        defaultCallback = newFunction;
    }

    /**
     * Attaches default callback for certain Message ID.
     *
     * @param messageId   Command ID.
     * @param newFunction The callback function.
     */
    public void attach(int messageId, IMessengerCallbackFunction newFunction) {
        callbackFunctionHashMap.put(messageId, newFunction);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constants.SOCKET_CONNECTED: {
                ConnectedThread bluetoothConnection = (ConnectedThread) msg.obj;
                bluetoothConnection.write("this is a message".getBytes());
                break;
            }
            case Constants.MESSAGE_READ: {
                // Add the string with received data to the buffer
                buffer += (String) msg.obj;

                // Parse the buffer into commands and process them
                // The parsed commands are removed from the buffer.
                parseLines();
                break;
            }
            default:
                break;
        }

    }

    /**
     * Process the received command.
     *
     * @param receivedCommand The received command.
     */
    private void processCommand(ReceivedCommand receivedCommand) {
        IMessengerCallbackFunction callback = null;

        if (receivedCommand.getOk()) {
            // receivedCommand contains a valid command Id
            if (callbackFunctionHashMap.indexOfKey(receivedCommand.getCmdId()) >= 0) {
                // The command Id was found in the list of attached callback methods
                callback = callbackFunctionHashMap.get(receivedCommand.getCmdId());
            } else {
                if (defaultCallback != null) callback = defaultCallback;
            }
        }

        if (callback != null) {
            callback.handleMessage(receivedCommand);
        }
    }

    private void parseLines() {
        do {
            // Get the next line from the buffer
            String currentLine = parseLine();
            // If there is not a complete line then quit.
            if (currentLine == null || currentLine == "") break;

            long lastLineTimeStamp = TimeUtils.millis;
            processLine(currentLine, lastLineTimeStamp);
        }
        while (true);
    }

    /**
     * Processes the line and converts it to a ReceivedCommand.
     *
     * @param line              The received line.
     * @param lastLineTimeStamp timestamp indicates when the line was received.
     */
    private void processLine(String line, long lastLineTimeStamp) {
        // read line from raw buffer and make command
        ReceivedCommand currentReceivedCommand = parseMessage(line);
        currentReceivedCommand.setRawString(line);
        // set time stamp
        currentReceivedCommand.setTimeStamp(lastLineTimeStamp);
        processCommand(currentReceivedCommand);
    }

    /**
     * Parse message.
     *
     * @param line The received command line.
     * @return The received command.
     */
    private ReceivedCommand parseMessage(String line) {
        // Trim and clean line
        String cleanedLine = line.replace('\r', ' ');
        cleanedLine = cleanedLine.replace('\n', ' ');
        cleanedLine = Escaping.remove(cleanedLine, settings.getCommandSeparator(), settings.getEscapeCharacter());

        return new ReceivedCommand(
                Escaping.split(cleanedLine, settings.getFieldSeparator(), settings.getEscapeCharacter(), true));
    }

    /**
     * Reads a float line from the buffer, if complete.
     *
     * @return Whether a complete line was present in the buffer.
     */
    private String parseLine() {
        if (!(buffer == null || buffer == "")) {
            // Check if an End-Of-Line is present in the string, and split on first
            //var i = buffer.IndexOf(commandSeparator);
            int i = findNextEol();
            if (i >= 0 && i < buffer.length()) {
                String line = buffer.substring(0, i + 1);
                if (!(line == null || line == "")) {
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
     *
     * @return The location in the string of the next End-Of-Line.
     */
    private int findNextEol() {
        int pos = 0;
        while (pos < buffer.length()) {
            boolean escaped = isEscaped.isEscapedChar(buffer.charAt(pos));
            if (buffer.charAt(pos) == settings.getCommandSeparator() && !escaped) {
                return pos;
            }
            pos++;
        }
        return pos;
    }


}
