package com.trie;

import java.util.HashMap;
import java.util.Map;

public class Trie {
	Node root;
	
	public Trie() {
		root = new Node('\0', "");
	}
	
	public Trie(String[] words) {
		root = new Node('\0', "");
		for(String word : words) {
			insert(word.toCharArray());
		}
	}
	
	public void insert(char[] word) {
		Node currNode = root;
		
		for(int i = 0; i < word.length; i++) {
			Node child = currNode.getChild(word[i]);
			if(child == null) {
				child = new Node(word[i], currNode.getData() + word[i]);
				currNode.addChild(child);
			}
			currNode = child;
		}
		currNode.setIsWord(true);
	}
	
	public boolean isValidPrefix(String word) {
		return contains(word.toCharArray(),false);
	}
	
	public boolean isFullWord(String word) {
		return contains(word.toCharArray(),true);
	}
	
	public boolean contains(char[] word, boolean isWord) {
		Node currNode = root;
		for(int i = 0; i < word.length; i++) {
			if(currNode == null) {
				return false;
			}
			currNode = currNode.getChild(word[i]);
		}
		if(currNode == null) {
			return false;
		}
		if(isWord) {
			return currNode.isWord();
		}
		return true;
	}
	
	private class Node {
		private char value;
		private String data;
		private Map<Character, Node> children;
		private boolean isWord;
		
		public Node(char value, String data) {
			this.value = value;
			this.data = data;
			children = new HashMap<Character,Node>();
		}
		
		public boolean isWord() {
			return this.isWord;
		}
		
		public void setIsWord(boolean bool) {
			this.isWord = bool;
		}
		
		public char getValue() {
			return this.value;
		}
		
		public String getData() {
			return this.data;
		}
		
		public void addChild(Node child) {
			if(!this.children.containsKey(child)) {
				children.put(child.getValue(), child);
			}
		}
		
		public boolean hasChild(char c) {
			if(children.containsKey(c)) {
				return true;
			}
			return false;
		}

		public Node getChild(char c) {
			return children.get(c);
		}
	}
}
