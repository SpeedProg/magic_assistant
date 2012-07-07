package com.reflexit.magiccards.core.sync;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ParseGathererBasicInfoTest extends TestCase {
	private ParseGathererBasicInfo parser;

	@Override
	protected void setUp() {
		parser = new ParseGathererBasicInfo();
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
		assertEquals("Wildblood Pack", card.getName()); // XXX bug in gatherer
	}
}
