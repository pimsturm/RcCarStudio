package com.github.pimsturm.commandmessenger.Transport.Bluetooth;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * Connect to a Bluetooth server with a specified address
 */
public class ConnectThread extends Thread {
    private final String TAG = "ConnectThread";
    private BluetoothSocket mBluetoothSocket;
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final BluetoothConnectionManager mBluetoothConnectionManager;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public ConnectThread(BluetoothConnectionManager bluetoothConnectionManager, BluetoothDevice device) {

        mBluetoothConnectionManager = bluetoothConnectionManager;
        try {
            mBluetoothSocket = device
                    .createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }
    }

    public void run() {
        // Always cancel discovery because it will slow down a connection
        mBluetoothAdapter.cancelDiscovery();

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            mBluetoothSocket.connect();

            // Start the connected thread from the handler's thread when this thread is done.
            mBluetoothConnectionManager.getmHandler().post(new Runnable() {
                @Override
                public void run() {
                    mBluetoothConnectionManager.connected(mBluetoothSocket);
                }
            });
        } catch (IOException connectException) {
            Log.d(TAG, "Connect exception: " + connectException.getMessage());
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mBluetoothConnectionManager.getmHandler().post(new Runnable() {
                @Override
                public void run() {
                    mBluetoothConnectionManager.connectionFailed();
                }
            });
        }

    }

    public void cancel() {
        /*
        try {
            mBluetoothSocket.close();
        } catch (IOException e) {
        } */
    }

}
