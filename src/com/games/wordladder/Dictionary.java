package com.games.wordladder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

public class Dictionary extends HashMap<String, String> {
	private static final int MAX_WORD_LENGTH = 12;
	private static final int MIN_WORD_LENGTH = 4;
	
	private Map<Integer, List<String>> wordLengthMap;
	private Random random;
	private List<String> keys;
	
	public Dictionary() {
		this(100000);
	}
	
	public Dictionary(int size) {
		super(size);
		random			= new Random();
		keys			= new ArrayList<String>(size);
		wordLengthMap	= new HashMap<Integer, List<String>>();
	}
	
	public String generateWord() {
//		XXX:We assume that there is bound to be a word of every length from MIN_WORD_LENGTH to MAX_WORD_LENGTH
		int wordLength = MIN_WORD_LENGTH + random.nextInt(MAX_WORD_LENGTH - MIN_WORD_LENGTH + 1);
		int mapSize = wordLengthMap.get(wordLength).size();
		int randomIndex = random.nextInt(mapSize);
		
		return wordLengthMap.get(wordLength).get(randomIndex);
	}
	
	@Override
	public String put(String key, String value) {
		if(MAX_WORD_LENGTH > key.length() || key.length() < MIN_WORD_LENGTH)
			return null;
		keys.add(key);
		if(!wordLengthMap.containsKey(key.length()))
			wordLengthMap.put(key.length(), new Vector<String>());
		wordLengthMap.get(key.length()).add(key);
		return super.put(key, value);
	}
}
