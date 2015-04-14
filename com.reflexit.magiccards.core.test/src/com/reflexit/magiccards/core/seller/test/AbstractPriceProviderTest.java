package com.reflexit.magiccards.core.seller.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.seller.IPriceProvider;

import static org.junit.Assert.assertNotEquals;

public abstract class AbstractPriceProviderTest extends TestCase {
	private MemoryCardStore<IMagicCard> store;
	protected IPriceProvider parser;
	private ICoreProgressMonitor monitor;

	@Override
	protected void setUp() {
		store = new MemoryCardStore<IMagicCard>();
		monitor = ICoreProgressMonitor.NONE;
		db().initialize();
		setParser(getPriceProvider());
	}

	public IDbCardStore<IMagicCard> db() {
		return DataManager.getInstance().getMagicDBStore();
	}

	protected void setParser(IPriceProvider priceProvider) {
		parser = priceProvider;
		DataManager.getDBPriceStore().setProviderByName(parser.getName());
	}

	protected abstract IPriceProvider getPriceProvider();

	public MagicCard checkcard(String name, String set) {
		MagicCard card2 = findCard(name, set);
		assertNotNull(card2);
		store.add(card2);
		doit();
		store.removeAll();
		System.err.println("Price for " + card2 + " " + centPrice(card2));
		return card2;
	}

	public List<IMagicCard> checkcards(String set) {
		List<IMagicCard> card2 = findCards(set);
		assertNotNull(card2);
		store.addAll(card2);
		doit();
		store.removeAll();
		return card2;
	}

	protected List<IMagicCard> findCards(String set) {
		Collection<IMagicCard> candidates = db().getCards();
		List<IMagicCard> setCards = new ArrayList<IMagicCard>();
		if (candidates != null) {
			for (IMagicCard mc : candidates) {
				if (mc.getSet().equals(set)) {
					setCards.add(mc);
				}
			}
		}
		return setCards;
	}

	protected MagicCard findCard(String name, String set) {
		Collection<IMagicCard> candidates = db().getCandidates(name);
		MagicCard card2 = null;
		if (candidates != null) {
			for (IMagicCard mc : candidates) {
				if (mc.getSet().equals(set)) {
					card2 = (MagicCard) mc;
					break;
				}
			}
		}
		return card2;
	}

	public void doit() {
		try {
			parser.updatePricesAndSync(store, monitor);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testMagic2010() {
		MagicCard card = checkcard("Coat of Arms", "Magic 2010");
		assertNotEquals(0, centPrice(card));
	}

	public void testAether() {
		// 		MagicCard card = checkcard("Æther Shockwave", "Saviors of Kamigawa");
		// latin capital uppercase letter AE (U+00C6)
		MagicCard card = checkcard("\u00C6ther Shockwave", "Saviors of Kamigawa");
		assertNotEquals(0, centPrice(card));
	}

	public void testSixthEdition() {
		MagicCard card = checkcard("Armageddon", "Classic Sixth Edition");
		assertNotEquals(0, centPrice(card));
	}

	public void testFifthEdition() {
		MagicCard card = checkcard("Armageddon", "Fifth Edition");
		assertNotEquals(0, centPrice(card));
	}

	public void testTenthEdition() {
		MagicCard card = checkcard("Arcane Teachings", "Tenth Edition");
		assertNotEquals(0, centPrice(card));
	}

	public void testBulkTenthEdition() {
		List<IMagicCard> cards = checkcards("Tenth Edition");
		assertNotEquals(0, centPrice(cards.get(0)));
	}

	protected int centPrice(IMagicCard mc) {
		return (int) (mc.getDbPrice() * 100);
	}

	public void xtestOneEach() {
		IDbCardStore<IMagicCard> magicDBStore = db();
		HashMap<String, IMagicCard> onemap = new HashMap<String, IMagicCard>();
		HashMap<String, IMagicCard> twomap = new HashMap<String, IMagicCard>();
		for (IMagicCard mc : magicDBStore) {
			String set = mc.getSet();
			if (set.startsWith("Masters"))
				continue;
			if (set.startsWith("Promo"))
				continue;
			if (mc.getCmc() == 0)
				continue;
			if (mc.getCardId() < 0)
				continue;
			if (!onemap.containsKey(set)) {
				onemap.put(set, mc);
			}
			twomap.put(set, mc);
		}
		ArrayList<IMagicCard> cards = new ArrayList<IMagicCard>();
		cards.addAll(onemap.values());
		cards.addAll(twomap.values());
		for (IMagicCard mc : cards) {
			store.removeAll();
			store.add(mc);
			try {
				parser.updatePricesAndSync(store, monitor);
			} catch (IOException e) {
				fail(e.getMessage());
			}
			int centPrice = centPrice(mc);
			if (centPrice == 0)
				System.err.println("Checking " + mc + " -> " + centPrice);
			else
				System.out.println("Checking " + mc + " -> " + centPrice);
			// assertThat("Failed to load price " + mc, 0, is(not(centPrice)));
		}
	}
}
