package com.dic.BTMesh;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

public class BTConnectionManager extends Activity {
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_BROADCASTING = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int STATE_SEARCHING = 4;
    private static final String TAG = "BTConnectionManager";
    private static final boolean D = true;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
    private BTMeshState BTMState;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
		BTMState = ((BTMeshState)getApplicationContext());
		updateView();

	}
	
	public void onResume() {
		super.onResume();
	    updateView();
	}
	
	public void updateView() {
	    TextView textview = new TextView(this);
	    String showText = "Connected to:\n";
	    ArrayList<String> names = BTMState.getDeviceNames();
	    for (int i = 0; i < names.size(); i++){
	    	if (names.get(i) != null) {
	    		showText += (names.get(i) + "\n");
	    	}
	    }
	    textview.setText(showText);
	    setContentView(textview);
	}
	
	// pending
	public int getNumLocalDevices(){
		return -1;
	}
	
	public int getNumGlobalDevices(){
		return -1;
	}

	
    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (BTMState.getBluetoothAdapter().getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            BTMState.setConnectionState(STATE_BROADCASTING);
            startActivity(discoverableIntent);
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
            	String address = data.getExtras()
            						.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                BluetoothDevice device = BTMState.getBluetoothAdapter().getRemoteDevice(address);
                BTMState.getService().connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
            	// Generalize to set up connection or something
            } else {
                // User did not enable Bluetooth or an error occured
            	// Do some sort of failure thing, ideally message and quit
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                BTMState.setConnectionState(STATE_NONE);
                finish();
            }
        }
    }
/*
    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BLuetoothDevice object
        BluetoothDevice device = BTMState.getBluetoothAdapter().getRemoteDevice(address);
        // Attempt to connect to the device
        BTMState.getService().connect(device);
    }
*/	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "options item selected");
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
        	BTMState.setConnectionState(STATE_SEARCHING);
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }

}
