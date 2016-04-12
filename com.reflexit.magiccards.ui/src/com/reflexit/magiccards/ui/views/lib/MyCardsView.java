package com.reflexit.magiccards.ui.views.lib;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.Locations;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.actions.ViewAsAction;
import com.reflexit.magiccards.ui.preferences.LibViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.utils.WaitUtils;
import com.reflexit.magiccards.ui.views.Presentation;
import com.reflexit.magiccards.ui.views.StackPageGroup;
import com.reflexit.magiccards.ui.views.ViewPageContribution;
import com.reflexit.magiccards.ui.views.ViewPageGroup;

public class MyCardsView extends AbstractMyCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.MyCardsView";
	private IFilteredCardStore mystore;

	public MyCardsView() {
	}

	class MyCardPresentation extends MyCardsListControl {
		public MyCardPresentation(Presentation type) {
			super(type);
		}

		@Override
		public IFilteredCardStore doGetFilteredStore() {
			return mystore;
		}

		@Override
		protected void hookDoubleClickAction() {
			viewer.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					MyCardsView.this.runDoubleClick();
				}
			});
		}

		@Override
		protected void makeActions() {
			super.makeActions();
			this.actionViewAs = new ViewAsAction(Arrays.asList(Presentation.values()), this::onViewChange) {
				@Override
				public boolean isChecked(Object object) {
					String cur = getLocalPreferenceStore().getString(PreferenceConstants.PRESENTATION_VIEW);
					if (cur != null && cur.equals(((Presentation) object).key())) {
						return true;
					}
					return super.isChecked();
				}
			};
			if (getPresentation() == Presentation.TABLE)
				getGroupAction().setEnabled(false);
		}

		private void onViewChange(Presentation selected) {
			getLocalPreferenceStore().setValue(PreferenceConstants.PRESENTATION_VIEW, selected.key());
			int i = getPageGroup().getPageIndex(selected.getLabel());
			getPageGroup().setActivePageIndex(i);
			getView().activate();
		}
	}

	public MyCardsView getView() {
		return this;
	}

	@Override
	protected ViewPageGroup createPageGroup() {
		return new StackPageGroup(this::preActivate, this::postActivate);
	}

	@Override
	protected void createPages() {
		addPage(Presentation.TREE, "Cards");
		addPage(Presentation.TABLE, "List");
		addPage(Presentation.SPLITTREE, "Groups");
		addPage(Presentation.GALLERY, "Gallery");
	}

	protected void addPage(Presentation pres, String name) {
		getPageGroup().add(new ViewPageContribution(pres.name(), pres.getLabel(), null, new MyCardPresentation(pres)));
	}

	@Override
	protected void createMainControl(Composite parent) {
		super.createMainControl(parent);
		String cur = getLocalPreferenceStore().getString(PreferenceConstants.PRESENTATION_VIEW);
		if (cur != null) {
			Presentation pres = Presentation.valueOf(cur);
			int i = getPageGroup().getPageIndex(pres.getLabel());
			getPageGroup().setActivePageIndex(i);
		}
	}

	@Override
	protected void makeActions() {
		super.makeActions();
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
	}

	@Override
	public String getHelpId() {
		return MagicUIActivator.helpId("viewcol");
	}

	@Override
	protected void loadInitialInBackground() {
		super.loadInitialInBackground();
		mystore = DataManager.getCardHandler().getLibraryFilteredStore();
		reloadData();
	}

	@Override
	protected String getPreferencePageId() {
		return LibViewPreferencePage.class.getName();
	}

	public void setLocationFilter(Location loc) {
		// getMagicControl().setStatus("Loading " + loc + "...");
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