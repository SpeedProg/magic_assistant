package com.reflexit.magiccards.core.model.utils;

import java.util.ArrayList;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.expr.BinaryExpr;
import com.reflexit.magiccards.core.model.utils.SearchStringTokenizer.SearchToken;
import com.reflexit.magiccards.core.model.utils.SearchStringTokenizer.TokenType;

public class SearchStringTokenizerTest extends TestCase {
	private SearchStringTokenizer tokenizer;
	private ArrayList<SearchStringTokenizer.SearchToken> tokens = new ArrayList<SearchStringTokenizer.SearchToken>();

	@Override
	public void setUp() {
		tokenizer = new SearchStringTokenizer();
	}

	private int tokenize(String text) {
		tokenizer.init(text);
		SearchToken token;
		BinaryExpr res = null;
		while ((token = tokenizer.nextToken()) != null) {
			tokens.add(token);
		}
		return tokens.size();
	}

	private void assertToken(int i, TokenType type, String string) {
		assertTrue("Not in range " + i + " of " + tokens.size(), i >= 0 && i < tokens.size());
		SearchToken token = tokens.get(i);
		assertNotNull(token);
		assertEquals(type, token.getType());
		assertEquals(string, token.getValue());
	}

	public void testEmpty() {
		assertEquals(0, tokenize(""));
	}

	public void testWords2() {
		assertEquals(2, tokenize("one two"));
		assertToken(0, SearchStringTokenizer.TokenType.WORD, "one");
	}

	public void testWordsM() {
		assertEquals(2, tokenize("mama m"));
		assertToken(0, SearchStringTokenizer.TokenType.WORD, "mama");
		assertToken(1, SearchStringTokenizer.TokenType.WORD, "m");
	}

	public void testRegex() {
		assertEquals(2, tokenize("one m/a.*b/"));
		assertToken(0, SearchStringTokenizer.TokenType.WORD, "one");
		assertToken(1, SearchStringTokenizer.TokenType.REGEX, "a.*b");
	}

	public void testRegex2() {
		assertEquals(2, tokenize("one m/[a-b]c/"));
		assertToken(0, SearchStringTokenizer.TokenType.WORD, "one");
		assertToken(1, SearchStringTokenizer.TokenType.REGEX, "[a-b]c");
	}

	public void testRegexEnd() {
		assertEquals(2, tokenize("one m/[a-b]c"));
		assertToken(0, SearchStringTokenizer.TokenType.WORD, "one");
		assertToken(1, SearchStringTokenizer.TokenType.REGEX, "[a-b]c");
	}

	public void testRegex3() {
		assertEquals(2, tokenize("m/[a-b]c/ one"));
		assertToken(1, SearchStringTokenizer.TokenType.WORD, "one");
		assertToken(0, SearchStringTokenizer.TokenType.REGEX, "[a-b]c");
	}

	public void testQuoted() {
		assertEquals(2, tokenize("one   \"3/3 \""));
		assertToken(0, SearchStringTokenizer.TokenType.WORD, "one");
		assertToken(1, SearchStringTokenizer.TokenType.QUOTED, "3/3 ");
	}

	public void testQuotedUnfinished() {
		assertEquals(1, tokenize("\"aaa"));
		assertToken(0, SearchStringTokenizer.TokenType.QUOTED, "aaa");
	}

	public void testAbi() {
		assertEquals(2, tokenize(" [draw] x"));
		assertToken(0, SearchStringTokenizer.TokenType.ABI, "draw");
		assertToken(1, SearchStringTokenizer.TokenType.WORD, "x");
	}

	public void testAbi2() {
		assertEquals(2, tokenize("x [draw]"));
		assertToken(1, SearchStringTokenizer.TokenType.ABI, "draw");
		assertToken(0, SearchStringTokenizer.TokenType.WORD, "x");
	}

	public void testAbiUnfinished() {
		assertEquals(1, tokenize("[draw"));
		assertToken(0, SearchStringTokenizer.TokenType.ABI, "draw");
	}

	public void testNot() {
		assertEquals(3, tokenize("a - b"));
		assertToken(0, SearchStringTokenizer.TokenType.WORD, "a");
		assertToken(1, SearchStringTokenizer.TokenType.NOT, "-");
		assertToken(2, SearchStringTokenizer.TokenType.WORD, "b");
	}
}
