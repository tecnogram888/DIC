package com.file.attach;

import java.io.File;
import java.io.FileInputStream;
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

public class FileChooser extends ListActivity {

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
		File[]dirs = f.listFiles();
		this.setTitle("Current Dir: "+f.getName());
		List<Option>dir = new ArrayList<Option>();
		List<Option>fls = new ArrayList<Option>();
		try{
			for(File ff: dirs)
			{
				if(ff.isDirectory())
					dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
				else
				{
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
		adapter = new FileArrayAdapter(FileChooser.this,R.layout.file_view,dir);
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
		}
	}

	/*
	 * Got the file, let's do something with it
	 * 
	 */
	private void onFileClick(Option o)
	{
		formStream(o);
		Toast.makeText(this, "File Clicked: "+o.getName(), Toast.LENGTH_SHORT).show();
	}


	private void formStream(Option o) {
		try {
			// read this file into InputStream
			InputStream inputStream = new FileInputStream(o.getPath());

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
}

