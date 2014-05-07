package com.reflexit.magiccards.core.test;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.locale.CardTextLocal;
import com.reflexit.magiccards.core.locale.LocalizedText;

public class CardTextNL1Test extends TestCase {
	public void testGetCardText() {
		CardTextLocal text = CardTextLocal.getCardText(LocalizedText.RUSSIAN);
		assertEquals("Артефакт", text.Type_Artifact);
	}

	public void testGetField() {
		CardTextLocal text = CardTextLocal.getCardText(LocalizedText.RUSSIAN);
		assertEquals("Артефакт", text.getFieldValue("Type_Artifact"));
	}
}
