package com.reflexit.magiccards.ui.views.analyzers;

import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.lib.DeckListControl;
import com.reflexit.magiccards.ui.views.lib.MyCardsListControl.Presentation;

public class SplitTreeDeckPage extends AbstractDeckListPage {
	@Override
	public AbstractMagicCardsListControl doGetMagicCardListControl() {
		return new DeckListControl(getDeckView(), Presentation.SPLITTREE);
	}

	@Override
	public void activate() {
		super.activate();
		getMagicControl().syncFilter();
		getMagicControl().loadData(null);
	}
}
