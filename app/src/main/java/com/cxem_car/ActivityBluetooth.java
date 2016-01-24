package com.cxem_car;

import android.app.Activity;

import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;

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

import com.github.pimsturm.commandmessenger.Transport.Bluetooth.BluetoothUtils;

public class ActivityBluetooth extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private Button onBtn;
    private Button offBtn;
    private Button listBtn;
    private Button findBtn;
    private TextView text;
    private Set<BluetoothDevice> pairedDevices;
    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        text = (TextView) findViewById(R.id.text);
        onBtn = (Button) findViewById(R.id.turnOn);
        offBtn = (Button) findViewById(R.id.turnOff);
        listBtn = (Button) findViewById(R.id.paired);
        findBtn = (Button) findViewById(R.id.search);

        if (BluetoothUtils.getPrimaryRadio() == null) {

            onBtn.setEnabled(false);
            offBtn.setEnabled(false);
            listBtn.setEnabled(false);
            findBtn.setEnabled(false);

            text.setText("Status: not supported");

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

            myListView = (ListView) findViewById(R.id.listView1);

            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
            BTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            myListView.setAdapter(BTArrayAdapter);
        }
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
        if (BluetoothUtils.getPrimaryRadio().isEnabled()) {
            text.setText("Status: Enabled");
        } else {
            text.setText("Status: Disabled");
        }

    }
    public void list(View view) {
        // get paired devices
        pairedDevices = BluetoothUtils.getPrimaryRadio().getBondedDevices();

        // put it's one to the adapter
        for (BluetoothDevice device : pairedDevices)
            BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

        Toast.makeText(getApplicationContext(), "Show Paired Devices",
                Toast.LENGTH_SHORT).show();

    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void find(View view) {
        if (BluetoothUtils.getPrimaryRadio().isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            BluetoothUtils.getPrimaryRadio().cancelDiscovery();
        } else {
            BTArrayAdapter.clear();
            BluetoothUtils.getPrimaryRadio().startDiscovery();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    public void off(View view) {
        BluetoothUtils.getPrimaryRadio().disable();
        text.setText("Status: Disconnected");

        Toast.makeText(getApplicationContext(), "Bluetooth turned off",
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }

}
