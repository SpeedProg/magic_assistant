package com.reflexit.magiccards.ui.commands;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.dialogs.CountConfirmationDialog;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class MoveToActiveDeckHandler extends AbstractHandler {
	private static final DataManager DM = DataManager.getInstance();

	/**
	 * The constructor.
	 */
	public MoveToActiveDeckHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		ISelection selection = window.getSelectionService().getSelection();
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			return null;
		}
		String scount = event.getParameter("com.reflexit.magiccards.ui.count");
		int count = -1;
		try {
			count = Integer.parseInt(scount);
		} catch (NumberFormatException e) {
		}
		IStructuredSelection iss = (IStructuredSelection) selection;
		final IFilteredCardStore activeDeckHandler = DataManager.getCardHandler().getActiveDeckHandler();
		if (activeDeckHandler != null) {
			List list = iss.toList();
			try {
				if (count == -1)
					moveCards(list, activeDeckHandler);
				else if (count == 0) {
					new CountConfirmationDialog(window.getShell(), iss) {
						@Override
						protected void runOperation() {
							Map<IMagicCard, Integer> map = getCountMap();
							List<IMagicCard> list = DM.splitCards(map);
							moveCards(list, activeDeckHandler);
						};
					}.open();
					// DataManager.moveCards(list,
					// activeDeckHandler.getLocation());
				} else {
					List<IMagicCard> x = DM.splitCards(list, count);
					moveCards(x, activeDeckHandler);
				}
			} catch (Exception e) {
				MessageDialog.openError(window.getShell(), "Error", e.getLocalizedMessage());
			}
		} else {
			MessageDialog.openError(window.getShell(), "Error", "No active deck, select an active deck by opening it");
		}
		return null;
	}

	private void moveCards(List<IMagicCard> x, final IFilteredCardStore activeDeckHandler) {
		DM.moveCards(x, activeDeckHandler.getLocation());
	}
}
