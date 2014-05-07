package com.reflexit.magicassistant.swtbot.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;

@RunWith(SWTBotJunit4ClassRunner.class)
public class RenameDeck extends AbstractSwtBotTest {
	@Test
	public void testRenameDeck() throws Exception {
		// create a deck
		bot.menu("File").menu("New...").menu("Deck").click();
		bot.shell("").activate();
		bot.sleep(1000);
		bot.text().setText("bbb");
		bot.button("Finish").click();
		bot.sleep(1000);
		// drag a drop card in the new deck
		SWTBotView deckView = bot.viewById(DeckView.ID);
		SWTBotView dbView = bot.viewById(MagicDbView.ID);
		dbView.setFocus();
		bot.sleep(1000);
		SWTBot dbbot = dbView.bot();
		SWTBotTableItem row = dbbot.table().getTableItem(0);
		row.select();
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		String name = row.getText(0);
		String type = row.getText(3);
		KeyboardFactory.getSWTKeyboard().pressShortcut(KeyStroke.getInstance("="));
		// new DndUtil(bot.getDisplay()).dragAndDrop(row, deckView);
		bot.sleep(1000);
		assertEquals(name, deckView.bot().table().getTableItem(0).getText(0));
		String ntype = deckView.bot().table().getTableItem(0).getText(3);
		assertEquals(type, ntype);
		// rename
		SWTBotView navView = bot.viewById(CardsNavigatorView.ID);
		navView.setFocus();
		bot.tree().getTreeItem("My Cards").expand();
		SWTBotTreeItem decks = bot.tree().getTreeItem("My Cards").getNode("Decks");
		decks.expand();
		bot.sleep(3000);
		decks.getNode("bbb (Active)").select();
		bot.sleep(500);
		KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.F2);
		bot.text().setText("ccc");
		bot.button("OK").click();
		deckView = bot.viewById(DeckView.ID);
		deckView.setFocus();
		bot.sleep(500);
		assertEquals(name, deckView.bot().table().getTableItem(0).getText(0));
		assertEquals(type, deckView.bot().table().getTableItem(0).getText(3));
	}
}