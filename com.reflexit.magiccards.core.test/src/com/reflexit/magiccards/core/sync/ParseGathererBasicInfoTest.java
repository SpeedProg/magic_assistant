package com.reflexit.magiccards.core.sync;

import java.io.IOException;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ParseGathererBasicInfoTest extends TestCase {
	private ParseGathererBasicInfo parser;

	@Override
	protected void setUp() {
		parser = new ParseGathererBasicInfo();
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
