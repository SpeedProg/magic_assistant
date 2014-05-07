package com.reflexit.magicassistant.ui.wt.tests;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.preferences.PrefixedPreferenceStore;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.condition.ICondition;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.UITestCaseSWT;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.FilteredTreeItemLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;

public class MagicTestCase extends UITestCaseSWT {
	protected IUIContext ui;

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setOut(new PrintStream(new ByteArrayOutputStream()));
		ui = getUI();
		ui.ensureThat(new WorkbenchLocator().hasFocus());
		ui.ensureThat(ViewLocator.forName("Welcome").isClosed());
		PrefixedPreferenceStore mdbStore = (PrefixedPreferenceStore) PreferenceInitializer.getMdbStore();
		mdbStore.setToDefault();
		PrefixedPreferenceStore deckStore = (PrefixedPreferenceStore) PreferenceInitializer.getDeckStore();
		deckStore.setToDefault();
		ui.ensureThat(new ViewLocator(DeckView.ID).isClosed());
	}

	public void setAlaraRebornInDb() throws Exception {
		setFilterSet("Alara Reborn");
	}

	public void setFilterSet(String set) throws Exception {
		ui.click(new SWTWidgetLocator(ToolItem.class, "", 3, new SWTWidgetLocator(ToolBar.class, 1, new SWTWidgetLocator(Composite.class))));
		ui.wait(new ShellShowingCondition("Preferences"));
		TreeItemLocator treeItemLocator = new TreeItemLocator("Set Filter");
		ui.click(treeItemLocator);
		FilteredTreeItemLocator treeItem = new FilteredTreeItemLocator(set) {
			{
				setSelectionModifiers(WT.CHECK);
			}
		};
		ui.click(treeItem);
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Preferences"));
	}

	protected void expand(final IWidgetReference widget) throws WidgetSearchException {
		ui.click(widget);
		ui.keyClick(WT.ARROW_RIGHT);
		ui.wait(new ICondition() {
			public boolean test() {
				return isExpanded((TreeItem) widget.getWidget());
			}
		});
	}

	protected boolean isExpanded(final TreeItem item) {
		final boolean[] expanded = new boolean[1];
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				expanded[0] = item.getExpanded();
			}
		});
		return expanded[0];
	}

	protected boolean isChecked(final TreeItem item) {
		final boolean[] checked = new boolean[1];
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				checked[0] = item.getChecked();
			}
		});
		return checked[0];
	}
}
