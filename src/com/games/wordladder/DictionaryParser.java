package com.games.wordladder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import test.TimeKeeper;

import com.android_app.MainActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import static com.android_app.MainActivity.TAG;

/**
 * Static class responsible for parsing the dictionary files and creating a {@link Dictionary} object.
 * @author Guru
 *
 */
public class DictionaryParser {
	/** Path to the directory containing the dictionary files */
	private static final String dictPath			= Environment.getExternalStorageDirectory().getAbsolutePath() + "/wordladder/dictionary.bin";
	
	/** Pattern of each entry in the dictionary files */
	private static Pattern dictionaryPattern	= Pattern.compile("(\\w+)\\s+\\(.*\\)\\s(.*)");
	
	/** The dictionary object */
	private static Dictionary dictionary		= null;
	
	
	/**
	 * Private method to initialize the {@link #dictionary} field.
	 * @return <b>true</b> if successfully initialized <br>
	 * <b>false</b> otherwise
	 * @throws DictionaryNotFound if {@link #dictFilesPath} does not exist
	 * @throws DictionaryPatternException if the {@link #dictionaryPattern} did not match all lines in the dictionary files
	 */
	public static void init(Context context) throws DictionaryNotFound, DictionaryPatternException {
		Dictionary dictionary = null;
		TimeKeeper tk = new TimeKeeper();
		tk.start();
		try {
			dictionary = Dictionary.deserialize(context, dictPath);
			DictionaryParser.dictionary = dictionary;
			tk.stop();
			if(dictionary != null) {
				Log.v(TAG, "Time taken to deserialize dictionary :" + tk.toString());
				return;
			}
			
		} catch(Exception e) {
			Log.v(TAG, "Could not deserialize dictionary..first run?");
		}
		
		dictionary = new Dictionary();
		StringBuffer errorBuffer = new StringBuffer();
		
		BufferedReader in = null;
		try {
			AssetManager am = context.getAssets();
			String[] files = am.list("dictionary"); 
			for(String f : files) {
				Log.v(TAG, "Opening file :" + f);
				in = new BufferedReader(new InputStreamReader(am.open("dictionary/" + f)));			
				String line;
				while((line = in.readLine()) != null) {
//					Empty line..continue to the next
					if(line.equals(""))
						continue;
					Matcher m = dictionaryPattern.matcher(line);
					if(!m.matches()) {
						errorBuffer.append("Line did not match pattern\n" +
								"File :" + f + "\n" +
								"Line :" + line);
						continue;
					}
//					We have a word..add it to the dictionary
					String word = m.group(1).toLowerCase();
					String defn = m.group(2);
					dictionary.put(word, defn);
				}
				in.close();
			}
		} catch(IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} finally {
			if(in != null) {
				try { in.close(); } catch(IOException e) { System.err.println(e.getMessage()); }
			}
		}
		tk.stop();
		Log.v(TAG, "Time taken to build dictionary :" + tk.toString());
		tk.start();
//		At this point, we can't fail. So set DictionaryParser.dictionary to the dictionary we just built.
		DictionaryParser.dictionary = dictionary;
		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/wordladder/");
		if(!file.exists()) {
			if(!file.mkdirs())
				Log.e(TAG, "Could not create directories");
		}
		dictionary.serialize(file.getAbsolutePath() + "/dictionary.bin");
		tk.stop();
		Log.v(TAG, "Time taken to serialize dictionary :" + tk.toString());
		if(errorBuffer.length() > 0) {
//			We had some errors while parsing..throw the pattern exception
			throw new DictionaryPatternException(errorBuffer.toString());
		}
	}

	/**
	 * Serialization method to store a {@link Dictionary}
	 * @param filePath {@code String} containing the file path
	 * @throws IOException if there was an error while serializing
	 */
	public static void storeDictionary(String filePath) throws IOException {
		File file = new File(filePath);
		if(file.exists())
			file.delete();
		ObjectOutputStream out = null;
		out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(dictionary);
		out.close();
	}

	/**
	 * De-serialization method to load a {@link Dictionary}
	 * @param filePath {@code String} containing the file path
	 * @throws FileNotFoundException if {@code filePath} points to a non-existent file
	 * @throws IOException if there was an error while de-serializing
	 * @throws ClassNotFoundException if there was an error during de-serialization
	 */
	public static void loadDictionary(String filePath) throws FileNotFoundException, IOException, ClassNotFoundException {
		File file = new File(filePath);
		ObjectInputStream out = null;
		out = new ObjectInputStream(new FileInputStream(file));
		dictionary = (Dictionary) out.readObject();
		out.close();
	}
	
	/**
	 * Exception class used to represent missing dictionary file(s)
	 * @author Guru
	 *
	 */
	public static class DictionaryNotFound extends IOException {
		public DictionaryNotFound(String string) {
			super(string);
		}
	}
	
	/**
	 * Exception class used to represent error in the dictionary pattern
	 * @author Guru
	 *
	 */
	public static class DictionaryPatternException extends Exception {
		public DictionaryPatternException(String string) {
			super(string);
		}
	}

	/**
	 * Return the {@link Dictionary} object parsed by the {@link DictionaryParser}. Call {@code init()} if necessary
	 * @return {@code Dictionary} object containing the parsed words
	 * @throws DictionaryNotFound if the dictionary file(s) were not found
	 * @throws DictionaryPatternException if the {@link #dictionaryPattern} did not match all lines in the dictionary files
	 */
	public static Dictionary getDictionary() {
		return dictionary;
	}
}
