package com.games.wordladder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

public class Dictionary extends HashMap<String, String> {
	private static final int MAX_WORD_LENGTH = 12;
	private static final int MIN_WORD_LENGTH = 4;
	
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
}
