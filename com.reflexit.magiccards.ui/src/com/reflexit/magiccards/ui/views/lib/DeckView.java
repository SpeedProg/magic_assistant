package com.reflexit.magiccards.ui.views.lib;

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

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.nav.Deck;
import com.reflexit.magiccards.core.model.storage.ICardEventManager;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.preferences.DeckViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.analyzers.HandView;

public class DeckView extends AbstractMyCardsView implements ICardEventListener {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.DeckView";
	Deck deck;
	private Action shuffle;
	private CTabFolder folder;
	private IPartListener2 partListener;
	private ArrayList<IDeckPage> pages;

	/**
	 * The constructor.
	 */
	public DeckView() {
		pages = new ArrayList<IDeckPage>();
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		String secondaryId = getViewSite().getSecondaryId();
		this.deck = DataManager.getModelRoot().getDeck(secondaryId);
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
			setPartName("Deck: " + ((ICardStore<IMagicCard>) s).getName());
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
		createDeckTab("Mana Curve", new ManaCurveControl(folder, SWT.BORDER));
		createDeckTab("Card Types", new TypeStatsControl(folder, SWT.BORDER));
		createDeckTab("Colors", new ColorControl(folder, SWT.BORDER));
		createDeckTab("Info", new InfoControl(folder));
		// Common
		folder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
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
		});
		folder.setSelection(0);
	}

	private void createDeckTab(String name, final IDeckPage page) {
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
		return DataManager.getCardHandler().getDeckHandler(secondaryId);
	}

	@Override
	public void handleEvent(CardEvent event) {
		super.handleEvent(event);
		if (event.getType() == CardEvent.REMOVE_CONTAINER) {
			if (DataManager.getModelRoot().getDeck(this.deck.getFileName()) == null) {
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
}