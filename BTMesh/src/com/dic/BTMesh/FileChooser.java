package com.dic.BTMesh;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dic.BTMesh.BTChat.BTChatListener;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;



public class FileChooser extends ListActivity {
	private static final String TAG = "BTMeshFileChooser";
	private static final boolean D = true;
	
	private BTFileListener BTMListener;
    private BTMeshState BTMState;
    private boolean listenerRegistered = false;
    
	private File currentDir;
	private FileArrayAdapter adapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentDir = new File("/sdcard/");
		fill(currentDir);
		BTMState = ((BTMeshState)getApplicationContext());
		BTMListener = new BTFileListener();
        if (!listenerRegistered) {
            registerReceiver(BTMListener, new IntentFilter("com.dic.BTMesh.filechooser"));
            listenerRegistered = true;
        }
		
	}
	private void fill(File f)
	{
		// This provides a list of files
		File[] dirs = f.listFiles();
		// this starts with SD card
		this.setTitle("Current Dir: "+f.getName());

		List<Option> dir = new ArrayList<Option>(); //directories
		List<Option> fls = new ArrayList<Option>(); //files
		try{
			for(File ff: dirs)
			{
				if(ff.isDirectory())
					dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
				else
				{
					// push all available files into the ff directory
					fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
				}
			}
		}catch(Exception e)
		{

		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		if(!f.getName().equalsIgnoreCase("sdcard"))
			dir.add(0,new Option("..","Parent Directory",f.getParent()));

		adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view, dir);
		this.setListAdapter(adapter);
	}
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Option o = adapter.getItem(position);

		//if folder we go to the folder
		if(o.getData().equalsIgnoreCase("folder")||o.getData().equalsIgnoreCase("parent directory")){
			currentDir = new File(o.getPath());
			fill(currentDir);
		}
		else
		{
			//onFileClick(o);
			// this list all the files in a string
			//onFileClickList(o, currentDir);
			//this prompts it as a byte array;
			writeFileAsByteArrayPrompt(readFileAsByteArray(o));
			
		}
	}

	/*
	 * Got the file, let's do something with it
	 * 
	 */
	private void onFileClick(Option o)
	{
		fileToStream(o);
		// right now only tell you the file
		Toast.makeText(this, "File Clicked: "+o.getName(), Toast.LENGTH_SHORT).show();
	}

	/*
	 * Got the file, let's display it in a string for parsing
	 */
	public void onFileClickList(Option o, File f)
	{
		fileToStream(o);

		File[] dirs = f.listFiles();
		// this starts with SD card
		this.setTitle("Current Dir: "+f.getName());

		List<Option> dir = new ArrayList<Option>(); //directories
		List<Option> fls = new ArrayList<Option>(); //files
		try{
			for(File ff: dirs)
			{
				if(ff.isDirectory())
					dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
				else
				{
					// push all available files into the ff directory
					fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
				}
			}
		}catch(Exception e)
		{

		}

		// right now only tell you the file
		String s = "Found Folders: ";
		for (File fff:dirs){
			s +=" " + fff.getName();
		}

		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}


	private void fileToStream(Option o) {
		try {
			// read this file into InputStream
			InputStream inputStream = new FileInputStream(o.getPath());
			writeFile(o);
			// write the inputStream to a FileOutputStream
			//OutputStream out = new FileOutputStream(new File("foodfood1"));

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				//out.write(bytes, 0, read);
			}

			inputStream.close();
			//out.flush();
			//out.close();

			System.out.println("New file created!");
		} catch (IOException e) {
			Toast.makeText(this, "Something went wrong with streams: "+o.getName(), Toast.LENGTH_SHORT).show();
			System.out.println(e.getMessage());
		}
	}

	public BufferedOutputStream readFileAsStream(Option o) {
		try {
			// read this file into InputStream
			FileOutputStream outputStream = new FileOutputStream(o.getPath());
			System.out.println("File read");

			return new BufferedOutputStream(outputStream);

		} catch (IOException e) {
			Toast.makeText(this, "Something went wrong with streams: "+o.getName(), Toast.LENGTH_SHORT).show();
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	public Option readFile(Option o) {
		return o;
	}
	
	/*
	 * API
	 * 
	 * Convert file to byte array
	 * 
	 */
	
	public byte[] readFileAsByteArray(Option o) {
		String sp = "@FILE";
		//String sp = "BTMESHSUPERPOWERFULL";
		String master = sp+o.getName() + sp + o.getData() + sp + o.getPath();
		
        byte[] byteArray = master.getBytes();
        
        //Initialize a BTMesh connection and broadcast
        BTMeshState BTMState = ((BTMeshState)getApplicationContext());
        BTMState.getService().write(byteArray);
        
		return byteArray;
	}
	
	/*
	 * API
	 * 
	 * Convert byte array to file and write it
	 * 
	 */
	
	public void writeFileAsByteArray(byte[] byteArray) {
		
		String master = new String(byteArray);
		//String sp = "BTMESHSUPERPOWERFULL";
		String sp = "@FILE";
		String[] tmp = master.split(sp);
		
		Option o = new Option(tmp[0],tmp[1],tmp[2]);
		writeFile(o);
			
	}
	
	public void writeFileAsByteArrayPrompt(byte[] byteArray) {
		
		String master = byteArray.toString();
		String sp = "BTMESHSUPERPOWERFULL";
		
        master = new String(byteArray);
        
		Toast.makeText(this, master, Toast.LENGTH_SHORT).show();
		
		//String[] tmp = master.split(sp);
		
		//Option o = new Option(tmp[0],tmp[1],tmp[2]);
		//writeFile(o);
			
	}

	/*
	 * API
	 * 
	 * Write file
	 * 
	 */
	private void writeFile(Option o){
		try {
			//SDcard is available
			
			String fileName = o.getName();
			File f=new File("/sdcard/"+fileName);
			if (!f.exists()) 
			{
				//File does not exists
				f.createNewFile();
			}
			/*
			//take your inputstream and write it to your file
			OutputStream out;

			out = new FileOutputStream(f);

			byte buf[]=new byte[1024];
			int len;
			while((len=inputStream.read(buf))>0)
				out.write(buf,0,len);
			out.close();*/
			System.out.println("\nFile is created...................................");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/*
	 * API
	 * 
	 * Get List of Files in Array of Options
	 * 
	 */
	public List<Option> getListOfFileNames(){
		currentDir = new File("/sdcard/");

		File[] dirs = currentDir.listFiles();

		List<Option> dir = new ArrayList<Option>(); //directories
		List<Option> fls = new ArrayList<Option>(); //files
		try{
			for(File ff: dirs)
			{
				if(ff.isDirectory())
					dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
				else
				{
					// push all available files into the ff directory
					fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
				}
			}
			return dir;
		}catch(Exception e)
		{
			return null;
		}
	}

	/*
	 * API
	 * 
	 * Get List of Files in Array of String
	 * 
	 */
	public List<String> getListOfFilesAsString(){
		currentDir = new File("/sdcard/");

		File[] dirs = currentDir.listFiles();

		List<String> dir = new ArrayList<String>(); //directories
		List<String> fls = new ArrayList<String>(); //files
		try{
			for(File ff: dirs)
			{
				if(ff.isDirectory())
					dir.add(ff.getName());
				else
				{
					// push all available files into the ff directory
					fls.add(ff.getName());
				}
			}
			return dir;
		}catch(Exception e)
		{
			return null;
		}
	}
	
    protected class BTFileListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (D) Log.d(TAG, "receive " + intent.getAction());
            if (intent.getAction().equals("com.dic.BTMesh.filechooser")) {
                if(D) Log.d(TAG, "BTFile received file");
            	String messages = intent.getStringExtra("messages");
            	//addMessagesToConvo(messages);
                // Do something
            }
        }
    }


}

