package com.reflexit.magiccards.core.model;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class GrouppingPerformanceTest extends TestCase {
	@Override
	protected void setUp() throws Exception {
		DataManager.getInstance().getMagicDBStore().initialize();
	}

	public void testGroupDb() {
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			IFilteredCardStore store = DataManager.getCardHandler().getMagicDBFilteredStore();
			store.getFilter().setGroupFields(null);
			store.update();
			store.getFilter().setGroupFields(MagicCardField.SET_CORE, MagicCardField.SET_BLOCK, MagicCardField.SET,
					MagicCardField.RARITY);
			store.update();
			ICard[] children = store.getCardGroupRoot().getChildren();
			int count = children[0].getInt(MagicCardField.COUNT);
			// System.err.println(count);
		}
		long end = System.currentTimeMillis();
		long millis = end - start;
		System.err.println("Time " + millis);
		assertTrue("Time was " + millis, millis < 1400);
	}
}
