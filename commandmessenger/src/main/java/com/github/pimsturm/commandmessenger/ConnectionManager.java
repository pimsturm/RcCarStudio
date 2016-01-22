package com.github.pimsturm.commandmessenger;

import android.util.Log;

/**
 * ConnectionManager: manage the connection to the device (e.g. Arduino)
 */
public abstract class ConnectionManager {
    private static final String TAG = "ConnectionManager";
    public IEventHandler connectionTimeout;
    public IEventHandler connectionFound;
    public IEventHandler progress; //ConnectionManagerProgressEventArgs

    protected Mode connectionManagerMode = Mode.Wait;

    private final CmdMessenger cmdMessenger;
    private final AsyncWorker asyncWorker;
    private final int identifyCommandId;
    private final String uniqueDeviceId;

    private long lastCheckTime;
    private long nextTimeOutCheck;
    private int watchdogTries; //uint

    private boolean connected;

    /**
     * Is connection manager currently connected to device.
     * @return true if connected.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * set connection status
     * @param status status of the connection, true if connected.
     */
    protected void setConnected(boolean status) {
        connected = status;
    }

    private int watchdogTimeout;

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

    private int watchdogRetryTimeout;
    public int getWatchdogRetryTimeout() {
        return watchdogRetryTimeout;
    }
    public void setWatchdogRetryTimeout(int timeout) {
        watchdogRetryTimeout = timeout;
    }
    private int watchdogMaxTries; //uint
    public int getWatchdogMaxTries() {
        return watchdogMaxTries;
    }
    public void setWatchdogMaxTries(int tries) {
        watchdogMaxTries = tries;
    }

    private boolean watchdogEnabled;

    /**
     * Is connection watchdog functionality enabled.
     * @return true if enabled
     */
    public boolean isWatchdogEnabled() {
        return watchdogEnabled;
    }

    /**
     * Enables or disables connection watchdog functionality using identify command and unique device id.
     * @param enabled true if the functionality must be enabled.
     */
    public void setWatchdogEnabled(boolean enabled) {
        if(enabled && (uniqueDeviceId == null || uniqueDeviceId.equals(""))) {
            throw new IllegalArgumentException("Watchdog can't be enabled without Unique Device ID.");
        }
        watchdogEnabled = enabled;
    }

    private boolean deviceScanEnabled;

    /**
     * Is device scanning enabled
     * @return true if device scanning is enabled
     */
    public boolean isDeviceScanEnabled() {
        return deviceScanEnabled;
    }

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

    protected ConnectionManager(CmdMessenger cmdMessenger, int identifyCommandId) {
        this(cmdMessenger, identifyCommandId, null);
    }

    protected ConnectionManager(CmdMessenger cmdMessenger, String uniqueDeviceId) {
        this(cmdMessenger, 0, uniqueDeviceId);
    }

    protected ConnectionManager(CmdMessenger cmdMessenger, int identifyCommandId, String uniqueDeviceId)
    {
        if (cmdMessenger == null)
            throw new NullPointerException("Command Messenger is null.");

        this.cmdMessenger = cmdMessenger;
        this.identifyCommandId = identifyCommandId;
        this.uniqueDeviceId = uniqueDeviceId;

        watchdogTimeout = 3000;
        watchdogRetryTimeout = 1500;
        watchdogMaxTries = 3;
        watchdogEnabled = false;

        persistentSettings = false;
        deviceScanEnabled = true;

        asyncWorker = new AsyncWorker(new doWork(), "ConnectionManager");

        if ((this.uniqueDeviceId == null || this.uniqueDeviceId == ""))
            this.cmdMessenger.attach(identifyCommandId, new onIdentifyResponse());
    }

    /**
     * start connection manager.
     */
    public void startConnectionManager()
    {
        if (!asyncWorker.isRunning()) asyncWorker.start();

        if (deviceScanEnabled)
        {
            startScan();
        }
        else
        {
            startConnect();
        }
    }

    /**
     * Stop connection manager.
     */
    public void stopConnectionManager()
    {
        if (asyncWorker.isRunning()) asyncWorker.stop();
        disconnect();
    }

    protected void connectionFoundEvent()
    {
        connectionManagerMode = Mode.Wait;

        if (watchdogEnabled) startWatchDog();

        invokeEvent(connectionFound, null);
    }

    protected void connectionTimeoutEvent()
    {
        connectionManagerMode = Mode.Wait;

        disconnect();

        invokeEvent(connectionTimeout, null);

        if (watchdogEnabled)
        {
            stopWatchDog();

            if (deviceScanEnabled)
            {
                startScan();
            }
            else
            {
                startConnect();
            }
        }
    }

    protected void Log(int level, String logMessage)
    {
        ConnectionManagerProgressEventArgs args = new ConnectionManagerProgressEventArgs();
        args.setLevel(level);
        args.setDescription(logMessage);

        invokeEvent(progress, args);
    }

    public class onIdentifyResponse implements IMessengerCallbackFunction {
        @Override
        public void handleMessage(ReceivedCommand responseCommand) {
            if (responseCommand.getOk() && !(uniqueDeviceId == null || uniqueDeviceId.equals("")))
            {
                validateDeviceUniqueId(responseCommand);
            }

        }
    }

    private <TEventHandlerArguments> void invokeEvent(IEventHandler eventHandler, TEventHandlerArguments eventHandlerArguments)
    {

        if (eventHandler == null) return;
        //Invoke here
        eventHandler.invokeEvent(this, eventHandlerArguments);

    }

