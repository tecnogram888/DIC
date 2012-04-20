package com.dic.BTMesh;

import java.util.ArrayList;

import com.dic.BTMesh.BTChat.BTChatListener;

import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class BTMesh extends TabActivity {
    private static final String TAG = "BTMesh";
    private static final boolean D = true;
    
    // Message types sent from the BluetoothMeshService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;    
    

    public static TextView mTitle;    
    
    private BTMeshState BTMState;
    
    private BTMeshListener BTMListener;
    private boolean listenerRegistered = false;

    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    // Visual Things
		super.onCreate(savedInstanceState);
	    Resources res = getResources(); // Resource object to get Drawables
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    setContentView(R.layout.main);	    
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        BTMListener = new BTMeshListener();
        
        if (!listenerRegistered) {
            registerReceiver(BTMListener, new IntentFilter("com.dic.BTMesh.updatestatus"));
            listenerRegistered = true;
        }
		
		BTMState = ((BTMeshState)getApplicationContext());
	    BTMState.newService(mHandler);
	    BTMState.newAdapter();
	    


        


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

	 // BTFileManager
	    intent = new Intent().setClass(this, BTFileManager.class);
	    
	    spec = tabHost.newTabSpec("fileManager").setIndicator("FileManager",
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

        // If the adapter is null, then Bluetooth is not supported
        if (BTMState.getBluetoothAdapter() == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        


        
        
	    
	}
	
    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!BTMState.getBluetoothAdapter().isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        }
    }
    
    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (BTMState.getService() != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (BTMState.getConnectionState() == BTMeshService.STATE_NONE) {
              // Start the Bluetooth chat services
              BTMState.getService().start();
            }
        }
        if (!listenerRegistered) {
            registerReceiver(BTMListener, new IntentFilter("com.dic.BTMesh.updatestatus"));
            listenerRegistered = true;
        }
    }
    
    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- BTMesh PAUSE -");
        /*if (listenerRegistered) {
            unregisterReceiver(BTMListener);
            listenerRegistered = false;
        }*/
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (BTMState.getService() != null) BTMState.getService().stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }
	
    
    // so this needs to go in the handler i guess
    public static void setStatus(int s) {
    	mTitle.setText(s);
    }
    
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                /*if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BTMeshService.STATE_CONNECTED:
                    BTMesh.mTitle.setText(R.string.title_connected_to);
                    //mConversationArrayAdapter.clear();
                    break;
                case BTMeshService.STATE_CONNECTING:
                    BTMesh.mTitle.setText(R.string.title_connecting);
                    break;
                case BTMeshService.STATE_LISTEN:
                case BTMeshService.STATE_NONE:
                    BTMesh.mTitle.setText(R.string.title_not_connected);
                    break;
                }*/
                break;
            case MESSAGE_WRITE:
                /*byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                //mConversationArrayAdapter.add(mBluetoothAdapter.getName() + ":  " + writeMessage);*/
                break;
            case MESSAGE_READ:
            	if (D) Log.d(TAG, "received a read message, sending to chat");
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                if (readMessage.length() > 0 ) {
                    //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                	Intent i = new Intent();
                	i.setAction("com.dic.BTMesh.addmessages");
                	i.putExtra("messages", readMessage);
                	sendBroadcast(i);
                }
                else {
                	if (D) Log.d(TAG, "message is empty, not sending actually");
                }
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                /*mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();*/
                break;
            //case MESSAGE_TOAST:
            //    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
            //                   Toast.LENGTH_SHORT).show();
            //    break;
            }
        }
    };
    protected class BTMeshListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.dic.BTMesh.updatestatus")) {
                if(D) Log.d(TAG, "BTMesh received updatestatus intent for " + intent.getStringExtra("status"));
            	String newStatus = intent.getStringExtra("status");
            	mTitle.setText(newStatus);
                // Do something
            }
        }
    }


}
