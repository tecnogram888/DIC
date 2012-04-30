package com.dic.BTMesh;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.lang.Math;

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
  public static final int STATE_LISTEN = 1;     // now listening for incoming connections
  public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
  public static final int STATE_CONNECTED = 3;  // now connected to a remote device
	
  private BTMeshService mService;
  private BluetoothAdapter mBluetoothAdapter = null;
  
  private int mConnectionState;

  
  public ArrayList<BTStateEdge> BTSEdges;
  
  
  public void newService(Handler mHandler){
	  if (D) Log.d(TAG, "Creating BTMService");
      mService = new BTMeshService(this, mHandler, this);
  }
  
  public void setup(){
      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      BTSEdges = new ArrayList<BTStateEdge>();
  }
  
  public String edgesToString(){
	  String s = "@EDGES";
	  for (int i = 0; i < BTSEdges.size(); i++) {
		  BTStateEdge e = BTSEdges.get(i);
		  s += "@start@addr1" + e.address1 + "@name1" + e.name1 + "@addr2" + e.address2 + "@name2" + e.name2 + "@end";
	  }
	  return s;
  }
  
  public void sendEdges(){
	  byte[] send = edgesToString().getBytes();
	  getService().write(send);
  }
  
  public void sendDisconnected(String addr){
	  byte[] send = ("@EDGES@DC@addr" + addr).getBytes();
	  getService().write(send);
  }
  
  public boolean existsEdge(String addr1, String addr2) {
	  for (int i = 0; i < BTSEdges.size(); i++) {
		  if ((BTSEdges.get(i).address1.equals(addr1) &&
				  BTSEdges.get(i).address2.equals(addr2)) || 
				  (BTSEdges.get(i).address1.equals(addr2) &&
						  BTSEdges.get(i).address2.equals(addr1)))			  
				  {
			  return true;
		  }
	  }
	  return false;
  }
  
  public void newEdge (String addr2, String name2) {
	  if (!existsEdge(mBluetoothAdapter.getAddress(), addr2)) {
		  BTSEdges.add(new BTStateEdge(mBluetoothAdapter.getAddress(), mBluetoothAdapter.getName(), addr2, name2));
	  }
	  updateConnected();
  }
  
  public void removeEdgesWith(String addr2) {
	  Iterator<BTStateEdge> itr = BTSEdges.iterator();
	  while (itr.hasNext()){
		  BTStateEdge e = itr.next();
		  if (e.address1.equals(addr2) || e.address2.equals(addr2)){
			  itr.remove();
		  }
	  }
	  updateConnected();
	  sendDisconnected(addr2);
  }
  
  public void addEdges(String in){
	  String e = in.substring(6);
	  if (e.startsWith("@DC@addr")) {
		  if (D) Log.d(TAG, "got a disconnect edge message");
		  removeEdgesWith(e.substring(8));
		  return;
	  }
	  int addr1Ind = e.indexOf("@addr1", 0);
	  int name1Ind = e.indexOf("@name1", 0);
	  int addr2Ind = e.indexOf("@addr2", 0);
	  int name2Ind = e.indexOf("@name2", 0);
	  int endInd = e.indexOf("@end", 0);
	  String addr1 = "";
	  String name1 = "";
	  String addr2 = "";
	  String name2 = "";
	  boolean passOnMessage = false;
	  while (addr1Ind != -1) {
		  addr1 = e.substring(addr1Ind + 6, name1Ind);
		  name1 = e.substring(name1Ind + 6, addr2Ind);
		  addr2 = e.substring(addr2Ind + 6, name2Ind);
		  name2 = e.substring(name2Ind + 6, endInd);
		  
		  if (!existsEdge(addr1, addr2)) {
			  passOnMessage = true;
			  BTSEdges.add(new BTStateEdge(addr1, name1, addr2, name2));
		  }
		  addr1Ind = e.indexOf("@addr1", addr1Ind+1);
		  name1Ind = e.indexOf("@name1", name1Ind+1);
		  addr2Ind = e.indexOf("@addr2", addr2Ind+1);
		  name2Ind = e.indexOf("@name2", name2Ind+1);
		  endInd = e.indexOf("@end", endInd+1);
	  }
	  if (passOnMessage){
		  sendEdges();
		  updateConnected();
	  }
  }
  
  public BTMeshService getService(){
	  return mService;
  }
  
  public BluetoothAdapter getBluetoothAdapter(){
	  return mBluetoothAdapter;
  }

  public int getNumLocalDevices(){
	return mService.numConnections();
  }
		
  public int getNumGlobalDevices(){
	HashSet<String> uniqueAddrs = new HashSet<String>();
	for (int i = 0; i < BTSEdges.size(); i++) {
		BTStateEdge e = BTSEdges.get(i);
		uniqueAddrs.add(e.address1);
		uniqueAddrs.add(e.address2);
	}
	return Math.max(0, uniqueAddrs.size() - 1);
  }
  
  public synchronized void updateConnected(){
	  setConnectionState(STATE_CONNECTED);
  }
  
  public synchronized int getConnectionState(){
	  return mConnectionState;
  }
  
  public synchronized void refreshConnectionState() {
	  setConnectionState(mConnectionState);
  }
  
  public synchronized void setConnectionState(int s){
	  if (D) Log.d(TAG, "setConnectionState to " + Integer.toString(s));
	  if (getConnectionState() == STATE_CONNECTED && s != STATE_CONNECTED) {
		  if (D) Log.d(TAG, "setConnectionState returning, already connected");
		  return;
	  }
	  mConnectionState = s;
  	  Intent i = new Intent();
  	  i.setAction("com.dic.BTMesh.updatestatus");
	  switch(mConnectionState){
	  case STATE_NONE:
		  i.putExtra("status", getString(R.string.title_not_connected));
		  break;
	  case STATE_CONNECTING:
		  i.putExtra("status", getString(R.string.title_connecting));
		  break;
	  case STATE_CONNECTED:
		  String fullStr = getString(R.string.title_connected) + ": "
				  	+ Integer.toString(getNumGlobalDevices()) + " ("
				  	+ Integer.toString(getNumLocalDevices()) + ")";
		  i.putExtra("status", fullStr);
          Intent j = new Intent();
      	  j.setAction("com.dic.BTMesh.updateCM");
      	  sendBroadcast(j);
      	  break;
	  case STATE_LISTEN:
		  i.putExtra("status", getString(R.string.title_searching));
		  break;
	  }
	  if (D) Log.d(TAG, "sending state broadcast " + Integer.toString(s));
  	  sendBroadcast(i);
  }
}