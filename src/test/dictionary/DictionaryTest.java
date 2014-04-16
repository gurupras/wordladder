package test.dictionary;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.games.wordladder.Dictionary;
import com.games.wordladder.DictionaryParser;
import com.games.wordladder.DictionaryParser.DictionaryNotFound;
import com.games.wordladder.DictionaryParser.DictionaryPatternException;

public class DictionaryTest {
	private static StringBuffer result = null;
	private static boolean verbose = false;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		result = new StringBuffer();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println(result.toString());
	}

	@Test
	public void patternTest() {
		StringBuffer localBuffer = new StringBuffer("Test whether dictionary pattern is correct              :");
		StringBuffer errorBuffer = new StringBuffer();
		boolean failed = false;
		try {
			DictionaryParser.getDictionary();
		} catch(DictionaryPatternException e) {
			if(verbose) {
				errorBuffer.append("Warning: Dictionary pattern failed to match all lines\n\t");
				errorBuffer.append(e.getMessage());
			}
		} catch(Exception e) {
			failed = true;
			errorBuffer.append(e.getMessage());
			e.printStackTrace();
		}
		if(failed) {
			localBuffer.append("Failed\n");
		}
		else
			localBuffer.append("Passed\n");
		localBuffer.append("\t" + errorBuffer + "\n");
		result.append(localBuffer.toString());
	}
	
	@Test
	public void dictionarySerializationTest() {
		StringBuffer localBuffer 	= new StringBuffer("Test whether dictionary serialization works             :");
		StringBuffer errorBuffer 	= new StringBuffer();
		Dictionary dictionary	 	= null;
		boolean failed				= false;

//		First check whether we're able to initialize the dictionary
		try {
			dictionary = DictionaryParser.getDictionary();
		} catch(DictionaryPatternException e) {
			if(verbose) {
				errorBuffer.append("Warning: Dictionary pattern failed to match all lines\n\t");
				errorBuffer.append(e.getMessage());
			}
		} catch(Exception e) {
			failed = true;
			errorBuffer.append(e.getMessage());
		}
		if(failed) {
			localBuffer.append("Failed\n");
		}
		
//		Now do the actual SerDe
		else {
			try {
				DictionaryParser.storeDictionary("test.dict");
			} catch(IOException e) {
				failed = true;
				errorBuffer.append(e.getMessage());
			}
			if(!failed) {
				try {
					DictionaryParser.loadDictionary("test.dict");
				} catch (Exception e) {
					failed = true;
					errorBuffer.append(e.getMessage());
				}
				if(!failed) {
					Dictionary newDictionary = null;
					try {
						newDictionary = DictionaryParser.getDictionary();
					} catch (DictionaryNotFound e) {
						failed = true;
						errorBuffer.append(e.getMessage());
					} catch (DictionaryPatternException e) {
						failed = true;
						errorBuffer.append(e.getMessage());
					}
					if(!failed) {
						for(Map.Entry<String, String> entry : dictionary.entrySet()) {
							String key = entry.getKey();
							String value = entry.getValue();
							if(!newDictionary.containsKey(key) || !newDictionary.get(key).equals(value)) {
								failed = true;
								errorBuffer.append("Serialized dictionary != deserialized dictionary");
								break;
							}
						}
						if(!failed) {
							for(Map.Entry<String, String> entry : newDictionary.entrySet()) {
								String key = entry.getKey();
								String value = entry.getValue();
								if(!dictionary.containsKey(key) || !dictionary.get(key).equals(value)) {
									failed = true;
									errorBuffer.append("Serialized dictionary != deserialized dictionary");
									break;
								}
							}
						}
					}
				}
			}
		}
		if(failed)
			localBuffer.append("Failed\n");
		else
			localBuffer.append("Passed\n");
		localBuffer.append("\t" + errorBuffer + "\n");
		result.append(localBuffer.toString());
		
//		Clean up
		File file = new File("test.dict");
		if(file.exists())
			file.delete();
	}
}
