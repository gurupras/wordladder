package com.server;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.games.wordladder.DictionaryParser;
import com.games.wordladder.DictionaryParser.DictionaryPatternException;
import com.games.wordladder.Difficulty;
import com.logger.Log;


public class Driver {
	private static final String TAG = "Server Driver";
	protected static final String OUTPUT_PATH = "wordladder.bin";
	
	protected static Map<Difficulty, Set<String>> wordLadderMaps = new HashMap<Difficulty, Set<String>>();
	protected static List<WorkerThread> threads;
	
	public static void main(String[] args) {
		try {
			Log.init(Log.STDOUT);
		} catch (IOException e1) {
			System.err.println("Unable to initialize logger");
			return;
		}
		
		threads = new ArrayList<WorkerThread>();
		
		wordLadderMaps = new HashMap<Difficulty, Set<String>>();
		
		for(Difficulty d : Difficulty.values()) {
			HashSet<String> h = new HashSet<String>();
			Set<String> s = Collections.synchronizedSet(h);
			wordLadderMaps.put(d, s);
		}
		
		for(Difficulty d : Difficulty.values()) {
			WorkerThread thread = new WorkerThread(d);
			thread.setName(d + "->" + "thread-" + "0");
			threads.add(thread);
		}
		
		ServerThread serverThread = new ServerThread();
		
		try {
			Log.v(TAG, "Initializing dictionary");
			DictionaryParser.init();
			Log.v(TAG, "Finished initializing dictionary");
			
			serverThread.start();
			serverThread.join();
		} catch(DictionaryPatternException e) {
		} catch(Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public synchronized static void pauseThreads() throws InterruptedException {
		for(WorkerThread thread : threads) {
			synchronized(thread) {
				thread.setPaused(true);
			}
		}
	}
	
	public synchronized static void resumeThreads() throws IllegalMonitorStateException {
		for(WorkerThread thread : threads) {
			synchronized(thread) {
				thread.setPaused(false);
				thread.notify();
			}
		}
	}
}
