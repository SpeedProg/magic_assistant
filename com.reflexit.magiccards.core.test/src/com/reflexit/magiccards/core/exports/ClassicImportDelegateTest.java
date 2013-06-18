/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.exports;

import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class ClassicImportDelegateTest extends AbstarctImportTest {
	/*-
	1 Krosan Reclamation
	11x Island
	 */
	private ClassicImportDelegate mimport = new ClassicImportDelegate();

	private void parse() {
		parse(true, mimport);
	}

	public void test1() {
		addLine("1 Krosan Reclamation");
		parse();
		assertEquals(1, resSize);
		assertEquals("Krosan Reclamation", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertFalse(((MagicCardPhysical) card1).isSideboard());
	}

	public void check11Islnad() {
		parse();
		assertEquals(1, resSize);
		assertEquals("Island", card1.getName());
		assertEquals(11, ((MagicCardPhysical) card1).getCount());
		assertFalse(((MagicCardPhysical) card1).isSideboard());
	}

	public void test2() {
		addLine("11x Island");
		check11Islnad();
	}

	public void test3() {
		addLine("11 x Island");
		check11Islnad();
	}

	public void test4() {
		addLine("11 Island");
		check11Islnad();
	}

	public void test5() {
		addLine(" 11  Island");
		check11Islnad();
	}

	public void test6() {
		addLine("Island x 11");
		check11Islnad();
	}
}
