package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;

import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IFilteredCardStore;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.MagicDbViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.card.CardDescView;

public class MagicDbView extends AbstractCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.MagicDbView";
	Action addToLib;

	/**
	 * The constructor.
	 */
	public MagicDbView() {
	}

	@Override
	public ViewerManager doGetViewerManager(AbstractCardsView abstractCardsView) {
		return new LazyTableViewerManager(this);
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		return DataManager.getCardHandler().getMagicCardHandler();
	}

	@Override
	protected void runDoubleClick() {
		try {
			getViewSite().getWorkbenchWindow().getActivePage().showView(CardDescView.ID);
		} catch (PartInitException e) {
			MagicUIActivator.log(e);
		}
	}

	protected void addToLibrary() {
		ISelection selection = getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (!sel.isEmpty()) {
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					Object o = iterator.next();
					if (o instanceof IMagicCard)
						DataManager.getCardHandler().getMagicLibraryHandler().getCardStore().addCard(o);
				}
			}
		}
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		this.addToLib = new Action("Add to Library") {
			@Override
			public void run() {
				addToLibrary();
			}
		};
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(this.addToLib);
		super.fillContextMenu(manager);
	}

	@Override
	protected String getPreferencePageId() {
		return MagicDbViewPreferencePage.class.getName();
	}

	@Override
	protected String getPrefenceColumnsId() {
		return PreferenceConstants.MDBVIEW_COLS;
	}
}