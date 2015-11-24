package com.reflexit.magiccards.ui.gallery;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicViewer;

public class GalleryListControl extends AbstractMagicCardsListControl {
	public GalleryListControl(AbstractCardsView abstractCardsView) {
		super(abstractCardsView);
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

	@Override
	public IFilteredCardStore<ICard> doGetFilteredStore() {
		return abstractCardsView.getFilteredStore();
	}

	@Override
	public void saveColumnLayout() {
		// we don't save columns here
	}

	@Override
	protected String getPreferencePageId() {
		return GalleryPreferencePage.getId();
	}
}