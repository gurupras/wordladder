package test.wordladder;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.TimeKeeper;

import com.games.wordladder.DictionaryParser;
import com.games.wordladder.Difficulty;
import com.games.wordladder.WordLadder;

public class WordLadderTest {
	private static StringBuffer result;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		result = new StringBuffer();
		try {
			DictionaryParser.init();
		} catch(Exception e) {
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println(result.toString());
	}

	@Test
	public void constructorTest() {
		StringBuffer localBuffer = new StringBuffer("Test whether WordLadder constructor works               :");
		StringBuffer errorBuffer = new StringBuffer();
		boolean failed = false;
		
		TimeKeeper timeKeeper = new TimeKeeper();
		try {
			timeKeeper.start();
			new WordLadder(Difficulty.EASY);
			timeKeeper.stop();
			System.out.println("Time Taken :" + timeKeeper.toString());
			timeKeeper.start();
			new WordLadder(Difficulty.MEDIUM);
			timeKeeper.stop();
			System.out.println("Time Taken :" + timeKeeper.toString());
			timeKeeper.start();
			new WordLadder(Difficulty.HARD);
			timeKeeper.stop();
			System.out.println("Time Taken :" + timeKeeper.toString());
		} catch(Exception e) {
			failed = true;
			errorBuffer.append(e.getMessage());
			e.printStackTrace();
		}
		if(failed)
			localBuffer.append("Failed\n");
		else
			localBuffer.append("Passed\n");
		localBuffer.append("\t" + errorBuffer + "\n");
		result.append(localBuffer.toString());
	}

}
