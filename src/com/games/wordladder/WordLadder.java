package com.games.wordladder;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class WordLadder {
	private Difficulty difficulty;
	private String origin;
	private String destination;
	private Dictionary dictionary;

	public WordLadder(Difficulty difficulty) {
		this.difficulty		= difficulty;
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
			getDestinations(origin, difficulty, destinationList, pathMap);
		}
		
		Random random = new Random(System.nanoTime());
		int randomIndex		= (int) (0 + random.nextDouble() * destinationList.size());
		this.destination	= (String) destinationList.toArray()[randomIndex];
		
	}
	
	public static Collection<String> getAllDestinations(String origin, Difficulty difficulty, 
			Collection<String> destinations, Map<String, Collection<String>> pathMap) {
		WordLadder wl = new WordLadder(origin, null);
		
		wl.getDestinations(origin, difficulty, destinations, pathMap);
		return destinations;
	}
	
	public WordLadder(String origin, String destination) {
		this.origin = origin;
		this.destination = destination;
		dictionary 			= DictionaryParser.getDictionary();
	}
	
	public String getPath() {
		StringBuffer sb = new StringBuffer();
		List<String> path = findPath(origin, destination);
		
		Iterator<String> iter = path.iterator();
		while(iter.hasNext()) {
			String s = iter.next();
			sb.append(s);
			if(iter.hasNext())
				sb.append("-->");
		}
		return sb.toString();
	}
	
	private List<String> findPath(String origin, String destination) {
		List<String> thePath = new LinkedList<String>();
		Set<String> visited = new HashSet<String>();
		visited.add(origin);
		Queue<String> q = new LinkedList<String>();
		q.add(origin);
		Map<String, String> pathMap = new HashMap<String,String>();
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
			} //end generation of adj words
			for(String str : adjWords) {
				if(str.equals(destination)) {
					thePath.add(str);
					while(word != null) {
						thePath.add(word);
						word = pathMap.get(word);
					}
					return thePath;
				}
				else {
					visited.add(str);
					q.offer(str);
					pathMap.put(str, word);
				}
			}
			
		} //end of while q not empty loop
		return thePath;
	}

	private void getDestinations(String origin, Difficulty difficulty, Collection<String> destinationList, Map<String,Collection<String>> pathMap) {
		Set<String> visited = new HashSet<String>();
		visited.add(origin);
		Queue<String> q = new LinkedList<String>();
		q.add(origin);
		int stepsTaken = 0;
		while(!q.isEmpty()) {
			String word = q.poll();
			char[] wArray = word.toCharArray();
			LinkedList<String> adjWords = new LinkedList<String>();
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
				if(!visited.contains(s)) {
					if(stepsTaken >= difficulty.getMinSteps()) {
						int optimalSteps = findPath(this.origin, s).size();
						if(optimalSteps >= difficulty.getMinSteps()) {
							destinationList.add(s);
						}
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
				Collection<String> list = new LinkedList<String>();
				list.addAll(pathMap.get(word));
				list.add(word);
				pathMap.put(s, list);
			}
			if(stepsTaken > difficulty.getMaxSteps()) {
				break;
			}
		}
		
	}

	public String origin() {
		return origin;
	}
	
	public String destination() {
		return destination;
	}
	
	public Difficulty difficulty() {
		return difficulty;
	}
	
	public Dictionary dictionary() {
		return dictionary;
	}
	
	public static int hammingDistance(String word1, String word2) {
		if(word1.length() != word2.length()) {
			throw new IllegalArgumentException("Word lengths do not match!\n"
					+ word1 + " " + word2);
		}

		int hammingDistance = 0;
		char[] wordArray1 = word1.toCharArray();
		char[] wordArray2 = word2.toCharArray();

		for(int idx = 0; idx < word1.length(); idx++) {
			if(wordArray1[idx] != wordArray2[idx])
				hammingDistance++;
		}
		return hammingDistance;
	}

}
	
