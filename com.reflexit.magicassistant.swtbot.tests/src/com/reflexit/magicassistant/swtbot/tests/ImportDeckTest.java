package com.reflexit.magicassistant.swtbot.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import com.reflexit.magicassistant.swtbot.model.SWTBotMagicView;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.ui.dnd.CopySupport;
import com.reflexit.magiccards.ui.views.Presentation;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ImportDeckTest extends AbstractSwtBotTest {
	private LinkedHashMap<String, Integer> deck = new LinkedHashMap<>();
	public @Rule TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void deck() {
		deck.clear();
		add("Grixis Sojourners", 2);
		add("Jund Hackblade", 2);
		add("Firewild Borderpost", 1);
		add("Bant Sureblade", 1);
		add("Swamp", 10);
		add("Forest", 10);
		removeAllDecks();
	}

	private void add(String string, int count) {
		deck.put(string, count);
	}

	protected static String getImportData() {
		ImportDeckTest test = new ImportDeckTest();
		test.deck();
		String res = "";
		for (String string : test.deck.keySet()) {
			res += string + " x" + test.deck.get(string) + "\n";
		}
		return res;
	}

	@Test
	public void testImportFromClipboard() {
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
		SWTBotMagicView deckView = bot.deck("imported");
		deckView.switchPresentation(Presentation.TREE);
		assertEquals("Grixis Sojourners", getFirstRowInViewTree(deckView).cell(0));
	}

	@Test
	public void testImportFromFile() throws IOException {
		File newFile = folder.newFile("im1.txt");
		FileUtils.saveString(getImportData(), newFile);
		bot.menu("File").menu("Import...").click();
		SWTBotShell sshell = bot.shell("Import");
		sshell.activate();
		bot.text().setText("Import Dec");
		bot.sleep(300);
		bot.tree().getTreeItem("Other").getNode("Import Deck or Collection").select();
		bot.button("Next >").click();
		bot.radio("File").click();
		bot.text(1).setText(newFile.getPath());
		bot.sleep(100);
		bot.button("Next >").click();
		bot.button("Finish").click();
		SWTBotMagicView deckView = bot.deck("im1");
		deckView.switchPresentation(Presentation.TREE);
		assertEquals("Grixis Sojourners", getFirstRowInViewTree(deckView).cell(0));
	}
}
