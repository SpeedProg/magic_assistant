package com.reflexit.magiccards.core.test;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.MagicCardFilter.Expr;

public class MagicCardFilterTest extends TestCase {
	MagicCardFilter filter;

	@Override
	public void setUp() {
		filter = new MagicCardFilter();
	}

	private void search(String expr, String text) {
		search(expr, text, true);
	}

	private void search(String expr, String text, boolean exp) {
		Expr e = MagicCardFilter.textSearch(MagicCardField.ORACLE, expr);
		MagicCard mc = new MagicCard();
		mc.setOracleText(text);
		boolean res = e.evaluate(mc);
		assertEquals(exp, res);
	}

	@Test
	public void testTextSearchWord() {
		search("word", "bla bla word");
		search("word", "bla word bla");
	}

	@Test
	public void testTextSearchCaseIns() {
		search("word", "bla bla Word");
		search("Word", "bla word bla");
	}

	@Test
	public void testTextSearchBound() {
		search("wor", "bla bla Word", false);
	}

	@Test
	public void testTextSearchSym() {
		search("1/1", "1/1 creatures");
		search("1/*", "1/* creatures");
	}

	@Test
	public void testTextSearchNot() {
		search("flying -with", "create with flying", false);
		search("flying -with", "Flying", true);
	}

	@Test
	public void testTextSearchAnd() {
		search("with flying", "create with flying");
		search("with flying", "with fear and flying");
	}

	@Test
	public void testTextSearchQuoted() {
		search("\"with flying\"", "creature with flying");
		search("\"with flying\"", "with fear and flying", false);
	}

	@Test
	public void testTextSearchRegex() {
		search("m/^$/", "");
		search("m/^$/", "with fear and flying", false);
	}

	@Test
	public void testTextSearchRegex2() {
		search("m/\\*/", "1/*");
	}

	private void searchType(String expr, String text, boolean expected) {
		Expr e = MagicCardFilter.textSearch(MagicCardField.TYPE, expr);
		MagicCard mc = new MagicCard();
		mc.setType(text);
		boolean res = e.evaluate(mc);
		assertEquals(expected, res);
	}

	@Test
	public void testBasicType() {
		searchType("Artifact", "Artifact", true);
		searchType("Artifact", "Artifact Creature", true);
	}
}
