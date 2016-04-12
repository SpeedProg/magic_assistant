package com.reflexit.magiccards.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class SimpleAction extends Action {
	private Runnable run;

	public SimpleAction(Runnable run) {
		this("No Name", null, IAction.AS_PUSH_BUTTON, null);
	}

	public SimpleAction(String name, Runnable run) {
		this(name, name, IAction.AS_PUSH_BUTTON, run);
	}

	public SimpleAction(String name, String tooltip, int style, Runnable run) {
		super(name, style);
		if (tooltip != null)
			setToolTipText(tooltip);
		this.run = run;
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