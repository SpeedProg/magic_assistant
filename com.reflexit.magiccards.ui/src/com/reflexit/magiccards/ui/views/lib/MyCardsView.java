package com.reflexit.magiccards.ui.views.lib;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.preference.IPreferenceStore;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.Locations;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.LibViewPreferencePage;
import com.reflexit.magiccards.ui.utils.WaitUtils;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicControl;
import com.reflexit.magiccards.ui.views.IViewPage;
import com.reflexit.magiccards.ui.views.StackPageGroup;
import com.reflexit.magiccards.ui.views.ViewPageContribution;
import com.reflexit.magiccards.ui.views.ViewPageGroup;
import com.reflexit.magiccards.ui.views.analyzers.AbstractMagicControlViewPage;
import com.reflexit.magiccards.ui.views.lib.MyCardsListControl.Presentation;

public class MyCardsView extends AbstractMyCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.MyCardsView";
	private AbstractMagicControlViewPage pageSplit;
	private MyCardPresentation pageFlat;

	public MyCardsView() {
	}

	class MyCardPresentation extends AbstractMagicControlViewPage {
		private Presentation presentation;

		public MyCardPresentation(Presentation type) {
			this.presentation = type;
		}

		@Override
		public AbstractMagicCardsListControl doGetMagicCardListControl() {
			return new MyCardsListControl(MyCardsView.this, presentation) {
				@Override
				public IFilteredCardStore doGetFilteredStore() {
					return DataManager.getCardHandler().getLibraryFilteredStore();
				}

				@Override
				protected void runDoubleClick() {
					MyCardsView.this.runDoubleClick();
				}

				@Override
				public void updateViewer() {
					boolean newGroupped = getFilter().isGroupped();
					int newActive = newGroupped ? 0 : 1;
					getPageGroup().activate(newActive);
					super.updateViewer();
				}
			};
		}
	}

	@Override
	protected ViewPageGroup createPageGroup() {
		return new StackPageGroup() {
			@Override
			public void activate() {
				IViewPage activePage = getPageGroup().getActivePage();
				preActivate(activePage);
				super.activate();
				postActivate(activePage);
			}
		};
	}

	@Override
	protected void createPages() {
		pageSplit = new MyCardPresentation(Presentation.SPLITTREE);
		getPageGroup().add(new ViewPageContribution("", "Groups", null, pageSplit));
		pageFlat = new MyCardPresentation(Presentation.TABLE);
		getPageGroup().add(new ViewPageContribution("", "List", null, pageFlat));
		getPageGroup().setActivePageIndex(0);
	}

	@Override
	protected IMagicControl getMagicControl() {
		return pageSplit.getMagicControl();
	}

	@Override
	public String getHelpId() {
		return MagicUIActivator.helpId("viewcol");
	}

	@Override
	protected void loadInitialInBackground() {
		super.loadInitialInBackground();
		reloadData();
	}

	@Override
	protected String getPreferencePageId() {
		return LibViewPreferencePage.class.getName();
	}

	public void setLocationFilter(Location loc) {
		getMagicControl().setStatus("Loading " + loc + "...");
		WaitUtils.scheduleJob("Updating location", () -> {
			IPreferenceStore preferenceStore = getLocalPreferenceStore();
			Collection ids = Locations.getInstance().getIds();
			String locId = Locations.getInstance().getPrefConstant(loc);
			for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
				String id = (String) iterator.next();
				if (id.startsWith(locId + ".") || id.startsWith(locId + "/") || id.equals(locId)
						|| id.equals(locId + Location.SIDEBOARD_SUFFIX)) {
					preferenceStore.setValue(id, true);
				} else {
					preferenceStore.setValue(id, false);
				}
			}
			reloadData();
		});
	}

	@Override
	public String getId() {
		return ID;
	}
}