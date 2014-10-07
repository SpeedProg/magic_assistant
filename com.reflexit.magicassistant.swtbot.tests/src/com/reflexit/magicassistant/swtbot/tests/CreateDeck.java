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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.reflexit.magicassistant.swtbot.utils.DndUtil;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.lib.DeckView;

@RunWith(SWTBotJunit4ClassRunner.class)
public class CreateDeck extends AbstractSwtBotTest {
	private SWTBotView dbView;
	private SWTBotView deckView;

	@Before
	public void init() {
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		dbView = bot.viewById(MagicDbView.ID);
	}

	public void createDeck(String deckName) {
		DataManager.getInstance().getLibraryCardStore();
		// create a deck
		bot.menu("File").menu("New...").menu("Deck").click();
		bot.shell("").activate();
		bot.sleep(500);
		bot.text().setText(deckName);
		bot.button("Finish").click();
		bot.sleep(500);
		deckView = bot.viewById(DeckView.ID);
	}

	@After
	public void deleteDeck() {
		// XXX
	}

	@Test
	public void testDandD() throws Exception {
		createDeck("deckDnd");
		// drag a drop card in the new deck
		SWTBotTableItem row = selectFirstRowInDb();
		String name = row.getText(0);
		// add card using DND
		new DndUtil(bot.getDisplay()).dragAndDrop(row, deckView);
		bot.sleep(1000);
		assertEquals(name, deckView.bot().table().getTableItem(0).getText(0));
		// delete the card using Delete shortcut
		deckView.bot().table().getTableItem(0).select();
		bot.sleep(500);
		assertEquals("Total 1 cards. Selected 1", bot.label().getText());
		KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.DELETE);
		bot.sleep(500);
		assertEquals("", bot.label().getText());
	}

	public SWTBotTableItem selectFirstRowInDb() {
		dbView.setFocus();
		bot.sleep(200);
		SWTBot dbbot = dbView.bot();
		SWTBotTableItem row = dbbot.table().getTableItem(0);
		row.select();
		return row;
	}

	@Test
	public void testPlus() throws Exception {
		createDeck("deckPlus");
		// drag a drop card in the new deck
		SWTBotTableItem row = selectFirstRowInDb();
		String name = row.getText(0);
		// add card using + shortcut (well = actually)
		KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.toKeys(0, '='));
		bot.sleep(500);
		assertEquals(name, deckView.bot().table().getTableItem(0).getText(0));
	}

	@Test
	public void testCutAndPaste() throws Exception {
		createDeck("deckPaste");
		// drag a drop card in the new deck
		SWTBotTableItem row = selectFirstRowInDb();
		String name = row.getText(0);
		row.setFocus();
		// add card using cut & paste
		KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.toKeys(SWT.CTRL, 'c'));
		bot.sleep(1000);
		deckView.setFocus();
		bot.sleep(1000);
		KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.toKeys(SWT.CTRL, 'v'));
		bot.sleep(1000);
		assertEquals(name, deckView.bot().table().getTableItem(0).getText(0));
		bot.sleep(500);
	}
}