package com.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * The {@code Logger} class is a Singleton class designed to handle all logging.
 * @author Guru
 */
public class Log {
	/** We need a way to distinguish stdout from other file names. We use a filename that is guaranteed to be invalid. */
	public static final String STDOUT = "\0";
	
	/** The {@code File} object of this {@code Logger}. */
	private File file;

	/** {@code PrintWriter} to {@code file}. */
	private PrintWriter out;
	
	/** Singleton instance */
	private static Log logger;
	
	private Log(String path, boolean append) throws IOException {
		if(path != STDOUT) {
			this.file = new File(path);
			this.out = new PrintWriter(new FileOutputStream(file, append), true);
		}
		else
			this.out = new PrintWriter(System.out, true);
	}

	/**
	 * Initialize the Singleton instance
	 * @param path {@code String} containing the path to the log file. Use {@link #STDOUT} to log to stdout.
	 * @param append {@code boolean} flag to determine whether to append to an already existing log file.
	 * @throws IOException if unable to open the log file.
	 */
	public static void init(String path, boolean append) throws IOException {
		logger = new Log(path, append);
	}
	
	/**
	 * Initialize the Singleton instance. The underlying log file is truncated.
	 * @param path {@code String} containing the path to the log file. Use {@link #STDOUT} to log to stdout.
	 * @throws IOException if unable to open the log file.
	 */
	public static void init(String path) throws IOException {
		logger = new Log(path, false);
	}
	
	/**
	 * Write an error to the log
	 * @param tag {@code tag of the log message}
	 * @param entry {@code entry to log}
	 */
	public static void e(String tag, String entry) {
		String string = buildString(Logtype.ERROR, tag, entry);
		logger.log(string);
	}
	
	/**
	 * Write a warning to the log
	 * @param tag {@code tag of the log message}
	 * @param entry {@code entry to log}
	 */
	public static void w(String tag, String entry) {
		String string = buildString(Logtype.WARN, tag, entry);
		logger.log(string);
	}
	
	/**
	 * Write debug information to the log
	 * @param tag {@code tag of the log message}
	 * @param entry {@code entry to log}
	 */
	public static void d(String tag, String entry) {
		String string = buildString(Logtype.DEBUG, tag, entry);
		logger.log(string);
	}
	
	/**
	 * Write information to the log
	 * @param tag {@code tag of the log message}
	 * @param entry {@code entry to log}
	 */
	public static void i(String tag, String entry) {
		String string = buildString(Logtype.INFO, tag, entry);
		logger.log(string);
	}
	
	/**
	 * Write verbose information to the log
	 * @param tag {@code tag of the log message}
	 * @param entry {@code entry to log}
	 */
	public static void v(String tag, String entry) {
		String string = buildString(Logtype.VERBOSE, tag, entry);
		logger.log(string);
	}
	
	/**
	 * Build the final log string out of the components
	 * @param type {@code Logtype}
	 * @param tag {@code String}
	 * @param message {@code String}
	 * @return constructed {@code String}
	 */
	private static String buildString(Logtype type, String tag, String message) {
		return type + "/" + tag + ": " + message;
	}
	
	/**
	 * Log {@code message} to the log stream
	 * @param message {@code String} to log
	 */
	private void log(String message) {
		logger.out.println(message);
		logger.out.flush();
	}
	
	private enum Logtype {
		ERROR("E"),
		WARN("W"),
		DEBUG("D"),
		INFO("I"),
		VERBOSE("V");
		
		private final String string;
		
		private Logtype(String string) {
			this.string = string;
		}
		
		@Override
		public String toString() {
			return string;
		}
	}
}