package com.reflexit.magicassistant.swtbot.model;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;

import com.reflexit.magiccards.ui.views.Presentation;

public class SWTBotDeckView extends SWTBotView {
	public SWTBotDeckView(String title, SWTWorkbenchBot bot) {
		super(bot.viewByTitle(title).getReference(), bot);
	}

	public void switchPresentation(Presentation pres) {
		// oolbarButton("View As").contextMenu(pres.getLabel()).click();
		bot().cTabItem("Cards").activate();
		setFocus();
		SWTBotToolbarDropDownButton groupBy = toolbarDropDownButton("View As");
		groupBy.click();
		bot.sleep(30);
		String label = pres.getLabel();
		System.err.println("clicking on " + label);
		final SWTBotMenu menuItem = groupBy.menuItem(label);
		// menuItem.setFocus();
		menuItem.click();
		// bot.sleep(100);
		// syncExec(new Runnable() {
		// @Override
		// public void run() {
		// Menu menu = menuItem.widget.getParent();
		// SWTAutomationUtils.hideMenuRec(menu);
		// }
		// });
	}

	protected void syncExec(final Runnable runnable) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				runnable.run();
			}
		});
	}
}
