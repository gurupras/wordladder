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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import static com.android_app.MainActivity.TAG;

public class Dictionary extends HashMap<String, String> {
	public static final boolean ANDROID = true;
	private static final int MAX_WORD_LENGTH = 12;
	private static final int MIN_WORD_LENGTH = 3;
	private Context context;
	
	private Map<Integer, List<String>> wordLengthMap;
	private List<String> keys;
	
	public Dictionary() {
		this(100000);
	}
	
	public Dictionary(Context context) {
		this.context = context;
	}
	
	public Dictionary(int size) {
		super(size);
		keys			= new ArrayList<String>(size);
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
		File file = new File(path);
		if(file.exists())
			file.delete();
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(this);
			Log.d(TAG, "Serialized dictionary to :" + file.getAbsolutePath());
		} catch (Exception e) {
			Log.e(TAG, "Could not serialize dictionary");
			Log.e(TAG, e.getMessage());
		} finally {
			try {out.close();} catch(Exception e) {}
		}
		
	}
	
	public static Dictionary deserialize(Context context, String path) throws FileNotFoundException {
		Dictionary dictionary = null;
		
		ObjectInputStream in = null;
		try {
			AssetManager am = context.getAssets();
			in = new ObjectInputStream(am.open("dictionary.bin"));
			dictionary = (Dictionary) in.readObject();
		} catch(Exception e) {
			Log.e(TAG, "AssetManager could not find dictionary.bin..falling back to sdcard :" + e.getMessage());
			File file = new File(path);
			if(!file.exists())
				throw new FileNotFoundException("Dictionary file does not exist at :" + path);
			try {
				in = new ObjectInputStream(new FileInputStream(file));
			} catch (Exception e1) {
				Log.e(TAG, "Could not find dictionary.bin in sdcard..null dictionary :" + e1.getMessage());
			}
		} finally {
			try {in.close();} catch(Exception e) {}
		}
		return dictionary;
	}
}
