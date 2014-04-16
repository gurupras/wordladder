package com.games.wordladder;

public enum Difficulty {
	EASY (7),
	MEDIUM (11),
	HARD (15),
	;
	
	private int steps;
	
	private Difficulty(int steps) {
		this.steps = steps;
	}
	
	public int getSteps() {
		return steps;
	}
}
