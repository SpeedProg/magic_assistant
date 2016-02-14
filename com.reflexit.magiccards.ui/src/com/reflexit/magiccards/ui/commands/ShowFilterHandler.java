package com.reflexit.magiccards.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.reflexit.magiccards.ui.dialogs.CardFilterDialog;
import com.reflexit.magiccards.ui.views.AbstractCardsView;

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
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart view = HandlerUtil.getActivePart(event);
		if (view instanceof AbstractCardsView) {
			CardFilterDialog cardFilterDialog = ((AbstractCardsView) view).getCardFilterDialog();
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
