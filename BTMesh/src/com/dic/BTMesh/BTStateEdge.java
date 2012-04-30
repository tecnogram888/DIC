package com.dic.BTMesh;

import android.util.Log;

public class BTStateEdge {
	private static final String TAG = "BTStateNode";
	private static final boolean D = false;
	public String name1;
	public String address1;
	public String name2;
	public String address2;
	
	public BTStateEdge(String a1, String n1, String a2, String n2) {
		name1 = n1;
		address1 = a1;
		name2 = n2;
		address2 = a2;
	}
}