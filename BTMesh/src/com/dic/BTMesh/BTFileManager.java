package com.dic.BTMesh;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BTFileManager extends Activity {
    // Debugging
    private static final String TAG = "BluetoothFileManager";
    private static final boolean D = true;

    // Message types sent from the BluetoothMeshService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Layout Views
    //private TextView mTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    //My name
    private String myAdapterName;
    
    private BTMeshState BTMState;
    private BTChatListener BTMListener;
    private boolean listenerRegistered = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ BTFileManager CREATE +++");

		BTMState = ((BTMeshState)getApplicationContext());
		
		/*BTMListener = new BTChatListener();
        if (!listenerRegistered) {
            registerReceiver(BTMListener, new IntentFilter("com.dic.BTMesh.addmessages"));
            listenerRegistered = true;
        }*/
        
        //TextView textview = new TextView(this);
	    //String showText = "Connected to:\n";
	    /*ArrayList<String> names = BTMState.getService().getDeviceNames();
	    for (int i = 0; i < names.size(); i++){
	    	if (names.get(i) != null) {
	    		showText += (names.get(i) + "\n");
	    	}
	    }
	    textview.setText(showText);
	    setContentView(textview);*/
	    
        
        // Set up the window layout
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.btchat);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
        // Get local Bluetooth adapter
        myAdapterName = BTMState.getBluetoothAdapter().getName();
        
        
        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
//                sendMessage(message);
            }
        });



        // Initialize the buffer for outgoing messages
//        mOutStringBuffer = new StringBuffer("");

		sendData();
		
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        if(D) Log.d(TAG, "BTChat sendMessage");
        

    	if (message.length() == 0) {
    		return;
    	}
    	
    	if (BTMState.getConnectionState() == BTMeshService.STATE_NONE) {
    		Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
    		return;
    	}
    	/*
    	String timeStamp = DateFormat.getDateTimeInstance().format(new Date());
    	mConversationArrayAdapter.add(myAdapterName + ": " + message);
    	BTMessage newMessage = new BTMessage(myAdapterName, timeStamp, message);
    	mConversationArrayFull.add(newMessage);
    	mConversationArrayUnsent.add(newMessage);
        //mConversationArrayAdapter.add(mBluetoothAdapter.getName() +  " @ " + timeStamp + ":\n" + message);
    	mOutStringBuffer.setLength(0);
    	mOutEditText.setText(mOutStringBuffer);
    	*/
    	sendData();
    	//mConversationArrayUnsent.clear();
        
        
    }

    private void sendData(){
        if(D) Log.d(TAG, "BTChat sendData");
        String testBTFileManager = "@BTFileManager<type>RequestForFiles</type>";
        BTMState.getService().write(testBTFileManager.getBytes());
    	//byte[] send = unsentConvoToString().getBytes();
    	//BTMState.getService().write(send);
    }
    
    private void processMessage(String message) {
    	String type = message.substring( message.indexOf("<type>") + 6, message.indexOf("</type>") );
    	if (type.equals("RequestForFiles")); {

            if(D) Log.d(TAG, "BTProcessMessage YAY RECEIVED DATA");
    	}
    }

    
    // Nested 'listener'
    protected class BTChatListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (D) Log.d(TAG, "receive " + intent.getAction());
            if (intent.getAction().equals("com.dic.BTFileManager.processMessage")) {
                if(D) Log.d(TAG, "BTFileManager received processMessage intent");
            	String message = intent.getStringExtra("message");
            	processMessage(message);
                // Do something
            }
        }
    }
}
