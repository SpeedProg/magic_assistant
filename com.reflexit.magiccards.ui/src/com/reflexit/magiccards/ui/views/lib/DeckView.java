package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardDeck;
import com.reflexit.magiccards.core.model.ICardStore;
import com.reflexit.magiccards.core.model.IFilteredCardStore;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.nav.Deck;
import com.reflexit.magiccards.ui.preferences.DeckViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.LazyTableViewerManager;
import com.reflexit.magiccards.ui.views.ViewerManager;

public class DeckView extends AbstractCardsView implements ICardEventListener {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.DeckView";
	protected Action delete;
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
		getFilteredStore().getCardStore().addListener(this);
		String secondaryId = getViewSite().getSecondaryId();
		this.deck = DataManager.getModelRoot().getDeck(secondaryId);
		if (this.deck.getStore() != getFilteredStore().getCardStore()) {
			throw new IllegalArgumentException("Bad store");
		}
		DataManager.getModelRoot().addListener(this);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#makeActions()
	 */
	@Override
	protected void makeActions() {
		super.makeActions();
		this.delete = new Action("Remove") {
			@Override
			public void run() {
				removeSelected();
			}
		};
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		this.delete.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
	}

	@Override
	protected void setGlobalHandlers(IActionBars bars) {
		super.setGlobalHandlers(bars);
		ActionHandler deleteHandler = new ActionHandler(this.delete);
		IHandlerService service = (IHandlerService) (getSite()).getService(IHandlerService.class);
		service.activateHandler("org.eclipse.ui.edit.delete", (IHandler) deleteHandler);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.lib.LibView#dispose()
	 */
	@Override
	public void dispose() {
		getFilteredStore().getCardStore().removeListener(this);
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
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		String secondaryId = getViewSite().getSecondaryId();
		return DataManager.getCardHandler().getDeckHandler(secondaryId);
	}

	@Override
	public ViewerManager doGetViewerManager(AbstractCardsView abstractCardsView) {
		return new LazyTableViewerManager(this);
	}

	public void handleEvent(CardEvent event) {
		if (event.getType() == CardEvent.REMOVE_CONTAINER) {
			if (DataManager.getModelRoot().getDeck(this.deck.getFileName()) == null) {
				this.deck.close();
				getViewSite().getPage().hideView(this);
				return;
			}
		}
		this.manager.loadData();
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