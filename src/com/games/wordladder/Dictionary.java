package com.games.wordladder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Dictionary extends HashMap<String, String> {
	private static final int MAX_WORD_LENGTH = 12;
	private static final int MIN_WORD_LENGTH = 4;
	
	private Map<Integer, Collection<String>> wordLengthMap	= null;
	private Random random;
	private List<String> keys;
 
	
	public Dictionary() {
		this(100000);
	}
	
	public Dictionary(int size) {
		super(size);
		random			= new Random();
		keys			= new ArrayList<String>(size);
		wordLengthMap	= new HashMap<Integer, Collection<String>>();
	}
	
	public String generateWord() {
		return null;
	}
	
	@Override
	public String put(String key, String value) {
		if(MAX_WORD_LENGTH >= key.length() || key.length() < MIN_WORD_LENGTH)
			return null;
		if(!wordLengthMap.containsKey(key.length()))
			wordLengthMap.put(key.length(), new HashSet<String>());
		wordLengthMap.get(key.length()).add(key);
		return super.put(key, value);
	}
}
