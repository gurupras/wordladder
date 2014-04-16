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
			getDestinations(origin, origin, steps, difficulty, validDestinations, null);
		}
		int randomIndex		= (int) (0 + Math.random() * validDestinations.size());
		this.destination	= (String) validDestinations.keySet().toArray()[randomIndex];
		
	}

	private void getDestinations(String origin, String current, int steps, Difficulty difficulty, Map<String, Collection<String>> result, HashSet<String> stack) {
		if(steps <= difficulty.getMinSteps()) {
			assert(dictionary.containsKey(current));
			result.put(current, stack);
			if(steps == 0)
				return;
		}

//		We have more steps to iterate..try all combinations possible
		char[] array = current.toLowerCase().toCharArray();
		for(int arrayIdx = 0; arrayIdx < array.length; arrayIdx++) {
			char arrayChar = array[arrayIdx];
			char permute = 'a';
			while(permute <= 'z') {
				if(permute != arrayChar) {
//					We don't want to permute the same word over and over
					array[arrayIdx] = permute;

					String permutation = String.copyValueOf(array);
					if(dictionary.containsKey(permutation)) {
//						FIXME: Use a suffix tree to short-circuit this
						if(stack == null) {
//							Top-level of recursion
							stack = new LinkedHashSet<String>();
							stack.add(origin);
						}
						if(!stack.contains(permutation)) {
							stack.add(permutation);
							getDestinations(origin, permutation, steps - 1, difficulty, result, stack);
						}
					}
					array[arrayIdx] = arrayChar;
				}
				permute++;
			}
		}
	}
	
	private int hammingDistance(String origin, String destination) {
		assert(origin.length() == destination.length()) : "Current implementation of hamming distance requires strings to be of equal length";
		int result = 0;
		for(int idx = 0; idx < origin.length(); idx++) {
			if(origin.charAt(idx) != destination.charAt(idx))
				result++;
		}
		return result;
	}
}
