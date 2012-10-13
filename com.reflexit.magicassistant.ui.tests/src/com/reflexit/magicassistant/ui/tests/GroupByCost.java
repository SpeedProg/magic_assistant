package com.reflexit.magicassistant.ui.tests;

import static com.windowtester.runtime.swt.locator.SWTLocators.treeItem;

import org.eclipse.swt.widgets.TreeItem;

import com.reflexit.magiccards.ui.views.MagicDbView;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
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
		IUIContext ui = getUI();
		ViewLocator magicDbView = new ViewLocator(MagicDbView.ID);
		TreeItemLocator treeItemLocator = treeItem("2 (29)").in(magicDbView);
		IWidgetLocator[] findAll = treeItemLocator.findAll(ui);
		for (int i = 0; i < findAll.length; i++) {
			IWidgetLocator iWidgetLocator = findAll[i];
			System.err.println(iWidgetLocator);
		}
		IWidgetReference treeNode = (IWidgetReference) (ui.find(treeItemLocator));
		TreeItem item = (TreeItem) treeNode.getWidget();
		if (!isExpanded(item))
			expand(treeNode);
	}
}