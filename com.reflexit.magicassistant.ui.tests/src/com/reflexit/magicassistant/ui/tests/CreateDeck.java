package com.reflexit.magicassistant.ui.tests;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;

import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.preferences.PrefixedPreferenceStore;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.editions.EditionsComposite;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.condition.HasTextCondition;
import com.windowtester.runtime.swt.UITestCaseSWT;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TableCellLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.PullDownMenuItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;

public class CreateDeck extends UITestCaseSWT {
	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IUIContext ui = getUI();
		ui.ensureThat(new WorkbenchLocator().hasFocus());
		ui.ensureThat(ViewLocator.forName("Welcome").isClosed());
		PrefixedPreferenceStore mdbStore = (PrefixedPreferenceStore) PreferenceInitializer.getMdbStore();
		mdbStore.setToDefault();
	}

	/**
	 * Main test method.
	 */
	public void setAlaraRebornInDb() throws Exception {
		IUIContext ui = getUI();
		ui.click(new SWTWidgetLocator(ToolItem.class, "", 3, new SWTWidgetLocator(ToolBar.class, 1, new SWTWidgetLocator(Composite.class))));
		ui.wait(new ShellShowingCondition("Preferences"));
		ui.click(new TreeItemLocator("Set Filter"));
		ui.click(new TreeItemLocator(WT.CHECK, "Alara Reborn", new SWTWidgetLocator(Tree.class, new SWTWidgetLocator(Composite.class,
				new SWTWidgetLocator(EditionsComposite.class)))));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Preferences"));
	}

	public void testAlara() throws Exception {
		setAlaraRebornInDb();
		IUIContext ui = getUI();
		ViewLocator mdbLocator = new ViewLocator(MagicDbView.ID);
		// ui.click(new SWTWidgetLocator(Table.class, mdbLocator));
		TableCellLocator tableItemLocator1 = new TableCellLocator(1, 2).in(mdbLocator);
		ui.click(tableItemLocator1);
		ui.assertThat(new HasTextCondition(tableItemLocator1, "Anathemancer"));
	}

	/**
	 * Main test method.
	 */
	public void testCreateDeck() throws Exception {
		// setAlaraRebornInDb();
		IUIContext ui = getUI();
		ui.click(new PullDownMenuItemLocator("New.../Deck", new ViewLocator(CardsNavigatorView.ID)));
		ui.wait(new ShellShowingCondition(""));
		ui.enterText("aaa");
		ui.click(new ButtonLocator("&Finish"));
		ViewLocator mdbLocator = new ViewLocator(MagicDbView.ID);
		ui.click(new SWTWidgetLocator(Table.class, mdbLocator));
		TableCellLocator tableItemLocator1 = new TableCellLocator(1, 2);
		ui.click(tableItemLocator1);
		String name = tableItemLocator1.getText(ui);
		ui.dragTo(new SWTWidgetLocator(Table.class, new ViewLocator(DeckView.ID)));
		// ui.click(new SWTWidgetLocator(Table.class, new
		// ViewLocator(DeckView.ID)));
		TableCellLocator tableItemLocator = new TableCellLocator(1, 2).in(new ViewLocator(DeckView.ID));
		// ui.assertThat(tableItemLocator.isSelected());
		ui.assertThat(new HasTextCondition(tableItemLocator, name));
	}
}