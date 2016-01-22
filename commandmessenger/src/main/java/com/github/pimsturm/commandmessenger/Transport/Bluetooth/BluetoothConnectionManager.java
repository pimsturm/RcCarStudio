package com.github.pimsturm.commandmessenger.Transport.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.widget.Toast;

import com.github.pimsturm.commandmessenger.ConnectionManager;
import com.github.pimsturm.commandmessenger.CmdMessenger;
import com.github.pimsturm.commandmessenger.deviceStatus;
import com.github.pimsturm.commandmessenger.Mode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.Arrays.asList;

/**
 * Connection manager for Bluetooth devices
 */
public class BluetoothConnectionManager extends ConnectionManager {
    private static final String TAG = "BtConnectionManager";

    private static final List<String> commonDevicePins = asList("0000", "1111", "1234");

    private enum ScanType { None, Quick, Thorough }

    private BluetoothConnectionManagerSettings bluetoothConnectionManagerSettings;
    private final IBluetoothConnectionStorer bluetoothConnectionStorer;
    private final BluetoothTransport bluetoothTransport;
    private ScanType scanType;

    // The control to invoke the callback on
    private final Object tryConnectionLock = new Object();
    private final ArrayList<BluetoothDevice> deviceList;   // BluetoothDeviceInfo
    private ArrayList<BluetoothDevice> prevDeviceList;

    private Map<String, String> devicePins;

    /**
     * Gets dictionary of Pincode per device
     * @return dictionary of Pincode per device
     */
    public Map<String, String> getDevicePins()
    {
        return devicePins;
    }

    /**
     * Sets dictionary of Pincode per device
     * @param newDevicePins dictionary of Pincode per device
     */
    public void setDevicePins(Map<String, String> newDevicePins)
    {
        devicePins = newDevicePins;
    }

    private ArrayList<String> generalPins;

    /**
     * Gets list of Pincodes tried for unknown devices
     * @return List of Pincodes
     */
    public ArrayList<String> getGeneralPins()
    {
        return generalPins;
    }

    /**
     * Sets list of Pincodes tried for unknown devices
     * @param newGeneralPins List of Pincodes
     */
    public void setGeneralPins(ArrayList<String> newGeneralPins)
    {
        generalPins = newGeneralPins;
    }


    /**
     * Connection manager for Bluetooth devices
     * @param bluetoothTransport
     * @param cmdMessenger
     * @param watchdogCommandId
     * @param uniqueDeviceId
     * @param bluetoothConnectionStorer
     */
    public BluetoothConnectionManager(BluetoothTransport bluetoothTransport, CmdMessenger cmdMessenger,
                                      int watchdogCommandId, String uniqueDeviceId,
                                      IBluetoothConnectionStorer bluetoothConnectionStorer)

    {
        super(cmdMessenger, watchdogCommandId, uniqueDeviceId);
        if (bluetoothTransport == null)
            throw new NullPointerException("Transport is null.");

        this.bluetoothTransport = bluetoothTransport;

        bluetoothConnectionManagerSettings = new BluetoothConnectionManagerSettings();
        this.bluetoothConnectionStorer = bluetoothConnectionStorer;
        setPersistentSettings(this.bluetoothConnectionStorer != null);
        readSettings();

        deviceList = new ArrayList<BluetoothDevice>();
        prevDeviceList = new ArrayList<BluetoothDevice>();

        devicePins = new HashMap<String, String>();
        generalPins = new ArrayList<String>();
    }

    public BluetoothConnectionManager(BluetoothTransport bluetoothTransport, CmdMessenger cmdMessenger,
                                      int watchdogCommandId, String uniqueDeviceId)
    {
        this(bluetoothTransport, cmdMessenger, watchdogCommandId, uniqueDeviceId, null);
    }

    public BluetoothConnectionManager(BluetoothTransport bluetoothTransport, CmdMessenger cmdMessenger,
                                      int watchdogCommandId)
    {
        this(bluetoothTransport, cmdMessenger, watchdogCommandId, null, null);
    }

    public BluetoothConnectionManager(BluetoothTransport bluetoothTransport, CmdMessenger cmdMessenger)
    {
        this(bluetoothTransport, cmdMessenger, 0, null, null);
    }

    //Try to connect using current connections settings and trigger event if successful
    protected void doWorkConnect()
    {
        final int timeOut = 1000;
        boolean activeConnection = false;

        try
        {
            activeConnection = tryConnection(timeOut);
        }
        catch (Exception e)
        {
            Log.d(TAG, e.getMessage());
        }

        if (activeConnection)
        {
            connectionFoundEvent();
        }
    }

