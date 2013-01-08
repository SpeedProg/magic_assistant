package com.reflexit.magiccards.ui.views.lib;

import java.util.ArrayList;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.exportWizards.ExportAction;
import com.reflexit.magiccards.ui.preferences.DeckViewPreferencePage;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicControl;
import com.reflexit.magiccards.ui.views.analyzers.AbilityPage;
import com.reflexit.magiccards.ui.views.analyzers.AbstractDeckListPage;
import com.reflexit.magiccards.ui.views.analyzers.CreaturePage;
import com.reflexit.magiccards.ui.views.analyzers.DrawPage;
import com.reflexit.magiccards.ui.views.analyzers.ManaCurvePage;
import com.reflexit.magiccards.ui.views.analyzers.SpellColourPage;
import com.reflexit.magiccards.ui.views.analyzers.TypePage;

public class DeckView extends AbstractMyCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.DeckView";
	private CardCollection deck;
	private Action shuffle;
	private CTabFolder folder;
	private ArrayList<IDeckPage> pages;
	private Action sideboard;
	private AbstractDeckListPage drawPage;
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
		return new ExportAction(col == null ? new StructuredSelection() : new StructuredSelection(col), getPreferencePageId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#init(org.eclipse.ui .IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		String secondaryId = getViewSite().getSecondaryId();
		this.deck = DataManager.getModelRoot().findCardCollectionById(secondaryId);
		if (getFilteredStore() != null && this.deck.getStore() != getFilteredStore().getCardStore()) {
			throw new IllegalArgumentException("Bad store");
		}
		site.getPage().addPartListener(PartListener.getInstance());
		if (export != null && deck != null) {
			((ExportAction) export).selectionChanged(new StructuredSelection(getCardCollection()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.lib.CollectionView#makeActions()
	 */
	@Override
	protected void makeActions() {
		super.makeActions();
		this.shuffle = new Action("Emulate Draw") {
			@Override
			public void run() {
				runShuffle();
			}
		};
		this.sideboard = new Action("Open Sideboard") {
			@Override
			public void run() {
				runCreateSideboard();
			}
		};
	}

	protected void runCreateSideboard() {
		Location location = deck.getLocation();
		Location sideboard = location.toSideboard();
		if (location.equals(sideboard))
			return;
		CollectionsContainer parent = (CollectionsContainer) deck.getParent();
		CardCollection s;
		if (!deck.getParent().contains(sideboard)) {
			s = parent.addDeck(sideboard.getBaseFileName());
		} else {
			s = (CardCollection) parent.findChield(sideboard);
		}
		IWorkbenchPage page = getViewSite().getWorkbenchWindow().getActivePage();
		try {
			page.showView(DeckView.ID, s.getFileName(), IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			MessageDialog.openError(new Shell(), "Error", e.getMessage());
		}
	}

	/**
	 *
	 */
	protected void runShuffle() {
		CTabItem[] items = folder.getItems();
		for (int i = 0; i < items.length; i++) {
			CTabItem cTabItem = items[i];
			if (cTabItem.getData() == drawPage) {
				folder.setSelection(cTabItem);
				drawPage.activate();
			}
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.lib.LibView#createPartControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		updatePartName();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, MagicUIActivator.helpId("viewdeck"));
	}

	@Override
	public SelectionProviderIntermediate getSelectionProvider() {
		return selProvider;
	}

	protected void updatePartName() {
		IFilteredCardStore filteredStore = getFilteredStore();
		if (filteredStore == null) {
			return;
		} else {
			ICardStore<IMagicCard> s = filteredStore.getCardStore();
			String name = s.getName();
			if (deck.getLocation().isSideboard()) {
				setPartName("Sideboard: " + deck.getLocation().toMainDeck().getName());
				if (sideboard != null)
					sideboard.setEnabled(false);
			} else {
				if (deck.isDeck()) {
					setPartName("Deck: " + name);
				} else
					setPartName("Collection: " + name);
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
		createDeckTab("Mana Curve", new ManaCurvePage());
		createDeckTab("Types", new TypePage());
		createDeckTab("Creatures", new CreaturePage());
		createDeckTab("Colors", new SpellColourPage());
		createDeckTab("Abilities", new AbilityPage());
		drawPage = new DrawPage();
		createDeckTab("Draw", drawPage);
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
		folder.setBackground(new Color[] { display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND), display.getSystemColor(SWT.COLOR_WHITE) },
				new int[] { 50 });
		refresh();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#fillLocalPullDown(
	 * org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		super.fillLocalPullDown(manager);
		manager.add(this.shuffle);
		manager.add(this.sideboard);
	}

	@Override
	public void handleEvent(final CardEvent event) {
		super.handleEvent(event);
		if (folder == null || folder.isDisposed())
			return;
		folder.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (event.getType() == CardEvent.REMOVE_CONTAINER) {
					if (deck != null && DataManager.getModelRoot().findCardCollectionById(deck.getFileName()) == null) {
						deck.close();
						getViewSite().getPage().hideView(DeckView.this);
						return;
					}
				} else if (event.getType() == CardEvent.ADD_CONTAINER) {
					// ignore
				} else if (event.getType() == CardEvent.RENAME_CONTAINER) {
					if (deck.getLocation().equals(((CardCollection) event.getSource()).getLocation())) {
						reloadData();
						// TODO does not work properly when moving to another container
					}
				} else {
					// System.err.println(event);
					updateActivePage();
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
	public void refresh() {
		setStore();
		reloadData();
	}

	protected void setStore() {
		IFilteredCardStore store = getFilteredStore();
		if (store == null)
			return;
		for (IDeckPage deckPage : pages) {
			IDeckPage page = deckPage;
			page.setFilteredStore(store);
		}
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
			}
		}
		return null;
	}

	@Override
	protected void runCopy() {
		IMagicControl active = getActiveControl();
		active.runCopy();
	}

	@Override
	protected void runPaste() {
		IMagicControl active = getActiveControl();
		active.runPaste();
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
}