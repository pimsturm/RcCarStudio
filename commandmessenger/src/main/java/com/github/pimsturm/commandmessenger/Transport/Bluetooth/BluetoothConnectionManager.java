package com.github.pimsturm.commandmessenger.Transport.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.util.Log;

import com.github.pimsturm.commandmessenger.CmdMessenger;
import com.github.pimsturm.commandmessenger.ConnectionManagerProgressEventArgs;
import com.github.pimsturm.commandmessenger.IEventHandler;
import com.github.pimsturm.commandmessenger.IMessengerCallbackFunction;
import com.github.pimsturm.commandmessenger.ReceiveQueue;
import com.github.pimsturm.commandmessenger.ReceivedCommand;
import com.github.pimsturm.commandmessenger.SendCommand;
import com.github.pimsturm.commandmessenger.SendQueue;
import com.github.pimsturm.commandmessenger.TimeUtils;
import com.github.pimsturm.commandmessenger.UseQueue;
import com.github.pimsturm.commandmessenger.deviceStatus;
import com.github.pimsturm.commandmessenger.Mode;

import java.util.Iterator;
import java.util.Set;

/**
 * Connection manager for Bluetooth devices
 */
public class BluetoothConnectionManager {
    private static final String TAG = "BtConnectionManager";
    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private Set<BluetoothDevice> mBtDevices;
    private Iterator mBtIterator;
    private boolean mSearchingArduino = false;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device


    public IEventHandler connectionFound;
    public IEventHandler progress; //ConnectionManagerProgressEventArgs

    protected Mode connectionManagerMode = Mode.Wait;

    private final CmdMessenger cmdMessenger;
    private final int identifyCommandId;
    private final String uniqueDeviceId;

    private long lastCheckTime;
    private long nextTimeOutCheck;

    /**
     * The handler for messages from the connect and connected threads
     * @return the handler
     */
    public Handler getmHandler() {
        return mHandler;
    }

    /**
     * Is connection manager currently connected to device.
     *
     * @return true if connected.
     */
    public boolean isConnected() {
        return mState == STATE_CONNECTED;
    }

    /**
     * Get the timeout for the watchdog
     * @return number of milliseconds of the timeout
     */
    public int getWatchdogTimeout() {
        return watchdogTimeout;
    }

    /**
     * set the timeout of the watchdog
     * @param timeout number of milliseconds of the timeout
     */
    public void setWatchdogTimeout(int timeout) {
        watchdogTimeout = timeout;
    }

    private int watchdogTimeout;
    private int watchdogRetryTimeout;

    public int getWatchdogRetryTimeout() {
        return watchdogRetryTimeout;
    }

    public void setWatchdogRetryTimeout(int timeout) {
        watchdogRetryTimeout = timeout;
    }

    private int watchdogMaxTries; //uint
    private int watchdogTries; //uint

    public int getWatchdogMaxTries() {
        return watchdogMaxTries;
    }

    public void setWatchdogMaxTries(int tries) {
        watchdogMaxTries = tries;
    }

    private boolean watchdogEnabled;

    /**
     * Enables or disables connection watchdog functionality using identify command and unique device id.
     *
     * @param enabled true if the functionality must be enabled.
     */
    public void setWatchdogEnabled(boolean enabled) {
        if (enabled && (uniqueDeviceId == null || uniqueDeviceId.equals(""))) {
            throw new IllegalArgumentException("Watchdog can't be enabled without Unique Device ID.");
        }
        watchdogEnabled = enabled;
    }

    private boolean deviceScanEnabled;


    /**
     * Enables or disables device scanning.
     * When disabled, connection manager will try to open connection to the device configured in the setting.
     * - For SerialConnection this means scanning for (virtual) serial ports,
     * - For BluetoothConnection this means scanning for a device on RFCOMM level
     * @param enabled true if device scanning must be enabled.
     */
    public void setDeviceScanEnabled(boolean enabled) {
        deviceScanEnabled = enabled;
    }

    private boolean persistentSettings;

    public boolean isPersistentSettings() {
        return persistentSettings;
    }

    /**
     * Enables or disables storing of last connection configuration in persistent file.
     * @param enabled true if the last configuration must be stored.
     */
    public void setPersistentSettings(boolean enabled) {
        persistentSettings = enabled;
    }

    public BluetoothConnectionManager(CmdMessenger cmdMessenger, int identifyCommandId, String uniqueDeviceId) {
        if (cmdMessenger == null)
            throw new NullPointerException("Command Messenger is null.");

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = cmdMessenger.getmReceiveHandler();

        this.cmdMessenger = cmdMessenger;
        this.identifyCommandId = identifyCommandId;
        this.uniqueDeviceId = uniqueDeviceId;

        watchdogTimeout = 3000;
        watchdogRetryTimeout = 1500;
        watchdogMaxTries = 3;
        watchdogEnabled = false;

        persistentSettings = false;
        deviceScanEnabled = true;

        if ((this.uniqueDeviceId == null || this.uniqueDeviceId.equals("")))
            this.cmdMessenger.attach(identifyCommandId, new onIdentifyResponse());
    }

