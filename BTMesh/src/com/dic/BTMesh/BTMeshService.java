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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BTMeshService {
    // Debugging
    private static final String TAG = "BTMeshService";
    private static final boolean D = false;

    // Name for the SDP record when creating server socket
    private static final String NAME = "BTMeshService";

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;


    //Eventually the two below will be some sort of node of a tree
    public ArrayList<String> mDeviceAddresses;
    public ArrayList<AcceptThread> mAcceptThreads;    
    private ArrayList<ConnectedThread> mConnectedThreads;
    /**
     * A bluetooth piconet can support up to 7 connections. This array holds 7 unique UUIDs.
     * When attempting to make a connection, the UUID on the client must match one that the server
     * is listening for. When accepting incoming connections server listens for all 7 UUIDs. 
     * When trying to form an outgoing connection, the client tries each UUID one at a time. 
     */
    private ArrayList<UUID> mUuids;
    
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    private BTMeshState BTMState;
    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BTMeshService(Context context, Handler handler, BTMeshState s) {
    	BTMState = s;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        BTMState.setConnectionState(STATE_NONE);
        mHandler = handler;
        mDeviceAddresses = new ArrayList<String>();
        mAcceptThreads = new ArrayList<AcceptThread>();
        mConnectedThreads = new ArrayList<ConnectedThread>();
        for (int i = 0; i < 7; i++) {
        	mDeviceAddresses.add(null);
        	mAcceptThreads.add(null);
        	mConnectedThreads.add(null);
        }
        mUuids = new ArrayList<UUID>();
        // 7 randomly-generated UUIDs. These must match on both server and client.
        mUuids.add(UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
        mUuids.add(UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));
        mUuids.add(UUID.fromString("e442e09a-51f3-4a7b-91cb-f638491d1412"));
        mUuids.add(UUID.fromString("a81d6504-4536-49ee-a475-7d96d09439e4"));
        mUuids.add(UUID.fromString("aa91eab1-d8ad-448e-abdb-95ebba4a9b55"));
        mUuids.add(UUID.fromString("4d34da73-d0a4-4f40-ac38-917e0a9dee97"));
        mUuids.add(UUID.fromString("5e14d4df-9c8a-4db7-81e4-c937564c86e0"));
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "BTMS start");
        
        // Start the thread to listen on a BluetoothServerSocket
        initAcceptThreads();
        BTMState.setConnectionState(STATE_LISTEN);
    }
    
    public synchronized int numConnections() {
    	int ret = 0;
    	for (int i = 0; i < 7; i++) {
    		if (mConnectedThreads.get(i) != null) {
    			ret += 1;
    		}
    	}
    	return ret;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "BTMS connect to: " + device);
        
        // Already connected, don't even try
        for (int i = 0; i < 7; i++) {
        	if (mConnectedThreads.get(i) != null &&
        			mConnectedThreads.get(i).device.getAddress().equals(device.getAddress())) {
        		if (D) Log.d(TAG, "BTMS already connected on channel " + Integer.toString(i));
        		return;
        	}
        }

         

        BTMState.setConnectionState(STATE_CONNECTING);
        boolean success = false;
        for (int i = 0; i < 7; i++) {
        	try {
        		if (connectSocket(device, mUuids.get(i), i)) {
        			if (D) Log.d(TAG, "BTMS connectSocket success on channel " + Integer.toString(i));
        			success = true;
        			break;
        		} else {
        			if (D) Log.d(TAG, "BTMS connectSocket fail on channel " + Integer.toString(i));
        		}
        	} catch (Exception e) {
        	}
        }
        if (!success) {
        	if (D) Log.d(TAG, "BTMS Connect failed for all channels");
        } else {
        	BTMState.setConnectionState(STATE_CONNECTED);
        }
    }

    public boolean connectSocket(BluetoothDevice device, UUID uuidToTry, int channel) {
    	if (D) Log.d(TAG, "BTMS connectSocket start on channel " + Integer.toString(channel));
    	if (mConnectedThreads.get(channel) != null) {
    		if (D) Log.d(TAG, "BTMS connectSocket stop because channel in use");
    		return false;
    	}
    	BluetoothSocket tmp = null;
		try {
			tmp = device.createInsecureRfcommSocketToServiceRecord(uuidToTry);
		} catch (IOException e1) {
			return false;
		}  
		BluetoothSocket mmSocket = tmp;
	    // Always cancel discovery because it will slow down a connection
	    mAdapter.cancelDiscovery();
	
	    // Make a connection to the BluetoothSocket
	    try {
	        // This is a blocking call and will only return on a
	        // successful connection or an exception
	        mmSocket.connect();
	        if (D) Log.d(TAG, "BTMS connectSocket connecting");
	    } catch (IOException e) {
	    	if (D) Log.d(TAG, "BTMS connectSocket fail");
	        try {
	            mmSocket.close();
	        } catch (IOException e2) {
	            Log.e(TAG, "unable to close() socket during connection failure", e2);
	        }
	        if (D) Log.d(TAG, "BTMS connectSocket returning fail");
	        return false;
	    }
	
	    // Start the connected thread
	    if (D) Log.d(TAG, "BTMS connectSocket returning success");
	    connected(mmSocket, device, channel);
	    return true;
    }    
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, int channel) {
        if (D) Log.d(TAG, "BTMS connected on channel " + Integer.toString(channel));
        

        // Start the thread to manage the connection and perform transmissions
        mDeviceAddresses.set(channel, (device.getAddress() + "-" + device.getName()));
        ConnectedThread c = new ConnectedThread(socket, device, channel);
        c.start();
        // Add each connected thread to an array
        if (D) Log.d(TAG, "BTMS adding connected thread to arraylist");
        mConnectedThreads.set(channel, c);

        BTMState.setConnectionState(STATE_CONNECTED);
        initAcceptThreads();
    }
    
    public synchronized void initAcceptThreads() {
    	if (D) Log.d(TAG, "BTMS initAcceptThreads");
    	for (int i = 0; i < 7; i++) {
    		if (mConnectedThreads.get(i) != null && mAcceptThreads.get(i) != null) {
    			if (D) Log.d(TAG, "iAT connected, so killing accept thread " + Integer.toString(i));
    			mAcceptThreads.get(i).cancel();
    			mAcceptThreads.set(i,null);
    		}
    		else if (mAcceptThreads.get(i) == null) {
    			if (D) Log.d(TAG, "iAT not connected, new accept thread " + Integer.toString(i));
    			mAcceptThreads.set(i, new AcceptThread(i));
    			mAcceptThreads.get(i).start();
    		}
    	}
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
    	if (D) Log.d(TAG, "BTMS STOP");
    	for (int i = 0; i < 7; i++) {
    		if (mConnectedThreads.get(i) != null) {
    			mConnectedThreads.get(i).cancel();
    			mConnectedThreads.set(i, null);
    		}
    		mDeviceAddresses.set(i, null);
    	}
        BTMState.setConnectionState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
    	if (D) Log.d(TAG, "BTMS WRITE");
    	// When writing, try to write out to all connected threads 
    	for (int i = 0; i < 7; i++) {
    		if (mConnectedThreads.get(i) != null) {
    			if (D) Log.d(TAG, "BTMS WRITE TO " + Integer.toString(i));
	    		try {
	                // Create temporary object
	                ConnectedThread r;
	                // Synchronize a copy of the ConnectedThread
	                synchronized (this) {
	                    if (BTMState.getConnectionState() != STATE_CONNECTED) return;
	                    r = mConnectedThreads.get(i);
	                }
	                // Perform the write unsynchronized
	                r.write(out);
	    		} catch (Exception e) {    			
	    		}
    		}
    	}
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
    	BluetoothServerSocket serverSocket = null;
    	int channel = -1;
        
        public AcceptThread(int inChannel) {
        	if (D) Log.d(TAG, "BTMS new acceptThread");
        	channel = inChannel;
        }

        public void run() {
            if (D) Log.d(TAG, "BTMS mAcceptThread RUN" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;
            try {
        		if (mConnectedThreads.get(channel) == null) {
        			if (D) Log.d(TAG, "BTMS AcceptThread listening on " + Integer.toString(channel));
            		serverSocket = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, mUuids.get(channel));
                    socket = serverSocket.accept();
                    if (socket != null) {
                    	if (D) Log.d(TAG, "BTMS AcceptThread connected on " + Integer.toString(channel));
                    	String address = socket.getRemoteDevice().getAddress();
                    	String name = socket.getRemoteDevice().getName();
	                    //mSockets.add(socket);
	                    mDeviceAddresses.set(channel, (address + "-" + name));
	                    connected(socket, socket.getRemoteDevice(), channel);
                    }	                    
        		}
            } catch (IOException e) {
                Log.e(TAG, "accept() failed", e);
            }
            if (D) Log.i(TAG, "END mAcceptThread " + Integer.toString(channel));
        }

        public void cancel() {
            if (D) Log.d(TAG, "cancel " + this);
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }



    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
    	public final int channel;
    	public final BluetoothDevice device;
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket s, BluetoothDevice d, int c) {
            Log.d(TAG, "BTMS new ConnectedThread " + Integer.toString(c));
        	device = d;
        	channel = c;
            mmSocket = s;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            BTMState.setConnectionState(STATE_CONNECTED);
        }

        public void run() {
            Log.i(TAG, "BTMS ConnectedThread RUN " + Integer.toString(channel));
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    if (D) Log.d(TAG, "BTMS connected thread got bytes " + Integer.toString(channel));
                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(BTMesh.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    if (D) Log.d(TAG, "BTMS Connected Thread disconnected " + Integer.toString(channel));
                    mConnectedThreads.set(channel, null);
                    mDeviceAddresses.set(channel, null);
                    BTMState.updateConnected();
                    // if we had stopped acceptthread due to no free sockets, start it up again
                    initAcceptThreads();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
            	if (D) Log.d(TAG, "BTMS connectedthread writing " + Integer.toString(channel));
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                //mHandler.obtainMessage(BTMesh.MESSAGE_WRITE, -1, -1, buffer)
                //        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    /*private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        public final BluetoothDevice device;
        public final int channel;

        public ConnectThread(BluetoothDevice inDevice, UUID uuidToTry, int index) {
        	if (D) Log.d(TAG, "BTMS new connectthread on channel " + Integer.toString(index));
        	channel = index;
            device = inDevice;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(uuidToTry);        	
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BTMS ConnectThread RUN " + Integer.toString(channel));
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
                if (D) Log.d(TAG, "BTMS ConnectThread socket connected " + Integer.toString(channel));
            } catch (IOException e) {
            	if (D) Log.d(TAG, "BTMS ConnectThread exception during connect " + Integer.toString(channel));
            	//if (tempUuid.toString().contentEquals(mUuids.get(6).toString())) {
                //    connectionFailed();
            	//}
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                // Questionable if this is the smart thing to do
                //if (D) Log.d(TAG, "BTMS ConnectThread restarting service");
                //BTMeshService.this.start();
                if (D) Log.d(TAG, "BTMS ConnectThread done, returning");
                return;
            }
            if (D) Log.d(TAG, "BTMS ConnectThread finished with connect " + Integer.toString(channel));
            // Reset the ConnectThread because we're done
            synchronized (BTMeshService.this) {
            	mConnectThreads.set(channel, null);
            }

            // Start the connected thread
            if (D) Log.d(TAG, "BTMS ConnectThread calling connected " + Integer.toString(channel));
            connected(mmSocket, device, channel);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }*/

}