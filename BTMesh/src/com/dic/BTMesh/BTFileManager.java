package com.dic.BTMesh;

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

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class BTFileManager extends ListActivity {

	private File currentDir;
	private FileArrayAdapter adapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentDir = new File("/sdcard/");
		fill(currentDir);
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

		adapter = new FileArrayAdapter(BTFileManager.this, R.layout.file_view, dir);
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
			onFileClick(o);
			onFileClickList(o, currentDir);
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
			writeFile(inputStream, "foood.txt");
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

	private Option fileToOption(Option o) {
		return o;
	}

	/*
	 * API
	 * 
	 * Write file
	 * 
	 */
	private void writeFile(InputStream inputStream, String fileName){
		try {
			//SDcard is available
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
	public List<Option> getListOfFiles(){
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






}

