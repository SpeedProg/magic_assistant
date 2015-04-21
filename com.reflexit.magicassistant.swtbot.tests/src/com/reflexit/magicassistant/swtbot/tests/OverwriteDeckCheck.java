package com.reflexit.magicassistant.swtbot.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class OverwriteDeckCheck extends AbstractSwtBotTest {
	/**
	 * Test if creating a new deck twice fails when it already exists.
	 * @throws Exception
	 */
	@Test
	public void testPreventOverwrite() throws Exception {
		// create a deck
		bot.menu("File").menu("New...").menu("Deck").click();
		bot.shell("").activate();
		bot.sleep(1000);
		bot.text().setText("deckOverwrite");
		bot.button("Finish").click();
		bot.sleep(1000);
		// try to create again
		bot.menu("File").menu("New...").menu("Deck").click();
		bot.shell("").activate();
		bot.sleep(1000);
		bot.text().setText("deckOverwrite");
		bot.sleep(500);
		assertTrue(!bot.button("Finish").isEnabled());
	}
}