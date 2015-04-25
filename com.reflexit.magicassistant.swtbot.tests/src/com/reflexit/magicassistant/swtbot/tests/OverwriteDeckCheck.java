package com.reflexit.magicassistant.swtbot.tests;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(SWTBotJunit4ClassRunner.class)
public class OverwriteDeckCheck extends AbstractSwtBotTest {
	/**
	 * Test if creating a new deck twice fails when it already exists.
	 *
	 * @throws Exception
	 */
	@Test
	public void testPreventOverwrite() throws Exception {
		// create a deck
		createDeck("deckOverwrite");
		bot.sleep(1000);
		// try to create again
		bot.menu("File").menu("New...").menu("Deck").click();
		bot.shell("New").activate();
		bot.text().setText("deckOverwrite");
		assertTrue(!bot.button("Finish").isEnabled());
		bot.shell("New").close();
	}
}