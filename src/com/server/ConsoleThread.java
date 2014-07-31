package com.server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.server.Driver.threads;
import static com.server.Driver.wordLadderMaps;

import com.games.wordladder.Difficulty;
import com.logger.Log;

public class ConsoleThread extends Thread implements Runnable {
	private String TAG = "ConsoleThread";
	
	private Socket socket;
	
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
	
	public ConsoleThread(Socket socket) {
		this.socket = socket;
		commandMap = new HashMap<Command, List<String>>();
		for(Command c : Command.values()) {
			int idx = Arrays.asList(Command.values()).indexOf(c);
			commandMap.put(c, Arrays.asList(commands[idx]));
		}
		TAG += "->" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
	}
	
	@Override
	public void run() {
		Log.i(TAG, "Started Console thread");
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while(true) {
				String command = "";
				while(true) {
					try {
						command = in.readLine();
						if(command == null || command.equals("")) {
							Thread.sleep(50);
							continue;
						}
						else
							break;
					} catch (SocketTimeoutException e) {
					}
				}
				Log.d(TAG, "Received command :" + command);
				handle(command);
				if(command.equals("exit"))
					break;
			}
		} catch(Exception e) {
			Log.e(TAG, "Caught console exception :" + e.getMessage() + "\n");
		}
		Log.d(TAG, "Finished session");
	}
	
	private synchronized void handle(String cmdString) {
		Command command = null;
		for(Map.Entry<Command, List<String>> entry : commandMap.entrySet()) {
			List<String> commandNames = entry.getValue();
			if(commandNames.contains(cmdString.toLowerCase()))
				command = entry.getKey();
		}
		if(command == null) {
			sendMessage_ln("Invalid command :" + cmdString);
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
	
	private void sendMessage_ln(String message) {
		Log.d(TAG, "Attempting to send message :" + message);
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(message);
		} catch(Exception e) {
			Log.e(TAG, "Caught exception while sending message :" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void help() {
		for(Map.Entry<Command, List<String>> entry : commandMap.entrySet()) {
			sendMessage_ln("" + entry.getValue());
		}
	}
	
	private void status() {
//		XXX: Assumption: If one thread is running, all threads are running
		boolean isPaused = threads.get(0).getPaused();
		
		if(stopThreads(Command.STATUS))
			return;
		for(Difficulty d : wordLadderMaps.keySet())
			sendMessage_ln(d + " :" + wordLadderMaps.get(d).size());
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
			sendMessage_ln("Warning: Using existing pre-loaded map");
		
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
			Log.e(TAG, command + ":" + "Caught exception while pausing threads :" + e.getMessage());
			return true;
		}
		return false;
	}
	
	private boolean resumeThreads(Command command) {
		try {
			Driver.resumeThreads();
		} catch(IllegalMonitorStateException e) {
			Log.e(TAG, command + ":" + "Caught exception while resuming threads :" + e.getMessage());
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
		Log.i(TAG, "Expecting client to terminate socket");
	}
}