import com.windowtester.runtime.swt.UITestCaseSWT;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.eclipse.PullDownMenuItemLocator;
import com.windowtester.runtime.IUIContext;

public class GrBy extends UITestCaseSWT {
	/* @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		IUIContext ui = getUI();
		ui.ensureThat(new WorkbenchLocator().hasFocus());
		ui.ensureThat(ViewLocator.forName("Welcome").isClosed());
	}

	/**
	 * Main test method.
	 */
	public void testGrBy() throws Exception {
		IUIContext ui = getUI();
		ui.click(new PullDownMenuItemLocator("Group By/Cost", new PullDownMenuItemLocator("Group By/None", new ViewLocator(
				"com.reflexit.magiccards.ui.views.MagicDbView"))));
	}
}