    /**
     * Set the current state of the Bluetooth connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * start connection manager.
     */
    public void startConnectionManager() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (deviceScanEnabled) {
            startScan();
        } else {
            startConnect();
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);
        Log(1, "connect to: " + device.getName());

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(this, device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     */
    public synchronized void connected(BluetoothSocket socket) {
        Log(1, "Connected to: " + socket.getRemoteDevice().getName());

        // Reset the ConnectThread because we're done
        synchronized (this) {
            // Cancel the thread that completed the connection
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(this, socket, mHandler);
        mConnectedThread.start();

        // Successfully connected to Bluetooth device
        invokeEvent(connectionFound, null);

        // Check if the identifier of the device matches
        if (isArduinoAvailable(watchdogTimeout) == deviceStatus.Available) {

            setState(STATE_CONNECTED);
        } else {
            // Not an Arduino, check the next device.
            Log(1, "Connected device is not the Arduino.");
            startConnect();
        }
    }

    /**
     * Stop all threads
     */
    public synchronized void stopConnectionManager() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    protected void connectionFailed() {
        Log(1, "Unable to connect device");

        // Try connecting to the next device
        startConnect();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    protected void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Try connecting again
        startConnect();
    }


    protected void Log(int level, String logMessage) {
        ConnectionManagerProgressEventArgs args = new ConnectionManagerProgressEventArgs();
        args.setLevel(level);
        args.setDescription(logMessage);

        invokeEvent(progress, args);
    }

    private class onIdentifyResponse implements IMessengerCallbackFunction {
        @Override
        public void handleMessage(ReceivedCommand responseCommand) {
            if (responseCommand.getOk() && !(uniqueDeviceId == null || uniqueDeviceId.equals(""))) {
                validateDeviceUniqueId(responseCommand);
            }

        }
    }

    private <TEventHandlerArguments> void invokeEvent(IEventHandler<TEventHandlerArguments> eventHandler, TEventHandlerArguments eventHandlerArguments) {

        if (eventHandler == null) return;
        //Invoke here
        eventHandler.invokeEvent(this, eventHandlerArguments);

    }

    /**
     * Check if Arduino is available
     *
     * @param timeOut Timeout for waiting on response
     * @return Check result.
     */
    private deviceStatus isArduinoAvailable(int timeOut) {
        SendCommand challengeCommand = new SendCommand(identifyCommandId, identifyCommandId, timeOut);
        ReceivedCommand responseCommand = cmdMessenger.sendCommand(challengeCommand, SendQueue.InFrontQueue, ReceiveQueue.Default, UseQueue.BypassQueue);

        if (responseCommand.getOk() && !(uniqueDeviceId == null || uniqueDeviceId.equals(""))) {
            return validateDeviceUniqueId(responseCommand) ? deviceStatus.Available : deviceStatus.IdentityMismatch;
        }

        return responseCommand.getOk() ? deviceStatus.Available : deviceStatus.NotAvailable;
    }

    /**
     * Check if Arduino is available
     *
     * @param timeOut Timeout for waiting on response
     * @param tries   Number of tries
     * @return Check result.
     */
    protected deviceStatus isArduinoAvailable(int timeOut, int tries) {
        for (int i = 1; i <= tries; i++) {
            Log(3, "Polling Arduino, try # " + i);

            deviceStatus status = isArduinoAvailable(timeOut);
            if (status == deviceStatus.Available
                    || status == deviceStatus.IdentityMismatch) return status;
        }
        return deviceStatus.NotAvailable;
    }

    private boolean validateDeviceUniqueId(ReceivedCommand responseCommand) {
        boolean valid = uniqueDeviceId.equals(responseCommand.readStringArg());
        if (!valid) {
            Log(3, "Invalid device response. Device ID mismatch.");
        }

        return valid;
    }


    /**
     * disconnect from Arduino
     *
     * @return true if successfully disconnected
     */
    private boolean disconnect() {
        if (mState == STATE_CONNECTED) {
            mState = STATE_NONE;
            return cmdMessenger.disconnect();
        }

        return true;
    }

    /**
     * start watchdog. Will check if connection gets interrupted
     */
    private void startWatchDog() {
        if (connectionManagerMode != Mode.Watchdog && mState == STATE_CONNECTED) {
            Log(1, "Starting Watchdog.");
            lastCheckTime = TimeUtils.millis;
            nextTimeOutCheck = lastCheckTime + watchdogTimeout;
            watchdogTries = 0;

            connectionManagerMode = Mode.Watchdog;
        }
    }

    /**
     * Stop watchdog.
     */
    private void stopWatchDog() {
        if (connectionManagerMode == Mode.Watchdog) {
            Log(1, "Stopping Watchdog.");
            connectionManagerMode = Mode.Wait;
        }
    }

    /**
     * start scanning for devices
     */
    protected void startScan() {
        if (connectionManagerMode != Mode.Scan && mState != STATE_CONNECTED) {
            Log(1, "Starting device scan.");
            connectionManagerMode = Mode.Scan;
        }
    }


    /**
     * start connect to device
     */
    private void startConnect() {
        if (!mSearchingArduino) {
            Log(1, "Trying to connect to a paired device.");
            // get paired devices
            mBtDevices = mAdapter.getBondedDevices();
            if (mBtDevices.isEmpty()) {
                Log(1, "No paired devices.");
                return;
            }
            mBtIterator = mBtDevices.iterator();
            mSearchingArduino = true;
        }

        if (mBtIterator.hasNext()) {
            connect((BluetoothDevice) mBtIterator.next());
        } else {
            mSearchingArduino = false;
            Log(1, "Could not find Arduino.");
        }

    }

    private void dispose(boolean disposing) {
        if (disposing) {
            stopConnectionManager();
        }
    }


}
