package com.games.wordladder;

import java.util.Collection;
import java.util.HashSet;

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

		Collection<String> validDestinations = new HashSet<String>();
		while(validDestinations.size() < 1) {
			this.origin			= dictionary.generateWord(maxWordLength);
			getDestinations(origin, steps, difficulty, validDestinations, null);
		}
		int randomIndex		= (int) (0 + Math.random() * validDestinations.size());
		this.destination	= (String) validDestinations.toArray()[randomIndex];
		
	}

	private void getDestinations(String origin, int steps, Difficulty difficulty, Collection<String> result, HashSet<String> stack) {
		if(steps <= difficulty.getMinSteps()) {
//			We don't have any more steps to iterate..just check if the word exists in the dictionary
//			add it to result if it does, and return
			if(dictionary.containsKey(origin))
				result.add(origin);
			return;
		}
//		We have more steps to iterate..try all combinations possible
		char[] array = origin.toLowerCase().toCharArray();
		for(int arrayIdx = 0; arrayIdx < array.length / 2; arrayIdx++) {
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
							stack = new HashSet<String>();
						}
						if(!stack.contains(permutation)) {
							stack.add(permutation);
							getDestinations(permutation, steps, difficulty, result, stack);
						}
					}
					array[arrayIdx] = arrayChar;
				}
				permute++;
			}
		}
	}
}
