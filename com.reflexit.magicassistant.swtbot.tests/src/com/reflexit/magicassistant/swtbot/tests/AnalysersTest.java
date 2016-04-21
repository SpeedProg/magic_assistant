package com.reflexit.magicassistant.swtbot.tests;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import com.reflexit.magicassistant.swtbot.model.SWTBotMagicView;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.ui.dnd.CopySupport;
import com.reflexit.magiccards.ui.views.Presentation;
import com.reflexit.magiccards.ui.views.lib.DeckView;

@RunWith(SWTBotJunit4ClassRunner.class)
public class AnalysersTest extends AbstractSwtBotTest {
	private static LinkedHashMap<String, Integer> deck = new LinkedHashMap<>();
	public @Rule TemporaryFolder folder = new TemporaryFolder();
	public static boolean imported = false;

	@BeforeClass
	public static void deck() {
		deck.clear();
		add("Grixis Sojourners", 2);
		add("Jund Hackblade", 2);
		add("Firewild Borderpost", 1);
		add("Bant Sureblade", 1);
		add("Swamp", 10);
		add("Forest", 10);
	}

	@Override
	public void setUp() {
		super.setUp();
		importFromClipboard();
	}

	private static void add(String string, int count) {
		deck.put(string, count);
	}

	protected static String getImportData() {
		AnalysersTest test = new AnalysersTest();
		test.deck();
		String res = "";
		for (String string : test.deck.keySet()) {
			res += string + " x" + test.deck.get(string) + "\n";
		}
		return res;
	}

	public void importFromClipboard() {
		if (imported) {
			final ModelRoot container = DataManager.getInstance().getModelRoot();
			CardCollection col = container.findCardCollectionById("imported");
			DeckView.openCollection(col, null);
			return;
		}
		imported = true;
		removeAllDecks();
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
	public void testMana() {
		SWTBotView deckView = bot.viewByTitle("imported");
		deckView.bot().cTabItem("Mana").activate();
		SWTBotTree tree = bot.tree(0);
		tree.getTreeItem("Mana Curve");
		SWTBotTreeItem two = tree.expandNode("Mana Curve", "2");
		SWTBotTreeItem node = two.getNode("Jund Hackblade");
		assertEquals("2", node.cell(1));
		assertEquals("33.3%", node.cell(2));
	}

	@Test
	public void testCreates() {
		SWTBotView deckView = bot.viewByTitle("imported");
		deckView.bot().cTabItem("Creatures").activate();
		SWTBotTree tree = bot.tree(0);
		SWTBotTreeItem two = tree.expandNode("Creature", "Creature");
		SWTBotTreeItem node = two.getNode("Zombie");
		assertEquals("2", node.cell(1));
		assertEquals("20.0%", node.cell(2));
	}

	@Test
	public void testColors() {
		SWTBotView deckView = bot.viewByTitle("imported");
		deckView.bot().cTabItem("Colors").activate();
		SWTBotTree tree = bot.tree(0);
		SWTBotTreeItem two = tree.expandNode("Colour", "Red-Green");
		SWTBotTreeItem node = two.getNode("Firewild Borderpost");
		assertEquals("1", node.cell(1));
		assertEquals("16.7%", node.cell(2));
	}

	@Test
	public void testLegality() {
		SWTBotView deckView = bot.viewByTitle("imported");
		deckView.bot().cTabItem("Legality").activate();
		SWTBotTree tree = bot.tree(0);
		SWTBotTreeItem two = tree.expandNode("Main Deck");
		SWTBotTreeItem node = two.getNode("Forest");
		assertEquals("10", node.cell(1));
		assertEquals("Standard", node.cell(3));
	}

	@Test
	public void testAbilities() {
		SWTBotView deckView = bot.viewByTitle("imported");
		deckView.bot().cTabItem("Abilities").activate();
		SWTBotTree tree = bot.tree(0);
		SWTBotTreeItem two = tree.expandNode("Ability", "Keyword Ability", "Haste");
		SWTBotTreeItem node = two.getNode("Jund Hackblade");
		assertEquals("2", node.cell(1));
		assertEquals("06.9%", node.cell(2));
	}
}
