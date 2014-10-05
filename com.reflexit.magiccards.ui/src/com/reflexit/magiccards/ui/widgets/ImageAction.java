package com.reflexit.magiccards.ui.widgets;

import org.eclipse.jface.action.Action;

import com.reflexit.magiccards.ui.MagicUIActivator;

public class ImageAction extends Action {
	private Runnable run;

	public ImageAction(String name, String iconKey, int style, Runnable run) {
		super(name, style);
		setImageDescriptor(MagicUIActivator.getImageDescriptor(iconKey));
		setToolTipText(name);
		this.run = run;
	}

	@Override
	public void run() {
		if (run != null)
			run.run();
	}
}