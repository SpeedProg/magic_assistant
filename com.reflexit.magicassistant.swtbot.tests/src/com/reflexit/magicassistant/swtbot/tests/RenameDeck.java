package com.reflexit.magicassistant.swtbot.tests;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;

import static org.junit.Assert.assertEquals;

@RunWith(SWTBotJunit4ClassRunner.class)
public class RenameDeck extends AbstractSwtBotTest {
	@Test
	public void testRenameDeck() throws Exception {
		// create a deck
		createDeck("bbb");
		// drag a drop card in the new deck
		SWTBotView deckView = bot.viewById(DeckView.ID);
		SWTBotView dbView = bot.viewById(MagicDbView.ID);
		SWTBotTableItem row = selectFirstRowInView(dbView);
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		String name = row.getText(0);
		String type = row.getText(3);
		KeyboardFactory.getSWTKeyboard().pressShortcut(KeyStroke.getInstance("="));
		// new DndUtil(bot.getDisplay()).dragAndDrop(row, deckView);
		assertEquals(name, getFirstRowInView(deckView).getText(0));
		String ntype = getFirstRowInView(deckView).getText(3);
		assertEquals(type, ntype);
		// rename
		SWTBotView navView = bot.viewById(CardsNavigatorView.ID);
		navView.setFocus();
		SWTBotTree tree = navView.bot().tree();
		tree.getTreeItem("My Cards").expand();
		SWTBotTreeItem decks = tree.getTreeItem("My Cards").getNode("Decks");
		decks.expand();
		decks.getNode("bbb (Active)").select();
		KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.F2);
		SWTBotShell sshell = bot.shell("Rename");
		sshell.activate();
		bot.text().setText("ccc");
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(sshell), 1000);
		decks.getNode("ccc (Active)").select();
		deckView = bot.viewById(DeckView.ID);
		assertEquals(name, getFirstRowInView(deckView).getText(0));
		assertEquals(type, getFirstRowInView(deckView).getText(3));
	}
}