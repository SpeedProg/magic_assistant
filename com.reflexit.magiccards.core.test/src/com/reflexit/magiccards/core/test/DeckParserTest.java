package com.reflexit.magiccards.core.test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import com.reflexit.magiccards.core.exports.ImportWorker;
import com.reflexit.magiccards.core.exports.ReportType;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.test.assist.MemCardHandler;

public class DeckParserTest extends junit.framework.TestCase {
	private MemCardHandler deck;
	private MagicCardFilter filter;
	private String line = "";
	private ImportWorker worker;
	int resSize;
	IMagicCard card1;
	IMagicCard card2;
	IMagicCard card3;

	@Override
	protected void setUp() throws Exception {
		this.deck = new MemCardHandler();
		this.filter = new MagicCardFilter();
	}

	private ArrayList<IMagicCard> extractStorageCards() {
		ArrayList<IMagicCard> res = new ArrayList<IMagicCard>();
		Iterator<IMagicCard> iterator = this.deck.getCardStore().iterator();
		for (; iterator.hasNext();) {
			IMagicCard card = iterator.next();
			res.add(card);
		}
		return res;
	}

	private void parse() {
		this.worker = new ImportWorker(new ByteArrayInputStream(line.getBytes()), deck, ReportType.TEXT_DECK_CLASSIC,
		        null);
		try {
			worker.runDeckImport(null);
		} catch (InvocationTargetException e) {
			fail(e.getCause().getMessage());
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

	private void addLine(String string) {
		line += string + "\n";
	}

	public void test1_N_x_C() {
		addLine("Counterspell x 2");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(2, ((MagicCardPhisical) card1).getCount());
	}

	public void test2_N_x_C() {
		addLine("Blust x 3");
		addLine("Counterspell x 2");
		parse();
		assertEquals(2, resSize);
		assertEquals("Counterspell", card2.getName());
		assertEquals(2, ((MagicCardPhisical) card2).getCount());
		assertEquals(3, ((MagicCardPhisical) card1).getCount());
	}

	public void test3_N_x_C() {
		addLine("Counterspell (Fifth Edition) x 2");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(2, ((MagicCardPhisical) card1).getCount());
		assertEquals("Fifth Edition", card1.getSet());
	}

	public void test5_N_x_C() {
		addLine("Myr Matrix x 2");
		parse();
		assertEquals(1, resSize);
		assertEquals("Myr Matrix", card1.getName());
		assertEquals(2, ((MagicCardPhisical) card1).getCount());
	}

	public void test4_N_x_C() {
		addLine("Blust X 3");
		addLine("Counterspell x2");
		parse();
		assertEquals(2, resSize);
		assertEquals("Counterspell", card2.getName());
		assertEquals(2, ((MagicCardPhisical) card2).getCount());
		assertEquals(3, ((MagicCardPhisical) card1).getCount());
	}

	public void test1_C_x_N() {
		addLine("2 x Counterspell (Fifth Edition)");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(2, ((MagicCardPhisical) card1).getCount());
		assertEquals("Fifth Edition", card1.getSet());
	}

	public void test2_C_x_N() {
		addLine("2 x Counterspell");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(2, ((MagicCardPhisical) card1).getCount());
	}

	public void test3_C_x_N() {
		addLine("4x Counterspell");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(4, ((MagicCardPhisical) card1).getCount());
	}
}
