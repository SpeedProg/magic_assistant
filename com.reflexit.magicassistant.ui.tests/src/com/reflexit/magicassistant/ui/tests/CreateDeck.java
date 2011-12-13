package com.reflexit.magicassistant.ui.tests;

import org.eclipse.swt.widgets.Table;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.condition.HasTextCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TableCellLocator;
import com.windowtester.runtime.swt.locator.eclipse.PullDownMenuItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;

public class CreateDeck extends MagicTestCase {
	public void testAlara() throws Exception {
		setAlaraRebornInDb();
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