package com.games.wordladder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Dictionary extends HashMap<String, String> {
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
		if(!wordLengthMap.containsKey(key.length()))
			wordLengthMap.put(key.length(), new HashSet<String>());
		wordLengthMap.get(key.length()).add(key);
		return super.put(key, value);
	}
}
