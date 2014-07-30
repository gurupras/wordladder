import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.games.wordladder.DictionaryParser;
import com.games.wordladder.DictionaryParser.DictionaryPatternException;
import com.games.wordladder.Difficulty;
import com.games.wordladder.WordLadder;


public class Driver {
	private static final String OUTPUT_PATH = "wordladder.bin";
	private static final int THREADS_PER_DIFFICULTY = 2;
	
	private static Map<Difficulty, Set<String>> wordLadderMaps = new HashMap<Difficulty, Set<String>>();
	private static List<WorkerThread> threads;
	
	public static void main(String[] args) {
		threads = new ArrayList<WorkerThread>();
		
		wordLadderMaps = new HashMap<Difficulty, Set<String>>();
		
		for(Difficulty d : Difficulty.values()) {
			HashSet<String> h = new HashSet<String>();
			Set<String> s = Collections.synchronizedSet(h);
			wordLadderMaps.put(d, s);
		}
		
		ConsoleThread consoleThread = new ConsoleThread();
		
		try {
			DictionaryParser.init();
			consoleThread.start();
			consoleThread.join();
		} catch(DictionaryPatternException e) {
		} catch(Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	private static class ConsoleThread extends Thread implements Runnable {
		private enum Command {
			HELP,
			STATUS,
			DUMP,
			RESTORE,
			START,
		}
		private final String[][] commands = {
				{"help", "h"},
				{"status", "info"},
				{"dump", "serialize"},
				{"restore", "deserialize"},
				{"start"},
		};
		
		private Map<Command, List<String>> commandMap;
		
		public ConsoleThread() {
			commandMap = new HashMap<Command, List<String>>();
			commandMap.put(Command.HELP, Arrays.asList(commands[0]));
			commandMap.put(Command.STATUS, Arrays.asList(commands[1]));
			commandMap.put(Command.DUMP, Arrays.asList(commands[2]));
			commandMap.put(Command.RESTORE, Arrays.asList(commands[3]));
			commandMap.put(Command.START, Arrays.asList(commands[4]));
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
				case STATUS:
					status();
					break;
				case DUMP:
					dump();
					break;
				case RESTORE:
					restore();
					break;
				case START:
					startThreads();
					break;
				}
			}
		}
		
		private void pauseThreads() throws InterruptedException {
			for(WorkerThread thread : threads) {
				synchronized(thread.getPaused()) {
					thread.setPaused(true);
				}
			}
		}
		
		private void resumeThreads() throws IllegalMonitorStateException {
			for(WorkerThread thread : threads) {
				synchronized(thread.getPaused()) {
					thread.setPaused(true);
					thread.getPaused().notify();
				}
			}
		}
		
		private void help() {
			for(Map.Entry<Command, List<String>> entry : commandMap.entrySet()) {
				System.err.println(entry.getValue());
			}
		}
		
		private void status() {
			try {
				pauseThreads();
			} catch(Exception e) {
				System.err.println(Command.STATUS + ":" + "Caught exception while pausing threads :" + e.getMessage());
				return;
			}
			for(Difficulty d : wordLadderMaps.keySet())
				System.out.println(d + " :" + wordLadderMaps.get(d).size());
			try {
				resumeThreads();
			} catch(IllegalMonitorStateException e) {
				System.err.println(Command.STATUS + ":" + "Caught exception while resuming threads :" + e.getMessage());
			}
		}
		
		private void dump() {
			try {
				pauseThreads();
			} catch(Exception e) {
				System.err.println(Command.DUMP + ":" + "Caught exception while pausing threads:" + e.getMessage());
				return;
			}
			
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
			try {
				resumeThreads();
			} catch(IllegalMonitorStateException e) {
				System.err.println(Command.DUMP + ":" + "Caught exception while resuming threads :" + e.getMessage());
			}
		}
	
		private void restore() {
			try {
				pauseThreads();
			} catch(Exception e) {
				System.err.println(Command.STATUS + ":" + "Caught exception while pausing threads :" + e.getMessage());
			}

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
			
			try {
				resumeThreads();
			} catch(IllegalMonitorStateException e) {
				System.err.println(Command.STATUS + ":" + "Caught exception while resuming threads :" + e.getMessage());
			}
		}
		
		private void startThreads() {
			for(Difficulty d : Difficulty.values()) {
				for(int idx = 0; idx < Driver.THREADS_PER_DIFFICULTY; idx++) {
					WorkerThread thread = new WorkerThread(d);
					thread.setName(d + "->" + "thread-" + idx);
					threads.add(thread);
					thread.start();
				}
			}
		}
	}
	
	private static class WorkerThread extends Thread implements Runnable {
		private Difficulty difficulty;
		private Set<String> wordLadderSet;
		private Boolean isPaused = false;
		
		public WorkerThread(Difficulty difficulty) {
			this.difficulty = difficulty;
			this.wordLadderSet = wordLadderMaps.get(difficulty);
			this.setPriority((MAX_PRIORITY - MIN_PRIORITY) / 2);
		}
		
		public Boolean getPaused() {
			return isPaused;
		}

		@Override
		public void run() {
			System.out.println("Started thread :" + getName());
			while(true) {
				try {
					synchronized(isPaused) {
						if(isPaused)
							isPaused.wait();
					}
					WordLadder l = new WordLadder(difficulty);
					wordLadderSet.add(l.origin() + "->" + l.destination());
				} catch(Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
		
		public void setPaused(boolean value) {
			this.isPaused = value;
		}
	}
}
