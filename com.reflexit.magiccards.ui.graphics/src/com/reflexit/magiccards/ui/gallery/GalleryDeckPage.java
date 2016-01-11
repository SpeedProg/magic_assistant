package com.reflexit.magiccards.ui.gallery;

import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.analyzers.AbstractDeckListPage;

public class GalleryDeckPage extends AbstractDeckListPage {
	@Override
	public AbstractMagicCardsListControl doGetMagicCardListControl() {
		return new GalleryListControl(getDeckView());
	}

	@Override
	public void activate() {
		super.activate();
		getMagicControl().loadData(null);
	}
}
