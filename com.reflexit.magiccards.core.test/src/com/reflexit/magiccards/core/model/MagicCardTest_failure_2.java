package com.reflexit.magiccards.core.model;

import junit.framework.*;

public class MagicCardTest_failure_2 extends TestCase {
	public static boolean debug = false;

	public void test1() throws Throwable {
		if (debug)
			System.out.printf("%nMagicCardTest_failure_2.test1");
		com.reflexit.magiccards.core.model.MagicCard var0 = new com.reflexit.magiccards.core.model.MagicCard();
		var0.setName("");
		com.reflexit.magiccards.core.model.MagicCard var7 = new com.reflexit.magiccards.core.model.MagicCard();
		var0.setEmptyFromCard(var7);
		// Checks the contract: equals-hashcode on var7 and var0
		assertTrue("Contract failed: equals-hashcode on var7 and var0",
				var7.equals(var0) ? var7.hashCode() == var0.hashCode() : true);
		// Checks the contract: equals-symmetric on var7 and var0.
		assertTrue("Contract failed: equals-symmetric on var7 and var0.", var7.equals(var0) == var0.equals(var7));
	}
}
