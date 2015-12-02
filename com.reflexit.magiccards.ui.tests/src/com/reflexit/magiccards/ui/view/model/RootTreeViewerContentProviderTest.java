package com.reflexit.magiccards.ui.view.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import java.util.Collection;

import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.model.RootTreeViewerContentProvider;

public class RootTreeViewerContentProviderTest extends TreeViewerContentProviderTest {
	@Override
	public void setUp() {
		super.setUp();
		provider = new RootTreeViewerContentProvider();
		provider.inputChanged(null, null, null);
	}

	@Override
	public void testFStore() {
		super.testFStore();
		IFilteredCardStore store = (IFilteredCardStore) input;
		assertArrayEquals(new Object[] { store.getCardGroupRoot() }, provider.getChildren(store));
	}

	@Override
	protected void checkGroup(ICardGroup cardGroup) {
		if (cardGroup == input) {
			int rootSize = cardGroup.size() > 0 ? 1 : 0;
			assertEquals(rootSize, provider.getSize(cardGroup));
			assertEquals(rootSize > 0, provider.hasChildren(cardGroup));
			if (rootSize > 0) {
				Object[] childrenRoots = provider.getChildren(cardGroup);
				Object root = childrenRoots[0];
				assertEquals(root, cardGroup);
			}
			checkInvariant(cardGroup);
		} else {
			super.checkGroup(cardGroup);
		}
	}

	@Override
	protected void checkCollection(Collection treeBoo) {
		if (treeBoo == input) {
			int rootSize = treeBoo.size() > 0 ? 1 : 0;
			assertEquals(rootSize, provider.getSize(treeBoo));
			assertEquals(rootSize > 0, provider.hasChildren(treeBoo));
			if (rootSize > 0) {
				Object[] childrenRoots = provider.getChildren(treeBoo);
				Object root = childrenRoots[0];
				Object[] children = provider.getChildren(root);
				assertArrayEquals(treeBoo.toArray(), children);
			}
			checkInvariant(treeBoo);
		} else {
			super.checkCollection(treeBoo);
		}
	}

	@Override
	public void testLong() {
		// skip
	}
}
