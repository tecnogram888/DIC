package com.dic.BTMesh;

public class BTMessage {
	public BTMessage(String author, String timestamp, String text) {
		myAuthor = author;
		myTimestamp = timestamp;
		myText = text;
	}
	public String getAuthor(){
		return myAuthor;
	}
	public String getTimestamp(){
		return myTimestamp;
	}
	public String getText(){
		return myText;
	}
	
	private String myAuthor;
	private String myTimestamp;
	private String myText;
}