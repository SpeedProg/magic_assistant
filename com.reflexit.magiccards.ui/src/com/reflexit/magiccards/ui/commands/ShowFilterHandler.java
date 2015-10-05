package com.reflexit.magiccards.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.reflexit.magiccards.ui.dialogs.CardFilterDialog;
import com.reflexit.magiccards.ui.dialogs.MyCardsFilterDialog;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.lib.AbstractMyCardsView;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ShowFilterHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public ShowFilterHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information from the application
	 * context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPart view = HandlerUtil.getActivePart(event);
		IPreferenceStore store;
		if (view instanceof AbstractMyCardsView) {
			store = ((AbstractMyCardsView) view).getFilterPreferenceStore();
			MyCardsFilterDialog cardFilterDialog = new MyCardsFilterDialog(window.getShell(), store);
			if (cardFilterDialog.open() == IStatus.OK) {
				return Status.OK_STATUS;
			} else {
				return Status.CANCEL_STATUS;
			}
		} else if (view instanceof AbstractCardsView) {
			store = ((AbstractCardsView) view).getFilterPreferenceStore();
			CardFilterDialog cardFilterDialog = new CardFilterDialog(window.getShell(), store);
			if (cardFilterDialog.open() == IStatus.OK) {
				return Status.OK_STATUS;
			} else {
				return Status.CANCEL_STATUS;
			}
		}
		return null;
	}

	public static boolean execute() {
		return CommandUtil.executeCommand("com.reflexit.magiccards.ui.commands.filterCommand");
	}
}