    // Perform scan to find connected systems
    protected void doWorkScan()
    {
        if (Thread.currentThread().getName() == null) Thread.currentThread().setName("BluetoothConnectionManager");
        boolean activeConnection = false;

        // Starting scan
        if (scanType == ScanType.None)
        {
            scanType = ScanType.Quick;
        }

        switch (scanType)
        {
            case Quick:
                try
                {
                    activeConnection = quickScan();
                } catch (Exception e)
                {
                    //Do nothing
                }
            scanType = ScanType.Thorough;
            break;

            case Thorough:
                try
                {
                    activeConnection = thoroughScan();
                } catch (Exception e)
                {
                    //Do nothing
                }
                scanType = ScanType.Quick;
                break;
        }

        // Trigger event when a connection was made
        if (activeConnection)
        {
            connectionFoundEvent();
        }
    }

    // Quick scan of available devices
    private void quickScanDevices()
    {
        // Fast
        prevDeviceList = deviceList;
        deviceList.clear();
        deviceList.addAll(BluetoothUtils.getPrimaryRadio().getBondedDevices());
    }

    // Thorough scan of available devices
    private void thoroughScanForDevices()
    {
        // Slow
        deviceList.clear();
        BluetoothUtils.getPrimaryRadio().startDiscovery();
        //ApplicationContextProvider.getContext().registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        //deviceList.addAll(bluetoothTransport.BluetoothClient.DiscoverDevices(65536, true, true, true, true));
    }

    // Pair a Bluetooth device
    private boolean pairDevice(BluetoothDevice device)
    {
        //if (device.Authenticated) return true;
        Log(2, "Trying to pair device " + device.getName() + " (" + device.getAddress()+ ") ");

        // Check if PIN  for this device has been injected in ConnectionManager
        String address = device.getAddress();

        String matchedDevicePin = findPin(address);
        if (matchedDevicePin != null)
        {

            Log(3, "Trying known key for device " + device.getName());
//            if (BluetoothSecurity.PairRequest(device.getAddress(), matchedDevicePin))
//            {
//                Log(2, "Pairing device " + device.getAddress() + " successful! ");
//                return true;
//            }
//            try {
//                // When trying PINS, you really need to wait in between
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {}
        }

        // Check if PIN has been previously found and stored
        if (bluetoothConnectionManagerSettings.getStoredDevicePins().containsKey(device.getAddress()))
        {
            Log(3, "Trying stored key for device " + device.getName() );
//            if (BluetoothSecurity.PairRequest(device.getAddress(), bluetoothConnectionManagerSettings.getStoredDevicePins().get(device.getAddress())))
//            {
//                Log(2, "Pairing device " + device.getAddress() + " successful! ");
//                return true;
//            }
//            try {
//                // When trying PINS, you really need to wait in between
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {}
        }

        // loop through general pins PIN numbers that have been injected to see if they pair
        for (String devicePin : generalPins)
        {

            Log(3, "Trying known general pin " + devicePin + " for device " + device.getName());
//            boolean isPaired = BluetoothSecurity.PairRequest(device.getAddress(), devicePin);
//            if (isPaired)
//            {
//                bluetoothConnectionManagerSettings.updateDevicePin(device.getAddress(), devicePin);
//                Log(2, "Pairing device " + device.getAddress() + " successful! ");
//                return true;
//            }
//            try {
//                // When trying PINS, you really need to wait in between
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {}
        }

        // loop through common PIN numbers to see if they pair
        for (String devicePin : commonDevicePins)
        {
            Log(3, "Trying common pin " + devicePin + " for device " + device.getName());
//            boolean isPaired = BluetoothSecurity.PairRequest(device.getAddress(), devicePin);
//            if (isPaired)
//            {
//                bluetoothConnectionManagerSettings.updateDevicePin(device.getAddress(), devicePin);
//                storeSettings();
//                Log(2, "Pairing device " + device.getName() + " successful! ");
//                return true;
//            }
//            try {
//                // When trying PINS, you really need to wait in between
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {}
        }

        Log(2, "Pairing device " + device.getName() + " unsuccessful ");
        return true;
    }

    // Find the pin code for a Bluetooth address
    private String findPin(String address)
    {
        return devicePins.get(address);
    }


    private boolean tryConnection(String bluetoothAddress, int timeOut)
    {
        if (bluetoothAddress == null) return false;
        // Find
        for (BluetoothDevice device : deviceList) {
            if (device.getAddress() == bluetoothAddress)
            {
                return tryConnection(device, timeOut);
            }
        }
        return false;
    }

    private boolean tryConnection(BluetoothDevice bluetoothDeviceInfo, int timeOut)
    {
        // Try specific settings
        bluetoothTransport.setCurrentBluetoothDeviceInfo(bluetoothDeviceInfo);
        return tryConnection(timeOut);
    }

