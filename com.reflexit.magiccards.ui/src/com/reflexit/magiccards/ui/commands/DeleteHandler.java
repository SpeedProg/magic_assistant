/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.ui.commands;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.ICardStore;

/**
 * @author Alena
 *
 */
public class DeleteHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands. ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		ISelection selection = window.getSelectionService().getSelection();
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			return null;
		}
		IStructuredSelection iss = (IStructuredSelection) selection;
		remove(iss);
		return null;
	}

	public static void remove(IStructuredSelection sel) {
		if (sel.isEmpty())
			return;
		ArrayList<CardElement> toBeRemoved = new ArrayList<CardElement>();
		for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
			CardElement el = (CardElement) iterator.next();
			if (isApplicable(el))
				toBeRemoved.add(el);
		}
		if (toBeRemoved.size() == 0)
			return;
		int sum = 0;
		for (Iterator iterator = toBeRemoved.iterator(); iterator.hasNext();) {
			CardElement el = (CardElement) iterator.next();
			sum += getOwnCount(el);
			if (!el.getLocation().isSideboard()) // if we delete deck we also deleting sideboard, not true the other way
				sum += getOwnCount(el.getRelated());
		}
		if (toBeRemoved.size() == 1) {
			CardElement el = toBeRemoved.get(0);
			if (sum > 0) {
				MessageDialog dialog = new MessageDialog(
						getShell(),
						"Removal Confirmantion",
						null,
						"Deleting "
								+ el.getName()
								+ " will also PERMANENTY delete "
								+ sum
								+ " non virtual cards from this deck. "
								+ "You can choose to disband this deck instead, which will move all its cards to the main collection"
								+ " (then deck will be removed)", MessageDialog.WARNING, new String[] {
								"Disband", "Delete", "Cancel" }, 0);
				int result = dialog.open();
				performOperation(toBeRemoved, result);
			} else {
				if (!MessageDialog
						.openQuestion(getShell(), "Removal Confirmation", "Are you sure you want to delete "
								+ el.getName() + "?")) {
					return;
				}
				performOperation(toBeRemoved, 1); // remove
			}
		} else {
			if (sum == 0) {
				if (!MessageDialog.openQuestion(getShell(), "Removal Confirmation",
						"Are you sure you want to delete these " + toBeRemoved.size() + " elements?")) {
					return;
				}
				performOperation(toBeRemoved, 1);
			} else {
				MessageDialog dialog = new MessageDialog(
						getShell(),
						"Removal Confirmantion",
						null,
						"You are abount to delete "
								+ toBeRemoved.size()
								+ " deck/collections. "
								+ "Deleting a deck/collection"
								+ " will also PERMANENTY delete "
								+ sum
								+ " non virtual cards from it. "
								+ "You can choose to disband them instead, which will move all their cards to the main collection"
								+ " (then decks will be removed)", MessageDialog.WARNING, new String[] {
								"Disband", "Delete", "Cancel" }, 0);
				int result = dialog.open();
				performOperation(toBeRemoved, result);
			}
		}
	}

	private static void performOperation(ArrayList<CardElement> toBeRemoved, int result) {
		if (result == 0) { // disband
			for (Iterator iterator = toBeRemoved.iterator(); iterator.hasNext();) {
				CardElement el = (CardElement) iterator.next();
				disbandle(el);
				if (!el.getLocation().isSideboard())
					disbandle(el.getRelated());
			}
		} else if (result == 1) { // remove
			for (Iterator iterator = toBeRemoved.iterator(); iterator.hasNext();) {
				CardElement el = (CardElement) iterator.next();
				remove(el);
				if (!el.getLocation().isSideboard())
					remove(el.getRelated());
			}
		}
	}

	private static int getOwnCount(CardElement el) {
		int ownCount = 0;
		if (el instanceof CardCollection) {
			((CardCollection) el).open();
			ICardStore<IMagicCard> store = ((CardCollection) el).getStore();
			for (IMagicCard card : store) {
				if (card instanceof MagicCardPhysical) {
					ownCount += ((MagicCardPhysical) card).getOwnCount();
				}
			}
		}
		return ownCount;
	}

	private static void disbandle(CardElement el) {
		if (el instanceof CardCollection) {
			ModelRoot root = getModelRoot();
			if (el != root.getDefaultLib()) {
				ICardStore<IMagicCard> store = ((CardCollection) el).getStore();
				ArrayList<IMagicCard> cards = new ArrayList<IMagicCard>(store.size());
				for (IMagicCard card : store) {
					cards.add(card);
				}
				DataManager.getInstance().moveCards(cards, root.getDefaultLib().getStore());
			}
		}
		remove(el);
	}

	protected static ModelRoot getModelRoot() {
		return DataManager.getInstance().getModelRoot();
	}

	public static void remove(CardElement f) {
		if (f != null && isApplicable(f))
			f.remove();
	}

	protected static boolean isApplicable(CardElement f) {
		ModelRoot root = getModelRoot();
		if (f == root.getDefaultLib()) {
			info("'" + f + "' is a special collection which cannot be renamed or moved. "
					+ "Move the cards from it using multi-select if you need to.");
			return false;
		}
		if (f instanceof CardOrganizer) {
			if (f == root.getDeckContainer() || f == root.getCollectionsContainer()) {
				info("This is special container which cannot be removed");
				return false;
			}
			if (((CardOrganizer) f).hasChildren()) {
				info("Cannot remove container - not empty. Remove all children elements first");
				return false;
			}
		}
		if (f.getParent() == root) {
			return false;
		}
		return true;
	}

	public static void info(final String msg) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openInformation(getShell(), "Cannot Remove", msg);
			}
		});
	}

	protected static Shell getShell() {
		Shell shell = null;
		try {
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		} catch (Exception e) {
			shell = new Shell();
		}
		return shell;
	}
}
