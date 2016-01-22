package com.github.pimsturm.commandmessenger.Transport.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;

import com.github.pimsturm.commandmessenger.AsyncWorker;
import com.github.pimsturm.commandmessenger.IAsyncWorkerJob;
import com.github.pimsturm.commandmessenger.IEventHandler;
import com.github.pimsturm.commandmessenger.Transport.ITransport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Manager for Bluetooth connection
 */
public class BluetoothTransport implements ITransport {
    private static final String TAG = "BluetoothTransport";
    private static final int BUFFER_SIZE = 4096;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private InputStream inputStream;
    private OutputStream outputStream;
    private AsyncWorker asyncWorker;
    private Object readLock = new Object();
    private Object writeLock = new Object();
    private byte[] readBuffer = new byte[BUFFER_SIZE];
    private int bufferFilled;
    private static BluetoothDevice runningBluetoothDeviceInfo;
    private static boolean showAsConnected;
    private static boolean lazyReconnect;
    private IEventHandler dataReceived;

    private BluetoothSocket btSocket = null;

    public void setDataReceived(IEventHandler newDataReceived)
    {
        dataReceived = newDataReceived;
    }

    private BluetoothDevice currentBluetoothDeviceInfo;

    /**
     * Sets Bluetooth device info
     * @param currentBluetoothDeviceInfo
     */
    public void setCurrentBluetoothDeviceInfo(BluetoothDevice currentBluetoothDeviceInfo) {
        this.currentBluetoothDeviceInfo = currentBluetoothDeviceInfo;
    }

    /**
     * Gets Bluetooth device info
     * @return
     */
    public BluetoothDevice getCurrentBluetoothDeviceInfo() {
        return currentBluetoothDeviceInfo;
    }

    public void setLazyReconnect(boolean lazyReconnect) {
        BluetoothTransport.lazyReconnect = lazyReconnect;
    }

    public boolean getLazyReconnect() {
        return lazyReconnect;
    }

    /**
     * Bluetooth transport constructor
     */
    public BluetoothTransport()
    {
        showAsConnected = false;
        lazyReconnect = true;
        asyncWorker = new AsyncWorker(new Poll(), "BluetoothTransport");
    }

    class Poll implements IAsyncWorkerJob {
        @Override
        public boolean execute() {
            int bytes = updateBuffer();
            if (bytes > 0 && dataReceived != null) dataReceived.invokeEvent(this, null);

            return true;
        }
    }

    /**
     * Connects to a serial port defined through the current settings.
     * @return true if it succeeds, false if it fails.
     */
    @Override
    public boolean connect() {
        // Reconnecting to the same device seems to fail a lot of the time, so see
        // if we can remain connected
        if (runningBluetoothDeviceInfo !=null && runningBluetoothDeviceInfo.getAddress() == currentBluetoothDeviceInfo.getAddress() && lazyReconnect) {
            currentBluetoothDeviceInfo = runningBluetoothDeviceInfo;
        } else {
            runningBluetoothDeviceInfo = currentBluetoothDeviceInfo;
            //BluetoothClient.close();
        }
        // Closing serial port if it is open
        //_stream = null;

        // set pin of device to connect with
        // check if device is paired
        //currentBluetoothDeviceInfo.Refresh();
        try
        {
            if (!(currentBluetoothDeviceInfo.getBondState() == BluetoothDevice.BOND_BONDED))
            {
                //Console.WriteLine("Not authenticated");
                showAsConnected = false;
                return showAsConnected;
            }

            if (btSocket != null && btSocket.isConnected() && !lazyReconnect)
            {
                //Previously connected, setting up new connection"
                //BluetoothUtils.UpdateClient();
            }

            // synchronous connection method
            if (!(btSocket != null && btSocket.isConnected()) || !lazyReconnect)
                try {
                    btSocket = createBluetoothSocket(currentBluetoothDeviceInfo);
                } catch (IOException e) {}

            if (!open())
            {
                // Desperate attempt: try full reset and open
                showAsConnected = updateConnectOpen();
                return showAsConnected;
            }

            // Check worker is not running as a precaution. This needs to be rechecked.
            if (!asyncWorker.isRunning()) asyncWorker.start();

            showAsConnected = true;
            return showAsConnected;
        }
        catch (IllegalStateException e)
        {
            // Desperate attempt: try full reset and open
            showAsConnected = updateConnectOpen();
            return showAsConnected;
        }

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
       return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    private boolean updateConnectOpen()
    {
        //BluetoothUtils.UpdateClient();
        try
        {
            btSocket = createBluetoothSocket(currentBluetoothDeviceInfo);
        }
        catch (IOException e)
        {
            return false;
        }
        return open();

    }

    /**
     * Opens the serial port.
     * @return true if it succeeds, false if it fails.
     */
    public boolean open()
    {
        if (btSocket == null) return false;
        try {
            btSocket.connect();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            return false;
        }
        synchronized (writeLock) {
            synchronized (readLock)
            {
                try {
                    inputStream = btSocket.getInputStream();
                    outputStream = btSocket.getOutputStream();
                } catch (IOException e) {
                    return false;
                }
                //inputStream.ReadTimeout = 2000;
                //outputStream.WriteTimeout = 1000;
            }
        }
        return true;
    }

    /**
     * Returns opened stream status
     * @return true when open
     */
    public boolean isOpen()
    {
        // note: this does not always work. Perhaps do a scan
        return isConnected() && (inputStream != null) && (outputStream != null);
    }


    /**
     * Closes the Bluetooth stream port.
     * @return true if it succeeds, false if it fails.
     */
    public boolean close()
    {
        synchronized (writeLock)
        {
            synchronized (readLock)
            {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        return false;
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        return false;
                    }
                }

            inputStream = null;
            outputStream = null;
            return true;
            }
        }
    }

    public boolean disconnect() {
        showAsConnected = false;
        // Check worker is running as a precaution.
        if (asyncWorker.isRunning()) asyncWorker.stop();
        if (lazyReconnect) return true;
        return close();
    }

    /**
     * Returns connection status
     * @return true when connected
     */
    public boolean isConnected() {
        // In case of lazy reconnect we will pretend to be disconnected
        if (lazyReconnect && !showAsConnected) return false;
        // If not, test if we are connected
        return btSocket != null && btSocket.isConnected();
    }

    public byte[] read() {
        //if (isOpen())
        {
            byte[] buffer;
            synchronized (readLock)
            {
                buffer = new byte[bufferFilled];
                System.arraycopy(readBuffer, 0, buffer, 0, bufferFilled);
                bufferFilled = 0;
            }
            return buffer;
        }
        //return new byte[0];
    }

    public void write(byte[] buffer) {
        try
        {
            if (isOpen())
            {
                synchronized (writeLock)
                {
                    outputStream.write(buffer, 0, buffer.length);
                }
            }
        }
        catch(Exception e)
        {
            //Do nothing
        }

    }

    private int updateBuffer()
    {
        if (isOpen())
        {
            try
            {

                int nbrDataRead = inputStream.read(readBuffer, bufferFilled, (BUFFER_SIZE - bufferFilled));
                synchronized (readLock)
                {
                    bufferFilled += nbrDataRead;
                    //Console.WriteLine("buf: {0}", bufferFilled.ToString().Length);
                }
                return bufferFilled;
            }
            catch (IOException e)
            {
                //Console.WriteLine("buf: TO");
                // Timeout (expected)
            }
        }
        else
        {
            // In case of no connection
            // Sleep a bit otherwise CPU load will go through roof
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {}
        }

        return bufferFilled;
    }


}
