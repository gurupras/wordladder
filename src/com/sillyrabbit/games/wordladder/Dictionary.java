package com.sillyrabbit.games.wordladder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import com.sillyrabbit.games.helper.TimeKeeper;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import static com.sillyrabbit.games.MainActivity.TAG;

public class Dictionary extends HashSet<String> {
	private static final long serialVersionUID = -3413172350787206582L;
	
	public static final boolean ANDROID = true;
	private static final int MAX_WORD_LENGTH = 12;
	private static final int MIN_WORD_LENGTH = 3;
	
	private HashMap<Integer, List<String>> wordLengthMap;
	
	public Dictionary() {
		this(100000);
	}
	
	public Dictionary(int size) {
		super(size);
		wordLengthMap	= new HashMap<Integer, List<String>>(MAX_WORD_LENGTH);
	}
	
	public String generateWord(int maxWordLength) {
//		XXX:We assume that there is bound to be a word of every length from MIN_WORD_LENGTH to MAX_WORD_LENGTH
		int wordLength = -1;
		int mapSize = -1;
		while(mapSize <= 0) {
			wordLength			= MIN_WORD_LENGTH + (int)(Math.random() *(maxWordLength - MIN_WORD_LENGTH + 1));
			List<String> list	= wordLengthMap.get(wordLength);
			if(list != null)
				mapSize				= list.size();
		}
		
		int randomIndex = 0 + (int)(Math.random() * mapSize);
		
		return wordLengthMap.get(wordLength).get(randomIndex);
	}
	
	public boolean containsKey(String key) {
		return super.contains(key);
	}
	
	public boolean put(String key) {
		if(key.length() > MAX_WORD_LENGTH || key.length() < MIN_WORD_LENGTH)
			return false;
		
		if(wordLengthMap.get(key.length()) == null)
			wordLengthMap.put(key.length(), new ArrayList<String>(5000));
		wordLengthMap.get(key.length()).add(key);

		return super.add(key);
	}
	
	public void serialize(String path) {
		ObjectOutputStream out = null;
		try {
			File dictFile = new File(path + "/dictionary.bin");
			out = new ObjectOutputStream(new FileOutputStream(dictFile));
			out.writeObject(this);
			Log.d(TAG, "Serialized dictionary to :" + dictFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Dictionary deserialize(Context context, String path) throws FileNotFoundException {
		Dictionary returnDictionary = null; 
		ObjectInputStream in = null;
		try {
			try {
				AssetManager am = context.getAssets();
				in = new ObjectInputStream(am.open("dictionary.bin"));
				Log.v(TAG, "AssetManager has dictionary");
			} catch(Exception e) {
				Log.e(TAG, "AssetManager could not find dictionary.bin :" + e.getMessage() + "\nFalling back to supplied path :" + path);
				File file = new File(path + "/dictionary.bin");
				Log.v(TAG, "SD card has dictionary");
				try {
					in = new ObjectInputStream(new FileInputStream(file));
				} catch (Exception e1) {
					Log.e(TAG, "Could not find dictionary.bin in supplied path :" + e1.getMessage());
				}
			}
			Log.d(TAG, "Managed to obtain input stream for dictionary. .");
			Dictionary d = (Dictionary) in.readObject();
			if(d.size() > 0) {
				returnDictionary = d;
			}
		} catch(Exception e) {
			Log.e(TAG, "Exception while parsing dictionary from stream :" + e.getMessage());
		} finally {
			try { in.close(); } catch(Exception e) {}
		}
		
		if(returnDictionary == null || returnDictionary.size() == 0)
			Log.w(TAG, "Failed to deserialize dictionary");
		if(returnDictionary.size() > 0)
			Log.d(TAG, "Successfully deserialized dictionary");
		return returnDictionary;
	}
}
