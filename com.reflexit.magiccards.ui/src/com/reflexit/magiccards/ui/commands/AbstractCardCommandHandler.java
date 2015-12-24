package com.reflexit.magiccards.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.lib.MyCardsView;

public abstract class AbstractCardCommandHandler extends AbstractHandler {
	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IStructuredSelection iss = getStructuredSelection(window);
		if (iss == null)
			return null;
		try {
			run(window, iss);
		} catch (MagicException e) {
			MessageDialog.openError(window.getShell(), "Error", e.getMessage());
		} catch (Exception e) {
			MagicLogger.log(e);
			MessageDialog.openError(window.getShell(), "Internal Error (Sorry)", e.getMessage());
		}
		return null;
	}

	protected IStructuredSelection getStructuredSelection(IWorkbenchWindow window) {
		ISelection selection = window.getSelectionService().getSelection();
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			return null;
		}
		IStructuredSelection iss = (IStructuredSelection) selection;
		return iss;
	}

	protected ICardStore<IMagicCard> getActiveDeckHandler(IWorkbenchWindow window) {
		IWorkbenchPart activePart = window.getPartService().getActivePart();
		ICardStore<IMagicCard> activeDeckHandler = null;
		if (activePart instanceof DeckView) {
			activeDeckHandler = ((DeckView) activePart).getFilteredStore().getCardStore();
		} else if (activePart instanceof MagicDbView || activePart instanceof MyCardsView) {
			activeDeckHandler = getDataManager().getCardHandler().getActiveStore();
		}
		return activeDeckHandler;
	}

	protected DataManager getDataManager() {
		return DataManager.getInstance();
	}

	public abstract void run(IWorkbenchWindow window, IStructuredSelection iss);
}
