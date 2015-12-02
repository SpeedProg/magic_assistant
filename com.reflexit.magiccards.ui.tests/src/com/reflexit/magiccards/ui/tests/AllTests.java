package com.reflexit.magiccards.ui.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.reflexit.magiccards.ui.view.model.RootTreeViewerContentProviderTest;
import com.reflexit.magiccards.ui.view.model.TreeViewerContentProviderTest;

@RunWith(Suite.class)
@SuiteClasses({ TreeViewerContentProviderTest.class, RootTreeViewerContentProviderTest.class })
public class AllTests {
}
