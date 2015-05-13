package com.reflexit.magicassistant.swtbot.tests;

import java.util.LinkedHashMap;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.collector.CollectorView;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;

@RunWith(SWTBotJunit4ClassRunner.class)
public class EnterDeck extends AbstractSwtBotTest {
	private LinkedHashMap<String, Integer> deck = new LinkedHashMap<>();
	private SWTBotView dbView;
	private SWTBotView collView;

	@Before
	public void init() {
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		dbView = bot.viewById(MagicDbView.ID);
		collView = bot.viewById(CollectorView.ID);
	}

	@Before
	public void deck() {
		deck.clear();
		add("Unbender Tine", 1);
		add("Mistvein Borderpost", 1);
		add("Reborn Hope", 1);
		add("Terminate", 1);
		add("Vithian Renegades", 2);
		add("Talon Trooper", 1);
		add("Ethercaste Knight", 1);
		add("Deadshot Minotaur", 1);
		add("Qasali Pridemage", 2);
		add("Etherium Abomination", 1);
		add("Singe-Mind Ogre", 1);
		add("Mask of Riddles", 1);
		add("Drastic Revelation", 1);
		add("Wildfield Borderpost", 1);
		add("Magefire Wings", 1);
		add("Sewn-Eye Drake", 1);
		add("Architects of Will", 1);
		add("Mind Funeral", 1);
		add("Kathari Bomber", 1);
		add("Sigil of the Nayan Gods", 1);
		add("Sovereigns of Lost Alara", 1);
		add("Cerodon Yearling", 1);
		add("Leonin Armorguard", 1);
		add("Dauntless Escort", 1);
		add("Soul Manipulation", 2);
		add("Vedalken Ghoul", 1);
		add("Deny Reality", 1);
		add("Bloodbraid Elf", 1);
		add("Captured Sunlight", 1);
		add("Nemesis of Reason", 1);
		add("Bant Sojourners", 1);
		add("Esper Sojourners", 2);
		add("Esper Stormblade", 1);
		add("Naya Hushblade", 1);
		add("Grixis Sojourners", 2);
		add("Jund Hackblade", 2);
		add("Firewild Borderpost", 1);
		add("Bant Sureblade", 1);
		add("Swamp", 10);
		add("Forest", 10);
	}

	private void add(String string, int count) {
		deck.put(string, count);
	}

	@Test
	public void enterCollec() throws ParseException {
		// open main
		SWTBotView navView = bot.viewById(CardsNavigatorView.ID);
		navView.setFocus();
		SWTBotTreeItem myCards = navView.bot().tree().getTreeItem("My Cards");
		myCards.expand();
		SWTBotTreeItem decks = myCards.getNode("Collections");
		decks.expand();
		try {
			decks.getNode("main").doubleClick();
		} catch (Exception e) {
			decks.getNode("main (Active)").doubleClick();
		}
		dbView.setFocus();
		for (String name : deck.keySet()) {
			dbView.bot().text().setFocus();
			dbView.bot().text().setText(name);
			bot.sleep(500);
			dbView.bot().table().setFocus();
			selectFirstRowInView(dbView);
			bot.sleep(500);
			//KeyboardFactory.getSWTKeyboard().pressShortcut(KeyStroke.getInstance(0, SWT.INSERT));
			KeyboardFactory.getSWTKeyboard().pressShortcut(KeyStroke.getInstance("m"));
			bot.sleep(500);
			dbView.bot().text().setText("");
		}
		collView.setFocus();
		bot.sleep(500);
	}
}
