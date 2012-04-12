package com.dic.BTMesh;

import java.util.ArrayList;

import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;

public class BTMesh extends TabActivity {
    private static final boolean D = true;
    
    //shared values
    // Local Bluetooth adapter
    public static BluetoothAdapter mBluetoothAdapter = null;
    public static TextView mTitle;
    public static ArrayList<String> mDeviceNames;

    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    // Visual Things
		super.onCreate(savedInstanceState);
	    
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    setContentView(R.layout.main);	    
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        
	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, BTChat.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("chat").setIndicator("Chat",
	                      res.getDrawable(R.drawable.ic_tab_chat))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, BTConnectionManager.class);
	    spec = tabHost.newTabSpec("connection").setIndicator("Connection",
	                      res.getDrawable(R.drawable.ic_tab_connectionmanager))
	                  .setContent(intent);
	    tabHost.addTab(spec);


	    tabHost.setCurrentTab(0);
	    
	    // Connection Things
	    mDeviceNames = new ArrayList<String>();
	    for (int i = 0; i < 7; i++) {
	    	mDeviceNames.add(null);
	    }
	    
	}
	

}
