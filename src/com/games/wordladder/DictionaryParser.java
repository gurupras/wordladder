package com.games.wordladder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DictionaryParser {
	private static final String dictPath		= "libs/dictionary/";
	private static Pattern dictionaryPattern	= Pattern.compile("(\\w+)\\s+\\(.*\\)\\s(.*)");
	private static Dictionary dictionary		= null;
	
	private static boolean init() throws DictionaryNotFound, DictionaryPatternException {
		dictionary = new Dictionary();
		boolean succeeded = true;
		StringBuffer errorBuffer = new StringBuffer();
		File file = new File(dictPath);
		if(!file.exists())
			throw new DictionaryNotFound("Dictionary path " + dictPath + " does not exit");
		
		BufferedReader in = null;
		try {
			File[] files = file.listFiles();
			for(File f : files) {
				in = new BufferedReader(new FileReader(f));
				String line;
				while((line = in.readLine()) != null) {
					if(line.equals(""))
						continue;
					Matcher m = dictionaryPattern.matcher(line);
					if(!m.matches()) {
						succeeded = false;
						errorBuffer.append("Line did not match pattern\n" +
								"File :" + f.getName() + "\n" +
								"Line :" + line);
						continue;
					}
					String word = m.group(1);
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
		if(errorBuffer.length() > 0) {
			throw new DictionaryPatternException(errorBuffer.toString());
		}
		return succeeded;
	}

	public static void storeDictionary(String filePath) throws FileNotFoundException, IOException {
		File file = new File(filePath);
		if(file.exists())
			file.delete();
		ObjectOutputStream out = null;
		out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(dictionary);
		out.close();
	}
	
	public static void loadDictionary(String filePath) throws FileNotFoundException, IOException, ClassNotFoundException {
		File file = new File(filePath);
		ObjectInputStream out = null;
		out = new ObjectInputStream(new FileInputStream(file));
		dictionary = (Dictionary) out.readObject();
		out.close();
	}
	
	
	public static class DictionaryNotFound extends IOException {
		public DictionaryNotFound(String string) {
			super(string);
		}
	}
	
	public static class DictionaryPatternException extends Exception {
		public DictionaryPatternException(String string) {
			super(string);
		}
	}

	public static Dictionary getDictionary() throws DictionaryNotFound, DictionaryPatternException {
		if(dictionary == null)
			init();
		return dictionary;
	}
}
