package com.reflexit.magiccards.ui.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.actions.SelectionProviderAction;

public abstract class AbstractMagicAction extends SelectionProviderAction {
	private Event event;

	protected AbstractMagicAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}

	@Override
	public void runWithEvent(Event event) {
		this.event = event;
		try {
			run();
		} catch (Throwable e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
		}
	}

	protected Shell getShell() {
		return getDisplay().getActiveShell();
	}

	protected Display getDisplay() {
		return getWidget().getDisplay();
	}

	protected Widget getWidget() {
		return getEvent().widget;
	}

	protected Event getEvent() {
		return event;
	}
}
