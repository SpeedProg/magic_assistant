package com.reflexit.magicassistant.swtbot.tests;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
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
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.BeforeClass;

import com.reflexit.magicassistant.swtbot.utils.SWTAutomationUtils;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.sync.WebUtils;
import com.reflexit.magiccards.ui.preferences.MagicDbViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.views.lib.DeckView;

public abstract class AbstractSwtBotTest {
	protected static SWTWorkbenchBot bot;

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		System.setProperty("junit.testing", "true");
		WebUtils.setWorkOffline(true);
	}

	@Before
	public void setUp() {
		IPreferenceStore mdbStore = PreferenceInitializer.getMdbStore();
		PreferenceInitializer.setToDefault(mdbStore);
		PreferenceInitializer.setToDefault(PreferenceInitializer.getFilterStore(MagicDbViewPreferencePage.PPID));
		mdbStore.setValue(PreferenceConstants.GROUP_FIELD, "");
		IPreferenceStore deckStore = PreferenceInitializer.getDeckStore();
		PreferenceInitializer.setToDefault(deckStore);
		deckStore.setValue(PreferenceConstants.GROUP_FIELD, "");
		try {
			bot.resetWorkbench();
		} catch (Exception e) {
			// ignore
		}
	}

	protected void editionsFilter(String setName) {
		IPreferenceStore mdbStore = PreferenceInitializer.getFilterStore(MagicDbViewPreferencePage.PPID);
		mdbStore.setValue(Editions.getInstance().getPrefConstantByName(setName), true);
	}

	public void clickViewToolBarItemByTooltip(String viewName, String tooltip) {
		SWTBotView viewByTitle = bot.viewByTitle(viewName);
		viewByTitle.show();
		viewByTitle.setFocus();
		// viewByTitle.toolbarButton("Opens a Card Filter Dialog").click();
		clickToolBarItemByTooltip(tooltip);
	}

	public void clickToolBarItemByTooltip(String tooltip) {
		List<SWTBotToolbarButton> toolbarButtons = bot.activeView().getToolbarButtons();
		for (Iterator iterator = toolbarButtons.iterator(); iterator.hasNext();) {
			SWTBotToolbarButton swtBotToolbarButton = (SWTBotToolbarButton) iterator.next();
			if (swtBotToolbarButton.getToolTipText().contains(tooltip)) {
				swtBotToolbarButton.click();
				break;
			}
		}
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

	public SWTBotTreeItem getFirstRowInView(SWTBotView view) {
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
