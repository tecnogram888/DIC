package com.dic.BTMesh;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.os.Handler;

class BTMeshState extends Application {
	
  private BTMeshService mService;
  private BluetoothAdapter mBluetoothAdapter = null;
  
  private int mConnectionState;
  
  public void newService(Handler mHandler){
      mService = new BTMeshService(this, mHandler, this);
  }
  
  public void newAdapter(){
      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  }
  
  public BTMeshService getService(){
	  return mService;
  }
  
  public BluetoothAdapter getBluetoothAdapter(){
	  return mBluetoothAdapter;
  }
  
  public synchronized int getConnectionState(){
	  return mConnectionState;
  }
  
  public synchronized void setConnectionState(int i){
	  mConnectionState = i;
  }
}