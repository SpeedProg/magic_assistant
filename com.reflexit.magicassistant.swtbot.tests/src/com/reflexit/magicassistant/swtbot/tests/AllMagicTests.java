package com.reflexit.magicassistant.swtbot.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.reflexit.magicassistant.ui.tests.AllUiTests;
import com.reflexit.magiccards.core.test.AllCoreTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({ //
//
SwtBotMagicTests.class, //
		AllUiTests.class, //
		AllCoreTests.class, //
})
public class AllMagicTests {
	// see above
}