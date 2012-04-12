package com.dic.BTMesh;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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

public class BTConnectionManager extends Activity {
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
    

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    TextView textview = new TextView(this);
	    String showText = "Connected to:\n";
	    for (int i = 0; i < BTMesh.mDeviceNames.size(); i++){
	    	if (BTMesh.mDeviceNames.get(i) != null) {
	    		showText += (BTMesh.mDeviceNames.get(i) + "\n");
	    	}
	    }
	    textview.setText(showText);
	    setContentView(textview);
	}
	
	public void onResume() {
		super.onResume();
	    TextView textview = new TextView(this);
	    String showText = "Connected to:\n";
	    for (int i = 0; i < BTMesh.mDeviceNames.size(); i++){
	    	if (BTMesh.mDeviceNames.get(i) != null) {
	    		showText += (BTMesh.mDeviceNames.get(i) + "\n");
	    	}
	    }
	    textview.setText(showText);
	    setContentView(textview);
		
	}
	
    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (BTMesh.mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
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
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
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
