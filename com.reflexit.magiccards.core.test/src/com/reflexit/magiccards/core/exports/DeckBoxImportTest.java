package com.reflexit.magiccards.core.exports;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

import static org.junit.Assert.assertNull;

public class DeckBoxImportTest extends AbstarctImportTest {
	private final DeckBoxImportDelegate importer = new DeckBoxImportDelegate();
	private final DeckBoxExportDelegate exporter = new DeckBoxExportDelegate();
	private ByteArrayOutputStream out;
	private String resline;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		exporter.setReportType(ImportExportFactory.createReportType("testdeckbox"));
		out = new ByteArrayOutputStream();
	}

	private void addInvHeader() {
		addLine("Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card Number");
	}

	// private void addWishHeader() {
	// addLine("Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card
	// Number");
	// }

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
		resline = resline.replaceAll("\\r", "");
	}

	// Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card Number
	// 4,0,Reya Dawnbringer,,,,,Duel Decks: Divine vs. Demonic,Near Mint,English,13
	// 1,0,Angel of Mercy,,,,,,Near Mint,English,
	// 3,0,Platinum Angel,,,,,Magic 2010,Near Mint,English,218
	@Test
	public void testGeneric() {
		parseCommentAbove();
		assertNull(importer.getResult().getError());
		assertEquals(3, resSize);
		assertEquals("Reya Dawnbringer", card1.getName());
		assertEquals(4, ((MagicCardPhysical) card1).getCount());
		assertEquals(0, ((MagicCardPhysical) card1).getForTrade());
		assertEquals("Duel Decks: Divine vs. Demonic", card1.getSet());
	}

	// Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card Number,Some
	// 4,0,Reya Dawnbringer,,,,,Duel Decks: Divine vs. Demonic,Near Mint,English,13,xxx
	@Test
	public void testExtraColumn() {
		parseCommentAbove();
		assertEquals(1, resSize);
		assertEquals("Reya Dawnbringer", card1.getName());
	}

	// Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,
	// 4,0,Reya Dawnbringer,,,,,Duel Decks: Divine vs. Demonic,Near Mint,English
	@Test
	public void testLessColumns() {
		parseCommentAbove();
		assertEquals(1, resSize);
		assertEquals("Reya Dawnbringer", card1.getName());
	}

	@Test
	public void testFoil() throws InvocationTargetException, InterruptedException {
		addInvHeader();
		addLine("3,0,Platinum Angel,foil,,,,Magic 2010,,English,218");
		parse();
		export();
		assertEquals(line, resline);
	}

	@Test
	public void testTurn() throws InvocationTargetException, InterruptedException {
		addInvHeader();
		addLine("3,0,Platinum Angel,foil,textless,promo,signed,Magic 2010,Near Mint,English,218");
		parse();
		export();
		assertEquals(line, resline);
	}

	@Test
	public void testMint() throws InvocationTargetException, InterruptedException {
		addInvHeader();
		addLine("3,0,Platinum Angel,,,,,Magic 2010,Mint,English,218");
		parse();
		export();
		assertEquals(line, resline);
	}

	// Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card Number
	// 4,1,Reya Dawnbringer,,,,,Duel Decks: Divine vs. Demonic,Near Mint,English,13
	@Test
	public void testForTrade() {
		parseCommentAbove();
		assertNull(importer.getResult().getError());
		assertEquals("Reya Dawnbringer", card1.getName());
		assertEquals("Duel Decks: Divine vs. Demonic", card1.getSet());
		if (resSize == 1) {
			assertEquals(4, ((MagicCardPhysical) card1).getCount());
			assertEquals(1, ((MagicCardPhysical) card1).getForTrade());
		} else {
			assertEquals(1, ((MagicCardPhysical) card1).getCount());
			assertEquals(1, ((MagicCardPhysical) card1).getForTrade());
			assertEquals(3, ((MagicCardPhysical) card2).getCount());
			assertEquals(0, ((MagicCardPhysical) card2).getForTrade());
		}
	}
	
//Count,Tradelist Count,Name,Edition,Card Number,Condition,Language,Foil,Signed,Artist Proof,Altered Art,Misprint,Promo,Textless,My Price,Type,Cost,Rarity
//1,0,Ashes of the Abhorrent,Ixalan,2,Near Mint,Russian,,,,,,,,0,Enchantment,{1}{W},Rare
//1,0,Bellowing Aegisaur,Ixalan,4,Near Mint,Russian,,,,,,,,0,Creature  - Dinosaur,{5}{W},Uncommon
//2,0,Bishop's Soldier,Ixalan,6,Near Mint,Russian,,,,,,,,0,Creature  - Vampire Soldier,{1}{W},Common
	@Test
	public void testMoreStuff() {
		IMagicCard candidate = getDB().getCard(435153);// 
		// Гнусные Останки
		ImportUtils.loadLanguageForCard("Russian", Collections.singletonList(candidate), getDB(), ICoreProgressMonitor.NONE);
		
		parseCommentAbove();
		assertEquals(3, resSize);
		assertEquals("Гнусные Останки", card1.getName());
		assertEquals("Russian", card1.getLanguage());
	}
}
