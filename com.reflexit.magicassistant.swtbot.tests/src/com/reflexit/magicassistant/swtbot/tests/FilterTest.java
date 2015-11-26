package com.reflexit.magicassistant.swtbot.tests;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertMatchesRegex;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.reflexit.magiccards.ui.views.MagicDbView;

@RunWith(SWTBotJunit4ClassRunner.class)
public class FilterTest extends AbstractSwtBotTest {
	protected void assertHasType(String pattern, String viewId) {
		bot.viewById(viewId).setFocus();
		String str = bot.table().cell(0, 3);
		assertMatchesRegex(pattern, str);
	}

	@Test
	public void testFilter() throws Exception {
		bot.viewByTitle("MTG Database").setFocus();
		openFilterShell();
		bot.tree().select("Set Filter");
		bot.treeInGroup("Select visible sets").getTreeItem("Alara Reborn").check();
		bot.button("OK").click();
		bot.sleep(2000);
		assertMatchesRegex("Anathemancer", bot.table().cell(0, 0));
	}

	@Test
	public void testFilterType() throws Exception {
		bot.viewByTitle("MTG Database").setFocus();
		openFilterShell();
		bot.tree().select("Basic Filter");
		bot.textWithLabel("Type").setText("Artifact");
		bot.button("OK").click();
		bot.sleep(2000);
		assertHasType("Artifact.*", MagicDbView.ID);
	}

	@Test
	public void testFilterType2() throws Exception {
		bot.viewByTitle("MTG Database").setFocus();
		bot.table().unselect();
		bot.tree().unselect();
		openFilterShell();
		bot.tree().select("Basic Filter");
		bot.checkBox("Artifact").select();
		bot.textWithLabel("Type").setText("Creature");
		bot.button("OK").click();
		bot.sleep(1000);
		assertHasType("Artifact Creature.*", MagicDbView.ID);
	}
}