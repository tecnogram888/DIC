package com.dic.BTMesh;

import java.util.ArrayList;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class BTMeshState extends Application {
  private static final String TAG = "BTMeshState";
  private static final boolean D = true;
  // Constants that indicate the current connection state
  public static final int STATE_NONE = 0;       // we're doing nothing
  public static final int STATE_BROADCASTING = 1;     // now listening for incoming connections
  public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
  public static final int STATE_CONNECTED = 3;  // now connected to a remote device
  public static final int STATE_SEARCHING = 4;
	
  private BTMeshService mService;
  private BluetoothAdapter mBluetoothAdapter = null;
  
  private int mConnectionState;
  
  
  public void newService(Handler mHandler){
	  if (D) Log.d(TAG, "Creating BTMService");
      mService = new BTMeshService(this, mHandler, this);
  }
  
  public void newAdapter(){
      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  }
  
  public BTMeshService getService(){
	  return mService;
  }
  
  public ArrayList<String> getDeviceNames(){
	  ArrayList<String> retAL = new ArrayList<String>();
	  for (int i = 0; i < mService.mConnectedThreads.size(); i++) {
		  retAL.add(mService.mConnectedThreads.get(i).deviceName);
	  }
	  return retAL;
  }
  
  public BluetoothAdapter getBluetoothAdapter(){
	  return mBluetoothAdapter;
  }
  
  public synchronized int getConnectionState(){
	  return mConnectionState;
  }
  
  public synchronized void setConnectionState(int s){
	  if (D) Log.d(TAG, "setConnectionState to " + Integer.toString(s));
	  mConnectionState = s;
  	  Intent i = new Intent();
  	  i.setAction("com.dic.BTMesh.updatestatus");
	  switch(mConnectionState){
	  case STATE_NONE:
		  i.putExtra("status", getString(R.string.title_not_connected));
		  break;
	  case STATE_BROADCASTING:
		  i.putExtra("status", getString(R.string.title_broadcasting));
		  break;
	  case STATE_CONNECTING:
		  i.putExtra("status", getString(R.string.title_connecting));
		  break;
	  case STATE_CONNECTED:
		  i.putExtra("status", getString(R.string.title_connected_to));
		  break;
	  case STATE_SEARCHING:
		  i.putExtra("status", getString(R.string.title_searching));
		  break;
	  }
	  if (D) Log.d(TAG, "sending state broadcast " + Integer.toString(s));
  	  sendBroadcast(i);
  }
}