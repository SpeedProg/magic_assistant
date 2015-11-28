package com.reflexit.magiccards.core.model;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.anyString;

import static org.mockito.Mockito.*;

import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.sync.TextPrinter;
import com.reflexit.unittesting.CardGenerator;

public class MagicCardTest extends TestCase {
	MagicCard card;
	String original;

	private MagicCard mockRealCards(MagicCardPhysical... mcps) {
		MagicCard card = mcps[0].getBase();
		if (!Mockito.mockingDetails(card).isSpy())
			card = spy(card);
		CardGroup realcards = new CardGroup(MagicCardField.ID, mcps[0].getName());
		for (MagicCardPhysical mcp : mcps) {
			mcp.setMagicCard(card);
			realcards.add(mcp);
		}
		doReturn(realcards).when(card).getRealCards();
		return card;
	}

	@Override
	@Before
	public void setUp() throws Exception {
		card = generateCard();
		original = toExternal();
	}

	@Override
	@After
	public void tearDown() throws Exception {
	}

	public MagicCard generateCard() {
		return CardGenerator.generateCardWithValues();
	}

	private String toExternal() {
		return TextPrinter.getString(card);
	}

	@Test
	public void testGetLegalityMap() {
		assertTrue(card.getLegalityMap().isEmpty());
		assertEquals(original, toExternal());
	}

	@Test
	public void testGetLegalityMap2() {
		LegalityMap legalityMap = card.getLegalityMap();
		card.setLegalityMap(legalityMap.put(Format.STANDARD, Legality.UNKNOWN));
		assertEquals(original, toExternal());
	}

	@Test
	public void testGetLegalityMapBasicLand() {
		card.setCost("");
		card.setType("Basic Land");
		assertTrue(card.getLegalityMap().isLegal(Format.STANDARD));
		// this one would use cached value
		assertTrue(card.getLegalityMap().isLegal(Format.STANDARD));
	}

	@Test
	public void testGetLegalityUnknownSet() {
		card.setSet("Tooglic");
		assertTrue(card.getLegalityMap().isEmpty());
	}

	@Test
	public void testGetNormId() {
		card.setCardId(22);
		card.setEnglishCardId(0);
		assertEquals(22, card.getNormId());
		card.setEnglishCardId(33);
		assertEquals(33, card.getNormId());
		card = new MagicCard();
		assertEquals(0, card.getNormId());
	}

	@Test
	public void testGetPrimeId() {
		card.setName("Bla");
		card.setCardId(22);
		card.setEnglishCardId(0);
		card = spy(card);
		IDbCardStore<IMagicCard> db = mock(IDbCardStore.class);
		when(card.db()).thenReturn(db);
		doReturn(null).when(db).getCard(22);
		assertEquals(22, card.getPrimeId());
		doReturn(card).when(db).getCard(22);
		doReturn(null).when(db).getPrime(anyString());
		assertEquals(22, card.getPrimeId());
		MagicCard two = card.cloneCard();
		two.setCardId(44);
		doReturn(two).when(db).getPrime("Bla");
		assertEquals(44, card.getPrimeId());
	}

	@Test
	public void testClone() {
		card.setLegalityMap(LegalityMap.createFromLegal("Standard"));
		assertTrue(card.getLegalityMap().isLegal(Format.STANDARD));
		MagicCard two = card.cloneCard();
		assertTrue(two.getLegalityMap().isLegal(Format.STANDARD));
		two.setLegalityMap(LegalityMap.createFromLegal("Modern"));
		assertTrue(!two.getLegalityMap().isLegal(Format.STANDARD));
		assertTrue(card.getLegalityMap().isLegal(Format.STANDARD));
	}

	@Test
	public void testGetLocation() {
		// given
		Location loc = Location.valueOf("test");
		MagicCardPhysical mcp = new MagicCardPhysical(card, loc);
		mockRealCards(mcp);
		// when
		Location location = mcp.getBase().getLocation();
		// then
		assertEquals(loc, location);
	}

	@Test
	public void testGetLocationNW() {
		// given
		card = spy(card);
		doReturn(null).when(card).getRealCards();
		// when
		Location location = card.getLocation();
		// then
		assertEquals(Location.NO_WHERE, location);
	}

	@Test
	public void testIsSideboardNull() {
		// given
		card = spy(card);
		doReturn(null).when(card).getRealCards();
		// when
		boolean sideboard = card.isSideboard();
		// then
		assertFalse(sideboard);
	}

