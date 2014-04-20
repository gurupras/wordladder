package test.trie;

import org.junit.Test;

import com.trie.Trie;

public class TrieTest {
	@Test
	public void test() {
		String[] arr = {"the","mango","tree","is","very","old"}; 
		Trie trie = new Trie(arr);
		System.out.println(trie.isFullWord("is"));
		System.out.println(trie.isValidPrefix("mn"));
		System.out.println(trie.isFullWord("tr"));
		System.out.println(trie.isValidPrefix("ee"));
	}

}
