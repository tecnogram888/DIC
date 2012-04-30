package com.dic.BTMesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class BTDrawGraph extends View {
    Paint paint = new Paint();
    Paint textPaint = new Paint();
    ArrayList<BTStateEdge> edges;
    ArrayList<String> nodes;
    HashMap<String,String> addrNameMap;
    HashMap<String,Integer> addrXMap;
    HashMap<String,Integer> addrYMap;
    int areaWidth;
    int areaHeight;
    int centerX;
    int centerY;
    int radiusX;
    int radiusY;
    double radianStep;
    
    public BTDrawGraph(Context context, ArrayList<BTStateEdge> e) {
        super(context);
        edges = e;
        paint.setColor(Color.WHITE);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(15);
        textPaint.setTextAlign(Paint.Align.CENTER);
        nodes = new ArrayList<String>();
        addrNameMap = new HashMap<String,String>();
        addrXMap = new HashMap<String,Integer>();
        addrYMap = new HashMap<String,Integer>();
    }
    
    public void setDimensions() {
        areaWidth = getWidth();
        areaHeight = getHeight();
        centerX = areaWidth/2;
        centerY = areaHeight/2;
        radiusX = (int) ((float)areaWidth * 0.3);
        radiusY = (int) ((float)areaHeight * 0.3);
    }
    public void getNodes() {
    	// first get all the nodes
    	for (int i = 0; i < edges.size(); i++) {
    		String addr1 = edges.get(i).address1;
    		String name1 = edges.get(i).name1;
    		String addr2 = edges.get(i).address2;
    		String name2 = edges.get(i).name2;
    		if (!nodes.contains(addr1)) {
    			nodes.add(addr1);
    			nodes.add(name1);
    		}
    		if (!nodes.contains(addr2)) {
    			nodes.add(addr2);
    			nodes.add(name2);
    		}
    	}
    }
    
    public void fillNodeMap() {
    	// nodes has 2 nodes per node, one for addr and one for name
    	radianStep = (4.0 * Math.PI)/ nodes.size();
    	double radians = 0.0;
    	for (int i = 0; i < nodes.size(); i+=2) {
    		String currAddr = nodes.get(i);
    		String currName = nodes.get(i+1);
    		if (!addrNameMap.containsKey(currAddr)) {
    			addrNameMap.put(currAddr, currName);
    			addrXMap.put(currAddr, (int)(centerX + (radiusX * Math.cos(radians))));
    			addrYMap.put(currAddr, (int)(centerY + (radiusY * Math.sin(radians))));
    		}
    		radians += radianStep;
    	}
    }
    @Override
    public void onDraw(Canvas canvas) {
    	setDimensions();
        getNodes();
        fillNodeMap();
        for (Entry<String, String> entry : addrNameMap.entrySet()) {
        	String addr = entry.getKey();
        	String name = entry.getValue();
        	int xVal = addrXMap.get(addr);
        	int yVal = addrYMap.get(addr);
        	canvas.drawText(name, xVal, yVal, textPaint);
        }
        for (int i = 0; i < edges.size(); i++) {
        	String addr1 = edges.get(i).address1;
        	String addr2 = edges.get(i).address2;
            int x1 = addrXMap.get(addr1);
            int y1 = addrYMap.get(addr1);
            int x2 = addrXMap.get(addr2);
            int y2 = addrYMap.get(addr2);
            canvas.drawLine(x1, y1, x2, y2, paint);
        }
    }

}