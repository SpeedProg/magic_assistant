package com.reflexit.magiccards.core.sync;

import java.io.IOException;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ParseGathererDetailsTest extends TestCase {
	private ParseGathererDetails parser;

	@Override
	protected void setUp() {
		parser = new ParseGathererDetails();
	}

	protected MagicCard load(int id) throws IOException {
		MagicCard card = new MagicCard();
		card.setCardId(id);
		parser.setCard(card);
		parser.load(ICoreProgressMonitor.NONE);
		return card;
	}

	protected MagicCard load(int id, String name) throws IOException {
		MagicCard card = new MagicCard();
		card.setCardId(id);
		card.setName(name);
		parser.setCard(card);
		parser.load(ICoreProgressMonitor.NONE);
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
		MagicCard card = load(273275, "Wildblood Pack");
		assertEquals("149b", card.getCollNumber());
		assertEquals("Wildblood Pack", card.getName());
	}

	public void testFlip() throws IOException {
		MagicCard card = load(78687, "Budoka Gardener");
		assertEquals("202a", card.getCollNumber());
		assertEquals("Budoka Gardener", card.getName());
	}

	public void testDouble() throws IOException {
		MagicCard card = load(126419);
		assertEquals(113, card.getCollectorNumberId());
		assertEquals("Dead", card.getName());
	}

	public void testSlashR() throws IOException {
		MagicCard card = load(366280);
		Object rating = card.get(MagicCardField.RATING);
		assertNotNull(rating);
		assertTrue("Cannot update rating", rating.toString().length() > 0);
		assertEquals(164, Integer.parseInt(card.getCollNumber()));
	}

	public void testText() throws IOException {
		MagicCard card = load(230074);
		assertEquals("86", card.getCollNumber());
		assertEquals("Gut Shot", card.getName());
		assertEquals("<i>({RP} can be paid with either {R} or 2 life.)</i><br>Gut Shot deals 1 damage to target creature or player.",
				card.getText());
	}
}
