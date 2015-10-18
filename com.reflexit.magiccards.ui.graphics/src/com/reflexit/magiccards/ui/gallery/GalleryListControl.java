package com.reflexit.magiccards.ui.gallery;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.LocationFilteredCardStore;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;

public class GalleryListControl extends AbstractMagicCardsListControl {

	public GalleryListControl(AbstractCardsView abstractCardsView) {
		super(abstractCardsView);
		IFilteredCardStore<ICard> fistore = getFilteredStore();
		MagicCardFilter filter = fistore.getFilter();
		// filter.setNameGroupping(false);
		filter.setGroupFields(MagicCardField.SET);
	}

	@Override
	protected Control createTableControl(Composite parent) {
		Control c = super.createTableControl(parent);
		manager.hookDragAndDrop();
		return c;
	}

	@Override
	public IMagicColumnViewer createViewerManager() {
		return new Gallery2ViewerManager(getPreferencePageId());
	}

	@Override
	public IFilteredCardStore<ICard> doGetFilteredStore() {
		IFilteredCardStore fistore = new LocationFilteredCardStore();
		return fistore;
	}

	@Override
	protected String getPreferencePageId() {
		return GalleryPreferencePage.getId();
	}

}