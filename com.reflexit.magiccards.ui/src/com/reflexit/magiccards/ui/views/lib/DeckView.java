package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.DeckFilterDialog;
import com.reflexit.magiccards.ui.exportWizards.ExportAction;
import com.reflexit.magiccards.ui.preferences.DeckViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.analyzers.ColorControl;
import com.reflexit.magiccards.ui.views.analyzers.HandView;
import com.reflexit.magiccards.ui.views.analyzers.ManaCurveControl;
import com.reflexit.magiccards.ui.views.analyzers.TypeStatsControl;

public class DeckView extends AbstractMyCardsView implements ICardEventListener {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.DeckView";
	CardCollection deck;
	private Action shuffle;
	private CTabFolder folder;
	private ArrayList<IDeckPage> pages;
	static class DeckPageExtension {
		String id;
		String name;
		IConfigurationElement elp;

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

	@Override
	protected void runShowFilter() {
		DeckFilterDialog cardFilterDialog = new DeckFilterDialog(getShell(), getPreferenceStore());
		if (cardFilterDialog.open() == IStatus.OK)
			this.manager.loadData(null);
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
		return new ExportAction(new StructuredSelection(getCardCollection()));
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#init(org.eclipse.ui.IViewSite)
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
		if (export != null) {
			((ExportAction) export).selectionChanged(new StructuredSelection(getCardCollection()));
		}
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
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.lib.LibView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		updatePartName();
		super.createPartControl(parent);
	}

	protected void updatePartName() {
		IFilteredCardStore filteredStore = this.manager.getFilteredStore();
		if (filteredStore == null) {
			return;
		} else {
			ICardStore<IMagicCard> s = filteredStore.getCardStore();
			String name = s.getName();
			if (deck.isDeck())
				setPartName("Deck: " + name);
			else
				setPartName("Collection: " + name);
		}
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
				reloadData();
			}
		});
		folder.setSelection(0);
	}

	protected void createExtendedTabs() {
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
		page.createContents(folder);
		page.setDeckView(this);
		final CTabItem item = new CTabItem(folder, SWT.CLOSE);
		item.setText(name);
		item.setShowClose(false);
		item.setControl(page.getControl());
		item.setData(page);
		pages.add(page);
	}

	@Override
	protected synchronized void updateStatus() {
		CTabItem sel = folder.getSelection();
		if (sel.getControl() == manager.getControl()) {
			setStatus(manager.getStatusMessage());
		} else {
			for (IDeckPage deckPage : pages) {
				IDeckPage page = deckPage;
				if (sel.getData() == page) {
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
	public void handleEvent(final CardEvent event) {
		super.handleEvent(event);
		folder.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (event.getType() == CardEvent.REMOVE_CONTAINER) {
					if (DataManager.getModelRoot().findCardCollectionById(deck.getFileName()) == null) {
						deck.close();
						getViewSite().getPage().hideView(DeckView.this);
						return;
					}
				} else if (event.getType() == CardEvent.ADD_CONTAINER) {
					// ignore
				} else {
					System.err.println(event);
					updateActivePage();
				}
			}
		});
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
		reloadData();
	}

	@Override
	protected void updateViewer() {
		if (folder.isDisposed()) return;
		super.updateViewer();
		updatePartName();
		updateActivePage();
		updateStatus();
	}

	protected synchronized void updateActivePage() {
		CTabItem sel = folder.getSelection();
		if (sel.isDisposed()) return;
		//System.err.println(sel + " " + sel.getData());
		for (IDeckPage deckPage : pages) {
			IDeckPage page = deckPage;
			//System.err.println(deckPage);
			if (sel.getData() == page) {
				if (sel.getControl() != page.getControl()) {
					System.err.println("oops sel " + sel.getControl().handle + " page " + page.getControl().handle);
				}
				page.setFilteredStore(getFilteredStore());
				page.updateFromStore();
			}
		}
	}
}