package com.reflexit.magiccards.ui.view.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.ui.views.model.ISizeContentProvider;
import com.reflexit.magiccards.ui.views.model.RootTreeViewerContentProvider;
import com.reflexit.magiccards.ui.views.model.TreeViewerContentProvider;
import com.reflexit.unittesting.CardGenerator;

public class RootTreeViewerContentProviderTest {
	protected RootTreeViewerContentProvider provider;
	protected Object input;

	private Collection<Object> cards(Object... objects) {
		ArrayList res = new ArrayList<>();
		for (Object object : objects) {
			if (object instanceof ICard)
				res.add(object);
			else if (object instanceof String)
				res.add(mcard((String) object));
		}
		return res;
	}

	protected void checkCollection(Collection treeBoo) {
		if (treeBoo == input) {
			int rootSize = treeBoo.size() > 0 ? 1 : 0;
			assertEquals(rootSize, getSize(treeBoo));
			assertEquals(rootSize > 0, provider.hasChildren(treeBoo));
			if (rootSize > 0) {
				Object[] childrenRoots = provider.getChildren(treeBoo);
				Object root = childrenRoots[0];
				Object[] children = provider.getChildren(root);
				assertArrayEquals(treeBoo.toArray(), children);
			}
			checkInvariant(treeBoo);
		} else {
			assertEquals(treeBoo.size(), getSize(treeBoo));
			assertEquals(treeBoo.size() > 0, provider.hasChildren(treeBoo));
			assertArrayEquals(treeBoo.toArray(), provider.getChildren(treeBoo));
			checkInvariant(treeBoo);
		}
	}

	protected void checkEmpty(Object element) {
		assertEquals(0, getSize(element));
		assertEquals(false, provider.hasChildren(element));
		assertArrayEquals(TreeViewerContentProvider.EMPTY_CHILDREN, provider.getChildren(element));
		checkInvariant(element);
	}

	protected void checkGroup(ICardGroup cardGroup) {
		assertEquals(cardGroup.size(), getSize(cardGroup));
		assertEquals(cardGroup.size() > 0, provider.hasChildren(cardGroup));
		assertArrayEquals(cardGroup.getChildren(), provider.getChildren(cardGroup));
		checkInvariant(cardGroup);
	}

	protected void checkInvariant(Object element) {
		Object[] children = provider.getChildren(element);
		assertEquals(children.length > 0, provider.hasChildren(element));
		assertEquals(children.length, getSize(element));
		for (int i = 0; i < children.length; i++) {
			Object child = children[i];
			assertNotNull("Child #" + i + " of " + element + " is null", child);
			assertFalse("Child #" + i + " of " + element + " is self", child.equals(element));
			checkInvariant(child);
		}
	}

	private int getSize(Object element) {
		return ((ISizeContentProvider) provider).getSize(element);
	}

	private CardGroup group(String name, Object... objects) {
		CardGroup res = new CardGroup(null, name);
		for (Object object : objects) {
			if (object instanceof String)
				res.add(group((String) object));
			else if (object instanceof ICard) {
				res.add((ICard) object);
			}
		}
		return res;
	}

	private MagicCard mcard() {
		return CardGenerator.generateCardWithValues();
	}

	private MagicCard mcard(String name) {
		MagicCard mcard = mcard();
		mcard.setName(name);
		return mcard;
	}

	protected void setInput(Object element) {
		input = element;
		provider.inputChanged(null, null, element);
	}

	@Before
	public void setUp() {
		provider = new RootTreeViewerContentProvider();
		provider.inputChanged(null, null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCardGroup() {
		CardGroup cardGroup = group("foo", group("bar"));
		setInput(cardGroup);
	}

	@Test
	public void testCollection() {
		Collection<Object> treeBoo;
		Collection<Object> treeFoo;
		treeBoo = cards("a", "b", treeFoo = cards("c"));
		setInput(treeBoo);
		checkCollection(treeBoo);
		checkCollection(treeFoo);
		checkEmpty("a");
	}

	@Test
	public void testFStore() {
		MemoryFilteredCardStore<ICard> store = new MemoryFilteredCardStore<>();
		store.add(mcard());
		store.add(mcard());
		store.update();
		setInput(store);
		checkInvariant(store);
		checkGroup(store.getCardGroupRoot());
		assertArrayEquals(new Object[] { store.getCardGroupRoot() }, provider.getChildren(store));
	}

	@Test
	public void testGetChildrenNull() {
		assertEquals(0, provider.getChildren(null).length);
	}

	@Test
	public void testGetElementsNull() {
		assertEquals(0, provider.getElements(null).length);
	}

	@Test
	public void testGetSizeNull() {
		assertEquals(0, ((ISizeContentProvider) provider).getSize(null));
	}

	@Test
	public void testHasChildrenNull() {
		assertFalse(provider.hasChildren(null));
	}
}
