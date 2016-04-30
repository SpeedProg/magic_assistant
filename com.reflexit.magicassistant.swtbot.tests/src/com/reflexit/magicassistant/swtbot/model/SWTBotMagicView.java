package com.reflexit.magicassistant.swtbot.model;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.ui.IViewReference;

import com.reflexit.magiccards.ui.views.Presentation;

public class SWTBotMagicView extends SWTBotView {
	private static KeyStroke esckey;
	static {
		try {
			esckey = KeyStroke.getInstance("ESC");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public SWTBotMagicView(IViewReference partReference, SWTWorkbenchBot bot) {
		super(partReference, bot);
	}

	public void switchPresentation(Presentation pres) {
		bot().cTabItem("Cards").activate();
		setFocus();
		clickToolbarDropDownButtonMenu("View As", pres.getLabel());
	}

	public void clickToolbarDropDownButtonMenu(String buttolTip, String label) {
		SWTBotToolbarDropDownButton groupBy = toolbarDropDownButton(buttolTip);
		final SWTBotMenu menuItem = groupBy.menuItem(label);
		menuItem.setFocus();
		menuItem.click();
		groupBy.pressShortcut(esckey);
	}

	public void syncExec(final Runnable runnable) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				runnable.run();
			}
		});
	}
}
