package com.reflexit.magiccards.ui.views.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.core.model.xml.DeckFilteredCardFileStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.CardReconcileDialog;
import com.reflexit.magiccards.ui.dialogs.LocationPickerDialog;
import com.reflexit.magiccards.ui.exportWizards.ExportAction;
import com.reflexit.magiccards.ui.preferences.DeckViewPreferencePage;
import com.reflexit.magiccards.ui.utils.SelectionProviderIntermediate;
import com.reflexit.magiccards.ui.utils.WaitUtils;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicControl;
import com.reflexit.magiccards.ui.views.analyzers.AbstractDeckListPage;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;

public class DeckView extends AbstractMyCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.DeckView";
	private CardCollection deck;
	private CTabFolder folder;
	private ArrayList<IDeckPage> pages;
	private Action sideboard;
	private Action materialize;
	private SelectionProviderIntermediate selProvider = new SelectionProviderIntermediate();

	private static class DeckPageExtension {
		private String name;
		private IConfigurationElement elp;

		private static DeckPageExtension parsePage(IConfigurationElement elp) {
			DeckPageExtension page = new DeckPageExtension();
			// page.id = elp.getAttribute("id");
			page.name = elp.getAttribute("name");
			page.elp = elp;
			return page;
		}
	}

	private static ArrayList<DeckPageExtension> extensionPages;
	static {
		loadExtensions();
	}

	/**
	 * The constructor.
	 */
	public DeckView() {
		pages = new ArrayList<IDeckPage>();
	}

	private static void loadExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(MagicUIActivator.PLUGIN_ID + ".deckPage");
		IConfigurationElement points[] = extensionPoint.getConfigurationElements();
		extensionPages = new ArrayList<DeckPageExtension>();
		for (IConfigurationElement el : points) {
			extensionPages.add(DeckPageExtension.parsePage(el));
		}
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
	public String getHelpId() {
		return MagicUIActivator.helpId("viewdeck");
	}

	@Override
	protected void loadInitialInBackground() {
		super.loadInitialInBackground();
		String secondaryId = getDeckId();
		this.deck = DataManager.getInstance().getModelRoot().findCardCollectionById(secondaryId);
		if (export != null && deck != null) {
			((ExportAction) export).selectionChanged(new StructuredSelection(getCardCollection()));
		}
		refreshView();
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		site.getPage().addPartListener(PartListener.getInstance());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.views.lib.CollectionView#makeActions()
	 */
	@Override
	protected void makeActions() {
		super.makeActions();
		this.sideboard = new Action("Open Sideboard") {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/obj16/sideboard16.png"));
			}

			@Override
			public void run() {
				runCreateSideboard();
			}
		};
		this.materialize = new Action("Materialize...") {
			{
				setToolTipText("Attempt to materialize a deck/collection by replacing"
						+ " all virtual cards with own cards from inventory");
			}

			@Override
			public void run() {
				runMaterialize();
			}
		};
	}

	protected IStorageInfo getStorageInfo() {
		IStorage<IMagicCard> storage = getFilteredStore().getCardStore().getStorage();
		if (storage instanceof IStorageInfo) {
			IStorageInfo si = ((IStorageInfo) storage);
			return si;
		}
		return null;
	}

	protected void runMaterialize() {
		Collection<IMagicCard> orig = getFilteredStore().getCardStore().getCards();
		LocationPickerDialog locationPickerDialog = new LocationPickerDialog(getShell(), SWT.SINGLE
				| SWT.READ_ONLY) {
			@Override
			protected Control createDialogArea(Composite parent) {
				Control area = super.createDialogArea(parent);
				setMessage("Pick collection(s) from which you want to pull cards to materialize this deck");
				return area;
			}
		};
		if (locationPickerDialog.open() == Window.OK) {
			List<CardCollection> collections = locationPickerDialog.getSelectedCardCollections();
			ArrayList<ICardStore<IMagicCard>> stores = new ArrayList<>();
			for (CardCollection collection : collections) {
				if (collection.getStore().equals(getFilteredStore().getCardStore()))
					continue;
				stores.add(collection.getStore());
			}
			Collection<MagicCardPhysical> res = DataManager.getInstance().materialize(orig, stores);
			CardReconcileDialog cardReconcileDialog = new CardReconcileDialog(getShell()) {
				@Override
				protected void okPressed() {
					ICardStore<IMagicCard> cardStore = getFilteredStore().getCardStore();
					getStorageInfo().setVirtual(false);
					DataManager.getInstance().remove(orig, cardStore);
					DataManager.getInstance().moveCards(elements, cardStore);
					super.okPressed();
				}
			};
			cardReconcileDialog.setBlockOnOpen(false);
			cardReconcileDialog.setInput(res);
			cardReconcileDialog.open();
		}
	}

	protected void runCreateSideboard() {
		Location location = deck.getLocation();
		Location sideboard = location.toSideboard();
		if (location.equals(sideboard))
			return;
		CollectionsContainer parent = (CollectionsContainer) deck.getParent();
		CardCollection s;
		if (!deck.getParent().contains(sideboard)) {
			s = parent.addDeck(sideboard.getBaseFileName(), deck.isVirtual());
		} else {
			s = (CardCollection) parent.findChield(sideboard);
		}
		openCollection(s);
	}

	public static void openCollection(final CardCollection col) {
		if (col == null)
			return;
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
					IViewPart navView = page
							.showView(CardsNavigatorView.ID, null, IWorkbenchPage.VIEW_CREATE);
					navView.getViewSite().getSelectionProvider().setSelection(new StructuredSelection(col));
					page.showView(DeckView.ID, col.getId(), IWorkbenchPage.VIEW_ACTIVATE);
				} catch (PartInitException e) {
					MessageDialog.openError(MagicUIActivator.getShell(), "Error", e.getMessage());
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.views.lib.LibView#dispose()
	 */
	@Override
	public void dispose() {
		if (deck != null)
			this.deck.close();
		for (IDeckPage deckPage : pages) {
			IDeckPage page = deckPage;
			page.dispose();
		}
		super.dispose();
	}

	@Override
	public SelectionProviderIntermediate getSelectionProvider() {
		return selProvider;
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
		//setPartProperty(name, name);
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
		folder = new CTabFolder(parent, SWT.BOTTOM);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		// Cards List
		final CTabItem cardsList = new CTabItem(folder, SWT.CLOSE);
		cardsList.setText("Cards");
		cardsList.setShowClose(false);
		Control control1 = control.createPartControl(folder);
		cardsList.setControl(control1);
		// Pages
		createExtendedTabs();
		// Common
		folder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateActivePage();
			}
		});
		folder.setSelection(0);
		folder.setSimple(false);
		Display display = folder.getDisplay();
		// folder.setBackground(display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
		folder.setBackground(
				new Color[] { display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND),
						display.getSystemColor(SWT.COLOR_WHITE) },
				new int[] { 50 });
		updatePartName();
	}

	@Override
	protected AbstractMagicCardsListControl doGetViewControl() {
		return new DeckListControl(this);
	}

	private void createExtendedTabs() {
		for (Object element : extensionPages) {
			DeckPageExtension ex = (DeckPageExtension) element;
			try {
				try {
					IDeckPage page = (IDeckPage) ex.elp.createExecutableExtension("class");
					createDeckTab(ex.name, page);
				} catch (CoreException e) {
					MagicUIActivator.log(e);
				}
			} catch (Exception e) {
				MagicUIActivator.log(e);
			}
		}
	}

	private void createDeckTab(String name, final IDeckPage page) {
		page.setDeckView(this);
		page.createContents(folder);
		final CTabItem item = new CTabItem(folder, SWT.CLOSE);
		item.setText(name);
		item.setShowClose(false);
		item.setControl(page.getControl());
		item.setData(page);
		pages.add(page);
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
		manager.add(this.sideboard);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#fillLocalPullDown( org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		super.fillLocalPullDown(manager);
		manager.add(this.sideboard);
		manager.add(this.materialize);
	}

	@Override
	public void handleEvent(final CardEvent event) {
		super.handleEvent(event);
		if (folder == null || folder.isDisposed())
			return;
		folder.getDisplay().asyncExec(new Runnable() {
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
							openCollection(deck);// reopen newly named deck, to change secondary id
							close();
							return;
						}
						reloadData();
					}
				} else {
					// System.err.println(event);
					updateActivePage();
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
		WaitUtils.waitForCondition(() -> (DeckFilteredCardFileStore.getStoreForKey(getDeckId()) != null),
				5000, 300);
		IFilteredCardStore<IMagicCard> store = getFilteredStore();
		if (store == null)
			return;
		for (IDeckPage deckPage : pages) {
			IDeckPage page = deckPage;
			page.setFilteredStore(store);
		}
	}

	public String getDeckId() {
		return getViewSite().getSecondaryId();
	}

	@Override
	protected void updateViewer() {
		if (folder.isDisposed())
			return;
		super.updateViewer();
		updatePartName();
		// updateActivePage();
	}

	protected synchronized void updateActivePage() {
		CTabItem sel = folder.getSelection();
		if (sel.isDisposed())
			return;
		if (sel.getControl() == control.getControl()) {
			activateCardsTab();
			return;
		}
		// System.err.println(sel + " " + sel.getData());
		for (IDeckPage deckPage : pages) {
			IDeckPage page = deckPage;
			// System.err.println(deckPage);
			if (sel.getData() == page) {
				page.setFilteredStore(getFilteredStore());
				page.activate();
			}
		}
	}

	protected synchronized IMagicControl getActiveControl() {
		CTabItem sel = folder.getSelection();
		if (sel.isDisposed())
			return null;
		if (sel.getControl() == control.getControl()) {
			return control;
		}
		// System.err.println(sel + " " + sel.getData());
		for (IDeckPage deckPage : pages) {
			IDeckPage page = deckPage;
			// System.err.println(deckPage);
			if (sel.getData() == page) {
				if (page instanceof AbstractDeckListPage) {
					return ((AbstractDeckListPage) page).getListControl();
				}
				if (page instanceof IMagicControl) {
					return (IMagicControl) page;
				}
			}
		}
		return null;
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

	protected void activateCardsTab() {
		// toolbar
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = bars.getToolBarManager();
		toolBarManager.removeAll();
		fillLocalToolBar(toolBarManager);
		toolBarManager.update(true);
		// local view menu
		IMenuManager viewMenuManager = bars.getMenuManager();
		viewMenuManager.removeAll();
		fillLocalPullDown(viewMenuManager);
		viewMenuManager.updateAll(true);
		// global
		setGlobalControlHandlers(bars);
		bars.updateActionBars();
		getSelectionProvider().setSelectionProviderDelegate(control.getSelectionProvider());
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
		return ((AbstractMagicCardsListControl) control).getGroupAction();
	}

	@Override
	protected void saveColumnLayout() {
		CTabItem sel = folder.getSelection();
		if (sel.isDisposed())
			return;
		if (sel.getControl() == control.getControl()) {
			super.saveColumnLayout();
		}
	}
}