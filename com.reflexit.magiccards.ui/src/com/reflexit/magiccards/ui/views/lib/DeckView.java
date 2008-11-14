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
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import com.alena.birt.ChartCanvas;
import com.alena.birt.IChartGenerator;
import com.alena.birt.ManaCurve;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.ICardDeck;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.nav.Deck;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.preferences.DeckViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.analyzers.HandView;

public class DeckView extends CollectionView implements ICardEventListener {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.DeckView";
	Deck deck;
	private Action shuffle;

	/**
	 * The constructor.
	 */
	public DeckView() {
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
		DataManager.getModelRoot().addListener(this);
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
		DataManager.getModelRoot().removeListener(this);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.lib.LibView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		ICardStore s = this.manager.getFilteredStore().getCardStore();
		if (s instanceof ICardDeck) {
			setPartName("Deck: " + ((ICardDeck) s).getDeckName());
		}
		super.createPartControl(parent);
	}

	@Override
	protected void createMainControl(Composite parent) {
		CTabFolder folder = new CTabFolder(parent, SWT.BORDER | SWT.BOTTOM);
		//folder.setSimple(false);
		CTabItem table = new CTabItem(folder, SWT.CLOSE);
		table.setText("Cards");
		table.setShowClose(false);
		Control control = this.manager.createContents(folder);
		//((Composite) control).setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setControl(control);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		final CTabItem mana = new CTabItem(folder, SWT.CLOSE);
		mana.setText("Mana Curve");
		mana.setShowClose(false);
		final ChartCanvas manaControl = new ChartCanvas(folder, SWT.BORDER);
		mana.setControl(manaControl);
		folder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.item == mana) {
					IChartGenerator gen = new ManaCurve(buildManaCurve());
					manaControl.setChartGenerator(gen);
					// set status
					ICardStore cardStore = getFilteredStore().getCardStore();
					String cardCountTotal = "";
					if (cardStore instanceof ICardCountable) {
						cardCountTotal = "Total cards: " + ((ICardCountable) cardStore).getCount();
					}
					setStatus(cardCountTotal);
				} else {
					DeckView.this.manager.updateStatus();
				}
			}
		});
		folder.setSelection(0);
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
		if (event.getType() == CardEvent.REMOVE_CONTAINER) {
			if (DataManager.getModelRoot().getDeck(this.deck.getFileName()) == null) {
				this.deck.close();
				getViewSite().getPage().hideView(this);
				return;
			}
		}
		super.handleEvent(event);
	}

	@Override
	protected String getPrefenceColumnsId() {
		return PreferenceConstants.DECKVIEW_COLS;
	}

	@Override
	protected String getPreferencePageId() {
		return DeckViewPreferencePage.class.getName();
	}

	protected int[] buildManaCurve() {
		return CardStoreUtils.getInstance().buildManaCurve(getFilteredStore().getCardStore());
	}
}