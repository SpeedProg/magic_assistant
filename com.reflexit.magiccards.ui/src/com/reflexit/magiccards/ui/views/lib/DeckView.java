package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.core.model.xml.DeckFilteredCardFileStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.actions.MaterializeAction;
import com.reflexit.magiccards.ui.actions.OpenSideboardAction;
import com.reflexit.magiccards.ui.dialogs.CardFilterDialog;
import com.reflexit.magiccards.ui.dialogs.DeckFilterDialog;
import com.reflexit.magiccards.ui.preferences.DeckViewPreferencePage;
import com.reflexit.magiccards.ui.utils.WaitUtils;
import com.reflexit.magiccards.ui.views.FolderPageGroup;
import com.reflexit.magiccards.ui.views.ViewPageGroup;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;

public class DeckView extends AbstractMyCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.DeckView";
	private CardCollection deck;
	private OpenSideboardAction sideboard;
	private MaterializeAction materialize;

	/**
	 * The constructor.
	 */
	public DeckView() {
	}

	@Override
	protected ViewPageGroup createPageGroup() {
		return new FolderPageGroup(this::preActivate, this::postActivate);
	}

	@Override
	protected void createPages() {
		getPageGroup().loadExtensions(null);
	}

	@Override
	public String getHelpId() {
		return MagicUIActivator.helpId("viewdeck");
	}

	@Override
	protected void loadInitialInBackground() {
		String secondaryId = getDeckId();
		this.deck = DataManager.getInstance().getModelRoot().findCardCollectionById(secondaryId);
		if (deck != null) {
			// if (export!=null) export.selectionChanged(new
			// StructuredSelection(getCardCollection()));
			sideboard.setDeck(getCardCollection());
		}
		refreshView();
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		site.getPage().addPartListener(PartListener.getInstance());
	}

	@Override
	public void dispose() {
		if (deck != null)
			this.deck.close();
		super.dispose();
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		this.sideboard = new OpenSideboardAction(deck);
		this.materialize = new MaterializeAction(getFilteredStore().getCardStore());
	}

	// @Override
	// protected ExportAction createExportAction() {
	// CardCollection col = getCardCollection();
	// return new ExportAction(col == null ? new StructuredSelection() : new
	// StructuredSelection(col),
	// getPreferencePageId());
	// }
	protected IStorageInfo getStorageInfo() {
		IStorage<IMagicCard> storage = getFilteredStore().getCardStore().getStorage();
		if (storage instanceof IStorageInfo) {
			IStorageInfo si = ((IStorageInfo) storage);
			return si;
		}
		return null;
	}

	public static DeckView openCollection(final CardCollection col, IStructuredSelection sel) {
		if (col == null)
			return null;
		DeckView deckViewRes[] = new DeckView[1];
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (win == null)
					return;
				IWorkbenchPage page = win.getActivePage();
				if (page == null)
					return;
				try {
					IViewPart navView = page.showView(CardsNavigatorView.ID, null, IWorkbenchPage.VIEW_CREATE);
					navView.getViewSite().getSelectionProvider().setSelection(new StructuredSelection(col));
					DeckView deckView = (DeckView) page.showView(DeckView.ID, col.getId(),
							IWorkbenchPage.VIEW_ACTIVATE);
					if (sel != null && !sel.isEmpty())
						deckView.setSelection(sel);
					deckViewRes[0] = deckView;
				} catch (PartInitException e) {
					MessageDialog.openError(MagicUIActivator.getShell(), "Error", e.getMessage());
				}
			}
		});
		return deckViewRes[0];
	}

	protected void updatePartName() {
		String deckId = getDeckId();
		Location location = Location.createLocation(deckId);
		String name = location.getName();
		setPartName(name);
		setTitleToolTip(deckId);
		if (deck == null) {
			// IMagicControl c = getMagicControl();
			// c.setStatus("Loading " + deckId + "...");
			return;
		}
		// setPartProperty(name, name);
		if (deck.getLocation().isSideboard()) {
			setPartName("#" + name);
			if (sideboard != null)
				sideboard.setEnabled(false);
			setTitleImage(MagicUIActivator.getDefault().getImage("icons/obj16/sideboard16.png"));
		} else {
			if (!deck.isDeck()) {
				setTitleImage(MagicUIActivator.getDefault().getImage("icons/lib32.png"));
			}
		}
		// used in drop adapter
		getPartControl().setData("deck", deck);
	}

	@Override
	public void activate() {
		super.activate();
		updatePartName();
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.sideboard);
		manager.add(new Separator());
		super.fillLocalToolBar(manager);
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		super.fillLocalPullDown(manager);
		manager.add(this.sideboard);
		manager.add(this.materialize);
	}

	@Override
	public void handleEvent(final CardEvent event) {
		if (getControl() == null || getControl().isDisposed())
			return;
		getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (deck == null)
					return;
				Location dataLocation = null;
				if (event.getData() instanceof CardElement) {
					dataLocation = ((CardElement) event.getData()).getLocation();
				}
				// System.err.println("DeckView " + getPartName() + " got " +
				// event);
				if (event.getType() == CardEvent.REMOVE_CONTAINER) {
					if (deck.getLocation().equals(dataLocation)) {
						close();
						deck.close();
						deck = null;
						// dispose();
						// System.err.println("---Removing itself");
						return;
					}
				} else if (event.getType() == CardEvent.ADD_CONTAINER) {
					// ignore
				} else if (event.getType() == CardEvent.RENAME_CONTAINER) {
					String secondaryId = getViewSite().getSecondaryId();
					Location srcLocation = (Location) event.getData();
					if (deck.getLocation().equals(srcLocation) || deck == event.getSource()) {
						updatePartName();
						if (!secondaryId.equals(deck.getLocation().getBaseFileName())) {
							// reopen newly named deck, to change secondary id
							openCollection(deck, getSelection());
							close();
							return;
						}
						reloadData();
					}
				} else {
					// System.err.println(event);
					// list control will do refresh
				}
			}
		});
	}

	@Override
	protected String getPreferencePageId() {
		return DeckViewPreferencePage.class.getName();
	}

	public CardCollection getCardCollection() {
		return deck;
	}

	@Override
	public void refreshView() {
		setStore();
		WaitUtils.asyncExec(() -> updatePartName());
		reloadData();
	}

	protected void setStore() {
		WaitUtils.waitForCondition(() -> (DeckFilteredCardFileStore.getStoreForKey(getDeckId()) != null), 5000, 300);
		IFilteredCardStore<IMagicCard> store = getFilteredStore();
	}

	public String getDeckId() {
		return getViewSite().getSecondaryId();
	}

	protected void updateViewer() {
		if (getControl().isDisposed())
			return;
		updatePartName();
		// updateActivePage();
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public CardFilterDialog getCardFilterDialog() {
		return new DeckFilterDialog(getShell(), getFilterPreferenceStore());
	}
}