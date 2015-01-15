package com.reflexit.magiccards.core.sync;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ParseGathererPrintedTest extends TestCase {
	private ParseGathererPrinted parser;
	private ICardStore magicDb;

	protected MagicCard load(int id) throws IOException {
		MagicCard card = new MagicCard();
		card.setCardId(id);
		parser.setCard(card);
		parser.setMagicDb(magicDb);
		parser.load(ICoreProgressMonitor.NONE);
		return card;
	}

	@Override
	protected void setUp() {
		parser = new ParseGathererPrinted();
		Set<ICardField> filter = new HashSet<ICardField>();
		filter.add(MagicCardField.NAME);
		filter.add(MagicCardField.TYPE);
		filter.add(MagicCardField.TEXT);
		filter.add(MagicCardField.COLLNUM);
		parser.setFilter(filter);
	}

	public void testLoad() throws IOException {
		MagicCard card = new MagicCard();
		card.setCardId(172550);
		card.setName("Name");
		parser.setCard(card);
		parser.load(ICoreProgressMonitor.NONE);
		assertEquals("Бурав Выжженной Пустоши", card.getName());
	}

	public void testInnistradSide2Russian() throws IOException {
		MagicCard card = new MagicCard();
		card.setCardId(273275);
		card.setName("Wildblood Pack");
		parser.setCard(card);
		parser.load(ICoreProgressMonitor.NONE);
		assertEquals("149b", card.getCollNumber());
		assertEquals("Стая Дикой Крови", card.getName());
	}

	public void testInnistradSide2English() throws IOException {
		MagicCard card = new MagicCard();
		card.setCardId(227419);
		card.setName("Name");
		parser.setCard(card);
		parser.load(ICoreProgressMonitor.NONE);
		assertEquals("149b", card.getCollNumber());
		assertEquals("Wildblood Pack", card.getName());
	}

	public void testDoubleCards() throws IOException {
		magicDb = new MemoryCardStore<IMagicCard>();
		MagicCard card = load(247159);
		assertEquals("198a", card.getCollNumber());
		assertEquals(198, card.getCollectorNumberId());
		// assertEquals("Fire", card.getName());
		// System.err.println(magicDb);
	}
}
