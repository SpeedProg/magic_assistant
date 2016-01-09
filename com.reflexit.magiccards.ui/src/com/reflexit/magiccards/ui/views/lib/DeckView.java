package com.reflexit.magiccards.ui.views.lib;

import java.util.ArrayList;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import com.reflexit.magiccards.ui.exportWizards.ExportAction;
import com.reflexit.magiccards.ui.preferences.DeckViewPreferencePage;
import com.reflexit.magiccards.ui.utils.WaitUtils;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.FolderPageGroup;
import com.reflexit.magiccards.ui.views.IMagicCardListControl;
import com.reflexit.magiccards.ui.views.IMagicControl;
import com.reflexit.magiccards.ui.views.IViewPage;
import com.reflexit.magiccards.ui.views.ViewPageGroup;
import com.reflexit.magiccards.ui.views.analyzers.AbstractDeckListPage;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;

public class DeckView extends AbstractMyCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.DeckView";
	private CardCollection deck;
	private Composite main;
	private ViewPageGroup pageGroup;
	private OpenSideboardAction sideboard;
	private MaterializeAction materialize;

	/**
	 * The constructor.
	 */
	public DeckView() {
		pageGroup = new FolderPageGroup() {
			@Override
			public void activate(IViewPage activePage) {
				DeckView.this.activate(activePage);
			}
		};
		pageGroup.init(this);
		pageGroup.loadExtensions(null);
	}

	@Override
	protected ExportAction createExportAction() {
		CardCollection col = getCardCollection();
		return new ExportAction(col == null ? new StructuredSelection() : new StructuredSelection(col),
				getPreferencePageId());
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
	}

	@Override
	protected void registerContextMenu(MenuManager menuMgr) {
		getSite().registerContextMenu(getId(), menuMgr, getSelectionProvider());
	}

	@Override
	public String getHelpId() {
		return MagicUIActivator.helpId("viewdeck");
	}

	@Override
	protected void loadInitialInBackground() {
		super.loadInitialInBackground();
		String secondaryId = getDeckId();
		this.deck = DataManager.getInstance().getModelRoot().findCardCollectionById(secondaryId);
		if (export != null && deck != null) {
			export.selectionChanged(new StructuredSelection(getCardCollection()));
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
	protected void makeActions() {
		super.makeActions();
		this.sideboard = new OpenSideboardAction(deck);
		this.materialize = new MaterializeAction(getFilteredStore().getCardStore());
	}

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
	public void dispose() {
		if (deck != null)
			this.deck.close();
		pageGroup.dispose();
		super.dispose();
	}

	@Override
	protected ISelectionProvider getSelectionProvider() {
		return super.getSelectionProvider();
	}

	protected void updatePartName() {
		String deckId = getDeckId();
		Location location = Location.createLocation(deckId);
		String name = location.getName();
		setPartName(name);
		setTitleToolTip(deckId);
		if (deck == null) {
			IMagicControl c = getActiveControl();
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
		// folder = new CTabFolder(parent, SWT.BOTTOM);
		// folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		// // Cards List
		// final CTabItem cardsList = new CTabItem(folder, SWT.CLOSE);
		// cardsList.setText("Cards");
		// cardsList.setShowClose(false);
		// Control control1 = getMagicControl().createPartControl(folder);
		// cardsList.setControl(control1);
		// Pages
		pageGroup.createContent(main);
		activate(pageGroup.getPage(0));
		updatePartName();
	}

	@Override
	protected AbstractMagicCardsListControl createViewControl() {
		return null;
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
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
		super.handleEvent(event);
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

	@Override
	protected void updateViewer() {
		if (main.isDisposed())
			return;
		super.updateViewer();
		updatePartName();
		// updateActivePage();
	}

	protected synchronized IMagicControl getActiveControl(IViewPage page) {
		// System.err.println(deckPage);
		if (page instanceof AbstractDeckListPage) {
			return ((AbstractDeckListPage) page).getListControl();
		}
		if (page instanceof IMagicControl) {
			return (IMagicControl) page;
		}
		return null;
	}

	protected synchronized IMagicControl getActiveControl() {
		IDeckPage page = (IDeckPage) pageGroup.getActivePage();
		return getActiveControl(page);
	}

	@Override
	public IFilteredCardStore getFilteredStore() {
		return ((IMagicCardListControl) getActiveControl(pageGroup.getPage(0))).getFilteredStore();
	}

	@Override
	protected void runCopy() {
		IMagicControl active = getActiveControl();
		if (active != null)
			active.runCopy();
		else
			MagicUIActivator.log("No copy control");
	}

	@Override
	protected void runPaste() {
		IMagicControl active = getActiveControl();
		if (active != null)
			active.runPaste();
		else
			MagicUIActivator.log("No paste control");
	}

	@Override
	protected void contributeToActionBars() {
		// toolbar
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = bars.getToolBarManager();
		// toolBarManager.removeAll();
		fillLocalToolBar(toolBarManager);
		toolBarManager.update(true);
		// local view menu
		IMenuManager viewMenuManager = bars.getMenuManager();
		// viewMenuManager.removeAll();
		fillLocalPullDown(viewMenuManager);
		viewMenuManager.updateAll(true);
		// global
		setGlobalHandlers(bars);
		bars.updateActionBars();
		// getViewSite().setSelectionProvider(getSelectionProvider());
		// hookContextMenu();
	}

	@Override
	protected void setGlobalControlHandlers(IActionBars bars) {
		IMagicControl active = getActiveControl();
		if (active != null)
			active.setGlobalControlHandlers(bars);
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

	protected void activate(IViewPage activePage) {
		setMagicControl(getActiveControl(activePage));
		((IDeckPage) activePage).setFilteredStore(getFilteredStore());
		int i = pageGroup.getPageIndex(activePage);
		if (i >= 0)
			pageGroup.activate(i);
		contributeToActionBars();
	}
}