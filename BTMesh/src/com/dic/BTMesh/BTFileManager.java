package com.dic.BTMesh;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.dic.BTMesh.BTChat.BTChatListener;

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
		
		BTMListener = new BTChatListener();
        if (!listenerRegistered) {
            registerReceiver(BTMListener, new IntentFilter("com.dic.BTMesh.addmessages"));
            listenerRegistered = true;
        }
        
        TextView textview = new TextView(this);
	    String showText = "Connected to:\n";
	    ArrayList<String> names = BTMState.getService().getDeviceNames();
	    for (int i = 0; i < names.size(); i++){
	    	if (names.get(i) != null) {
	    		showText += (names.get(i) + "\n");
	    	}
	    }
	    textview.setText(showText);
	    setContentView(textview);
	    
        
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
        
    }
    

    // Nested 'listener'
    protected class BTChatListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (D) Log.d(TAG, "receive " + intent.getAction());
            if (intent.getAction().equals("com.dic.BTMesh.addmessages")) {
                if(D) Log.d(TAG, "BTChat received addmessages intent");
            	String messages = intent.getStringExtra("messages");
//            	addMessagesToConvo(messages);
                // Do something
            }
        }
    }
}
