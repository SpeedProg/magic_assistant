package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.MagicDbViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.card.CardDescView;
import com.reflexit.magiccards.ui.views.printings.PrintingsView;

public class MagicDbView extends AbstractCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.MagicDbView";
	MenuManager addToDeck;
	protected Action showPrintings;

	/**
	 * The constructor.
	 */
	public MagicDbView() {
	}

	@Override
	public ViewerManager doGetViewerManager(AbstractCardsView abstractCardsView) {
		return new CompositeViewerManager(this);
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		return DataManager.getCardHandler().getMagicDBFilteredStore();
	}

	@Override
	protected void runDoubleClick() {
		try {
			getViewSite().getWorkbenchWindow().getActivePage().showView(CardDescView.ID);
		} catch (PartInitException e) {
			MagicUIActivator.log(e);
		}
	}

	protected IDeckAction copyToDeck;

	@Override
	protected void makeActions() {
		super.makeActions();
		this.addToDeck = new MenuManager("Add to");
		this.addToDeck.setRemoveAllWhenShown(true);
		this.addToDeck.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillDeckMenu(manager, copyToDeck);
			}
		});
		copyToDeck = new IDeckAction() {
			public void run(String id) {
				IFilteredCardStore fstore = DataManager.getCardHandler().getCardCollectionFilteredStore(id);
				Location loc = fstore.getLocation();
				ISelection selection = getViewer().getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) selection;
					if (!sel.isEmpty()) {
						DataManager.getCardHandler().copyCards(sel.toList(), loc);
					}
				}
			}
		};
		showPrintings = new Action("Show Other Sets") {
			@Override
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				if (window != null) {
					IWorkbenchPage page = window.getActivePage();
					if (page != null) {
						try {
							PrintingsView view = (PrintingsView) page.showView(PrintingsView.ID);
							view.setDbMode(true);
						} catch (PartInitException e) {
							MagicUIActivator.log(e);
						}
					}
				}
			}
		};
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(this.addToDeck);
		manager.add(this.copyText);
		manager.add(this.showPrintings);
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