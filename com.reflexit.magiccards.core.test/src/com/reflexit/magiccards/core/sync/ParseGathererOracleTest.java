package com.reflexit.magiccards.core.sync;

import java.io.IOException;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ParseGathererOracleTest extends TestCase {
	private ParseGathererOracle parser;
	private ICardStore magicDb;

	@Override
	protected void setUp() {
		parser = new ParseGathererOracle();
		magicDb = new MemoryCardStore<IMagicCard>();
	}

	protected MagicCard load(int id) throws IOException {
		MagicCard card = prep(id);
		parser.load(ICoreProgressMonitor.NONE);
		return card;
	}

	private MagicCard prep(int id) {
		MagicCard card = new MagicCard();
		card.setCardId(id);
		parser.setCard(card);
		parser.setMagicDb(magicDb);
		return card;
	}

	protected MagicCard load(int id, String name) throws IOException {
		MagicCard card = prep(id);
		card.setName(name);
		parser.load(ICoreProgressMonitor.NONE);
		return card;
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

	public void testFlip2() throws IOException {
		MagicCard card = load(-78687);
		assertEquals("202b", card.getCollNumber());
		assertEquals("Dokai, Weaver of Life", card.getName());
	}

	public void testDouble() throws IOException {
		MagicCard card = load(126419);
		card.setName("Dead");
		assertEquals(113, card.getCollectorNumberId());
		assertEquals("Dead", card.getName());
	}

	public void testDouble2() throws IOException {
		MagicCard card = load(126419);
		String origName = "Dead // Gone (Dead)";
		card.setName(origName);
		assertEquals(113, card.getCollectorNumberId());
		assertEquals("113a", card.getCollNumber());
		// oracle should not change name
		assertEquals(origName, card.getName());
	}

	public void testDoubleCards() throws IOException {
		MagicCard card = prep(247159);
		String origName = "Fire // Ice (Fire)";
		card.setName(origName);
		parser.load(ICoreProgressMonitor.NONE);
		assertEquals(origName, card.getName());
		assertEquals("Magic: The Gathering-Commander", card.getSet());
		// FileUtils.saveString(parser.getHtml(), new File("c:/tmp/",
		// card.getCollNumber() + ".html"));
		assertEquals("198a", card.getCollNumber());
		assertEquals(198, card.getCollectorNumberId());
		assertTrue(card.getOracleText().startsWith("Fire deals"));
		// System.err.println(magicDb);
	}

	public void testFlipCards() throws IOException {
		MagicCard card = prep(74671);
		// card.setSet("Magic: The Gathering-Commander");
		parser.load(ICoreProgressMonitor.NONE);
		assertEquals("Cunning Bandit", card.getName());
		// FileUtils.saveString(parser.getHtml(), new File("c:/tmp/", card.getCollNumber() + ".html"));
		assertEquals(99, card.getCollectorNumberId());
		assertEquals("99a", card.getCollNumber());
		// System.err.println(magicDb);
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
		assertEquals(
				"<i>({RP} can be paid with either {R} or 2 life.)</i><br>Gut Shot deals 1 damage to target creature or player.",
				card.getText());
	}

	public void testCollNumber() throws IOException {
		MagicCard card = load(191338);
		assertEquals(220, Integer.parseInt(card.getCollNumber()));
	}

	public void testExpansionParser() throws IOException {
		// Card "Terminate" in Alara Reborn expansion
		// Language English
		MagicCard card = load(176449);
		assertEquals("Alara Reborn", card.getSet());
		// Language Chinese Simplified
		card = load(196433);
		assertEquals("Alara Reborn", card.getSet());
		// Language German
		card = load(196868);
		assertEquals("Alara Reborn", card.getSet());
		// Language Russian
		card = load(195998);
		assertEquals("Alara Reborn", card.getSet());
	}

	public void testPlancechase() throws IOException {
		MagicCard card = load(205371);
		assertEquals("Planechase", card.getSet());
		// 205371: Rorix Bladewing [Planechase]
	}

	public void testSets() throws IOException {
		MagicCard card = load(193767); // Serra Angel
		assertTrue(magicDb.size() > 8);
	}

	public void testColorIndicator() throws IOException {
		MagicCard card = load(221185, "Homicidal Brute");
		assertEquals("47b", card.getCollNumber());
		assertEquals("Homicidal Brute", card.getName());
		assertEquals("Red", card.get(MagicCardField.COLOR_INDICATOR));
	}

	public void testColorIndicatorBG() throws IOException {
		MagicCard card = load(245251, "Garruk, the Veil-Cursed");
		assertEquals("181b", card.getCollNumber());
		assertEquals("Black, Green", card.get(MagicCardField.COLOR_INDICATOR));
		// String icost = Colors.getInstance().getColorIdentityAsCost(card);
		Object object = card.get(MagicCardField.COLOR_IDENTITY);
		assertEquals("{B}{G}", object);
	}
}
