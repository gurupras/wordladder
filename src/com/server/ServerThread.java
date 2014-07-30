package com.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ServerThread extends Thread implements Runnable {
	public static final int SERVER_PORT = 9091;
	
	private Map<Socket, ConsoleThread> connections;
	
	public ServerThread() {
		connections = new HashMap<Socket, ConsoleThread>();
	}
	
	@Override
	public void run() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
			serverSocket.setReuseAddress(true);
		} catch (IOException e) {
			System.err.println("Error in initializing server socket :" + e.getMessage());
			return;
		}
		try {
			while(true) {
				Socket socket = serverSocket.accept();
				ConsoleThread consoleThread = new ConsoleThread(socket);
				connections.put(socket, consoleThread);
				consoleThread.start();
			}
		} catch (Exception e) {
			System.err.println("ServerThread caught exception :" + e.getMessage());
		} finally {
			try { serverSocket.close(); } catch (Exception e) {}
		}
	}
}
