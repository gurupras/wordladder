package com.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.games.wordladder.Dictionary;
import com.games.wordladder.DictionaryParser;
import com.games.wordladder.Difficulty;
import com.games.wordladder.WordLadder;

import static com.server.Driver.wordLadderMaps;

public class WorkerThread extends Thread implements Runnable {
	private Difficulty difficulty;
	private Set<String> wordLadderSet;
	private Boolean isPaused = false;
	
	public WorkerThread(Difficulty difficulty) {
		this.difficulty = difficulty;
		this.wordLadderSet = wordLadderMaps.get(difficulty);
		this.setPriority((MAX_PRIORITY - MIN_PRIORITY) / 2);
		this.isPaused = true;
	}
	
	public synchronized void setPaused(boolean b) {
		this.isPaused = b;
	}

	public synchronized Boolean getPaused() {
		return isPaused;
	}

	@Override
	public void run() {
		System.out.println("Started thread :" + getName());
		Dictionary dictionary = DictionaryParser.getDictionary();
		for(int len = Dictionary.MIN_WORD_LENGTH; len <= difficulty.getMaxWordLength(); len++) {
			for(String origin : dictionary.getWordLengthMap().get(len)) {
				try {
					Collection<String> destinations = new LinkedList<String>();
					Map<String, Collection<String>> pathMap = new HashMap<String, Collection<String>>();
					WordLadder.getAllDestinations(origin, difficulty, destinations, pathMap);
					
					for(String destination : destinations) {
						WordLadder wl = new WordLadder(origin, destination);
						wordLadderSet.add(wl.origin() + "--" + wl.destination() + ":" + wl.getPath());	
					}
				} catch(Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
		System.out.println("Finished thread :" + getName());
	}
}