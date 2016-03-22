package com.reflexit.magiccards.core.model;

import junit.framework.*;

public class MagicCardTest_failure_1 extends TestCase {

  public static boolean debug = false;

  public void test1() throws Throwable {

    if (debug) System.out.printf("%nMagicCardTest_failure_1.test1");


    com.reflexit.magiccards.core.model.MagicCard var0 = new com.reflexit.magiccards.core.model.MagicCard();
    var0.setText("hi!");
		// int var3 = var0.getUniqueCount();
    com.reflexit.magiccards.core.model.MagicCard var4 = var0.cloneCard();
    
    // Checks the contract:  equals-hashcode on var0 and var4
    assertTrue("Contract failed: equals-hashcode on var0 and var4", var0.equals(var4) ? var0.hashCode() == var4.hashCode() : true);
    
    // Checks the contract:  equals-hashcode on var4 and var0
    assertTrue("Contract failed: equals-hashcode on var4 and var0", var4.equals(var0) ? var4.hashCode() == var0.hashCode() : true);

  }

}
