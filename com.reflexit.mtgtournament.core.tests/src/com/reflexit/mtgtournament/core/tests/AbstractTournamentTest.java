package com.reflexit.mtgtournament.core.tests;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.fail;

@FixMethodOrder(MethodSorters.JVM)
public abstract class AbstractTournamentTest {
	protected String getAboveComment() {
		return getContents(1)[0].toString();
	}

	protected StringBuilder[] getContents(int sections) {
		try {
			return TestFileUtils.getContentsForTest("src", getClass(),
					getName(), sections);
		} catch (Exception e) {
			fail(e.getMessage());
			return null;
		}
	}

	@Rule
	public TestName name = new TestName();

	public String getName() {
		return name.getMethodName();
	}
}
