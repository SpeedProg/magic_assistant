package com.reflexit.magiccards.core.sync;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.reflexit.magiccards.core.model.MagicCard;

public class ParseGathererRulingsTest extends TestCase {
	private ParseGathererDetails parser;

	@Override
	protected void setUp() {
		parser = new ParseGathererDetails();
	}

	protected MagicCard load(int id) throws IOException {
		MagicCard card = new MagicCard();
		card.setCardId(id);
		parser.setCard(card);
		parser.load(new NullProgressMonitor());
		return card;
	}

	public void testCollNumber() throws IOException {
		MagicCard card = load(191338);
		assertEquals(220, Integer.parseInt(card.getCollNumber()));
	}

	public void testInnistradSide2() throws IOException {
		MagicCard card = load(227419);
		assertEquals("149b", card.getCollNumber());
		assertEquals("Wildblood Pack", card.getName());
		assertEquals("Creature - Werewolf", card.getType());
	}

	public void testInnistradSide1() throws IOException {
		MagicCard card = load(227415);
		assertEquals("149a", card.getCollNumber());
		assertEquals("Instigator Gang", card.getName());
		assertEquals("Creature - Human Werewolf", card.getType());
	}

	public void testInnistradSide2Russian() throws IOException {
		MagicCard card = load(273275);
		assertEquals("149b", card.getCollNumber());
		assertEquals("Wildblood Pack", card.getName());
	}

	public void testFlip() throws IOException {
		MagicCard card = load(78687);
		assertEquals("202", card.getCollNumber());
		assertEquals("Budoka Gardener", card.getName());
	}

	public void testDouble() throws IOException {
		MagicCard card = load(126419);
		assertEquals("113", card.getCollNumber());
		assertEquals("Dead // Gone", card.getName());
	}
}