	@Test
	public void testIsSideboardNot() {
		// given
		Location loc = Location.valueOf("test");
		MagicCardPhysical mcp = new MagicCardPhysical(card, loc);
		mockRealCards(mcp);
		card = mcp.getBase();
		// when
		boolean sideboard = card.isSideboard();
		// then
		assertFalse(sideboard);
	}

	@Test
	public void testIsSideboardYes() {
		// given
		Location loc = Location.valueOf("test").toSideboard();
		MagicCardPhysical mcp = new MagicCardPhysical(card, loc);
		mockRealCards(mcp);
		card = mcp.getBase();
		// when
		boolean sideboard = card.isSideboard();
		// then
		assertTrue(sideboard);
	}

	@Test
	public void testUniqueCount() {
		// given
		card = mockRealCards(new MagicCardPhysical(card, null));
		// when
		int count = card.getUniqueCount();
		// then
		assertEquals(1, count);
		assertEquals(false, card.isPhysical());
	}

	@Test
	public void testUniqueCount2() {
		// when
		int count = card.getUniqueCount();
		// then
		assertEquals(1, count);
		assertEquals(false, card.isPhysical());
	}
	/*
	 * @Test public void testHashCode() { fail("Not yet implemented"); }
	 *
	 *
	 * @Test public void testGetCount() { fail("Not yet implemented"); }
	 *

	 *
	 * @Test public void testIsPhysical() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetCost() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetCardId() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetOracleText() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetRarity() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetSet() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetType() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetPower() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetToughness() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetColorType() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetCmc() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetDbPrice() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetCommunityRating() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetArtist() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetRulings() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetBase() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetText() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetLanguage() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetEnglishCardId() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetFlipId() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetUniqueCount() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetSide() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetCost() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetCardId() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetEnglishCardId() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetName() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetName() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetOracleText() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetRarity() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetSet() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetType() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetId() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetPower() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetToughness() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetColorType() { fail("Not yet implemented"); }
	 *
	 * @Test public void testEqualsObject() { fail("Not yet implemented"); }
	 *
	 * @Test public void testToString() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetHeaderNames() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetValues() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGet() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetDbPrice() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetCommunityRating() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetArtist() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetRulings() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetLanguage() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetCollNumber() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetCollNumberString() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSet() { fail("Not yet implemented"); }
	 *
	 * @Test public void testClone() { fail("Not yet implemented"); }
	 *
	 * @Test public void testCloneCard() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetEmptyFromCard() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetNonEmptyFromCardMagicCard() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetNonEmptyFromCardSetOfICardFieldMagicCard() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetNonEmptyFromCardICardFieldArrayMagicCard() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetNonEmptyFromCardICardFieldIMagicCard() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetNonEmpty() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetIfEmpty() { fail("Not yet implemented"); }
	 *
	 * @Test public void testIsEmptyValue() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetText() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetCollNumberInt() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetProperties() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetProperties() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetPropertyICardFieldObject() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetPropertyStringObject() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetPropertyICardField() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetPropertyString() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetPart() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetPhysicalCards() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetLocation() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetCount4() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSyntesizeId() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetRealCards() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetImageUrl() { fail("Not yet implemented"); }
	 *
	 * @Test public void testSetLegalityMap() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetString() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetInt() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetFloat() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetBoolean() { fail("Not yet implemented"); }
	 *
	 * @Test public void testConvertFloat() { fail("Not yet implemented"); }
	 *
	 * @Test public void testCloneCard1() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetComment() { fail("Not yet implemented"); }
	 *
	 * @Test public void testIsOwn() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetForTrade() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetSpecial() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetOwnCount() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetOwnUnique() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetOwnTotalAll() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetPrice() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetGathererId() { fail("Not yet implemented"); }
	 *
	 * @Test public void testMatchesICardFieldTextValue() { fail("Not yet implemented"); }
	 *
	 * @Test public void testMatchesIMagicCardICardFieldTextValue() { fail("Not yet implemented"); }
	 *
	 * @Test public void testGetCollectorNumberId() { fail("Not yet implemented"); }
	 *
	 * @Test public void testIsBasicLand() { fail("Not yet implemented"); }
	 *
	 * @Test public void testAccept() { fail("Not yet implemented"); }
	 */
}
