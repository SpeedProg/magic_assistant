package com.reflexit.magicassistant.ui.tests;

import static com.windowtester.runtime.swt.locator.SWTLocators.treeItem;

import org.eclipse.swt.widgets.TreeItem;

import com.reflexit.magiccards.ui.views.MagicDbView;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.locator.eclipse.PullDownMenuItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;

public class GroupByCost extends MagicTestCase {
	/**
	 * Main test method.
	 */
	public void testGroupByCost() throws Exception {
		setAlaraRebornInDb();
		ui.click(new PullDownMenuItemLocator("Group By/Cost", new ViewLocator(MagicDbView.ID)));
		expand0();
	}

	protected void expand0() throws WidgetSearchException {
		IWidgetReference errorNode = ((IWidgetReference) getUI().find(treeItem("2 (29)").in(new ViewLocator(MagicDbView.ID))));
		TreeItem errorItem = (TreeItem) errorNode.getWidget();
		if (!isExpanded(errorItem))
			expand(errorNode);
	}
}