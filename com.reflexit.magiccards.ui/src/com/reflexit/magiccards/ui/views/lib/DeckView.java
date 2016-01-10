package com.reflexit.magiccards.ui.views.lib;

import java.util.ArrayList;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
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
import com.reflexit.magiccards.ui.preferences.DeckViewPreferencePage;
import com.reflexit.magiccards.ui.utils.SelectionProviderIntermediate;
import com.reflexit.magiccards.ui.utils.WaitUtils;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.FolderPageGroup;
import com.reflexit.magiccards.ui.views.IMagicCardListControl;
import com.reflexit.magiccards.ui.views.IMagicControl;
import com.reflexit.magiccards.ui.views.IViewPage;
import com.reflexit.magiccards.ui.views.ViewPageGroup;
import com.reflexit.magiccards.ui.views.analyzers.AbstractDeckListPage;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;

public class DeckView extends AbstractCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.DeckView";
	private CardCollection deck;
	private Composite main;
	private ViewPageGroup pageGroup;
	private OpenSideboardAction sideboard;
	private MaterializeAction materialize;
	private SelectionProviderIntermediate selectionProviderBridge = new SelectionProviderIntermediate();
	private LibraryEventListener eventListener = new LibraryEventListener();
	private MenuManager menuMgr;

	/**
	 * The constructor.
	 */
	public DeckView() {
		pageGroup = new FolderPageGroup() {
			@Override
			public void activate() {
				IViewPage activePage = pageGroup.getActivePage();
				preActivate(activePage);
				super.activate();
				postActivate(activePage);
			}
		};
		pageGroup.init(this);
		pageGroup.loadExtensions(null);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		eventListener.init(getViewSite(), this::loadInitialInBackground);
	}

	@Override
	public String getHelpId() {
		return MagicUIActivator.helpId("viewdeck");
	}

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
		eventListener.setEventHandler(this::handleEvent);
		site.getPage().addPartListener(PartListener.getInstance());
	}

	@Override
	public void dispose() {
		if (deck != null)
			this.deck.close();
		pageGroup.dispose();
		eventListener.dispose();
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

	@Override
	protected ISelectionProvider getSelectionProvider() {
		return selectionProviderBridge;
	}

	protected void updatePartName() {
		String deckId = getDeckId();
		Location location = Location.createLocation(deckId);
		String name = location.getName();
		setPartName(name);
		setTitleToolTip(deckId);
		if (deck == null) {
			IMagicControl c = getMagicControl();
			c.setStatus("Loading " + deckId + "...");
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
	}

	@Override
	protected void createMainControl(Composite parent) {
		main = new Composite(parent, SWT.NONE);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(GridLayoutFactory.fillDefaults().create());
		// Pages
		pageGroup.createContent(main);
	}

	@Override
	protected void activate() {
		pageGroup.setActivePageIndex(0);
		pageGroup.activate();
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

	public void handleEvent(final CardEvent event) {
		if (main == null || main.isDisposed())
			return;
		main.getDisplay().asyncExec(new Runnable() {
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
					if (deck != null && deck.getLocation().equals(dataLocation)) {
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
					pageGroup.refresh();
					reloadData();// XXX
				}
			}
		});
	}

	private void close() {
		try {
			getViewSite().getPage().hideView(DeckView.this);
		} catch (Exception e) {
			// ignore
		}
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
		if (main.isDisposed())
			return;
		updatePartName();
		// updateActivePage();
	}

	protected synchronized IMagicControl getMagicControl(IViewPage page) {
		// System.err.println(deckPage);
		if (page instanceof AbstractDeckListPage) {
			return ((AbstractDeckListPage) page).getListControl();
		}
		if (page instanceof IMagicControl) {
			return (IMagicControl) page;
		}
		return null;
	}

	protected synchronized IMagicControl getMagicControl() {
		IDeckPage page = (IDeckPage) pageGroup.getActivePage();
		return getMagicControl(page);
	}

	@Override
	public IFilteredCardStore getFilteredStore() {
		return ((IMagicCardListControl) getMainMagicControl()).getFilteredStore();
	}

	@Override
	protected void hookContextMenu() {
		// register view menu
		registerContextMenu(getContextMenuManager());
	}

	@Override
	protected boolean hookContextMenu(MenuManager menuMgr) {
		// do nothing active page hooks it
		return true;
	}

	@Override
	protected void setGlobalHandlers(IActionBars bars) {
		super.setGlobalHandlers(bars);
		IMagicControl active = getMagicControl();
		if (active != null)
			active.setGlobalHandlers(bars);
	}

	@Override
	public String getId() {
		return ID;
	}

	public IAction getGroupAction() {
		return ((AbstractMagicCardsListControl) getMagicControl()).getGroupAction();
	}

	public void setSelection(IStructuredSelection structuredSelection) {
		ArrayList<Object> l = new ArrayList<Object>();
		for (Object o : structuredSelection.toList()) {
			if (o instanceof MagicCard) {
				l.addAll(((MagicCard) o).getRealCards().getChildrenList());
				continue;
			} else if (o instanceof IMagicCard) {
				l.add(o);
			}
		}
		getSelectionProvider().setSelection(new StructuredSelection(l));
	}

	protected void preActivate(IViewPage activePage) {
		// clean toolbar
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = bars.getToolBarManager();
		toolBarManager.removeAll();
		toolBarManager.update(true);
		// clean local view menu
		IMenuManager viewMenuManager = bars.getMenuManager();
		viewMenuManager.removeAll();
		viewMenuManager.updateAll(true);
		bars.updateActionBars();
		// reset context menu
		menuMgr = createContentMenuManager();
		pageGroup.getActivePage().setContextMenuManager(menuMgr);
		// set fstore
		((IDeckPage) activePage).setFilteredStore(getFilteredStore());
	}

	protected void postActivate(IViewPage activePage) {
		// contribute this view extra actions
		contributeToActionBars();
		selectionProviderBridge.setSelectionProviderDelegate(activePage.getSelectionProvider());
		getSite().setSelectionProvider(selectionProviderBridge);
	}

	private MenuManager getContextMenuManager() {
		return menuMgr;
	}

	@Override
	public void reloadData() {
		if (getMagicControl() != null)
			getMagicControl().reloadData();
	}

	public IPersistentPreferenceStore getLocalPreferenceStore() {
		IMagicControl magicControl = getMagicControl();
		if (magicControl instanceof IMagicCardListControl)
			return ((IMagicCardListControl) magicControl).getColumnsPreferenceStore();
		magicControl = getMainMagicControl();
		if (magicControl instanceof IMagicCardListControl)
			return ((IMagicCardListControl) magicControl).getColumnsPreferenceStore();
		return null;
	}

	protected IMagicControl getMainMagicControl() {
		return getMagicControl(pageGroup.getPage(0));
	}

	@Override
	public IPersistentPreferenceStore getFilterPreferenceStore() {
		if (getMagicControl() instanceof IMagicCardListControl)
			return ((IMagicCardListControl) getMagicControl()).getElementPreferenceStore();
		return null;
	}
}