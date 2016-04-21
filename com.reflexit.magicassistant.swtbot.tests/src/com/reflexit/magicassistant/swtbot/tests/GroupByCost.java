package com.reflexit.magicassistant.swtbot.tests;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.reflexit.magicassistant.swtbot.model.SWTBotMagicView;
import com.reflexit.magiccards.ui.views.MagicDbView;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertMatchesRegex;

@RunWith(SWTBotJunit4ClassRunner.class)
public class GroupByCost extends AbstractSwtBotTest {
	@Override
	@Before
	public void setUp() {
		bot.resetWorkbench();
		super.setUp();
		editionsFilter("Alara Reborn");
		bot.sleep(1000);
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
		SWTBotMagicView dbView = bot.viewById(MagicDbView.ID);
		assertMatchesRegex("Anathemancer", bot.table().cell(0, 0));
		dbView.clickToolbarDropDownButtonMenu("Group By", "Cost");
		dbView.show();
		bot.sleep(1000);
		SWTBotTree tree = bot.tree(0);
		// System.err.println(tree.getAllItems()[0].getText());
		tree.expandNode("All (145)", "Cost 2 (29)");
		bot.sleep(1000);
	}
}