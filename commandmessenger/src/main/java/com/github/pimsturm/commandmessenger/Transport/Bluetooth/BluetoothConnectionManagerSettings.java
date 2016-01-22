package com.github.pimsturm.commandmessenger.Transport.Bluetooth;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for storing last successful connection
 */
public class BluetoothConnectionManagerSettings {
    private String bluetoothAddress;
    public String getBluetoothAddress()
    {
        return bluetoothAddress;
    }
    public void setBluetoothAddress(String newBluetoothAddress)
    {
        bluetoothAddress = newBluetoothAddress;
    }

    private Map<String, String> storedDevicePins;
    public Map<String, String> getStoredDevicePins()
    {
        return storedDevicePins;
    }
    public void setStoredDevicePins(Map<String, String> newStoredDevicePins)
    {
        storedDevicePins = newStoredDevicePins;
    }
    public void updateDevicePin(String address, String pin)
    {
        storedDevicePins.put(address, pin);
    }

    public BluetoothConnectionManagerSettings()
    {
        storedDevicePins = new HashMap<String, String>();
    }
}
