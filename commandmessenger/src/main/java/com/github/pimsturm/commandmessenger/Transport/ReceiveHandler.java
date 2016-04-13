package com.github.pimsturm.commandmessenger.Transport;

import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;

import com.github.pimsturm.commandmessenger.CommunicationManager;
import com.github.pimsturm.commandmessenger.IMessengerCallbackFunction;
import com.github.pimsturm.commandmessenger.ReceivedCommand;
import com.github.pimsturm.commandmessenger.Transport.Bluetooth.ConnectedThread;
import com.github.pimsturm.commandmessenger.Transport.Bluetooth.Constants;

/**
 * Handler for data received from the Arduino
 */
public class ReceiveHandler extends Handler {
    private SparseArray<IMessengerCallbackFunction> callbackFunctionHashMap = new SparseArray<>();   // List of callbacks
    private ConnectedThread mBluetoothConnection = null;
    private IMessengerCallbackFunction defaultCallback;                 // The default callback
    private CommunicationManager communicationManager;                 // The communication manager

    /**
     * Sets the CommunicationManager
     *
     * @param communicationManager The CommunicationManager
     */
    public void setCommunicationManager(CommunicationManager communicationManager) {
        this.communicationManager = communicationManager;
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
                ReceivedCommand receivedCommand = (ReceivedCommand) msg.obj;
                processCommand(receivedCommand);
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
    public void processCommand(ReceivedCommand receivedCommand) {
        IMessengerCallbackFunction callback = null;

        if (receivedCommand.getOk()) {
            if (callbackFunctionHashMap.indexOfKey(receivedCommand.getCmdId()) >= 0) {
                callback = callbackFunctionHashMap.get(receivedCommand.getCmdId());
            } else {
                if (defaultCallback != null) callback = defaultCallback;
            }
        } else {
            // Empty command
            receivedCommand = new ReceivedCommand(communicationManager);
        }

        if (callback != null) {
            callback.handleMessage(receivedCommand);
        }
    }

}