    private boolean tryConnection(int timeOut) {
        synchronized (tryConnectionLock) {
            // Check if an (old) connection exists
            if (bluetoothTransport.getCurrentBluetoothDeviceInfo() == null) return false;

            setConnected(false);
            Log(1, "Trying Bluetooth device " + bluetoothTransport.getCurrentBluetoothDeviceInfo().getName());
            if (bluetoothTransport.connect()) {
                Log(3,
                        "Connected with Bluetooth device " + bluetoothTransport.getCurrentBluetoothDeviceInfo().getName() +
                                ", requesting response");

                deviceStatus status = isArduinoAvailable(timeOut, 5);
                setConnected(status == deviceStatus.Available);

                if (isConnected()) {
                    Log(1,
                            "Connected with Bluetooth device " +
                                    bluetoothTransport.getCurrentBluetoothDeviceInfo().getName());
                    storeSettings();
                } else {
                    Log(3,
                            "Connected with Bluetooth device " +
                                    bluetoothTransport.getCurrentBluetoothDeviceInfo().getName() + ", received no response");
                }
                return isConnected();
            } else {
                Log(3,
                        "No connection made with Bluetooth device " + bluetoothTransport.getCurrentBluetoothDeviceInfo().getName());
            }
            return false;
        }
    }

    protected void startScan()
    {
        super.startScan();

        if (connectionManagerMode == Mode.Scan)
        {
            scanType = ScanType.None;
        }
    }

    private boolean quickScan()
    {
        Log(3, "Performing quick scan");
        final int longTimeOut =  1000;
        final int shortTimeOut = 1000;

        // First try if currentConnection is open or can be opened
        if (tryConnection(longTimeOut)) return true;

        // Do a quick rescan of all devices in range
        quickScanDevices();

        if (isPersistentSettings())
        {
            // Then try if last stored connection can be opened
            Log(3, "Trying last stored connection");
            if (tryConnection(bluetoothConnectionManagerSettings.getBluetoothAddress(), longTimeOut)) return true;
        }

        // Then see if new devices have been added to the list
        if (newDevicesScan()) return true;

        for (BluetoothDevice device : deviceList)
        {

            try {
                Thread.sleep(100); // Bluetooth devices seem to work more reliably with some waits
            } catch (InterruptedException e) {}
            Log(1, "Trying Device " + device.getName() + " (" + device.getAddress() + ") " );
            if (tryConnection(device, shortTimeOut)) return true;
        }

        return false;
    }

    private boolean thoroughScan()
    {
        Log(3, "Performing thorough scan");
        final int longTimeOut = 1000;
        final int shortTimeOut = 1000;

        // First try if currentConnection is open or can be opened
        if (tryConnection(longTimeOut)) return true;

        // Do a quick rescan of all devices in range
        thoroughScanForDevices();

        // Then try if last stored connection can be opened
        Log(3, "Trying last stored connection");
        if (tryConnection(bluetoothConnectionManagerSettings.getBluetoothAddress(), longTimeOut)) return true;

        // Then see if new devices have been added to the list
        if (newDevicesScan()) return true;

        for (BluetoothDevice device : deviceList)
        {
            try {
                Thread.sleep(100); // Bluetooth devices seem to work more reliably with some waits
            } catch (InterruptedException e) {}
            if (pairDevice(device))
            {
                Log(1, "Trying Device " + device.getName() + " (" + device.getAddress() + ") ");
                if (tryConnection(device, shortTimeOut)) return true;
            }
        }
        return false;
    }

    private boolean newDevicesScan()
    {
        final int shortTimeOut = 200;

        // Then see if port list has changed
        List<BluetoothDevice> newDevices = newDevicesInList();
        if (newDevices.size() == 0) { return false; }

        Log(1, "Trying new devices");

        for (BluetoothDevice device : newDevices)
        {
            if (tryConnection(device, shortTimeOut)) return true;
            try {
                Thread.sleep(100); // Bluetooth devices seem to work more reliably with some waits
            } catch (InterruptedException e) {}
        }
        return false;
    }

    private List<BluetoothDevice> newDevicesInList()
    {
        ArrayList<BluetoothDevice> newDevices = new ArrayList<BluetoothDevice>();
        for (BluetoothDevice device : deviceList) {
            if(!prevDeviceList.contains(device))
            {
                newDevices.add(device);
            }
        }
        return newDevices;
    }

    protected void storeSettings()
    {
        if (!isPersistentSettings()) return;
        bluetoothConnectionManagerSettings.setBluetoothAddress(bluetoothTransport.getCurrentBluetoothDeviceInfo().getAddress());

        bluetoothConnectionStorer.storeSettings(bluetoothConnectionManagerSettings);
    }

    protected final void readSettings()
    {
        if (!isPersistentSettings()) return;
        bluetoothConnectionManagerSettings = bluetoothConnectionStorer.retrieveSettings();
    }

}
