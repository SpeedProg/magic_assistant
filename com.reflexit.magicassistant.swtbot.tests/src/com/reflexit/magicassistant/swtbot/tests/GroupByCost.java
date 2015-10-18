package com.reflexit.magicassistant.swtbot.tests;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.views.MagicDbView;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertMatchesRegex;

import org.eclipse.jface.preference.IPreferenceStore;

@RunWith(SWTBotJunit4ClassRunner.class)
public class GroupByCost extends AbstractSwtBotTest {
	@Override
	@Before
	public void setUp() {
		IPreferenceStore mdbStore = PreferenceInitializer.getMdbStore();
		PreferenceInitializer.setToDefault(mdbStore);
		mdbStore.setValue(Editions.getInstance().getPrefConstantByName("Alara Reborn"), true);
		IPreferenceStore deckStore = PreferenceInitializer.getDeckStore();
		PreferenceInitializer.setToDefault(deckStore);
		bot.resetWorkbench();
		SWTBotView dbView = bot.viewById(MagicDbView.ID);
		dbView.setFocus();
		((MagicDbView) dbView.getViewReference().getView(false)).reloadData();
		bot.sleep(2000);
	}

	/**
	 * Main test method.
	 */
	@Test
	public void testGroupByCost() throws Exception {
		SWTBotView dbView = bot.viewById(MagicDbView.ID);
		assertMatchesRegex("Anathemancer", bot.table().cell(0, 0));
		SWTBotToolbarDropDownButton groupBy = dbView.toolbarDropDownButton("Group By");
		groupBy.click();
		bot.sleep(10);
		final SWTBotMenu menuItem = groupBy.menuItem("Cost");
		clickMenuItem(menuItem);
		dbView.show();
		bot.sleep(1000);
		SWTBotTree tree = bot.tree(0);
		// System.err.println(tree.getAllItems()[0].getText());
		tree.expandNode("All (145)", "2 (29)");
		bot.sleep(1000);
	}
}