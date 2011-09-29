package com.reflexit.magicassistant.ui.tests;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.preferences.PrefixedPreferenceStore;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.condition.HasTextCondition;
import com.windowtester.runtime.swt.UITestCaseSWT;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.LabeledTextLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TableCellLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;

public class FilterType extends UITestCaseSWT {
	private IUIContext ui;

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ui = getUI();
		ui.ensureThat(new WorkbenchLocator().hasFocus());
		ui.ensureThat(ViewLocator.forName("Welcome").isClosed());
		ui.ensureThat(ViewLocator.forId(DeckView.ID).isClosed());
		PrefixedPreferenceStore mdbStore = (PrefixedPreferenceStore) PreferenceInitializer.getMdbStore();
		mdbStore.setToDefault();
	}

	protected void assertHasType(String pattern, String viewId) throws WidgetSearchException {
		TableCellLocator tableItemLocator = new TableCellLocator(1, 5);
		if (viewId != null)
			tableItemLocator.in(new ViewLocator(viewId));
		ui.assertThat(new HasTextCondition(tableItemLocator, pattern));
	}

	public void testFilterType() throws Exception {
		ui.click(new SWTWidgetLocator(ToolItem.class, "", 3, new SWTWidgetLocator(ToolBar.class, 1, new SWTWidgetLocator(Composite.class))));
		ui.wait(new ShellShowingCondition("Preferences"));
		ui.click(new TreeItemLocator("Basic Filter"));
		ui.click(new LabeledTextLocator("Type"));
		ui.enterText("Artifact");
		ui.keyClick(WT.CR);
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Preferences"));
		assertHasType("Artifact.*", MagicDbView.ID);
	}

	public void testFilterType2() throws Exception {
		ui.click(new SWTWidgetLocator(ToolItem.class, "", 3, new SWTWidgetLocator(ToolBar.class, 1, new SWTWidgetLocator(Composite.class))));
		ui.wait(new ShellShowingCondition("Preferences"));
		ui.click(new TreeItemLocator("Basic Filter"));
		ButtonLocator buttonLocator = new ButtonLocator("Artifact");
		if (!buttonLocator.isSelected().testUI(ui)) {
			ui.click(buttonLocator);
		}
		ui.click(new LabeledTextLocator("Type"));
		ui.enterText("Creature");
		ui.keyClick(WT.CR);
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Preferences"));
		assertHasType("Artifact Creature.*", MagicDbView.ID);
	}
}