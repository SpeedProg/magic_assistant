package com.reflexit.magiccards.ui.views.nav;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;

public class MagicDeckTransferTest extends TestCase {
	private MagicDeckTransfer trans;
	private CardCollection defaultLib;
	private ModelRoot root;

	@Override
	protected void setUp() {
		trans = MagicDeckTransfer.getInstance();
		DataManager.getInstance().reset();
		root = DataManager.getInstance().getModelRoot();
		defaultLib = root.getDefaultLib();
	}

	@Test
	public void testFromByteArray() {
		byte[] byteArray = trans.toByteArray(new CardElement[] { defaultLib });
		CardElement[] res = trans.fromByteArray(byteArray);
		assertEquals(defaultLib.getLocation(), res[0].getLocation());
	}

	public void testOrg() {
		CollectionsContainer decks = this.root.getDeckContainer();
		CollectionsContainer con = decks.addCollectionsContainer("Decks/cox");
		CardElement element = this.root.findElement("Decks/cox");
		assertEquals(con, element);
		byte[] byteArray = trans.toByteArray(new CardElement[] { defaultLib,
				con });
		CardElement[] res = trans.fromByteArray(byteArray);
		assertEquals(con.getLocation(), res[1].getLocation());
	}
}
