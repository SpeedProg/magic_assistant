package com.reflexit.magiccards.core.seller.test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import junit.framework.TestCase;

public abstract class AbstractPriceProviderTest extends TestCase {
	private MemoryCardStore<IMagicCard> store;
	protected IPriceProvider parser;
	private ICoreProgressMonitor monitor;

	@Override
	protected void setUp() {
		store = new MemoryCardStore<IMagicCard>();
		parser = getPriceProvider();
		monitor = ICoreProgressMonitor.NONE;
		DataManager.getMagicDBStore().initialize();
		DataManager.getDBPriceStore().setProviderName(parser.getName());
	}

	protected abstract IPriceProvider getPriceProvider();

	public MagicCard addcard(String name, String set) {
		MagicCard card1 = new MagicCard();
		card1.setName(name);
		card1.setSet(set);
		card1.setCardId(1);
		store.add(card1);
		doit();
		return card1;
	}

	public void doit() {
		try {
			parser.updatePricesAndSync(store, monitor);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testgetPriceMed() {
		MagicCard card = addcard("Flameborn Viron", "New Phyrexia");
		assertThat(0, is(not(centPrice(card))));
	}

	public void xtestSwamp() {
		MagicCard card = addcard("Swamp", "Magic 2013");
		assertThat(0, is(not(centPrice(card))));
	}

	public void testMagic2010() {
		MagicCard card = addcard("Coat of Arms", "Magic 2010");
		assertThat(0, is(not(centPrice(card))));
	}

	public void testAether() {
		MagicCard card = addcard("Æther Shockwave", "Saviors of Kamigawa");
		assertThat(0, is(not(centPrice(card))));
	}

	public void testMagic2014() {
		MagicCard card = addcard("Artificer's Hex", "Magic 2014 Core Set");
		assertThat(0, is(not(centPrice(card))));
	}

	public void testSixthEdition() {
		MagicCard card = addcard("Armageddon", "Classic Sixth Edition");
		assertThat(0, is(not(centPrice(card))));
	}

	public void testFifthEdition() {
		MagicCard card = addcard("Armageddon", "Fifth Edition");
		assertThat(0, is(not(centPrice(card))));
	}

	public void testTenthEdition() {
		MagicCard card = addcard("Arcane Teachings", "Tenth Edition");
		assertThat(0, is(not(centPrice(card))));
	}

	protected int centPrice(IMagicCard mc) {
		return (int) (mc.getDbPrice() * 100);
	}

	public void xtestOneEach() {
		IDbCardStore<IMagicCard> magicDBStore = DataManager.getMagicDBStore();
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