    public class doWork implements IAsyncWorkerJob {
        public boolean execute() {
            // Switch between waiting, device scanning and watchdog
            switch (connectionManagerMode)
            {
                case Scan:
                    doWorkScan();
                    break;
                case Connect:
                    doWorkConnect();
                    break;
                case Watchdog:
                    doWorkWatchdog();
                    break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.d(TAG, e.getMessage());
            }
            return true;

        }
    }

    /**
     * Check if Arduino is available
     * @param timeOut Timeout for waiting on response
     * @return Check result.
     */
    protected deviceStatus isArduinoAvailable(int timeOut)
    {
        SendCommand challengeCommand = new SendCommand(identifyCommandId, identifyCommandId, timeOut);
        ReceivedCommand responseCommand = cmdMessenger.sendCommand(challengeCommand, SendQueue.InFrontQueue, ReceiveQueue.Default, UseQueue.BypassQueue);

        if (responseCommand.getOk() && !(uniqueDeviceId == null || uniqueDeviceId.equals("")))
        {
            return validateDeviceUniqueId(responseCommand) ? deviceStatus.Available : deviceStatus.IdentityMismatch;
        }

        return responseCommand.getOk() ? deviceStatus.Available : deviceStatus.NotAvailable;
    }

    /**
     * Check if Arduino is available
     * @param timeOut Timeout for waiting on response
     * @param tries Number of tries
     * @return Check result.
     */
    protected deviceStatus isArduinoAvailable(int timeOut, int tries)
    {
        for (int i = 1; i <= tries; i++)
        {
            Log(3, "Polling Arduino, try # " + i);

            deviceStatus status = isArduinoAvailable(timeOut);
            if (status == deviceStatus.Available
                    || status == deviceStatus.IdentityMismatch) return status;
        }
        return deviceStatus.NotAvailable;
    }

    protected boolean validateDeviceUniqueId(ReceivedCommand responseCommand)
    {
        boolean valid = uniqueDeviceId.equals(responseCommand.readStringArg());
        if (!valid)
        {
            Log(3, "Invalid device response. Device ID mismatch.");
        }

        return valid;
    }

    //Try to connect using current connections settings
    protected abstract void doWorkConnect();

    // Perform scan to find connected systems
    protected abstract void doWorkScan();

    protected void doWorkWatchdog()
    {
        long lastLineTimeStamp = cmdMessenger.getLastReceivedCommandTimeStamp();
        long currentTimeStamp = TimeUtils.millis;

        // If timeout has not elapsed, wait till next watch time
        if (currentTimeStamp < nextTimeOutCheck) return;

        // if a command has been received recently, set next check time
        if (lastLineTimeStamp >= lastCheckTime)
        {
            Log(3, "Successful watchdog response.");
            lastCheckTime = currentTimeStamp;
            nextTimeOutCheck = lastCheckTime + watchdogTimeout;
            watchdogTries = 0;
            return;
        }

        // Apparently, other side has not reacted in time
        // If too many tries, notify and stop
        if (watchdogTries >= watchdogMaxTries)
        {
            Log(2, "Watchdog received no response after final try #" + watchdogMaxTries);
            watchdogTries = 0;
            connectionManagerMode = Mode.Wait;
            connectionTimeoutEvent();
            return;
        }

        // We'll try another time
        // We queue the command in order to not be intrusive, but put it in front to get a quick answer
        cmdMessenger.sendCommand(new SendCommand(identifyCommandId));
        watchdogTries++;

        lastCheckTime = currentTimeStamp;
        nextTimeOutCheck = lastCheckTime + watchdogRetryTimeout;
        Log(3, watchdogTries == 1 ?
                "Watchdog detected no communication for " + watchdogTimeout / 1000.0 + "s, asking for response"
                : "Watchdog received no response, performing try #" + watchdogTries);
    }

    /**
     * disconnect from Arduino
     * @return true if successfully disconnected
     */
    private boolean disconnect()
    {
        if (connected)
        {
            connected = false;
            return cmdMessenger.disconnect();
        }

        return true;
    }

    public void dispose()
    {
        dispose(true);
        //GC.SuppressFinalize(this);
    }

    /**
     * start watchdog. Will check if connection gets interrupted
     */
    protected void startWatchDog()
    {
        if (connectionManagerMode != Mode.Watchdog && connected)
        {
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
    protected void stopWatchDog()
    {
        if (connectionManagerMode == Mode.Watchdog)
        {
            Log(1, "Stopping Watchdog.");
            connectionManagerMode = Mode.Wait;
        }
    }

    /**
     * start scanning for devices
     */
    protected void startScan()
    {
        if (connectionManagerMode != Mode.Scan && !connected)
        {
            Log(1, "Starting device scan.");
            connectionManagerMode = Mode.Scan;
        }
    }

    /**
     * Stop scanning for devices
     */
    protected void stopScan()
    {
        if (connectionManagerMode == Mode.Scan)
        {
            Log(1, "Stopping device scan.");
            connectionManagerMode = Mode.Wait;
        }
    }

    /**
     * start connect to device
     */
    protected void startConnect()
    {
        if (connectionManagerMode != Mode.Connect && !connected)
        {
            Log(1, "start connecting to device.");
            connectionManagerMode = Mode.Connect;
        }
    }

    /**
     * Stop connect to device
     */
    protected void stopConnect()
    {
        if (connectionManagerMode == Mode.Connect)
        {
            Log(1, "Stop connecting to device.");
            connectionManagerMode = Mode.Wait;
        }
    }

    protected void storeSettings() { }

    protected void readSettings() { }

    protected void dispose(boolean disposing)
    {
        if (disposing)
        {
            stopConnectionManager();
        }
    }

}
