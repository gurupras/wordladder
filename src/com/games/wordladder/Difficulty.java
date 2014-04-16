package com.games.wordladder;

public enum Difficulty {
	EASY (7, 3, 5),
	MEDIUM (11, 6, 8),
	HARD (15, 9, 12),
	;
	
	private int maxSteps;
	private int minSteps;
	private int maxWordLength;
	
	private Difficulty(int maxSteps, int minSteps, int maxWordLength) {
		this.maxSteps		= maxSteps;
		this.minSteps		= minSteps;
		this.maxWordLength	= maxWordLength;
	}
	
	public int getMaxSteps() {
		return maxSteps;
	}
	
	public int getMinSteps() {
		return minSteps;
	}
	
	public int getMaxWordLength() {
		return maxWordLength;
	}
}
