package com.reflexit.magiccards.ui.gallery;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.MagicDbView;

public class GalleryView extends MagicDbView {
	public static final String ID = "com.reflexit.magiccards.ui.gallery.GalleryView";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getHelpId() {
		return MagicUIActivator.helpId("viewgallery");
	}

	@Override
	protected AbstractMagicCardsListControl doGetViewControl() {
		return new GalleryListControl(this) {
			@Override
			protected String getPreferencePageId() {
				return getViewPreferencePageId();
			}

			@Override
			public IFilteredCardStore doGetFilteredStore() {
				return DataManager.getCardHandler().getMagicDBFilteredStoreWorkingCopy();
			}
		};
	}

	@Override
	public String getPreferencePageId() {
		return GalleryPreferencePage.getId();
	}
}
