package com.reflexit.magiccards.ui.gallery;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.MagicDbViewPreferencePage;
import com.reflexit.magiccards.ui.utils.WaitUtils;
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
			protected void hookDoubleClickAction() {
				viewer.addDoubleClickListener(new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {
						runDoubleClick();
					}
				});
			}

			@Override
			public void handleEvent(CardEvent event) {
				Object data = event.getFirstDataElement();
				if (data instanceof MagicCard) {
					int type = event.getType();
					switch (type) {
					case CardEvent.UPDATE:
						WaitUtils.asyncExec(() -> viewer.refresh());
						break;
					case CardEvent.ADD:
						setNextSelection(new StructuredSelection(data));
						loadData(null);
					case CardEvent.REMOVE:
						loadData(null);
						break;
					default:
						break;
					}
				}
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
