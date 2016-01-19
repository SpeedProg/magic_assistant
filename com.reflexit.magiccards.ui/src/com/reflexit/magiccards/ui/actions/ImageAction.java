package com.reflexit.magiccards.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class ImageAction extends Action {
	private Runnable run;

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
		super(name, style);
		if (tooltip != null)
			setToolTipText(tooltip);
		if (iconPath != null)
			setImageDescriptor(MagicUIActivator.getImageDescriptor(iconPath));
		this.run = run;
	}

	public ImageAction(String name, String iconKey, int style) {
		this(name, iconKey, null, style, () -> {
			throw new IllegalArgumentException("Runnable is not defined");
		});
	}

	@Override
	public void runWithEvent(Event event) {
		try {
			run();
		} catch (MagicException e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			MagicUIActivator.log(e);
		}
	}

	public Shell getShell() {
		return MagicUIActivator.getShell();
	}

	@Override
	public void run() {
		if (run != null)
			run.run();
	}
}