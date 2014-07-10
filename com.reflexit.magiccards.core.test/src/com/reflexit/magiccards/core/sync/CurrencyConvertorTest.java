package com.reflexit.magiccards.core.sync;

import java.util.Currency;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class CurrencyConvertorTest extends TestCase {
	Currency CAD = Currency.getInstance("CAD");
	Currency EUR = Currency.getInstance("EUR");
	Currency RUB = Currency.getInstance("RUB");
	Currency USD = Currency.getInstance("USD");

	@Override
	@Before
	public void setUp() {
		CurrencyConvertor.setCurrency(CurrencyConvertor.USD);
	}

	@Test
	public void testGetCurrency() {
		assertEquals(CurrencyConvertor.USD, CurrencyConvertor.getCurrency());
	}

	@Test
	public void testGetCurrency2() {
		CurrencyConvertor.setCurrency(CAD);
		assertEquals(CAD, CurrencyConvertor.getCurrency());
	}

	@Test
	public void testGetRate() {
		assertTrue(CurrencyConvertor.getRate(USD, CAD) > 1.03);
	}

	@Test
	public void testConvertFrom() {
		float res = CurrencyConvertor.convertFromInto(1, CAD, USD);
		assertTrue(res + "", res < 0.95);
	}

	@Test
	public void testConvertInto() {
		assertTrue(CurrencyConvertor.convertFromInto(1, USD, CAD) > 1.03);
	}

	@Test
	public void testConvertFromR() {
		float res = CurrencyConvertor.convertFromInto(100, RUB, USD);
		assertTrue(res + "", res < 3);
	}
}
