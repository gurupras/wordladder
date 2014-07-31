package com.server;

import static com.server.ServerThread.SERVER_PORT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Test;

public class Client {
	private static final Object messenger = new Object();
	private static final String IP_ADDRESS = "127.0.0.1";

	public static void main(String[] args) {
		SenderThread senderThread = null;
		ReceiverThread receiverThread = null;
		Socket socket = null;
		try {
			socket = new Socket(IP_ADDRESS, SERVER_PORT);
			senderThread = new SenderThread(socket);
			receiverThread = new ReceiverThread(socket);
			senderThread.start();
			receiverThread.start();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			senderThread.join();
			receiverThread.join();
		} catch(Exception e) {
		}
	}
	
	
	private static class SenderThread extends Thread implements Runnable {
		private Socket socket;
		public SenderThread(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			try {
				PrintWriter sockOutput = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
				while(true) {
					String command = null;
					System.out.print(">");
					command = input.readLine();
					sockOutput.println(command);
					synchronized(messenger) {
//						messenger.wait();
					}
				}
			} catch (Exception e) {
				System.err.println("CommunicationThread caught exception :" + e.getMessage());
			} finally {
				try { socket.close(); } catch (Exception e) {}
			}
		}
	}
	
	private static class ReceiverThread extends Thread implements Runnable {
		private Socket socket;
		
		public ReceiverThread(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			try {
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String line = null;
				while(true) {
					line = input.readLine();
					if(line == null || line == "") {
						Thread.sleep(50);
						continue;
					}
					else 
						System.out.println(line);
					Thread.sleep(50);
					synchronized(messenger) {
//						messenger.notify();
					}
				}
			} catch (Exception e) {
				System.err.println("ReceiverThread caught exception  :" + e.getMessage());
				return;
			} finally {
				try { socket.close(); } catch (Exception e) {}
			}
		}
	}
}
