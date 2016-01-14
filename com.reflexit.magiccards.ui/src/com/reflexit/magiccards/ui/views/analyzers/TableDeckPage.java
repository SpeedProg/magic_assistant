package com.reflexit.magiccards.ui.views.analyzers;

import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl.Presentation;
import com.reflexit.magiccards.ui.views.lib.DeckListControl;

public class TableDeckPage extends AbstractDeckListPage {
	@Override
	public AbstractMagicCardsListControl doGetMagicCardListControl() {
		return new DeckListControl(getDeckView(), Presentation.TABLE);
	}

	@Override
	public void activate() {
		super.activate();
		getMagicControl().loadData(null);
	}
}
