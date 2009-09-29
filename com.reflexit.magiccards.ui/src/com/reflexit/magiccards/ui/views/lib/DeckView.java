package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.storage.ICardEventManager;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.DeckViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.analyzers.HandView;

public class DeckView extends AbstractMyCardsView implements ICardEventListener {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.DeckView";
	CardCollection deck;
	private Action shuffle;
	private CTabFolder folder;
	private IPartListener2 partListener;
	private ArrayList<IDeckPage> pages;
	static class DeckPageExtension {
		String id;
		String name;
		private IDeckPage page;

		public static Collection<DeckPageExtension> parseElement(IConfigurationElement el) {
			Collection<DeckPageExtension> res = new ArrayList<DeckPageExtension>();
			IConfigurationElement[] children = el.getChildren();
			for (IConfigurationElement elp : children) {
				res.add(parsePage(elp));
			}
			return res;
		}

		public static DeckPageExtension parsePage(IConfigurationElement elp) {
			DeckPageExtension page = new DeckPageExtension();
			page.id = elp.getAttribute("id");
			page.name = elp.getAttribute("name");
			try {
				page.page = (IDeckPage) elp.createExecutableExtension("class");
			} catch (CoreException e) {
				Activator.log(e);
			}
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

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		String secondaryId = getViewSite().getSecondaryId();
		this.deck = DataManager.getModelRoot().findCardCollectionById(secondaryId);
		if (this.deck.getStore() != getFilteredStore().getCardStore()) {
			throw new IllegalArgumentException("Bad store");
		}
		DataManager.getCardHandler().getMyCardsHandler().getCardStore().addListener(this);
		site.getPage().addPartListener(partListener = new PartListener());
	}

	/* (non-Javadoc)
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
	}

	/**
	 * 
	 */
	protected void runShuffle() {
		try {
			HandView view = (HandView) getViewSite().getWorkbenchWindow().getActivePage().showView(HandView.ID);
			view.selectionChanged(this, new StructuredSelection(this.deck));
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.lib.LibView#dispose()
	 */
	@Override
	public void dispose() {
		this.deck.close();
		DataManager.getCardHandler().getMyCardsHandler().getCardStore().removeListener(this);
		getSite().getPage().removePartListener(partListener);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.lib.LibView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		ICardEventManager s = this.manager.getFilteredStore().getCardStore();
		if (s instanceof ICardStore) {
			String name = ((ICardStore<IMagicCard>) s).getName();
			if (deck.isDeck())
				setPartName("Deck: " + name);
			else
				setPartName("Collection: " + name);
		}
		super.createPartControl(parent);
	}

	@Override
	protected void createMainControl(Composite parent) {
		folder = new CTabFolder(parent, SWT.BORDER | SWT.BOTTOM);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		// Cards List
		final CTabItem cardsList = new CTabItem(folder, SWT.CLOSE);
		cardsList.setText("Cards");
		cardsList.setShowClose(false);
		Control control = this.manager.createContents(folder);
		cardsList.setControl(control);
		// Pages
		createDeckTab("Mana Curve", new ManaCurveControl());
		createDeckTab("Card Types", new TypeStatsControl());
		createDeckTab("Colors", new ColorControl());
		createExtendedTabs();
		// Common
		folder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshActivePage();
			}
		});
		folder.setSelection(0);
	}

	protected void createExtendedTabs() {
		for (Object element : extensionPages) {
			DeckPageExtension ex = (DeckPageExtension) element;
			try {
				IDeckPage page = ex.page;
				createDeckTab(ex.name, page);
			} catch (Exception e) {
				MagicUIActivator.log(e);
			}
		}
	}

	private void createDeckTab(String name, final IDeckPage page) {
		page.createContents(folder);
		final CTabItem item = new CTabItem(folder, SWT.CLOSE);
		item.setText(name);
		item.setShowClose(false);
		item.setControl(page.getControl());
		pages.add(page);
	}

	@Override
	protected void updateStatus() {
		CTabItem sel = folder.getSelection();
		if (sel.getControl() == manager.getControl()) {
			setStatus(manager.getStatusMessage());
		} else {
			for (IDeckPage deckPage : pages) {
				IDeckPage page = deckPage;
				if (sel.getControl() == page.getControl()) {
					setStatus(page.getStatusMessage());
				}
			}
		}
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#fillLocalPullDown(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		super.fillLocalPullDown(manager);
		manager.add(this.shuffle);
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		String secondaryId = getViewSite().getSecondaryId();
		return DataManager.getCardHandler().getCardCollectionHandler(secondaryId);
	}

	@Override
	public void handleEvent(CardEvent event) {
		super.handleEvent(event);
		if (event.getType() == CardEvent.REMOVE_CONTAINER) {
			if (DataManager.getModelRoot().findCardCollectionById(this.deck.getFileName()) == null) {
				this.deck.close();
				getViewSite().getPage().hideView(this);
				return;
			}
		} else if (event.getType() == CardEvent.ADD_CONTAINER) {
			// ignore
		} else {
			for (IDeckPage deckPage : pages) {
				IDeckPage page = deckPage;
				page.updateFromStore();
			}
		}
	}

	@Override
	protected String getPrefenceColumnsId() {
		return PreferenceConstants.DECKVIEW_COLS;
	}

	@Override
	protected String getPreferencePageId() {
		return DeckViewPreferencePage.class.getName();
	}

	public CardCollection getCardCollection() {
		return deck;
	}

	@Override
	protected void refresh() {
		refreshActivePage();
	}

	protected void refreshActivePage() {
		reloadData();
		CTabItem sel = folder.getSelection();
		for (IDeckPage deckPage : pages) {
			IDeckPage page = deckPage;
			if (sel.getControl() == page.getControl()) {
				page.setFilteredStore(getFilteredStore());
				page.updateFromStore();
			}
		}
		updateStatus();
	}
}