package com.reflexit.magicassistant.swtbot.tests;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.junit.Before;
import org.junit.BeforeClass;

import com.reflexit.magicassistant.swtbot.utils.SWTAutomationUtils;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.preferences.PrefixedPreferenceStore;
import com.reflexit.magiccards.ui.views.lib.DeckView;

public abstract class AbstractSwtBotTest {
	protected static SWTWorkbenchBot bot;

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		System.setProperty("junit.testing", "true");
	}

	@Before
	public void setUp() {
		PrefixedPreferenceStore mdbStore = (PrefixedPreferenceStore) PreferenceInitializer.getMdbStore();
		mdbStore.setToDefault();
		PrefixedPreferenceStore deckStore = (PrefixedPreferenceStore) PreferenceInitializer.getDeckStore();
		deckStore.setToDefault();
		//		try {
		//			bot.resetWorkbench();
		//		} catch (Exception e) {
		//			// ignore
		//		}
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

	public SWTBotTableItem getFirstRowInView(SWTBotView view) {
		SWTBot dbbot = view.bot();
		SWTBotTable table = dbbot.table();
		SWTBotTableItem row = table.getTableItem(0);
		return row;
	}

	public SWTBotTableItem selectFirstRowInView(SWTBotView view) {
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
