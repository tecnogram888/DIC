package com.dic.BTMesh;

import java.util.ArrayList;

import com.dic.BTMesh.BTChat.BTChatListener;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    private static final String TAG = "BTConnectionManager";
    private static final boolean D = true;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
    private BTMeshState BTMState;
    private BTCMListener BTMListener;
    private boolean listenerRegistered = false;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
		BTMState = ((BTMeshState)getApplicationContext());
		BTMListener = new BTCMListener();
        if (!listenerRegistered) {
            registerReceiver(BTMListener, new IntentFilter("com.dic.BTMesh.updateCM"));
            listenerRegistered = true;
        }
		updateView();

	}
	
	public void onResume() {
		super.onResume();
	    updateView();
	}
	
	public void updateView() {
	    TextView textview = new TextView(this);
	    String showText = "Connected to:\n";
/*	    ArrayList<ConnectedThread> connections = BTMState.getService().mConnectedThreads;
	    for (int i = 0; i < connections.size(); i++){
	    	ConnectedThread c = connections.get(i);
	    	if (c != null) {
	    		showText += (Integer.toString(c.index) + " " + c.deviceName + ":\t" + c.deviceAddress + "\n");
	    	}
	    }*/
	    ArrayList<String> addresses = BTMState.getService().mDeviceAddresses;
	    for (int i = 0; i < addresses.size(); i++) {
	    	if (addresses.get(i) != null) {
	    		showText += (addresses.get(i) + "\n");
	    	} else {
	    		showText += ("no connection\n");
	    	}
	    }
	    textview.setText(showText);
	    setContentView(textview);
	}

	
    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (BTMState.getBluetoothAdapter().getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            //BTMState.setConnectionState(BTMesh.STATE_BROADCASTING);
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
        	//BTMState.setConnectionState(STATE_SEARCHING);
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
    protected class BTCMListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.dic.BTMesh.updateCM")) {
            	updateView();
                // Do something
            }
        }
    }

}
