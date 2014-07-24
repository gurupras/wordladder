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
		
		TimeKeeper timeKeeper	= new TimeKeeper();
		StringBuffer logSB		= new StringBuffer();
		try {
			for(Difficulty d : Difficulty.values()) {
				logSB.append("\tDifficulty :" + d + "\n");
				timeKeeper.start();
				WordLadder wl = new WordLadder(d);
				logSB.append("\t\t" + wl.getPath() + "\n");
				timeKeeper.stop();
				logSB.append("\t\tTime Taken :" + timeKeeper.toString() + "\n");
			}
		} catch(Exception e) {
			failed = true;
			errorBuffer.append(e.getMessage());
			e.printStackTrace();
		}
		if(failed)
			localBuffer.append("Failed\n");
		else {
			localBuffer.append("Passed\n");
			localBuffer.append(logSB.toString());
		}
		localBuffer.append("\t" + errorBuffer + "\n");
		result.append(localBuffer.toString());
	}

	@Test
	public void pathTest() {
		String origin = "hand";
		String destination = "sane";
		WordLadder wl = new WordLadder(origin, destination);
//		System.out.println(wl.getPath());
	}
	
	@Test
	public void timingTest() {
		final int ITERATIONS = 10;
		StringBuffer localBuffer = new StringBuffer("Average time taken to initiaze each difficulty          :");
		StringBuffer errorBuffer = new StringBuffer();
		boolean failed = false;
		
		TimeKeeper timeKeeper	= new TimeKeeper();
		StringBuffer logSB		= new StringBuffer();
		try {
			for(Difficulty d : Difficulty.values()) {
				logSB.append("\tDifficulty :" + d + "\n");
				double sum = 0;
				for(int i = 0; i < ITERATIONS; i++) {
					timeKeeper.start();
					new WordLadder(d);
					timeKeeper.stop();
					sum += timeKeeper.getTimeTaken();
				}
				TimeKeeper averageTime = new TimeKeeper((long) (sum / ITERATIONS));
				logSB.append("\t\tTime Taken :" + averageTime.toString() + "\n");
			}
		} catch(Exception e) {
			failed = true;
			errorBuffer.append(e.getMessage());
			e.printStackTrace();
		}
		if(failed)
			localBuffer.append("Failed\n");
		else {
			localBuffer.append("Passed\n");
			localBuffer.append(logSB.toString());
		}
		localBuffer.append("\t" + errorBuffer + "\n");
		result.append(localBuffer.toString());
	}
}
