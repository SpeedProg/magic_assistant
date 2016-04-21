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
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.reflexit.magicassistant.swtbot.model.SWTBotMagicView;
import com.reflexit.magicassistant.swtbot.utils.DndUtil;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.Presentation;

@RunWith(SWTBotJunit4ClassRunner.class)
public class CreateDeck extends AbstractSwtBotTest {
	private SWTBotView dbView;
	private SWTBotMagicView deckView;

	@Before
	public void init() {
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		dbView = bot.viewById(MagicDbView.ID);
	}

	@Override
	@Before
	public void setUp() {
		bot.resetWorkbench();
		super.setUp();
		editionsFilter("Alara Reborn");
		SWTBotView dbView = bot.viewById(MagicDbView.ID);
		dbView.setFocus();
		((MagicDbView) dbView.getViewReference().getView(false)).reloadData();
		bot.sleep(1000);
	}

	@Override
	public SWTBotView createDeck(String deckName) {
		super.createDeck(deckName);
		deckView = bot.deck(deckName);
		deckView.switchPresentation(Presentation.TREE);
		return deckView;
	}

	@After
	public void deleteDeck() {
		if (deckView != null)
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
		SWTBotTreeItem row2 = selectFirstRowInView(deckView);
		assertEquals(name, row2.cell(0));
		// delete the card using Delete shortcut
		assertEquals("Total 1 cards. Selected 1", bot.label().getText());
		KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.DELETE);
		bot.sleep(500);
		assertEquals("", bot.label().getText());
	}

	public SWTBotTableItem selectFirstRowInDb() {
		return selectFirstRowInViewT(dbView);
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
		assertEquals(name, selectFirstRowInView(deckView).cell(0));
	}

	@Test
	public void testPlusGallery() throws Exception {
		createDeck("deckPlusGal");
		deckView.switchPresentation(Presentation.GALLERY);
		// drag a drop card in the new deck
		SWTBotTableItem row = selectFirstRowInDb();
		String name = row.getText(0);
		// add card using + shortcut (well = actually)
		KeyboardFactory.getSWTKeyboard().pressShortcut(Keystrokes.toKeys(0, '='));
		bot.sleep(500);
		selectFirstRowInView(deckView).cell(0);
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
		assertEquals(name, selectFirstRowInView(deckView).cell(0));
	}
}