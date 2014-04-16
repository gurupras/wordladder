package com.games.wordladder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

public class WordLadder {
	private Difficulty difficulty;
	private String origin;
	private String destination;
	private Dictionary dictionary;

	public WordLadder(Difficulty difficulty) {
		int steps 			= difficulty.getMaxSteps();
		int maxWordLength	= difficulty.getMaxWordLength();
		dictionary 			= DictionaryParser.getDictionary();
		assert(dictionary != null);

		Map<String, Collection<String>> validDestinations = new HashMap<String, Collection<String>>();
		while(validDestinations.size() < 1) {
			this.origin			= dictionary.generateWord(maxWordLength);
			getDestinations(origin, origin, 0, difficulty.getMaxSteps(), difficulty, validDestinations, null);
		}
		int randomIndex		= (int) (0 + Math.random() * validDestinations.size());
		this.destination	= (String) validDestinations.keySet().toArray()[randomIndex];
		
	}

	private void getDestinations(String origin, String current, int arrayIdx, int steps, Difficulty difficulty, Map<String, Collection<String>> result, Collection<String> stack) {
		if(steps <= difficulty.getMinSteps()) {
			assert(dictionary.containsKey(current));
			result.put(current, stack);
			if(steps == 0)
				return;
		}
		char[] array = origin.toLowerCase().toCharArray();
		for(; arrayIdx < array.length; arrayIdx++) {
			char arrayChar	= array[arrayIdx];
			char permute	= 'a';
			while(permute <= 'z') {
				if(permute != arrayChar) {
					array[arrayIdx] = permute;
					String permutation = String.copyValueOf(array);
					getDestinations(origin, permutation, arrayIdx + 1, steps - 1, difficulty, result, new LinkedHashSet<String>());
				}
			}
		}
	}
}
	
