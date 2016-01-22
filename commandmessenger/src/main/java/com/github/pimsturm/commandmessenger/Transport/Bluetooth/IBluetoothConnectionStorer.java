package com.github.pimsturm.commandmessenger.Transport.Bluetooth;

/**
 * Interface for storing and retrieving the standard Bluetooth connection.
 */
public interface IBluetoothConnectionStorer {
    void storeSettings(BluetoothConnectionManagerSettings bluetoothConnectionManagerSettings);
    BluetoothConnectionManagerSettings retrieveSettings();
}
