package com.github.pimsturm.commandmessenger.Transport.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.pimsturm.commandmessenger.IEventHandler;

public class BluetoothReceiver extends BroadcastReceiver {
    private static BluetoothReceiver uniqueInstance;

    private boolean registered;

    /**
     * Check if the broadcast receiver is registered.
     *
     * @return true if the broadcast receiver is registered.
     */
    public boolean isRegistered() {
        return registered;
    }

    /**
     * Mark the broadcast receiver as not registered.
     */
    public void unregister() {
        this.registered = false;
    }

    /**
     * Mark the broadcast receiver as registered.
     */
    public void register() {
        this.registered = true;
    }

    private IEventHandler<BluetoothDevice> deviceFound;

    /**
     * Add an event handler to execute when a new Bluetooth device is found.
     *
     * @param deviceFound event handler
     */
    public void setDeviceFound(IEventHandler<BluetoothDevice> deviceFound) {
        this.deviceFound = deviceFound;
    }

    private BluetoothReceiver() {
    }

    /**
     * Create an instance of the BluetoothReceiver
     * Only one instance is allowed.
     *
     * @return an instance of BluetoothReceiver.
     */
    public static synchronized BluetoothReceiver getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new BluetoothReceiver();
        }
        return uniqueInstance;
    }

    /**
     * When a broadcast is received.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // When discovery finds a device
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            deviceFound.invokeEvent(this, device);
        }

    }
}
