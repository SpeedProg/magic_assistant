package com.reflexit.magiccards.ui.actions;

import org.eclipse.jface.action.IAction;

import com.reflexit.magiccards.ui.MagicUIActivator;

public class ImageAction extends SimpleAction {
	public ImageAction(String name, String iconPath, String tooltip) {
		this(name, iconPath, tooltip, IAction.AS_PUSH_BUTTON, null);
	}

	public ImageAction(String name, String iconPath, Runnable run) {
		this(name, iconPath, null, IAction.AS_PUSH_BUTTON, run);
	}

	public ImageAction(String name, String iconPath, String tooltip, int style) {
		this(name, iconPath, tooltip, style, null);
	}

	public ImageAction(String name, String iconPath, String tooltip, Runnable run) {
		this(name, iconPath, tooltip, IAction.AS_PUSH_BUTTON, run);
	}

	public ImageAction(String name, String iconPath, String tooltip, int style, Runnable run) {
		super(name, tooltip, style, run);
		if (iconPath != null)
			setImageDescriptor(MagicUIActivator.getImageDescriptor(iconPath));
	}

	public ImageAction(String name, String iconKey, int style) {
		this(name, iconKey, null, style, () -> {
			throw new IllegalArgumentException("Runnable is not defined");
		});
	}
}