package com.reflexit.magiccards.ui.views.analyzers;

import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl.Presentation;
import com.reflexit.magiccards.ui.views.lib.DeckListControl;

public class TreeDeckPage extends AbstractDeckListPage {
	@Override
	public AbstractMagicCardsListControl doGetMagicCardListControl() {
		return new DeckListControl(getDeckView(), Presentation.TREE);
	}

	@Override
	public void activate() {
		super.activate();
		getMagicControl().syncFilter();
		getMagicControl().loadData(null);
	}
}
