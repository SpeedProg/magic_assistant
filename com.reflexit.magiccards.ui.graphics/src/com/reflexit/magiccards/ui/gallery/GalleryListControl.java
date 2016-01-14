package com.reflexit.magiccards.ui.gallery;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.IMagicViewer;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;
import com.reflexit.magiccards.ui.views.lib.DeckListControl;

public class GalleryListControl extends DeckListControl {
	public GalleryListControl(AbstractCardsView abstractCardsView) {
		super(abstractCardsView, Presentation.GALLERY);
	}

	@Override
	protected Control createTableControl(Composite parent) {
		Control c = super.createTableControl(parent);
		viewer.hookDragAndDrop();
		return c;
	}

	@Override
	public IMagicViewer createViewer(Composite parent) {
		return new Gallery2Viewer(getPreferencePageId(), parent);
	}

	// @Override
	// public IFilteredCardStore<ICard> doGetFilteredStore() {
	// return abstractCardsView.getFilteredStore();
	// }

	@Override
	public void saveColumnLayout() {
		// we don't save columns here
	}

	@Override
	public ColumnCollection getSortColumnCollection() {
		return new MagicColumnCollection("");
	}

	@Override
	protected String getPreferencePageId() {
		return GalleryPreferencePage.getId();
	}
}