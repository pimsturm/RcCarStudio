package com.github.pimsturm.commandmessenger.Transport.Bluetooth;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class BluetoothConnectionStorer implements IBluetoothConnectionStorer, Serializable {
    private final String _settingsFileName;

    /**
     * Constructor of Store/Retrieve object for SerialConnectionManagerSettings
     * The file is serialized as a simple binary file
     */
    public BluetoothConnectionStorer()
    {
        _settingsFileName = "BluetoothConnectionManagerSettings.cfg";
    }

    /**
     * Constructor of Store/Retrieve object for SerialConnectionManagerSettings
     * The file is serialized as a simple binary file
     * @param settingsFileName Filename of the settings file
     */
    public BluetoothConnectionStorer(String settingsFileName)
    {
        _settingsFileName = settingsFileName;
    }

    /**
     * Store SerialConnectionManagerSettings
     * @param bluetoothConnectionManagerSettings BluetoothConnectionManagerSettings
     */
    public void storeSettings(BluetoothConnectionManagerSettings bluetoothConnectionManagerSettings)
    {
        FileOutputStream fileStream;
        ObjectOutputStream os;
        try {
            fileStream = new FileOutputStream(_settingsFileName);
        } catch (FileNotFoundException e) {
            return;
        }

        try {
            os = new ObjectOutputStream(fileStream);
            os.writeObject(bluetoothConnectionManagerSettings);
            os.close();
        } catch (IOException e) {
            return;
        }

        try {
            fileStream.close();
        } catch (IOException e) {}

    }

    /**
     * Retrieve SerialConnectionManagerSettings
     * @return SerialConnectionManagerSettings
     */
    public BluetoothConnectionManagerSettings retrieveSettings()
    {
        FileInputStream fis;
        try {
            fis = new FileInputStream(_settingsFileName);
        } catch (IOException e) {
            return null;
        }

        try {
            ObjectInputStream is = new ObjectInputStream(fis);
            BluetoothConnectionManagerSettings bluetoothConnectionManagerSettings = (BluetoothConnectionManagerSettings) is.readObject();
            is.close();
            fis.close();
            return bluetoothConnectionManagerSettings;
        } catch (IOException e) {} catch (ClassNotFoundException e) {}

        return null;

    }
}
