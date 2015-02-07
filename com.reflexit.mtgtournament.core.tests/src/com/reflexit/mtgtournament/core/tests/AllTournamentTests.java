package com.reflexit.mtgtournament.core.tests;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import com.reflexit.mtgtournament.core.edit.CmdAddTableTest;
import com.reflexit.mtgtournament.core.model.ScheduleTest;
import com.reflexit.mtgtournament.core.xml.XmlRegressionTest;

@RunWith(AllTests.class)
public class AllTournamentTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.reflexit.mtgtournament.core.model");
		// $JUnit-BEGIN$
		// juni4
		suite.addTest(new JUnit4TestAdapter(CmdAddTableTest.class));
		suite.addTest(new JUnit4TestAdapter(XmlRegressionTest.class));
		// core
		suite.addTestSuite(ScheduleTest.class);
		return suite;
	}
}