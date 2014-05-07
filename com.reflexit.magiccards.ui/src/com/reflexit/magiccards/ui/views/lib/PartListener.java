package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class PartListener implements IPartListener2 {
	private static PartListener instance;

	public synchronized static PartListener getInstance() {
		if (instance == null)
			instance = new PartListener();
		return instance;
	}

	private PartListener() {
		// singleton
	}

	public void partActivated(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof DeckView) {
			DeckView deckView = (DeckView) part;
			IFilteredCardStore store = deckView.getFilteredStore();
			activateDeck(store);
		}
	}

	public void activateDeck(IFilteredCardStore store) {
		if (store != null) {
			DataManager.getCardHandler().setActiveDeckHandler(store);
			String id = store.getLocation().getBaseFileName();
			CardCollection coll = DataManager.getModelRoot().findCardCollectionById(id);
			if (coll != null)
				coll.update();
			else
				MagicUIActivator.log("Cannot find collection by id " + id);
		}
	}

	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
	}

	public void partClosed(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
	}

	public void partDeactivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
	}

	public void partHidden(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
	}

	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
	}

	public void partOpened(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof DeckView) {
			DeckView deckView = (DeckView) part;
			IFilteredCardStore store = deckView.getFilteredStore();
			activateDeck(store);
		}
	}

	public void partVisible(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
	}
}