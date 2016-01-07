package com.reflexit.magiccards.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;

import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.ui.views.lib.DeckView;

public class OpenSideboardAction extends ImageAction {
	private CardCollection deck;

	public OpenSideboardAction() {
		super("Open Sideboard", "icons/obj16/sideboard16.png", IAction.AS_PUSH_BUTTON);
	}

	public OpenSideboardAction(CardCollection deck) {
		this();
		this.deck = deck;
	}

	@Override
	public void run() {
		runCreateSideboard();
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
		DeckView.openCollection(s, new StructuredSelection());
	}

	public void setDeck(CardCollection deck) {
		this.deck = deck;
	}
}
