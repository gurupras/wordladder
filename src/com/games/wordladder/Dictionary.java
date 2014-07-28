package com.games.wordladder;

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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import test.TimeKeeper;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import static com.android_app.MainActivity.TAG;

public class Dictionary extends HashMap<String, String> {
	private static final long serialVersionUID = 8294139649593205421L;
	
	public static final boolean ANDROID = true;
	private static final int MAX_WORD_LENGTH = 12;
	private static final int MIN_WORD_LENGTH = 3;
	
	private Map<Integer, List<String>> wordLengthMap;
	private HashSet<String> keys;
	
	public Dictionary() {
		this(100000);
	}
	
	public Dictionary(int size) {
		super(size);
		keys			= new HashSet<String>(size);
		wordLengthMap	= new HashMap<Integer, List<String>>();
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
		
		int randomIndex = 0 + (int)(Math.random() *mapSize);
		
		return wordLengthMap.get(wordLength).get(randomIndex);
	}
	
	@Override
	public String get(Object key) {
		if(super.containsKey(key))
			return super.get(key);
		if(keys.contains(key))
			return "";
		return null;
	}
	
	@Override
	public boolean containsKey(Object key) {
		if(super.containsKey(key))
			return true;
		if(keys.contains(key))
			return true;
		return false;
	}
	
	@Override
	public String put(String key, String value) {
		if(key.length() > MAX_WORD_LENGTH || key.length() < MIN_WORD_LENGTH)
			return null;
		keys.add(key);
		if(!wordLengthMap.containsKey(key.length()))
			wordLengthMap.put(key.length(), new Vector<String>());
		wordLengthMap.get(key.length()).add(key);
		return super.put(key, value);
	}
	
	public void serialize(String path) {
		final File file = new File(path);
		ObjectOutputStream out = null;
		try {

			File keysFile = new File(file.getAbsolutePath() + "/dict_keys.bin");
			out = new ObjectOutputStream(new FileOutputStream(keysFile));
			out.writeObject(keys);
			out.writeObject(wordLengthMap);
			Log.d(TAG, "Serialized keys to :" + keysFile.getAbsolutePath());
		} catch (Exception e) {
			Log.e(TAG, "Could not serialize dictionary");
			Log.e(TAG, e.getMessage());
		} finally {
			try {out.close();} catch(Exception e) {}
		}
		
		out = null;
		try {
			File dictFile = new File(DictionaryParser.dictPath + "/dictionary.bin");
			out = new ObjectOutputStream(new FileOutputStream(dictFile));
			out.writeObject(this);
			Log.d(TAG, "Serialized dictionary to :" + dictFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean deserialize(Context context, String path, final Dictionary returnDictionary) throws FileNotFoundException {
		ObjectInputStream in = null;
		try {
			AssetManager am = context.getAssets();
			in = new ObjectInputStream(am.open("dict_keys.bin"));
			returnDictionary.keys = (HashSet<String>) in.readObject();
			returnDictionary.wordLengthMap = (HashMap<Integer, List<String>>) in.readObject();
		} catch(Exception e) {
			Log.e(TAG, "AssetManager could not find dict_keys.bin..falling back to sdcard :" + e.getMessage());
			File file = new File(DictionaryParser.dictPath + "/dict_keys.bin");
			try {
				in = new ObjectInputStream(new FileInputStream(file));
			} catch (Exception e1) {
				Log.e(TAG, "Could not find dictionary.bin in sdcard..null dictionary :" + e1.getMessage());
			}
		} finally {
			try {in.close();} catch(Exception e) {}
		}
		Thread deserializeThread = new Thread(new DeserializeDictionaryRunnable(context, returnDictionary));
		deserializeThread.start();
		if(returnDictionary.keys.size() > 0)
			return true;
		else {
			try {
				deserializeThread.join();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(returnDictionary.size() > 0)
			return true;
		else
			return false;
	}
	
	private static class DeserializeDictionaryRunnable implements Runnable {
		protected Dictionary dictionary;
		private Context context;
		
		public DeserializeDictionaryRunnable(Context context, Dictionary dictionary) {
			this.context = context;
			this.dictionary = dictionary;
		}
		
		public void run() {
			ObjectInputStream in = null;
			TimeKeeper tk = new TimeKeeper();
			tk.start();
			try {
				AssetManager am = context.getAssets();
				in = new ObjectInputStream(am.open("dictionary.bin"));
				dictionary = (Dictionary) in.readObject();
			} catch(Exception e) {
				Log.e(TAG, "AssetManager could not find dictionary.bin..falling back to sdcard :" + e.getMessage());
				File file = new File(DictionaryParser.dictPath + "/dictionary.bin");
				if(!file.exists())
						Log.e(TAG, "Dictionary file does not exist at :" + file.getAbsolutePath());
				try {
					in = new ObjectInputStream(new FileInputStream(file));
				} catch (Exception e1) {
					Log.e(TAG, "Could not find dictionary.bin in sdcard..null dictionary :" + e1.getMessage());
				}
			} finally {
				try {in.close();} catch(Exception e) {}
			}
			tk.stop();
			if(dictionary.size() > 0)
				Log.d(TAG, "Loaded full dictionary successfully : " + tk.toString());
			else
				Log.d(TAG, "Failed to load full dictionary" + tk.toString());
		}
	}
}
