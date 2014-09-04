package com.reflexit.magiccards.ui.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class AddToActiveDeckHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public AddToActiveDeckHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information from the application
	 * context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		ISelection selection = window.getSelectionService().getSelection();
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			return null;
		}
		IStructuredSelection iss = (IStructuredSelection) selection;
		IFilteredCardStore activeDeckHandler = DataManager.getInstance().getCardHandler().getActiveDeckHandler();
		if (activeDeckHandler != null) {
			List list = iss.toList();
			try {
				DataManager.getInstance().copyCards(list, activeDeckHandler.getLocation());
			} catch (Exception e) {
				MessageDialog.openError(window.getShell(), "Error", e.getLocalizedMessage());
			}
		} else {
			MessageDialog.openError(window.getShell(), "Error", "No active deck, select an active deck by opening it");
		}
		return null;
	}
}
