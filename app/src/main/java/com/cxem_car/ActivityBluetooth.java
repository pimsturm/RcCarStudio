package com.cxem_car;

import android.app.Activity;

import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Set;

import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pimsturm.commandmessenger.EventHandler;
import com.github.pimsturm.commandmessenger.Transport.Bluetooth.BluetoothReceiver;
import com.github.pimsturm.commandmessenger.Transport.Bluetooth.BluetoothUtils;

public class ActivityBluetooth extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private TextView text;
    private ArrayAdapter<String> BTArrayAdapter;
    private final BluetoothReceiver bReceiver = BluetoothReceiver.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        text = (TextView) findViewById(R.id.text);
        Button onBtn = (Button) findViewById(R.id.turnOn);
        Button offBtn = (Button) findViewById(R.id.turnOff);
        Button listBtn = (Button) findViewById(R.id.paired);
        Button findBtn = (Button) findViewById(R.id.search);

        if (BluetoothUtils.getPrimaryRadio() == null) {

            onBtn.setEnabled(false);
            offBtn.setEnabled(false);
            listBtn.setEnabled(false);
            findBtn.setEnabled(false);

            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            onBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    on(v);
                }
            });

            offBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    off(v);
                }
            });

            listBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    list(v);
                }
            });

            findBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    find(v);
                }
            });

            ListView myListView = (ListView) findViewById(R.id.listView1);

            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
            BTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            myListView.setAdapter(BTArrayAdapter);
        }

        bReceiver.setDeviceFound(new EventHandler<BluetoothDevice>() {
            @Override
            public void invokeEvent(Object sender, BluetoothDevice device) {
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();

            }
        });

        setStatusText();
    }

    public void on(View view) {
        if (!BluetoothUtils.getPrimaryRadio().isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            Toast.makeText(getApplicationContext(), "Bluetooth turned on",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is already on",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            setStatusText();
        }
    }

    private void setStatusText() {
        if (BluetoothUtils.getPrimaryRadio() == null) {
            text.setText(R.string.bluetooth_not_available);
        } else if (BluetoothUtils.getPrimaryRadio().isEnabled()) {
            text.setText(R.string.bluetooth_enabled);
        } else {
            text.setText(R.string.bluetooth_disabled);
        }

    }
    public void list(View view) {
        // get paired devices
        Set<BluetoothDevice> pairedDevices = BluetoothUtils.getPrimaryRadio().getBondedDevices();

        // add them one by one to the adapter
        BTArrayAdapter.clear();
        for (BluetoothDevice device : pairedDevices)
            BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

        Toast.makeText(getApplicationContext(), "Show Paired Devices",
                Toast.LENGTH_SHORT).show();

    }

    public void find(View view) {
        if (BluetoothUtils.getPrimaryRadio().isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            BluetoothUtils.getPrimaryRadio().cancelDiscovery();
        } else {
            BTArrayAdapter.clear();
            BluetoothUtils.getPrimaryRadio().startDiscovery();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            bReceiver.register();
        }
    }

    public void off(View view) {
        BluetoothUtils.getPrimaryRadio().disable();
        text.setText(R.string.bluetooth_disconnected);

        Toast.makeText(getApplicationContext(), "Bluetooth turned off",
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bReceiver.isRegistered()) {
            unregisterReceiver(bReceiver);
            bReceiver.unregister();
        }
    }

}
