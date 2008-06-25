package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardDeck;
import com.reflexit.magiccards.core.model.ICardStore;
import com.reflexit.magiccards.core.model.IFilteredCardStore;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.nav.Deck;
import com.reflexit.magiccards.ui.preferences.MagicDbViewPreferencePage;

public class DeckView extends LibView {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.DeckView";
	Deck deck;

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
		DataManager.getModelRoot().addListener(this);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.lib.LibView#dispose()
	 */
	@Override
	public void dispose() {
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
	public IFilteredCardStore doGetFilteredStore() {
		String secondaryId = getViewSite().getSecondaryId();
		return DataManager.getCardHandler().getDeckHandler(secondaryId);
	}

	@Override
	public void handleEvent(CardEvent event) {
		if (event.getType() == CardEvent.REMOVE_CONTAINER) {
			String secondaryId = getViewSite().getSecondaryId();
			if (DataManager.getModelRoot().getDeck(this.deck.getFileName()) == null) {
				getViewSite().getPage().hideView(this);
				return;
			}
		}
		super.handleEvent(event);
	}

	@Override
	protected String getPreferencePageId() {
		return MagicDbViewPreferencePage.class.getName();
	}
}