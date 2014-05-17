package com.games.wordladder;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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

//		Map<String, Collection<String>> validDestinations = new HashMap<String, Collection<String>>();
//		while(validDestinations.size() < 1) {
//			this.origin			= dictionary.generateWord(maxWordLength);
//			getDestinations(origin, origin, 0, difficulty.getMaxSteps(), difficulty, validDestinations, null);
//		}
		Collection<String> destinationList = new LinkedList<String>();
		Map<String, Collection<String>> pathMap = new HashMap<String,Collection<String>>();
		while(destinationList.size() < 1) {
			destinationList.clear();
			pathMap.clear();
			this.origin			= dictionary.generateWord(maxWordLength);
			getDestinations(origin, difficulty.getMaxSteps(), destinationList, pathMap);
		}
		
		int randomIndex		= (int) (0 + Math.random() * destinationList.size());
		this.destination	= (String) destinationList.toArray()[randomIndex];
		
	}

	private void getDestinations(String origin, int maxSteps, Collection<String> destinationList, Map<String,Collection<String>> pathMap) {
		Set<String> visited = new HashSet<String>();
		visited.add(origin);
		Queue<String> q = new LinkedList<String>();
		q.add(origin);
		int stepsTaken = 0;
		while(!q.isEmpty()) {
			String word = q.poll();
			char[] wArray = word.toCharArray();
			Set<String> adjWords = new HashSet<String>();
			for(int i = 0; i < wArray.length; i++) {
				char[] wCopy = Arrays.copyOf(wArray, wArray.length);
				for(char ch = 'a'; ch <= 'z'; ch++) {
					if(wArray[i] != ch) {
						wCopy[i] = ch;
						String perm = String.copyValueOf(wCopy);
						if(dictionary.containsKey(perm) && !visited.contains(perm)) {
							adjWords.add(perm);
						}
					}
				}
			}
			stepsTaken++;
			for(String s : adjWords) {
				if(dictionary.containsKey(s) && !visited.contains(s)) {
					if(stepsTaken == maxSteps) {
						destinationList.add(s);
					}
					visited.add(s);
					q.add(s);
					Collection<String> list = pathMap.get(word);
					if(list == null) {
						 list = new LinkedList<String>();
					}
					list.add(s);
					pathMap.put(word, list);
				}
				visited.add(s);
				q.add(s);
				Collection<String> list = new LinkedList<String>();
				list.addAll(pathMap.get(word));
				list.add(word);
				pathMap.put(s, list);
			}
			if(stepsTaken == difficulty.getMaxSteps()) {
				break;
			}
			if(stepsTaken == maxSteps) {
				break;
			}
		}
		
	}
	
}
	
