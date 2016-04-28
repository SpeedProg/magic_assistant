package com.reflexit.magiccards.ui.gallery;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.actions.SimpleAction;
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
		return GalleryPreferencePage.getId();
	}

	@Override
	protected AbstractMagicCardsListControl createViewControl() {
		return new GalleryListControl() {
			@Override
			protected String getPreferencePageId() {
				return getViewPreferencePageId();
			}

			@Override
			public IFilteredCardStore doGetFilteredStore() {
				return DataManager.getCardHandler().getMagicDBFilteredStore();
			}

			@Override
			protected void makeActions() {
				actionDoubleClick = new SimpleAction(() -> runDoubleClick());
				super.makeActions();
			}

			@Override
			public void handleEvent(CardEvent event) {
				mcEventHandler(event);
			}
		};
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
