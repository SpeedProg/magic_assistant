package com.reflexit.magiccards.ui.views.analyzers;

import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.lib.DeckListControl;
import com.reflexit.magiccards.ui.views.lib.MyCardsListControl.Presentation;

public class TreeDeckPage extends AbstractDeckListPage {
	@Override
	public AbstractMagicCardsListControl doGetMagicCardListControl() {
		return new DeckListControl(view, Presentation.SPLITTREE);
	}

	@Override
	public void activate() {
		super.activate();
		getListControl().syncFilter();
		getListControl().loadData(null);
	}
}
