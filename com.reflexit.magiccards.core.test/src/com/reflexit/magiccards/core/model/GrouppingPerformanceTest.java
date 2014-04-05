package com.reflexit.magiccards.core.model;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

import junit.framework.TestCase;

public class GrouppingPerformanceTest extends TestCase {
	@Override
	protected void setUp() throws Exception {
		DataManager.getMagicDBStore().initialize();
	}

	public void testA() {
		// do nothing
	}

	public void testGroupDb() {
		for (int i = 0; i < 10; i++) {
			IFilteredCardStore store = DataManager.getCardHandler().getMagicDBFilteredStore();
			store.getFilter().setGroupField(null);
			store.update();
			store.getFilter().setGroupFields(
					new ICardField[] { MagicCardField.SET_CORE, MagicCardField.SET_BLOCK, MagicCardField.SET, MagicCardField.RARITY });
			store.update();
			ICard[] children = store.getCardGroupRoot().getChildren();
			int count = children[0].getInt(MagicCardField.COUNT);
			// System.err.println(count);
		}
	}
}
