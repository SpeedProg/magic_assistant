package com.reflexit.magiccards.core.exports;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.test.assist.AbstractMagicTest;
import com.reflexit.magiccards.core.test.assist.MemCardHandler;
import com.reflexit.unittesting.CardGenerator;
import com.reflexit.unittesting.TestFileUtils;

@FixMethodOrder(MethodSorters.JVM)
public class AbstarctExportTest extends AbstractMagicTest {
	protected static final DataManager DM = DataManager.getInstance();
	protected MemCardHandler deck;
	protected MagicCardPhysical card1;
	protected MagicCardPhysical card2;
	protected IMagicCard card3;
	protected ByteArrayOutputStream out;
	String[] lines;
	private static boolean reset;

	@Before
	public void setUp() throws Exception {
		if (reset == false) {
			TestFileUtils.resetDb();
			reset = true;
		}
		this.deck = new MemCardHandler();
		this.out = new ByteArrayOutputStream();
		this.card1 = CardGenerator.generatePhysicalCardWithValues();
		this.card2 = CardGenerator.generatePhysicalCardWithValues();
		this.card3 = CardGenerator.generatePhysicalCardWithValues();
		card1.setLocation(deck.getLocation());
		card2.setLocation(deck.getLocation());
		((MagicCardPhysical) card3).setLocation(deck.getLocation());
	}

	public void makeDeck() {
		deck.getCardStore().removeAll();
		if (card1 != null)
			deck.getCardStore().add(card1);
		if (card2 != null)
			deck.getCardStore().add(card2);
		if (card3 != null)
			deck.getCardStore().add(card3);
		deck.update();
	}

	public void run(IExportDelegate dele) {
		makeDeck();
		dele.init(out, true, deck);
		try {
			dele.run(null);
		} catch (InvocationTargetException e) {
			fail(e.getMessage());
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		splitLines();
	}

	public void splitLines() {
		String x = out.toString().replaceAll("\\r", "");
		lines = x.split("\n");
	}
}
