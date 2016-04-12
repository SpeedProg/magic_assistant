package com.reflexit.magicassistant.swtbot.tests;

import java.util.Collection;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.internal.Assert;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.BeforeClass;

import com.reflexit.magicassistant.swtbot.model.SWTMABot;
import com.reflexit.magicassistant.swtbot.utils.SWTAutomationUtils;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.sync.WebUtils;
import com.reflexit.magiccards.ui.preferences.MagicDbViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.views.lib.DeckView;

public abstract class AbstractSwtBotTest {
	protected static SWTMABot bot;

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTMABot();
		WebUtils.setWorkOffline(true);
	}

	@Before
	public void setUp() {
		bot.resetPrefs();
	}

	protected void removeAllDecks() {
		Collection<CardCollection> allDecks = DataManager.getInstance().getModelRoot().getDeckContainer()
				.getAllElements();
		for (CardCollection cardCollection : allDecks) {
			cardCollection.remove();
		}
	}

	protected void editionsFilter(String setName) {
		IPreferenceStore mdbStore = PreferenceInitializer.getFilterStore(MagicDbViewPreferencePage.PPID);
		mdbStore.setValue(Editions.getInstance().getPrefConstantByName(setName), true);
	}

	public void clickMenuItem(final SWTBotMenu menuItem) {
		menuItem.setFocus();
		menuItem.click();
		bot.sleep(100);
		syncExec(new Runnable() {
			@Override
			public void run() {
				Menu menu = menuItem.widget.getParent();
				SWTAutomationUtils.hideMenuRec(menu);
			}
		});
	}

	public void openFilterShell() {
		bot.activeView().toolbarButton("Opens a Card Filter Dialog").click();
		SWTBotShell shell = bot.shell("Preferences");
		shell.activate();
	}

	public boolean ensureViewIsClosed(String id) {
		try {
			SWTBotView view = bot.viewById(id);
			if (view != null) {
				view.close();
				return true;
			}
		} catch (Exception e) {
			// oki
		}
		return false;
	}

	protected void syncExec(final Runnable runnable) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				runnable.run();
			}
		});
	}

	public SWTBotView createDeck(String deckName) {
		DataManager.getInstance().getLibraryCardStore();
		// create a deck
		bot.menu("File").menu("New...").menu("Deck").click();
		SWTBotShell sshell = bot.shell("New");
		sshell.activate();
		sshell.bot().text().setText(deckName);
		bot.button("Finish").click();
		bot.waitUntil(Conditions.shellCloses(sshell), 1000);
		SWTBotView deckView = bot.viewById(DeckView.ID);
		return deckView;
	}

	public SWTBotTreeItem getFirstRowInViewTree(SWTBotView view) {
		SWTBot dbbot = view.bot();
		SWTBotTree table = dbbot.tree();
		SWTBotTreeItem row = getTreeItem(table, 0);
		return row;
	}

	public SWTBotTreeItem getTreeItem(SWTBotTree tree, int row) {
		int rowCount = tree.rowCount();
		Assert.isLegal(row < rowCount,
				"The row number (" + row + ") is more than the number of rows(" + rowCount + ") in the tree."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return UIThreadRunnable.syncExec(tree.display, new Result<SWTBotTreeItem>() {
			@Override
			public SWTBotTreeItem run() {
				TreeItem[] items = tree.widget.getItems();
				return new SWTBotTreeItem(items[row]);
			}
		});
	}

	public SWTBotTreeItem selectFirstRowInView(SWTBotView view) {
		view.setFocus();
		SWTBot dbbot = view.bot();
		SWTBotTree table = dbbot.tree();
		table.setFocus();
		SWTBotTreeItem row = getTreeItem(table, 0);
		row.select();
		row.setFocus();
		return row;
	}

	public SWTBotTableItem getFirstRowInViewT(SWTBotView view) {
		SWTBot dbbot = view.bot();
		SWTBotTable table = dbbot.table();
		SWTBotTableItem row = table.getTableItem(0);
		return row;
	}

	public SWTBotTableItem selectFirstRowInViewT(SWTBotView view) {
		view.setFocus();
		SWTBot dbbot = view.bot();
		SWTBotTable table = dbbot.table();
		table.setFocus();
		SWTBotTableItem row = table.getTableItem(0);
		row.select();
		row.setFocus();
		return row;
	}
}
