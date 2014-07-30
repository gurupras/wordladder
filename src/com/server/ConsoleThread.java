package com.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.server.Driver.OUTPUT_PATH;
import static com.server.Driver.threads;
import static com.server.Driver.wordLadderMaps;

import com.games.wordladder.Difficulty;

public class ConsoleThread extends Thread implements Runnable {
	private enum Command {
		HELP,
		START,
		STOP,
		RESUME,
		STATUS,
		DUMP,
		RESTORE,
		CLEAR,
		EXIT,
	}
	private final String[][] commands = {
			{"help", "h"},
			{"start"},
			{"pause", "stop"},
			{"resume", "continue"},
			{"status", "info"},
			{"dump", "serialize"},
			{"restore", "deserialize"},
			{"clear", "reset"},
			{"exit"},
	};
	
	private Map<Command, List<String>> commandMap;
	
	public ConsoleThread() {
		commandMap = new HashMap<Command, List<String>>();
		for(Command c : Command.values()) {
			int idx = Arrays.asList(Command.values()).indexOf(c);
			commandMap.put(c, Arrays.asList(commands[idx]));
		}
	}
	
	@Override
	public void run() {
		while(true) {
			System.out.print(">");
			String command = "";
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(System.in));
				command = in.readLine();
				if(command.equals(""))
					continue;
				handle(command);
				Thread.sleep(50);
			} catch(Exception e) {
				System.err.println("Caught console exception :" + e.getMessage() + "\n");
			}
		}
	}
	
	private synchronized void handle(String cmdString) {
		Command command = null;
		for(Map.Entry<Command, List<String>> entry : commandMap.entrySet()) {
			List<String> commandNames = entry.getValue();
			if(commandNames.contains(cmdString.toLowerCase()))
				command = entry.getKey();
		}
		if(command == null) {
			System.err.println("Invalid command :" + cmdString);
			help();
		}
		else {
			switch(command) {
			case HELP:
				help();
				break;
			case START:
				startThreads();
				break;
			case STOP:
				stopThreads(command);
				break;
			case RESUME:
				resumeThreads(command);
				break;
			case STATUS:
				status();
				break;
			case DUMP:
				dump();
				break;
			case RESTORE:
				restore();
				break;
			case CLEAR:
				clearMap();
				break;
			case EXIT:
				exit();
				break;
			default:
				throw new IllegalStateException("Unimplemented command :" + command);
			}
		}
	}
	
	private void help() {
		for(Map.Entry<Command, List<String>> entry : commandMap.entrySet()) {
			System.err.println(entry.getValue());
		}
	}
	
	private void status() {
//		XXX: Assumption: If one thread is running, all threads are running
		boolean isPaused = threads.get(0).getPaused();
		
		if(stopThreads(Command.STATUS))
			return;
		for(Difficulty d : wordLadderMaps.keySet())
			System.out.println(d + " :" + wordLadderMaps.get(d).size());
		if(!isPaused)
			resumeThreads(Command.STATUS);
	}
	
	private void dump() {
//		XXX: Assumption: If one thread is running, all threads are running
		boolean isPaused = threads.get(0).getPaused();
		
		if(stopThreads(Command.DUMP))
			return;
		
		File file = new File(Driver.OUTPUT_PATH);
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(wordLadderMaps);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { out.close(); } catch(Exception e) {}
		}
		if(!isPaused)
			resumeThreads(Command.STATUS);
	}

	private void restore() {
//		XXX: Assumption: If one thread is running, all threads are running
		boolean isPaused = threads.get(0).getPaused();
		
		if(stopThreads(Command.RESTORE))
			return;

		File file = new File(Driver.OUTPUT_PATH);
		ObjectInputStream out = null;
		try {
			out = new ObjectInputStream(new FileInputStream(file));
			wordLadderMaps = (Map<Difficulty, Set<String>>) out.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try { out.close(); } catch(Exception e) {}
		}
		if(!isPaused)
			resumeThreads(Command.STATUS);
	}
	
	private void startThreads() {
		int size = 0;
		for(Map.Entry<Difficulty, Set<String>> entry : wordLadderMaps.entrySet()) {
			Set<String> value = entry.getValue();
			size = size > value.size() ? size : value.size();
		}
		if(size > 0)
			System.err.println("Warning: Using existing pre-loaded map");
		
		for(WorkerThread thread : threads) {
			thread.start();
		}
//		XXX: Because of the way we've implemented the waiting mechanism, we need to call resume to notify the threads
//		Since the constructor sets isPaused to true to signal that it isn't running (yet).
		resumeThreads(Command.START);
	}
	
	private boolean stopThreads(Command command) {
		try {
			Driver.pauseThreads();
		} catch (InterruptedException e) {
			System.err.println(command + ":" + "Caught exception while pausing threads :" + e.getMessage());
			return true;
		}
		return false;
	}
	
	private boolean resumeThreads(Command command) {
		try {
			Driver.resumeThreads();
		} catch(IllegalMonitorStateException e) {
			System.err.println(command + ":" + "Caught exception while resuming threads :" + e.getMessage());
			return true;
		}
		return false;
	}

	private void clearMap() {
//		XXX: Assumption: If one thread is running, all threads are running
		boolean isPaused = threads.get(0).getPaused();
		
		if(stopThreads(Command.CLEAR))
			return;
		wordLadderMaps.clear();
		if(!isPaused)
			resumeThreads(Command.CLEAR);
	}
	
	private void exit() {
//		We dump anyway just to be safe
		if(!stopThreads(Command.EXIT))
			dump();
		
		System.exit(-1);
	}
}