package com.reflexit.magiccards.ui.commands;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.Predicate;
import com.reflexit.magiccards.core.model.storage.ICardStore;

/**
 * Increase card number
 */
public class IncreaseCardCountHandler extends AbstractCardCommandHandler {
	@Override
	public void run(IWorkbenchWindow window, IStructuredSelection iss) {
		increase(window, iss, getActiveDeckHandler(window));
	}

	protected void increase(IWorkbenchWindow window, IStructuredSelection iss,
			ICardStore<IMagicCard> activeDeckHandler) {
		if (activeDeckHandler != null) {
			ArrayList<IMagicCard> toAdd = new ArrayList<IMagicCard>();
			// increase count for all magic card physical
			for (Iterator iterator = iss.iterator(); iterator.hasNext();) {
				IMagicCard magicCard = (IMagicCard) iterator.next();
				if (magicCard instanceof MagicCardPhysical) {
					MagicCardPhysical mc = (MagicCardPhysical) magicCard;
					getDataManager().setField(activeDeckHandler, mc, MagicCardField.COUNT, mc.getCount() + 1);
				} else {
					toAdd.add(magicCard);
				}
			}
			// find rest of magic cards and add them (count 1) to action deck
			if (toAdd.size() > 0) {
				ArrayList<MagicCard> in = new ArrayList<MagicCard>();
				DataManager.expandGroups(in, toAdd, new Predicate<Object>() {
					@Override
					public boolean test(Object card) {
						if (card instanceof MagicCard)
							return true;
						return false;
					}
				});
				getDataManager().copyCards(in, activeDeckHandler);
			}
		} else {
			MessageDialog.openError(window.getShell(), "Error", "No active deck/collection");
		}
	}
}
