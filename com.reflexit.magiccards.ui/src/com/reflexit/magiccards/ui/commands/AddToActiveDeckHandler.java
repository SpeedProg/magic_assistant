package com.reflexit.magiccards.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
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
		IFilteredCardStore activeDeckHandler = DataManager.getCardHandler().getActiveDeckHandler();
		if (activeDeckHandler != null) {
			List list = iss.toList();
			ArrayList<IMagicCard> toAdd = new ArrayList<IMagicCard>();
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				IMagicCard magicCard = (IMagicCard) iterator.next();
				if (magicCard instanceof MagicCardPhisical) {
					magicCard = new MagicCardPhisical(magicCard, null);
					((MagicCardPhisical) magicCard).setCount(1);
				}
				toAdd.add(magicCard);
			}
			activeDeckHandler.getCardStore().addAll(toAdd);
		} else {
			Display display = window.getShell().getDisplay();
			MessageDialog.openError(window.getShell(), "Error", "No active deck");
		}
		return null;
	}
}
