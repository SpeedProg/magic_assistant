package com.reflexit.magiccards.core.model;

import java.util.HashMap;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.expr.BinaryExpr;
import com.reflexit.magiccards.core.model.expr.Expr;
import com.reflexit.unittesting.CardGenerator;

public class MagicCardFilterTest extends TestCase {
	MagicCardFilter filter;
	private HashMap<String, String> propMap;
	private MagicCard mc;
	private MagicCardPhysical mcp;

	@Override
	public void setUp() {
		propMap = new HashMap<String, String>();
		filter = new MagicCardFilter();
		mc = CardGenerator.generateCardWithValues();
		mcp = mcp();
	}

	private void search(String expr, String text) {
		search(expr, text, true);
	}

	private void search(String expr, String text, boolean exp) {
		Expr e = BinaryExpr.textSearch(MagicCardField.ORACLE, expr);
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
		Expr e = BinaryExpr.textSearch(MagicCardField.TYPE, expr);
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

	public void setQuickFilter(FilterField filterField, String value) {
		propMap.put(filterField.getPrefConstant(), value);
		filter.update(propMap);
	}

	public void setFilterTrue(String... ids) {
		for (int i = 0; i < ids.length; i++) {
			propMap.put(ids[i], "true");
		}
		filter.update(propMap);
	}

	public void genericFieldText(FilterField ff, String value) {
		setQuickFilter(ff, value);
		checkNotFound();
		mcp.set(ff.getField(), value);
		checkFound();
	}

	public void genericFieldTextNot(FilterField ff, String value) {
		setQuickFilter(ff, value);
		checkFound();
		mcp.set(ff.getField(), value);
		checkNotFound();
	}

	public void checkNotFound() {
		checkNotFound(mcp);
	}

	public void checkNotFound(ICard o) {
		assertTrue("Card matches the filter, but should not " + filter + " " + o, filter.isFiltered(o));
	}

	public void testNameBoo() {
		setQuickFilter(FilterField.NAME_LINE, "Boo");
		assertTrue(filter.isFiltered(mc));
		mc.setName("Boo");
		assertFalse(filter.isFiltered(mc));
		mc.setName("Boo Hoo");
		assertFalse(filter.isFiltered(mc));
	}

	public void testNameBooPhy() {
		String boo = "Boo";
		setQuickFilter(FilterField.NAME_LINE, boo);
		mcp.getBase().setName(boo);
		assertEquals(boo, mcp.getName());
		checkFound();
	}

	public void testTEXT_LINE() {
		genericFieldText(FilterField.TEXT_LINE, "Boo");
	}

	public void testTYPE_LINE() {
		genericFieldText(FilterField.TYPE_LINE, "Boo");
	}

	public void testNAME_LINE() {
		genericFieldText(FilterField.NAME_LINE, "Boo");
	}

	public void testPOWER() {
		genericFieldText(FilterField.POWER, 15);
	}

	public void testTOUGHNESS() {
		genericFieldText(FilterField.TOUGHNESS, 22);
	}

	public void testEDITION() {
		FilterField ff = FilterField.EDITION;
		propMap.put(Editions.getInstance().getPrefConstantByName("Alara Reborn"), "true");
		filter.update(propMap);
		checkNotFound();
		mcp.set(ff.getField(), "Alara Reborn");
		checkFound();
	}

	public void testCCC() {
		mcp.set(MagicCardField.COST, "{3}");
		intFieldCheck(FilterField.CCC, 3);
	}

	protected void genericFieldText(FilterField ff, int i) {
		genericFieldText(ff, String.valueOf(i));
		intFieldCheck(ff, i);
	}

	protected void intFieldCheck(FilterField ff, int value) {
		setQuickFilter(ff, String.valueOf(value));
		checkFound();
		setQuickFilter(ff, "= " + value);
		checkFound();
		setQuickFilter(ff, "==" + value);
		checkFound();
		setQuickFilter(ff, ">=" + value);
		checkFound();
		setQuickFilter(ff, "<=" + value);
		checkFound();
		setQuickFilter(ff, "<= " + (value - 1));
		checkNotFound();
		setQuickFilter(ff, ">= 0");
		checkFound();
		setQuickFilter(ff, "=" + value);
		checkFound();
	}

	public void testCOUNT() {
		mcp.setCount(1);
		genericFieldText(FilterField.COUNT, 3);
	}

	public void checkFound() {
		checkFound(mcp);
	}

	public void checkFound(ICard o) {
		assertFalse("Not matching " + filter, filter.isFiltered(o));
	}

	public void testPRICE() {
		genericFieldText(FilterField.PRICE, 2);
	}

	public void testDBPRICE() {
		genericFieldText(FilterField.DBPRICE, 2);
	}

	public void testCOMMUNITYRATING() {
		genericFieldText(FilterField.COMMUNITYRATING, 5);
	}

	public void testCOLLNUM() {
		genericFieldText(FilterField.COLLNUM, 23);
	}

	public void testARTIST() {
		genericFieldText(FilterField.ARTIST, "Boo");
	}

	public void testCOMMENT() {
		genericFieldText(FilterField.COMMENT, "Boo");
	}

	public void testFORMAT() {
		genericFieldText(FilterField.FORMAT, "Standard");
	}

	public void testFORMAT2() {
		FilterField ff = FilterField.FORMAT;
		setQuickFilter(ff, "Standard");
		checkNotFound();
		mcp.set(ff.getField(), "Modern");
		checkNotFound();
	}

	public void testOWNERSHIP() {
		mcp.setOwn(true);
		mcp.setCount(1);
		genericFieldText(FilterField.OWNERSHIP, "false");
	}

	public void testTEXT_LINE_2() {
		genericFieldText(FilterField.TEXT_LINE_2, "Boo");
	}

	public void testTEXT_LINE_3() {
		genericFieldText(FilterField.TEXT_LINE_3, "Boo");
	}

	public void testTEXT_NOT_1() {
		genericFieldTextNot(FilterField.TEXT_NOT_1, "Boo");
	}

	public void testTEXT_NOT_2() {
		genericFieldTextNot(FilterField.TEXT_NOT_2, "Boo");
	}

	public void testTEXT_NOT_3() {
		genericFieldTextNot(FilterField.TEXT_NOT_3, "Boo");
	}

	public void testFORTRADECOUNT() {
		genericFieldText(FilterField.SPECIAL, "fortrade");
	}

	public void testSPECIAL() {
		genericFieldText(FilterField.SPECIAL, "promo");
	}

	public void testLANG() {
		genericFieldText(FilterField.LANG, "French");
	}

	private static final String BLACK_COST = "{B}";
	private static final String RED_COST = "{R}";
	private static final String WHITE_COST = "{W}";

	public void testColorBlack() {
		mcp.getCard().setCost(RED_COST);
		String id = Colors.getInstance().getPrefConstant(Colors.getColorName(BLACK_COST));
		setFilterTrue(id);
		checkNotFound();
		mcp.getCard().setCost(BLACK_COST);
		checkFound();
	}

	String black_id = Colors.getInstance().getPrefConstant(Colors.getColorName(BLACK_COST));
	String red_id = Colors.getInstance().getPrefConstant(Colors.getColorName(RED_COST));

	public MagicCardPhysical mcp() {
		return CardGenerator.generatePhysicalCardWithValues();
	}

	public MagicCardPhysical mcpCost(String cost) {
		MagicCardPhysical b = mcp();
		b.getCard().setCost(cost);
		return b;
	}

	public void testColorBlackOrRed() {
		MagicCardPhysical b = mcpCost(BLACK_COST);
		MagicCardPhysical r = mcpCost(RED_COST);
		MagicCardPhysical w = mcpCost(WHITE_COST);
		MagicCardPhysical wb = mcpCost(WHITE_COST + BLACK_COST);
		setFilterTrue(black_id, red_id);
		checkFound(b);
		checkFound(r);
		checkNotFound(w);
		checkFound(wb);
	}

	public void testColorBlackOrRedOnly() {
		MagicCardPhysical b = mcpCost(BLACK_COST);
		MagicCardPhysical r = mcpCost(RED_COST);
		MagicCardPhysical w = mcpCost(WHITE_COST);
		MagicCardPhysical wb = mcpCost(WHITE_COST + BLACK_COST);
		MagicCardPhysical br = mcpCost(BLACK_COST + RED_COST);
		setFilterTrue(black_id, red_id, ColorTypes.ONLY_ID);
		checkFound(b);
		checkFound(r);
		checkNotFound(w);
		checkNotFound(wb);
		checkFound(br);
		setFilterTrue(black_id, red_id, ColorTypes.ONLY_ID, ColorTypes.AND_ID);
		checkNotFound(b);
		checkNotFound(r);
		checkNotFound(w);
		checkNotFound(wb);
		checkFound(br);
		setFilterTrue(black_id, red_id, ColorTypes.AND_ID);
		MagicCardPhysical wbr = mcpCost(WHITE_COST + BLACK_COST + RED_COST);
		checkNotFound(b);
		checkNotFound(r);
		checkNotFound(w);
		checkNotFound(wb);
		checkFound(br);
		checkFound(wbr);
	}

	public void testColorBlackOrRedIdentity() {
		MagicCardPhysical b = mcpCost(BLACK_COST);
		MagicCardPhysical r = mcpCost(RED_COST);
		MagicCardPhysical w = mcpCost(WHITE_COST);
		MagicCardPhysical wb = mcpCost(WHITE_COST + BLACK_COST);
		MagicCardPhysical br = mcpCost(BLACK_COST + RED_COST);
		setFilterTrue(black_id, red_id, ColorTypes.IDENTITY_ID);
		checkFound(b);
		checkFound(r);
		checkNotFound(w);
		checkNotFound(wb);
		checkFound(br);
		b.set(MagicCardField.ORACLE, "{W} - do something"); // white in text
		checkNotFound(b);
		b.set(MagicCardField.ORACLE, "{R} - do something"); // red
		checkFound(b);
		b.set(MagicCardField.ORACLE, "{W/R} - do something"); // combined cost
		checkNotFound(b);
		b.set(MagicCardField.ORACLE, "Win"); // W but not cost
		checkFound(b);
	}

	public void testColorBlackAndRedIdentity() {
		MagicCardPhysical b = mcpCost(BLACK_COST);
		MagicCardPhysical r = mcpCost(RED_COST);
		MagicCardPhysical w = mcpCost(WHITE_COST);
		MagicCardPhysical wb = mcpCost(WHITE_COST + BLACK_COST);
		MagicCardPhysical br = mcpCost(BLACK_COST + RED_COST);
		MagicCardPhysical brh = mcpCost("{B/R}");
		setFilterTrue(black_id, red_id, ColorTypes.IDENTITY_ID, ColorTypes.AND_ID);
		checkNotFound(b);
		checkNotFound(r);
		checkNotFound(w);
		checkNotFound(wb);
		checkFound(br);
		checkFound(brh);
		br.set(MagicCardField.ORACLE, "{W} - do something"); // white in text
		checkNotFound(br);
		br.set(MagicCardField.ORACLE, "{R} - do something"); // red
		checkFound(br);
		br.set(MagicCardField.ORACLE, "{W/R} - do something"); // combined cost
		checkNotFound(br);
		br.set(MagicCardField.ORACLE, "Win"); // W but not cost
		checkFound(br);
	}

	public void testColorBlackOnly() {
		MagicCardPhysical b = mcpCost(BLACK_COST);
		MagicCardPhysical r = mcpCost(RED_COST);
		MagicCardPhysical w = mcpCost(WHITE_COST);
		MagicCardPhysical wb = mcpCost(WHITE_COST + BLACK_COST);
		MagicCardPhysical br = mcpCost(BLACK_COST + RED_COST);
		setFilterTrue(black_id, ColorTypes.ONLY_ID);
		checkFound(b);
		checkNotFound(r);
		checkNotFound(w);
		checkNotFound(wb);
		checkNotFound(br);
		setFilterTrue(black_id, ColorTypes.ONLY_ID, ColorTypes.AND_ID);
		checkFound(b);
		checkNotFound(r);
		checkNotFound(w);
		checkNotFound(wb);
		checkNotFound(br);
		setFilterTrue(black_id, ColorTypes.AND_ID);
		MagicCardPhysical wbr = mcpCost(WHITE_COST + BLACK_COST + RED_COST);
		checkFound(b);
		checkNotFound(r);
		checkNotFound(w);
		checkFound(wb);
		checkFound(br);
		checkFound(wbr);
	}

	public void testColorlessIdentity() {
		MagicCardPhysical c2 = mcpCost("{2}");
		MagicCardPhysical c1 = mcpCost("{1}");
		MagicCardPhysical c0 = mcpCost("");
		MagicCardPhysical wc = mcpCost("{W}{1}");
		String colorless_id = Colors.getInstance().getPrefConstant(Colors.getColorName("{1}"));
		setFilterTrue(colorless_id, ColorTypes.IDENTITY_ID);
		checkFound(c2);
		checkFound(c1);
		checkFound(c0);
		checkNotFound(wc);
	}
}
