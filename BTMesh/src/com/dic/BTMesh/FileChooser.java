package com.dic.BTMesh;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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

		refreshView();
		ListView v = getListView();
		registerForContextMenu( v );
		BTMState = ((BTMeshState)getApplicationContext());
		BTMListener = new BTFileListener();
		if (!listenerRegistered) {
			registerReceiver(BTMListener, new IntentFilter("com.dic.BTMesh.filechooser"));
			listenerRegistered = true;
		}
		
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}

	// Refreshes the view
	private void refreshView(){
		currentDir = new File("/sdcard/");
		fill(currentDir);
		adapter.notifyDataSetChanged ();
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
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int position, long id) {
			if(D) Log.d(TAG, "ITem Clicked!");
			finish();
		}
	};


	AdapterContextMenuInfo info;
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		 info = (AdapterContextMenuInfo) item.getMenuInfo();
		//int menuItemIndex = item.getItemId();
		//item.
		//int n = info.position;
		//if(D) Log.d(TAG, String.format("Selected %d for item %s", n, "foo"));

		//Option o = adapter.getItem(menuItemIndex);
		//String[] menuItems = getResources().getStringArray(R.menu.context_menu);
		//String menuItemName = menuItems[menuItemIndex];

		//TextView text = (TextView)findViewById(R.id.footer);
		//text.setText(String.format("Selected %s for item %s", menuItemName, listItemName));

		//if(D) Log.d(TAG, String.format("Selected %s for item %s", o.getName(), "foo"));
		//return true;

/*
		if(item.getTitle()=="broadcast"){function1(item.getItemId());}  
		else if(item.getTitle()=="delete"){function2(item.getItemId());}  
		else {function3(item);}  
		return true;  
		*/
		Option o;
		switch (item.getItemId()) {
		case R.id.broadcast:
			//editNote(info.id);
			o = adapter.getItem((int) info.id);
			//we broadcast the file;
			broadcastFileAsByteArrayAndPrompt(readFileAsByteArray(o));
			return true;
		case R.id.delete:
			o = adapter.getItem((int) info.id);
			Toast.makeText(this, "Deleting " + o.getName() + "...", Toast.LENGTH_SHORT).show();
			File file = new File("/sdcard/"+o.getName());
			boolean deleted = file.delete();
			if (deleted) {
				Toast.makeText(this, o.getName() + " deleted", Toast.LENGTH_SHORT).show();
				refreshView();
			} else {
				Toast.makeText(this, "Error: Failed to delete " +o.getName(), Toast.LENGTH_SHORT).show();
			}
			return true;
		default:
			return super.onContextItemSelected(item);
		}
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

			//send info in readFileAsByteArray to Broadcast

			openContextMenu( v );
			//((AdapterView<ListAdapter>) v).setOnItemClickListener(mDeviceClickListener);
			//broadcastFileAsByteArrayAndPrompt(readFileAsByteArray(o));

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
		Toast.makeText(this, "File Clicked: "+o.getName(), Toast.LENGTH_LONG).show();
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
			checkAndWriteFile(o);
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

	/*
	 * API: Convert file to byte array
	 */

	public byte[] readFileAsByteArray(Option o) {
		String sp = "@FILE";
		//String sp = "BTMESHSUPERPOWERFULL";
		String master = sp+o.getName() + sp + o.getData() + sp + o.getPath();

		byte[] byteArray = master.getBytes();
		return byteArray;
	}

	/*
	 * API: Convert byte array to file and write it
	 */

	public void writeFileFromByteArray(byte[] byteArray) {
		String master = new String(byteArray);
		//String sp = "BTMESHSUPERPOWERFULL";
		checkFileFromString(master);
	}

	public void checkFileFromString(String master) {
		String sp = "@FILE";
		String[] tmp = master.split(sp);
		Option o = new Option(tmp[1],tmp[2],tmp[3]);
		checkAndWriteFile(o);
	}

	public void broadcastFileAsByteArrayAndPrompt(byte[] byteArray) {
		//Initialize a BTMesh connection and broadcast
		BTMeshState BTMState = ((BTMeshState)getApplicationContext());
		BTMState.getService().write(byteArray);
	}

	/*
	 * API
	 * 
	 * Check and Write file
	 * 
	 */

	Option tempO;
	File tmpf;
	HashSet<String> allFiles = new HashSet<String>();
	File f;
	String fileName;
	private void checkAndWriteFile(Option o){
		//SDcard is available
		tempO = o;
		if(D) Log.d(TAG, "WRITING THE FILE IN WRITE FILE FUNCTION");
		fileName = o.getName();
		f=new File("/sdcard/"+fileName);
		boolean passOnFile = false;
		if (!allFiles.contains(fileName)){
			passOnFile = true;		
		}
		allFiles.add(fileName);

		if (!f.exists()) 
		{
			if(D) Log.d(TAG, "FILE DOESNT EXIST GONNA WRITE FILE");
			if(D) Log.d(TAG, "FILE PATH AND NAME IS"+"/sdcard/"+fileName);
			//File does not exists
			//f.createNewFile();

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Do you want this " +fileName+" file?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					//ACTIVITY.this.finish();
					//makeFile(tempO);
					f=new File("/sdcard/"+fileName);
					try {
						f.createNewFile();
						refreshView();
						
					} catch (IOException e) {
						if(D) Log.d(TAG, "DIDNT WORK!! FILE PATH AND NAME IS"+"/sdcard/"+fileName);
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();

			if (passOnFile){
				broadcastFileAsByteArrayAndPrompt(readFileAsByteArray(o));
			}
		} else {
			if(D) Log.d(TAG, "FILE PATH AND NAME IS"+"/sdcard/"+fileName);
			if(D) Log.d(TAG, "FILE DOES EXIST NOT DOING ANYTHING");

		}
		System.out.println("\nFile is created...................................");
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
	private void makeFile(Option tempO){
		final String TESTSTRING = new String(tempO.getData()); 

		// ##### Write a file to the disk #####
		/* We have to use the openFileOutput()-method 
		 * the ActivityContext provides, to
		 * protect your file from others and 
		 * This is done for security-reasons. 
		 * We chose MODE_WORLD_READABLE, because
		 *  we have nothing to hide in our file */		
		FileOutputStream fOut;
		try {
			fOut = openFileOutput(tempO.getName(), MODE_WORLD_READABLE);

			OutputStreamWriter osw = new OutputStreamWriter(fOut);	

			// Write the string to the file
			osw.write(TESTSTRING);
			/* ensure that everything is 
			 * really written out and close */
			osw.flush();
			osw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Listener that waits for a sign to write the new file passed in
	protected class BTFileListener extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (D) Log.d(TAG, "receive " + intent.getAction());
			if (intent.getAction().equals("com.dic.BTMesh.filechooser")) {
				if(D) Log.d(TAG, "BTFile received file");
				String messages = intent.getStringExtra("messages");
				if(D) Log.d(TAG, messages);      	

				checkFileFromString(messages);
				//addMessagesToConvo(messages);
				// Do something
				refreshView();
			}
		}
	}


}

