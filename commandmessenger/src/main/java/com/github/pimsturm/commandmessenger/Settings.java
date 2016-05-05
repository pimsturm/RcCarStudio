package com.github.pimsturm.commandmessenger;

/**
 * Settings for cmdMessenger
 */
public final class Settings {
    private static Settings uniqueInstance;

    // From BluetoothConnectionManager
    private int identifyCommandId;
    private String uniqueDeviceId;
    private int watchdogTimeout;
    private int watchdogRetryTimeout;
    private int watchdogMaxTries;
    private boolean watchdogEnabled;
    private boolean deviceScanEnabled;
    private boolean persistentSettings;

    public void setIdentifyCommandId(int identifyCommandId) {
        this.identifyCommandId = identifyCommandId;
    }

    public int getIdentifyCommandId() {
        return identifyCommandId;
    }

    public void setUniqueDeviceId(String uniqueDeviceId) {
        this.uniqueDeviceId = uniqueDeviceId;
    }

    public String getUniqueDeviceId() {
        return uniqueDeviceId;
    }

    public void setWatchdogTimeout(int watchdogTimeout) {
        this.watchdogTimeout = watchdogTimeout;
    }

    public int getWatchdogTimeout() {
        return watchdogTimeout;
    }

    public void setWatchdogRetryTimeout(int watchdogRetryTimeout) {
        this.watchdogRetryTimeout = watchdogRetryTimeout;
    }

    public int getWatchdogRetryTimeout() {
        return watchdogRetryTimeout;
    }

    public void setWatchdogMaxTries(int watchdogMaxTries) {
        this.watchdogMaxTries = watchdogMaxTries;
    }

    public int getWatchdogMaxTries() {
        return watchdogMaxTries;
    }

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

    /**
     * Indicates if the watchdog functionality is enabled.
     *
     * @return true if watchdog is enabled
     */
    public boolean isWatchdogEnabled() {
        return watchdogEnabled;
    }

    /**
     * Enables or disables device scanning.
     * When disabled, connection manager will try to open connection to the device configured in the setting.
     * - For SerialConnection this means scanning for (virtual) serial ports,
     * - For BluetoothConnection this means scanning for a device on RFCOMM level
     *
     * @param enabled true if device scanning must be enabled.
     */
    public void setDeviceScanEnabled(boolean enabled) {
        deviceScanEnabled = enabled;
    }

    public boolean isDeviceScanEnabled() {
        return deviceScanEnabled;
    }


    /**
     * Enables or disables storing of last connection configuration in persistent file.
     *
     * @param enabled true if the last configuration must be stored.
     */
    public void setPersistentSettings(boolean enabled) {
        persistentSettings = enabled;
    }

    /**
     * Indicates if the last connection configuration is stored in a persistent file.
     *
     * @return true if the last configuration is stored.
     */
    public boolean isPersistentSettings() {
        return persistentSettings;
    }


    // From CommunicationManager
    /**
     * The field separator
     */
    private char fieldSeparator;

    public void setFieldSeparator(char fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public char getFieldSeparator() {
        return fieldSeparator;
    }

    /**
     * The command separator
     */
    private char commandSeparator;

    public void setCommandSeparator(char commandSeparator) {
        this.commandSeparator = commandSeparator;
    }

    public char getCommandSeparator() {
        return commandSeparator;
    }

    /**
     * The escape character
     */
    private char escapeCharacter;

    public void setEscapeCharacter(char escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
    }

    public char getEscapeCharacter() {
        return escapeCharacter;
    }

    private boolean printLfCr;

    /**
     * Gets or sets a whether to print a line feed carriage return after each command.
     *
     * @param printLfCr true if print line feed carriage return, false if not.
     */
    public void setPrintLfCr(boolean printLfCr) {
        this.printLfCr = printLfCr;
    }

    public boolean getPrintLfCr() {
        return printLfCr;
    }

    private BoardType boardType;

    public void setBoardType(BoardType boardType) {
        this.boardType = boardType;
    }

    public BoardType getBoardType() {
        return boardType;
    }


    private Settings() {
    }

    /**
     * Create an instance of the Settings
     * Only one instance is allowed.
     *
     * @return an instance of the Settings.
     */
    public static synchronized Settings getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new Settings();
            uniqueInstance.init();
        }

        return uniqueInstance;
    }

    private void init() {
        // Set defaults
        watchdogTimeout = 3000;
        watchdogRetryTimeout = 1500;
        watchdogMaxTries = 3;
        watchdogEnabled = false;

        boardType = BoardType.Bit16;

        persistentSettings = false;
        deviceScanEnabled = true;

        fieldSeparator = ',';
        commandSeparator = ';';
        escapeCharacter = '/';
        printLfCr = false;

        Escaping.setEscapeChars(fieldSeparator, commandSeparator, escapeCharacter);

    }

}

