package com.reflexit.magiccards.ui.views.lib;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
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
import com.reflexit.magiccards.ui.utils.SelectionProviderIntermediate;
import com.reflexit.magiccards.ui.utils.WaitUtils;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicControl;
import com.reflexit.magiccards.ui.views.analyzers.AbstractDeckListPage;
import com.reflexit.magiccards.ui.views.lib.MyCardsListControl.Presentation;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;

public class DeckView extends AbstractMyCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.DeckView";
	private CardCollection deck;
	private CTabFolder folder;
	private ArrayList<IDeckPage> pages;
	private OpenSideboardAction sideboard;
	private MaterializeAction materialize;
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
		folder = new CTabFolder(parent, SWT.BOTTOM);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		// Cards List
		final CTabItem cardsList = new CTabItem(folder, SWT.CLOSE);
		cardsList.setText("Cards");
		cardsList.setShowClose(false);
		Control control1 = getMagicControl().createPartControl(folder);
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
		folder.setBackground(new Color[] { display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND),
				display.getSystemColor(SWT.COLOR_WHITE) }, new int[] { 50 });
		updatePartName();
	}

	@Override
	protected AbstractMagicCardsListControl createViewControl() {
		return new DeckListControl(this, Presentation.TREE);
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
		final CTabItem item = new CTabItem(folder, SWT.CLOSE);
		item.setText(name);
		item.setShowClose(false);
		page.setDeckView(this);
		page.createContents(folder);
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
							// reopen newly named deck, to change secondary id
							openCollection(deck, getSelection());
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
		WaitUtils.waitForCondition(() -> (DeckFilteredCardFileStore.getStoreForKey(getDeckId()) != null), 5000, 300);
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
		if (sel.getControl() == getMagicControl().getControl()) {
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
		if (sel.getControl() == getMagicControl().getControl()) {
			return getMagicControl();
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
		contributeToActionBars();
	}

	@Override
	protected void contributeToActionBars() {
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
		setGlobalHandlers(bars);
		bars.updateActionBars();
		getSelectionProvider().setSelectionProviderDelegate(getMagicControl().getSelectionProvider());
		hookContextMenu();
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

	@Override
	protected void saveColumnLayout() {
		CTabItem sel = folder.getSelection();
		if (sel.isDisposed())
			return;
		if (sel.getControl() == getMagicControl().getControl()) {
			super.saveColumnLayout();
		}
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
}