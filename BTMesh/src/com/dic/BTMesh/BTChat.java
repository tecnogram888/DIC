/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dic.BTMesh;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * This is the main Activity that displays the current chat session.
 */
public class BTChat extends Activity {
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothMeshService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothMeshService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String CONN_ID = "connection_id";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    //private TextView mTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    
    //My name
    private String myAdapterName;
    
    // Array adapter for the conversation thread at some point, each should be an object with timestamp and received status.
    private ArrayAdapter<BTMessage> mConversationArrayFull;
    private ArrayAdapter<BTMessage> mConversationArrayUnsent;
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    
    private BTMeshState BTMState;
    private BTChatListener BTMListener;
    private boolean listenerRegistered = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ BTChat CREATE +++");
        
		BTMState = ((BTMeshState)getApplicationContext());
		BTMListener = new BTChatListener();
        
        // Set up the window layout
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.btchat);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
        // Get local Bluetooth adapter
        myAdapterName = BTMState.getBluetoothAdapter().getName();
        
        
        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationArrayFull = new ArrayAdapter<BTMessage>(this, R.layout.message);
        mConversationArrayUnsent = new ArrayAdapter<BTMessage>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });



        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    
    @Override
    protected void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+++ BTChat Resume +++");

        if (!listenerRegistered) {
            registerReceiver(BTMListener, new IntentFilter("com.dic.BTMesh.addmessages"));
            listenerRegistered = true;
        }
    }


    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- BTChat PAUSE -");
        if (listenerRegistered) {
            unregisterReceiver(BTMListener);
            listenerRegistered = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- BTChat STOP --");
    }



    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "BTChat ensure discoverable");
        if (BTMState.getBluetoothAdapter().getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    
    private void addMessagesToConvo(String m) {
        if(D) Log.d(TAG, "BTChat adding Messages");
    	int authorInd = m.indexOf("@author", 0);
    	int timestampInd = m.indexOf("@timestamp", 0);
    	int textInd = m.indexOf("@text", 0);
    	int endInd = m.indexOf("@end", 0);
    	String author = "";
    	String timestamp = "";
    	String text = "";
    	boolean passOnMessage = false;
    	while(authorInd != -1) {
    		author = m.substring(authorInd+8, timestampInd);
    		timestamp = m.substring(timestampInd+10, textInd);
    		text = m.substring(textInd+5, endInd);
    		BTMessage newMessage = new BTMessage(author, timestamp, text);
    		//causes ordering to be confusing
    		//int addIndex = mConversationArrayFull.getCount()-1; //index to add the message to, -1 if don't add
    		boolean add = true;
    		for (int existingInd = 0; existingInd < mConversationArrayFull.getCount(); existingInd++){
    			BTMessage currMessage = mConversationArrayFull.getItem(existingInd);
    			//mConversationArrayAdapter.add("Comparing " + author + timestamp + text + " with " + currMessage.getAuthor() + currMessage.getTimestamp() + currMessage.getText());
    			if (currMessage.getAuthor().equals(author) &&
    				  currMessage.getTimestamp().equals(timestamp) &&
    				  currMessage.getText().equals(text)){
    				add = false;
    				break;
    			}
    			/*
    	        DateFormat format =
    	                DateFormat.getDateTimeInstance(
    	                DateFormat.MEDIUM, DateFormat.SHORT);
				try {
					Date currDate = format.parse(currMessage.getTimestamp());
					Date myDate = format.parse(timestamp);
	    			if (currDate.after(myDate)){
	    				addIndex = existingInd;
	    				break;
	    			}
				} catch (ParseException e) {
				}*/
    		}
    		/*if (mConversationArrayFull.getCount() == 0){
    			addIndex = 0;
    		}
    		if (addIndex != -1){
				mConversationArrayFull.insert(newMessage, addIndex);
				mConversationArrayAdapter.insert(author + ": " + text, addIndex);
    		}*/
    		if (add){
    			passOnMessage = true;
    			mConversationArrayFull.add(newMessage);
    			mConversationArrayUnsent.add(newMessage);
    			mConversationArrayAdapter.add(author + ": " + text);
    		}
    		authorInd = m.indexOf("@author", authorInd+1);
    		timestampInd = m.indexOf("@timestamp", timestampInd+1);
    		textInd = m.indexOf("@text", textInd+1);
    		endInd = m.indexOf("@end", endInd+1);
    	}
    	if (passOnMessage){
    		sendData();
    	}
    }

    private String unsentConvoToString() {
        if(D) Log.d(TAG, "BTChat unsentConvoToString");
    	String flat = "";
    	for (int i = 0; i < mConversationArrayUnsent.getCount(); i++){
    		BTMessage m = mConversationArrayUnsent.getItem(i);
    		flat = flat + "@start@author:" + m.getAuthor() + "@timestamp" + m.getTimestamp() + "@text" + m.getText() + "@end";
    	}
    	return flat;
    }
    
    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        if(D) Log.d(TAG, "BTChat sendMessage");
        /*// Check that we're actually connected before trying anything
        if (mChatService.getState() != BTMeshService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        //}

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }*/
    	if (message.length() == 0) {
    		return;
    	}
    	
    	if (BTMState.getConnectionState() == BTMeshService.STATE_NONE) {
    		Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	String timeStamp = DateFormat.getDateTimeInstance().format(new Date());
    	mConversationArrayAdapter.add(myAdapterName + ": " + message);
    	BTMessage newMessage = new BTMessage(myAdapterName, timeStamp, message);
    	mConversationArrayFull.add(newMessage);
    	mConversationArrayUnsent.add(newMessage);
        //mConversationArrayAdapter.add(mBluetoothAdapter.getName() +  " @ " + timeStamp + ":\n" + message);
    	mOutStringBuffer.setLength(0);
    	mOutEditText.setText(mOutStringBuffer);
    	
    	sendData();
    	mConversationArrayUnsent.clear();

    }
    
    private void sendData(){
        if(D) Log.d(TAG, "BTChat sendData");
    	byte[] send = unsentConvoToString().getBytes();
    	BTMState.getService().write(send);
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if(D) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };
    
    // Nested 'listener'
    protected class BTChatListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("com.dic.BTMesh.addmessages")) {
                if(D) Log.d(TAG, "BTChat received addmessages intent");
            	String messages = intent.getStringExtra("messages");
            	addMessagesToConvo(messages);
                // Do something
            }
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    
/*
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
            	String address = data.getExtras()
            						.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                BluetoothDevice device = BTMeshService.mBluetoothAdapter.getRemoteDevice(address);
                mChatService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BLuetoothDevice object
        BluetoothDevice device = BTMeshService.mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device);
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
    }*/

}
