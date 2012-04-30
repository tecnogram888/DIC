/*
THIS APPROACH STORES NODES, EDGES MAY BE BETTER

package com.dic.BTMesh;

import java.util.ArrayList;

import android.util.Log;

public class BTStateNode {
	private static final String TAG = "BTStateNode";
	private static final boolean D = false;
	public BTStateNode(String address, String name) {
		myName = name;
		myAddress = address;
		others = new ArrayList<BTStateNode>();
		for (int i = 0; i < 7; i++) {
			others.add(null);
		}
	}
	public String getName(){
		return myName;
	}
	public String getAddress(){
		return myAddress;
	}	
	
	public void newNode(int i, String address, String name) {
		BTStateNode n = new BTStateNode(address, name);
		others.set(i, n);
	}
	
	public void deleteNode(int i) {
		others.set(i, null);
	}
	
	public BTStateNode getNode(int i) {
		return others.get(i);
	}
	
	public String getFullName(int i) {
		return others.get(i).getAddress() + ":\t" + others.get(i).getName();
	}
	
	public String stringify(){
		String s = "@STATE@0A" + myAddress + "@0N" + myName;
		for (int i = 0; i < 7; i++) {
			BTStateNode n = others.get(i);
			if (n == null) {
				s += "@" + Integer.toString(i) + "null";
			}
			else {
				s += "@" + Integer.toString(i) + "A" + n.getAddress();
				s += "@" + Integer.toString(i) + "N" + n.getName();
				s += n.stringify();
				s += "@" + Integer.toString(i) + "END" + n.getAddress();
			}
		}
		s += "@0END" + myAddress;
		return s;
	}
	
	
	
	public void mergeNodes(String s) {
		if (D) Log.d(TAG, "BTSN merging nodes");
		//filter out the @STATE
		s = s.substring(6);
		int address
	}
	
	public int totalNodes() {
		ArrayList<String> uniqueAddrs = new ArrayList<String>();
		ArrayList<BTStateNode> nodesToCheck = new ArrayList<BTStateNode>();
		for (int i = 0; i < 7; i++) {
			if (others.get(i) != null) {
				uniqueAddrs.add(others.get(i).getAddress());
				nodesToCheck.add(others.get(i));
			}
		}
		while (nodesToCheck.size() > 0) {
			BTStateNode n = nodesToCheck.get(0);
			if (uniqueAddrs.contains(n.getAddress())) {
				nodesToCheck.remove(0);
			}
			else {
				uniqueAddrs.add(n.getAddress());
				for (int i = 0; i < 7; i++) {
					BTStateNode n2 = n.others.get(i);
					if (n2 != null && !uniqueAddrs.contains(n2.getAddress())) {
						uniqueAddrs.add(n2.getAddress());
						nodesToCheck.add(n2);
					}
				}
			}
		}
		return uniqueAddrs.size();
	}
	private ArrayList<BTStateNode> others;
	private String myName;
	private String myAddress;
}*/