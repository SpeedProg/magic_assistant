package com.reflexit.magicassistant.swtbot.tests;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertMatchesRegex;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.reflexit.magicassistant.swtbot.model.SWTBotMagicView;

@RunWith(SWTBotJunit4ClassRunner.class)
public class GalleryTest extends AbstractSwtBotTest {
	protected void assertHasType(String pattern, String viewId) {
		bot.viewById(viewId).setFocus();
		String str = bot.table().cell(0, 3);
		assertMatchesRegex(pattern, str);
	}

	@Test
	public void testFilterType() throws Exception {
		SWTBotMagicView gallery = bot.gallery();
		gallery.setFocus();
		openFilterShell();
		bot.tree().select("Basic Filter");
		bot.textWithLabel("Type").setText("Planeswalker");
		bot.button("OK").click();
		// assertHasType("Planeswalker.*", MagicDbView.ID);
	}

}