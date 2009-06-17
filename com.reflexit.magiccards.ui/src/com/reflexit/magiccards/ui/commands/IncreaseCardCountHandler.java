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

import java.util.ArrayList;
import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.lib.DeckView;

/**
 * Increase card number
 */
public class IncreaseCardCountHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public IncreaseCardCountHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
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
			if (activeDeckHandler != null) {
				for (Iterator iterator = iss.iterator(); iterator.hasNext();) {
					IMagicCard magicCard = (IMagicCard) iterator.next();
					if (magicCard instanceof MagicCardPhisical) {
						MagicCardPhisical mc = (MagicCardPhisical) magicCard;
						int count = mc.getCount();
						mc.setCount(count + 1);
						activeDeckHandler.getCardStore().update(mc);
					} else {
						// not possible
						throw new IllegalArgumentException();
					}
				}
			} else {
				MessageDialog.openError(window.getShell(), "Error", "Huh?");
			}
		} else if (activePart instanceof MagicDbView) {
			activeDeckHandler = DataManager.getCardHandler().getActiveDeckHandler();
			if (activeDeckHandler != null) {
				ArrayList<IMagicCard> toAdd = new ArrayList<IMagicCard>();
				for (Iterator iterator = iss.iterator(); iterator.hasNext();) {
					IMagicCard magicCard = (IMagicCard) iterator.next();
					if (magicCard instanceof MagicCardPhisical) {
						// not possible
						throw new IllegalArgumentException();
					} else {
						toAdd.add(magicCard);
					}
				}
				activeDeckHandler.getCardStore().addAll(toAdd);
			} else {
				MessageDialog.openError(window.getShell(), "Error", "No active deck/collection");
			}
		}
		return null;
	}
}
