package com.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.logger.Log;

public class ServerThread extends Thread implements Runnable {
	public static final int SERVER_PORT = 9091;
	public static final String TAG = "ServerThread";
	
	private Map<Socket, ConsoleThread> connections;
	
	public ServerThread() {
		connections = new HashMap<Socket, ConsoleThread>();
	}
	
	@Override
	public void run() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
			Log.i(TAG, "Created server socket");
			serverSocket.setReuseAddress(true);
			Log.i(TAG,  "Successfully set reuseAddr");
		} catch (IOException e) {
			System.err.println("Error in initializing server socket :" + e.getMessage());
			return;
		}
		try {
			Log.d(TAG, "Listening for connections");
			while(true) {
				Socket socket = serverSocket.accept();
				socket.setSoTimeout(500);
				Log.d(TAG, "Received connection from :" + socket.getInetAddress().getHostAddress());
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
