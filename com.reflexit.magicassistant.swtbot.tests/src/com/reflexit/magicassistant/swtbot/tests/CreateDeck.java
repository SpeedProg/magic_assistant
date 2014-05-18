package com.reflexit.magicassistant.swtbot.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.reflexit.magicassistant.swtbot.utils.DndUtil;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.lib.DeckView;

@RunWith(SWTBotJunit4ClassRunner.class)
public class CreateDeck extends AbstractSwtBotTest {
	@Test
	public void testCreateDeck() throws Exception {
		// create a deck
		bot.menu("File").menu("New...").menu("Deck").click();
		bot.shell("").activate();
		bot.sleep(1000);
		bot.text().setText("aaa");
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
		String name = row.getText(0);
		// add card using DND
		new DndUtil(bot.getDisplay()).dragAndDrop(row, deckView);
		bot.sleep(1000);
		assertEquals(name, deckView.bot().table().getTableItem(0).getText(0));
		// delete the card using Delete shortcut
		deckView.bot().table().getTableItem(0).select();
		bot.sleep(500);
		assertEquals("Total 1 cards. Selected 1", bot.label().getText());
		// add card using + shortcut (well = actually)
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.DELETE);
		bot.sleep(500);
		assertEquals("", bot.label().getText());
		// insert using + shortcut
		dbView.setFocus();
		row = dbbot.table().getTableItem(0);
		row.select();
		KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.toKeys(0, '='));
		bot.sleep(500);
		assertEquals(name, deckView.bot().table().getTableItem(0).getText(0));
		deckView.bot().table().getTableItem(0).select();
		KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.DELETE);
		bot.sleep(500);
		// add card using cut & paste
		dbView.setFocus();
		dbbot.table().getTableItem(0).select();
		KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.toKeys(SWT.CTRL, 'c'));
		deckView.setFocus();
		bot.sleep(500);
		KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.toKeys(SWT.CTRL, 'v'));
		bot.sleep(500);
		assertEquals(name, deckView.bot().table().getTableItem(0).getText(0));
		bot.sleep(500);
	}
}