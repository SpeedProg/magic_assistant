package com.reflexit.magiccards.core.test;

import java.util.HashMap;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.MagicCardFilter.Expr;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.test.assist.CardGenerator;

public class MagicCardFilterTest extends TestCase {
	MagicCardFilter filter;
	private HashMap propMap;
	private MagicCard mc;
	private MagicCardPhysical mcp;

	@Override
	public void setUp() {
		propMap = new HashMap();
		filter = new MagicCardFilter();
		mc = CardGenerator.generateCardWithValues();
		mcp = CardGenerator.generatePhysicalCardWithValues();
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

	public void setQuickFilter(FilterField filterField, Object value) {
		propMap.put(filterField.getPrefConstant(), value);
		filter.update(propMap);
	}

	public void genericFieldText(FilterField ff, String value) {
		setQuickFilter(ff, value);
		checkNotFound();
		mcp.setObjectByField(ff.getField(), value);
		checkFound();
	}

	public void genericFieldTextNot(FilterField ff, String value) {
		setQuickFilter(ff, value);
		checkFound();
		mcp.setObjectByField(ff.getField(), value);
		checkNotFound();
	}

	public void checkNotFound() {
		assertTrue("Matching " + filter, filter.isFiltered(mcp));
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
		genericFieldText(FilterField.POWER, "22");
	}

	public void testTOUGHNESS() {
		genericFieldText(FilterField.TOUGHNESS, "22");
	}

	public void testCCC() {
		FilterField ff = FilterField.CCC;
		mcp.setObjectByField(MagicCardField.COST, "{3}");
		// Object a = mcp.getObjectByField(ff.getField());
		setQuickFilter(ff, "3");
		checkFound();
	}

	public void testCOUNT() {
		genericFieldText(FilterField.COUNT, "3");
	}

	public void testCOUNTLess() {
		FilterField ff = FilterField.COUNT;
		setQuickFilter(ff, "<=3");
		mcp.setObjectByField(ff.getField(), "3");
		checkFound();
		setQuickFilter(ff, "<=2");
		mcp.setObjectByField(ff.getField(), "3");
		checkNotFound();
	}

	public void checkFound() {
		assertFalse("Not matching " + filter, filter.isFiltered(mcp));
	}

	public void testPRICE() {
		genericFieldText(FilterField.PRICE, "2");
	}

	public void testDBPRICE() {
		genericFieldText(FilterField.DBPRICE, "2");
	}

	public void testCOMMUNITYRATING() {
		genericFieldText(FilterField.COMMUNITYRATING, "5");
	}

	public void testCOLLNUM() {
		genericFieldText(FilterField.COLLNUM, "23");
	}

	public void testARTIST() {
		genericFieldText(FilterField.ARTIST, "Boo");
	}

	public void testCOMMENT() {
		genericFieldText(FilterField.COMMENT, "Boo");
	}

	public void testOWNERSHIP() {
		mcp.setOwn(true);
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
		genericFieldText(FilterField.FORTRADECOUNT, "1");
	}

	public void testSPECIAL() {
		genericFieldText(FilterField.SPECIAL, "foil");
	}

	public void testLANG() {
		genericFieldText(FilterField.LANG, "Boo");
	}
}
