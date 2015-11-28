package com.reflexit.magiccards.core.model.utils;

import java.util.ArrayList;

import org.junit.Test;

import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.Colors.ManaColor;
import com.reflexit.unittesting.CardGenerator;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

import static org.junit.Assert.assertEquals;

public class CardStoreUtilsTest {
	public MagicCardPhysical mcp() {
		return CardGenerator.generatePhysicalCardWithValues();
	}

	public MagicCardPhysical mcpCost(ManaColor... colors) {
		MagicCardPhysical b = mcp();
		b.getCard().setCost(Colors.getInstance().toCost(colors));
		return b;
	}

	ArrayList<IMagicCard> cards = new ArrayList<>();

	@Test
	public void testEmpty() {
		String buildColors = CardStoreUtils.buildColors(cards);
		assertEquals("", buildColors);
	}

	@Test
	public void testNull() {
		String buildColors = CardStoreUtils.buildColors(null);
		assertEquals("", buildColors);
	}

	@Test
	public void testBuildColors() {
		cards.add(mcpCost(ManaColor.BLACK));
		String buildColors = CardStoreUtils.buildColors(cards);
		assertEquals("{B}", buildColors);
	}

	@Test
	public void testBuildColors2() {
		cards.add(mcpCost(ManaColor.BLACK));
		cards.add(mcpCost(ManaColor.RED));
		String buildColors = CardStoreUtils.buildColors(cards);
		assertEquals("{B}{R}", buildColors);
	}

	@Test
	public void testBuildColorsHyb() {
		cards.add(mcpCost(ManaColor.BLACK, ManaColor.RED));
		cards.add(mcpCost(ManaColor.RED));
		String buildColors = CardStoreUtils.buildColors(cards);
		assertEquals("{B}{R}", buildColors);
	}

	@Test
	public void testBuildColorsLand() {
		cards.add(mcpCost());
		cards.add(mcpCost(ManaColor.RED));
		String buildColors = CardStoreUtils.buildColors(cards);
		assertEquals("{R}", buildColors);
	}

	@Test
	public void testBuildColorsCless() {
		cards.add(mcpCost(ManaColor.COLORLESS));
		cards.add(mcpCost(ManaColor.RED));
		String buildColors = CardStoreUtils.buildColors(cards);
		assertEquals("{R}{1}", buildColors);
	}

	@Test
	public void testBuildColorsClessOnly() {
		cards.add(mcpCost(ManaColor.COLORLESS));
		String buildColors = CardStoreUtils.buildColors(cards);
		assertEquals("{1}", buildColors);
	}

	@Test
	public void testBuildColorsLandOnly() {
		cards.add(mcpCost());
		String buildColors = CardStoreUtils.buildColors(cards);
		assertEquals("", buildColors);
	}
}
