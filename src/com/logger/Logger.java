package com.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * The {@code Logger} class is a Singleton class designed to handle all logging.
 * @author Guru
 */
public class Logger {
	/** We need a way to distinguish stdout from other file names. We use a filename that is guaranteed to be invalid. */
	public static final String STDOUT = "\0";
	
	/** The {@code File} object of this {@code Logger}. */
	private File file;

	/** {@code BufferedWriter} to {@code file}. */
	private BufferedWriter out;
	
	/** Singleton instance */
	private static Logger logger;
	
	private Logger(String path, boolean append) throws IOException {
		if(path != STDOUT) {
			this.file = new File(path);
			this.out = new BufferedWriter(new FileWriter(file, append));
		}
		else
			this.out = new BufferedWriter(new OutputStreamWriter(System.out));
	}

	/**
	 * Initialize the Singleton instance
	 * @param path {@code String} containing the path to the log file. Use {@link #STDOUT} to log to stdout.
	 * @param append {@code boolean} flag to determine whether to append to an already existing log file.
	 * @throws IOException if unable to open the log file.
	 */
	public static void init(String path, boolean append) throws IOException {
		logger = new Logger(path, append);
	}
	
	/**
	 * Initialize the Singleton instance. The underlying log file is truncated.
	 * @param path {@code String} containing the path to the log file. Use {@link #STDOUT} to log to stdout.
	 * @throws IOException if unable to open the log file.
	 */
	public static void init(String path) throws IOException {
		logger = new Logger(path, false);
	}
	
	/**
	 * Write an entry to the log.
	 * @param entry {@code String}
	 */
	public static void log(String entry) {
		try {
			logger.out.write(entry);
			logger.out.flush();
		} catch (IOException e) {
			System.err.println("Unable to log message :" + e.getMessage());
		}
	}
}