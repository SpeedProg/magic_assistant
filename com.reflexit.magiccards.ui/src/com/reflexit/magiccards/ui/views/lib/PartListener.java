package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class PartListener implements IPartListener2 {
	public void partActivated(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof DeckView) {
			DeckView deckView = (DeckView) part;
			IFilteredCardStore store = deckView.getFilteredStore();
			DataManager.getCardHandler().setActiveDeckHandler(store);
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
		// TODO Auto-generated method stub
	}

	public void partVisible(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
	}
}
