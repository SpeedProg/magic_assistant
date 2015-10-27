package com.reflexit.magicassistant.swtbot.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.reflexit.magicassistant.swtbot.utils.DndUtil;
import com.reflexit.magiccards.ui.views.MagicDbView;

@RunWith(SWTBotJunit4ClassRunner.class)
public class CreateDeck extends AbstractSwtBotTest {
	private SWTBotView dbView;
	private SWTBotView deckView;

	@Before
	public void init() {
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		dbView = bot.viewById(MagicDbView.ID);
	}

	@Override
	public SWTBotView createDeck(String deckName) {
		deckView = super.createDeck(deckName);
		return deckView;
	}

	@After
	public void deleteDeck() {
		deckView.close();
		// XXX delete deck
	}

	@Ignore
	public void testDandD() throws Exception {
		createDeck("deckDnd");
		// drag a drop card in the new deck
		SWTBotTableItem dbRow = selectFirstRowInDb();
		String name = dbRow.getText(0);
		// add card using DND
		bot.sleep(1000);
		UIThreadRunnable.syncExec(() -> {
			dbView.bot().table().widget.redraw();
		});

		// dbRow.dragAndDrop(deckView.bot().table());
		new DndUtil(bot.getDisplay()).dragAndDrop(dbRow, deckView);
		bot.sleep(1000);
		SWTBotTableItem row2 = selectFirstRowInView(deckView);
		assertEquals(name, row2.getText(0));
		// delete the card using Delete shortcut
		assertEquals("Total 1 cards. Selected 1", bot.label().getText());
		KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.DELETE);
		bot.sleep(500);
		assertEquals("", bot.label().getText());
	}

	public SWTBotTableItem selectFirstRowInDb() {
		return selectFirstRowInView(dbView);
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
		assertEquals(name, selectFirstRowInView(deckView).getText(0));
	}

	@Test
	public void testCutAndPaste() throws Exception {
		createDeck("deckPaste");
		// drag a drop card in the new deck
		String name = selectFirstRowInDb().getText(0);
		// add card using cut & paste
		if (Platform.getOS().toLowerCase().contains("mac")) {
			KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.toKeys(SWT.COMMAND, 'c'));
			bot.sleep(500);
			deckView.setFocus();
			KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.toKeys(SWT.COMMAND, 'v'));
		} else {
			KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.toKeys(SWT.CTRL, 'c'));
			bot.sleep(500);
			deckView.setFocus();
			KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.toKeys(SWT.CTRL, 'v'));
		}
		assertEquals(name, selectFirstRowInView(deckView).getText(0));
	}
}