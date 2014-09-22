package com.reflexit.magiccards.core.exports;

import static org.junit.Assert.*;

import org.junit.Test;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class DeckBoxImportTest extends AbstarctImportTest {
	private final DeckBoxImportDelegate worker = new DeckBoxImportDelegate();

	public void parseCommentAbove() {
		line = getAboveComment();
		parse(worker);
	}

// Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card Number
// 4,1,Reya Dawnbringer,,,,,Duel Decks: Divine vs. Demonic,Near Mint,English,13
// 1,0,Angel of Mercy,,,,,,Near Mint,English,
// 3,0,Platinum Angel,,,,,Magic 2010,Near Mint,English,218
	@Test
	public void testGeneric() {
		parseCommentAbove();
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
}
