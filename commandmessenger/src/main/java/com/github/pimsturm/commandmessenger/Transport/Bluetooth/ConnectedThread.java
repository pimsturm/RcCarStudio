package com.github.pimsturm.commandmessenger.Transport.Bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;


/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
public class ConnectedThread extends Thread {
    private static final String TAG = "ConnectedThread";
    private final BluetoothSocket mBluetoothSocket;
    private final BluetoothConnectionManager mBluetoothConnectionManager;
    private final Handler mHandler;
    private final InputStream mInStream;
    private final OutputStream mOutStream;

    /**
     * Constructor
     *
     * @param socket  Bluetooth socket to read and write to
     * @param handler message handler
     */
    public ConnectedThread(BluetoothConnectionManager connectionManager, BluetoothSocket socket, Handler handler) {
        mBluetoothConnectionManager = connectionManager;
        mBluetoothSocket = socket;
        mHandler = handler;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
        }

        mInStream = tmpIn;
        mOutStream = tmpOut;

    }

    @Override
    public void run() {

        Log.i(TAG, "BEGIN mConnectedThread");
        byte[] buffer = new byte[1024];
        int bytes;

        // Keep listening to the InputStream while connected
        while (true) {
            try {
                // Read from the InputStream
                bytes = mInStream.read(buffer);

                // Send the obtained bytes to the UI Activity
                mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
                mBluetoothConnectionManager.getmHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothConnectionManager.connectionLost();
                    }
                });
                break;
            }
        }

    }

    public void write(byte[] bytes) {
        try {
            mOutStream.write(bytes);

            // Share the sent message back to the UI Activity
            mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, bytes)
                    .sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }

    }

    public void cancel() {
        try {
            mBluetoothSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }

}
