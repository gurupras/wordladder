package com.games.wordladder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

public class Dictionary extends HashMap<String, String> {
	private static final long serialVersionUID = -6336547234958289248L;
	
	public static final int MAX_WORD_LENGTH = 12;
	public static final int MIN_WORD_LENGTH = 3;
	
	private Map<Integer, List<String>> wordLengthMap;
	private List<String> keys;
	
	public Dictionary() {
		this(100000);
	}
	
	public Dictionary(int size) {
		super(size);
		keys			= new ArrayList<String>(size);
		wordLengthMap	= new HashMap<Integer, List<String>>();
	}
	
	public String generateWord(int maxWordLength) {
//		XXX:We assume that there is bound to be a word of every length from MIN_WORD_LENGTH to MAX_WORD_LENGTH
		Random random = new Random(System.nanoTime());
		int wordLength = -1;
		int mapSize = -1;
		while(mapSize <= 0) {
			wordLength			= MIN_WORD_LENGTH + (int)(random.nextDouble() *(maxWordLength - MIN_WORD_LENGTH + 1));
			List<String> list	= wordLengthMap.get(wordLength);
			if(list != null)
				mapSize				= list.size();
		}
		
		int randomIndex = 0 + (int)(random.nextDouble() * mapSize);
		
		return wordLengthMap.get(wordLength).get(randomIndex);
	}
	
	public Map<Integer, List<String>> getWordLengthMap() {
		return wordLengthMap;
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {out.close();} catch(Exception e) {}
		}
	}
	
	public static Dictionary deserialize(String path) throws FileNotFoundException {
		Dictionary dictionary = null;
		File file = new File(path);
		if(!file.exists())
			throw new FileNotFoundException("Dictionary file does not exist at :" + path);
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(file));
			dictionary = (Dictionary) in.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {in.close();} catch(Exception e) {}
		}
		return dictionary;
	}
}
