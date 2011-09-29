package com.reflexit.magicassistant.ui.tests;

import static com.windowtester.runtime.swt.locator.SWTLocators.treeItem;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.preferences.PrefixedPreferenceStore;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.editions.EditionsComposite;
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
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.PullDownMenuItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;

public class GroupByCost extends UITestCaseSWT {
	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IUIContext ui = getUI();
		ui.ensureThat(new WorkbenchLocator().hasFocus());
		ui.ensureThat(ViewLocator.forName("Welcome").isClosed());
		ui.ensureThat(ViewLocator.forId(DeckView.ID).isClosed());
		PrefixedPreferenceStore mdbStore = (PrefixedPreferenceStore) PreferenceInitializer.getMdbStore();
		mdbStore.setToDefault();
	}

	public void setAlaraRebornInDb() throws Exception {
		IUIContext ui = getUI();
		ui.click(new PullDownMenuItemLocator("Filter...", new ViewLocator(MagicDbView.ID)));
		ui.wait(new ShellShowingCondition("Preferences"));
		// new DebugHelper().printWidgets();
		ui.click(new TreeItemLocator("Set Filter"));
		TreeItemLocator treeItemLocator = new TreeItemLocator(WT.CHECK, "Alara Reborn", new SWTWidgetLocator(Tree.class,
				new SWTWidgetLocator(Composite.class, new SWTWidgetLocator(EditionsComposite.class))));
		IWidgetReference set = (IWidgetReference) ui.find(treeItemLocator);
		TreeItem widget = (TreeItem) set.getWidget();
		if (!isChecked(widget)) {
			ui.click(treeItemLocator);
		}
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Preferences"));
	}

	/**
	 * Main test method.
	 */
	public void testGroupByCost() throws Exception {
		IUIContext ui = getUI();
		setAlaraRebornInDb();
		ui.click(new PullDownMenuItemLocator("Group By/Cost", new ViewLocator(MagicDbView.ID)));
		expand0();
	}

	private void expand0() throws WidgetSearchException {
		IWidgetReference errorNode = ((IWidgetReference) getUI().find(treeItem("2 .*").in(new ViewLocator(MagicDbView.ID))));
		TreeItem errorItem = (TreeItem) errorNode.getWidget();
		if (!isExpanded(errorItem))
			expand(errorNode);
	}

	private void expand(final IWidgetReference widget) throws WidgetSearchException {
		IUIContext ui = getUI();
		ui.click(widget);
		ui.keyClick(WT.ARROW_RIGHT);
		ui.wait(new ICondition() {
			public boolean test() {
				return isExpanded((TreeItem) widget.getWidget());
			}
		});
	}

	private boolean isExpanded(final TreeItem item) {
		final boolean[] expanded = new boolean[1];
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				expanded[0] = item.getExpanded();
			}
		});
		return expanded[0];
	}

	private boolean isChecked(final TreeItem item) {
		final boolean[] checked = new boolean[1];
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				checked[0] = item.getChecked();
			}
		});
		return checked[0];
	}
}