package com.reflexit.magiccards.ui.commands;

import java.util.ArrayList;
import java.util.Iterator;

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
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.lib.MyCardsView;

/**
 * Increase card number
 */
public class IncreaseCardCountHandler extends AbstractHandler {
	private static final DataManager DM = DataManager.getInstance();

	/**
	 * The constructor.
	 */
	public IncreaseCardCountHandler() {
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
		IWorkbenchPart activePart = window.getPartService().getActivePart();
		IFilteredCardStore activeDeckHandler = null;
		if (activePart instanceof DeckView) {
			activeDeckHandler = ((DeckView) activePart).getFilteredStore();
			increase(window, iss, activeDeckHandler);
		} else if (activePart instanceof MagicDbView) {
			activeDeckHandler = DM.getCardHandler().getActiveDeckHandler();
			if (activeDeckHandler != null)
				DM.copyCards(iss.toList(), activeDeckHandler.getLocation());
			else
				throw new ExecutionException("No active deck");
		} else if (activePart instanceof MyCardsView) {
			activeDeckHandler = DM.getCardHandler().getActiveDeckHandler();
			increase(window, iss, activeDeckHandler);
		}
		return null;
	}

	protected void increase(IWorkbenchWindow window, IStructuredSelection iss, IFilteredCardStore activeDeckHandler) {
		if (activeDeckHandler != null) {
			ArrayList<IMagicCard> toAdd = new ArrayList<IMagicCard>();
			for (Iterator iterator = iss.iterator(); iterator.hasNext();) {
				IMagicCard magicCard = (IMagicCard) iterator.next();
				if (magicCard instanceof MagicCardPhysical) {
					MagicCardPhysical mc = (MagicCardPhysical) magicCard;
					int count = mc.getCount();
					mc.setCount(count + 1);
					DataManager.getInstance().update(activeDeckHandler.getCardStore(), mc);
				} else {
					toAdd.add(magicCard);
				}
			}
			DataManager.getInstance().add(activeDeckHandler.getCardStore(), toAdd);
		} else {
			MessageDialog.openError(window.getShell(), "Error", "No active deck/collection");
		}
	}
}
