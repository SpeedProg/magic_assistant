package com.reflexit.magiccards.core.model.storage;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.expr.BinaryExpr;
import com.reflexit.magiccards.core.model.expr.Expr;
import junit.framework.TestCase;

@FixMethodOrder(MethodSorters.JVM)
public class PerformanceFilteringTest extends TestCase {
	private IFilteredCardStore fstore;
	private MagicCardFilter filter;

	@Override
	protected void setUp() {
		db().initialize();
		fstore = DataManager.getInstance().getCardHandler().getMagicDBFilteredStoreWorkingCopy();
		filter = fstore.getFilter();
	}

	@Override
	protected void tearDown() throws Exception {
		fstore.clear();
		super.tearDown();
	}

	public IDbCardStore<IMagicCard> db() {
		return DataManager.getInstance().getMagicDBStore();
	}

	@Test
	public void testWarmUp() {
		// warm up JIT
		runfiltering();
	}

	void assertFast(long expected, long actual) {
		String msg = " expected <" + expected + "> but was <" + actual + ">";
		System.out.println(getName() + msg);
		assertTrue("test was slower then expected," + msg, expected >= actual);
	}

	private long time(Runnable runnable, int samples) {
		time(runnable); // warm up
		long sum = 0;
		for (int i = 0; i < samples; i++) {
			sum += time(runnable);
		}
		return sum / samples;
	}

	private long time(Runnable runnable) {
		long start = System.currentTimeMillis();
		runnable.run();
		long end = System.currentTimeMillis();
		long diff = end - start;
		// System.out.println(getName() + " " + diff);
		return diff;
	}

	private long runfiltering() {
		return time(() -> {
			fstore.clear();
			fstore.update();
			fstore.getElements();
		} , 5);
	}

	private Expr textFilter(String string) {
		return BinaryExpr.textSearch(MagicCardField.TEXT, string);
	}

	@Test
	public void testDefaultFilter() {
		assertFast(10, runfiltering());
	}

	@Test
	public void testNameGroupping() {
		this.filter.setSortField(MagicCardField.NAME, true);
		this.filter.setGroupFields(MagicCardField.NAME);
		assertFast(80, runfiltering());
	}

	@Test
	public void testCostGroupping() {
		this.filter.setGroupFields(MagicCardField.COST);
		assertFast(100, runfiltering());
		assertEquals(33, fstore.getCardGroupRoot().size());
	}

	@Test
	public void testSRGroupping() {
		this.filter.setGroupFields(MagicCardField.SET, MagicCardField.RARITY);
		assertFast(60, runfiltering());
		assertEquals(4, fstore.getCardGroupRoot().getSubGroup("Lorwyn").size());
	}

	@Test
	public void testNameGrouppingAndFiltering() {
		this.filter.setSortField(MagicCardField.NAME, true);
		this.filter.setGroupFields(MagicCardField.NAME);
		this.filter.setFilter(textFilter("\"o\""));
		assertFast(90, runfiltering());
		assertTrue("was " + fstore.getSize(), fstore.getSize() > 14000);
		assertTrue("was " + fstore.getSize(), fstore.getSize() < 15000);
		this.filter.setFilter(textFilter("\"ob\""));
		assertFast(90, runfiltering());
		assertTrue("was " + fstore.getSize(), fstore.getSize() >= 260);
		assertTrue("was " + fstore.getSize(), fstore.getSize() < 300);
	}
}
