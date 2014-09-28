package com.reflexit.magiccards.core.exports;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class DeckBoxImportTest extends AbstarctImportTest {
	private final DeckBoxImportDelegate importer = new DeckBoxImportDelegate();
	private final DeckBoxExportDelegate exporter = new DeckBoxExportDelegate();
	private ByteArrayOutputStream out;
	private String[] lines;
	private String resline;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		exporter.setReportType(ReportType.createReportType("testdeckbox"));
		out = new ByteArrayOutputStream();
	}

	private void addInvHeader() {
		addLine("Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card Number");
	}

	private void addWishHeader() {
		addLine("Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card Number");
	}

	public void parseCommentAbove() {
		line += getAboveComment();
		parse();
	}

	public void parse() {
		parse(importer);
		deck.getCardStore().removeAll();
		deck.getCardStore().addAll(result);
		deck.update();
	}

	public void export() throws InvocationTargetException, InterruptedException {
		exporter.init(out, true, deck);
		exporter.run(null);
		splitLines();
	}

	public void splitLines() {
		resline = out.toString();
		String x = resline.replaceAll("\\r", "");
		lines = x.split("\n");
	}

// Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card Number
// 4,1,Reya Dawnbringer,,,,,Duel Decks: Divine vs. Demonic,Near Mint,English,13
// 1,0,Angel of Mercy,,,,,,Near Mint,English,
// 3,0,Platinum Angel,,,,,Magic 2010,Near Mint,English,218
	@Test
	public void testGeneric() {
		parseCommentAbove();
		assertNull(importer.getResult().getError());
		assertEquals(3, resSize);
		assertEquals("Reya Dawnbringer", card1.getName());
		assertEquals(4, ((MagicCardPhysical) card1).getCount());
		assertEquals(1, ((MagicCardPhysical) card1).getForTrade());
		assertEquals("Duel Decks: Divine vs. Demonic", card1.getSet());
	}

// Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card Number,Some
// 4,1,Reya Dawnbringer,,,,,Duel Decks: Divine vs. Demonic,Near Mint,English,13,xxx
	@Test
	public void testExtraColumn() {
		parseCommentAbove();
		assertEquals(1, resSize);
		assertEquals("Reya Dawnbringer", card1.getName());
	}

	// Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,
	// 4,1,Reya Dawnbringer,,,,,Duel Decks: Divine vs. Demonic,Near Mint,English
	@Test
	public void testLessColumns() {
		parseCommentAbove();
		assertEquals(1, resSize);
		assertEquals("Reya Dawnbringer", card1.getName());
	}

	public void testFoil() throws InvocationTargetException, InterruptedException {
		addInvHeader();
		addLine("3,0,Platinum Angel,foil,,,,Magic 2010,,English,218");
		parse();
		export();
		assertEquals(line, resline);
	}

	public void testTurn() throws InvocationTargetException, InterruptedException {
		addInvHeader();
		addLine("3,0,Platinum Angel,foil,textless,promo,signed,Magic 2010,Near Mint,English,218");
		parse();
		export();
		assertEquals(line, resline);
	}

	public void testMint() throws InvocationTargetException, InterruptedException {
		addInvHeader();
		addLine("3,0,Platinum Angel,,,,,Magic 2010,Mint,English,218");
		parse();
		export();
		assertEquals(line, resline);
	}
}
