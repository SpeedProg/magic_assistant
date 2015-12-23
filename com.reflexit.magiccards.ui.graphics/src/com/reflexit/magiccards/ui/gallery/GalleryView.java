package com.reflexit.magiccards.ui.gallery;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.MagicDbViewPreferencePage;
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
	protected String getPreferencePageId() {
		return MagicDbViewPreferencePage.class.getName();
	}

	@Override
	protected AbstractMagicCardsListControl createViewControl() {
		return new GalleryListControl(this) {
			@Override
			protected String getPreferencePageId() {
				return getViewPreferencePageId();
			}

			@Override
			public IFilteredCardStore doGetFilteredStore() {
				return DataManager.getCardHandler().getMagicDBFilteredStore();
			}

			@Override
			protected void runDoubleClick() {
				GalleryView.this.runDoubleClick();
			}
		};
	}

	@Override
	protected void saveColumnLayout() {
		// do not touch
	}

	@Override
	protected void runDoubleClick() {
		super.runDoubleClick();
		try {
			IViewPart showView = getViewSite().getWorkbenchWindow().getActivePage().showView(GallerySelectionView.ID);
			((GallerySelectionView) showView).setDetails(getSelection());
		} catch (PartInitException e) {
			MagicUIActivator.log(e);
		}
	}
}
