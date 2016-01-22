package com.github.pimsturm.commandmessenger.Transport.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothUtils {
    private static final String TAG = "BluetoothUtils";
    private static BluetoothAdapter PrimaryRadio;

    public static void setPrimaryRadio(BluetoothAdapter bluetoothAdapter) {
        PrimaryRadio = bluetoothAdapter;
    }

    public static BluetoothAdapter getPrimaryRadio() {
        return PrimaryRadio;
    }

    static {
        PrimaryRadio = BluetoothAdapter.getDefaultAdapter();
        if (PrimaryRadio == null) {
            Log.d(TAG, "No radio hardware or unsupported software stack");
        }

    }

    public static BluetoothDevice deviceByAddress(String address)
    {
        try
        {
            return PrimaryRadio.getRemoteDevice(address);
        }
        catch (Exception e)
        {
            Log.d(TAG, e.getMessage());
            return null;
        }
    }

}
