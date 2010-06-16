package com.reflexit.magiccards.core.exports;

import org.eclipse.core.runtime.NullProgressMonitor;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.test.assist.MemCardHandler;

public class AbstarctImportTest extends junit.framework.TestCase {
	protected MemCardHandler deck;
	protected String line = "";
	protected int resSize;
	protected IMagicCard card1;
	protected IMagicCard card2;
	protected IMagicCard card3;

	@Override
	protected void setUp() throws Exception {
		this.deck = new MemCardHandler();
	}

	protected ArrayList<IMagicCard> extractStorageCards() {
		ArrayList<IMagicCard> res = new ArrayList<IMagicCard>();
		Iterator<IMagicCard> iterator = this.deck.getCardStore().iterator();
		for (; iterator.hasNext();) {
			IMagicCard card = iterator.next();
			res.add(card);
		}
		return res;
	}

	protected void parse(boolean header, ReportType type) {
		try {
			ImportUtils.performImport(new ByteArrayInputStream(line.getBytes()), type, header, new HashMap(), deck,
			        new NullProgressMonitor());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		ArrayList<IMagicCard> res = extractStorageCards();
		resSize = res.size();
		if (resSize >= 1)
			card1 = res.get(0);
		if (resSize >= 2)
			card2 = res.get(1);
		if (resSize >= 3)
			card3 = res.get(2);
	}

	protected void addLine(String string) {
		line += string + "\n";
	}
}
