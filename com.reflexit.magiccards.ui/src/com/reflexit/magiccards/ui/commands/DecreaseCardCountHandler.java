package com.reflexit.magiccards.ui.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.lib.MyCardsView;

/**
 * Decrease card number, if number is 1 remove the card
 */
public class DecreaseCardCountHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public DecreaseCardCountHandler() {
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
			decrease(window, iss, activeDeckHandler);
		} else if (activePart instanceof MagicDbView || activePart instanceof MyCardsView) {
			activeDeckHandler = DataManager.getCardHandler().getActiveDeckHandler();
			decrease(window, iss, activeDeckHandler);
		}
		return null;
	}

	protected void decrease(IWorkbenchWindow window, IStructuredSelection iss, IFilteredCardStore activeDeckHandler) {
		if (activeDeckHandler != null) {
			List list = iss.toList();
			ArrayList<IMagicCard> toRemove = new ArrayList<IMagicCard>();
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				IMagicCard magicCard = (IMagicCard) iterator.next();
				if (magicCard instanceof MagicCardPhysical) {
					MagicCardPhysical mc = (MagicCardPhysical) magicCard;
					int count = mc.getCount();
					if (count <= 1) {
						toRemove.add(new MagicCardPhysical(mc, mc.getLocation()));
					} else {
						mc.setCount(count - 1);
						DataManager.getInstance().update(activeDeckHandler.getCardStore(), mc, Collections.singleton(MagicCardField.COUNT));
					}
				} else {
					MagicCardPhysical magicCardCopy = new MagicCardPhysical(magicCard, null);
					magicCardCopy.setCount(1);
					toRemove.add(magicCardCopy);
				}
			}
			DataManager.getInstance().remove(toRemove, activeDeckHandler.getCardStore());
		} else {
			MessageDialog.openError(window.getShell(), "Error", "No active deck");
		}
	}
}
