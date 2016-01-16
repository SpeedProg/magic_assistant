package com.reflexit.magicassistant.swtbot.tests;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.reflexit.magiccards.ui.dnd.CopySupport;
import com.reflexit.magiccards.ui.views.lib.DeckView;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ImportDeck extends AbstractSwtBotTest {
	private LinkedHashMap<String, Integer> deck = new LinkedHashMap<>();

	@Before
	public void deck() {
		deck.clear();
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

	protected static String getImportData() {
		ImportDeck test = new ImportDeck();
		test.deck();
		String res = "";
		for (String string : test.deck.keySet()) {
			res += string + " x" + test.deck.get(string) + "\n";
		}
		return res;
	}

	@Test
	public void testImport() {
		syncExec(() -> CopySupport.runCopy(getImportData()));
		bot.menu("File").menu("Import...").click();
		SWTBotShell sshell = bot.shell("Import");
		sshell.activate();
		bot.text().setText("Import Dec");
		bot.sleep(300);
		bot.tree().getTreeItem("Other").getNode("Import Deck or Collection").select();
		bot.button("Next >").click();
		bot.radio("Clipboard").click();
		bot.button("Next >").click();
		bot.button("Finish").click();
		bot.viewByTitle("imported").show();
		SWTBotView deckView = bot.viewById(DeckView.ID);
		assertEquals("Grixis Sojourners", getFirstRowInView(deckView).cell(0));
	}
}
