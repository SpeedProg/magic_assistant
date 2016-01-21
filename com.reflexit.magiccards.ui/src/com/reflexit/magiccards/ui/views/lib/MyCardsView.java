package com.reflexit.magiccards.ui.views.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.Locations;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.LibViewPreferencePage;
import com.reflexit.magiccards.ui.utils.WaitUtils;
import com.reflexit.magiccards.ui.views.IViewPage;
import com.reflexit.magiccards.ui.views.Presentation;
import com.reflexit.magiccards.ui.views.StackPageGroup;
import com.reflexit.magiccards.ui.views.ViewPageContribution;
import com.reflexit.magiccards.ui.views.ViewPageGroup;
import com.reflexit.magiccards.ui.widgets.ComboContributionItem;

public class MyCardsView extends AbstractMyCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.MyCardsView";
	private IFilteredCardStore mystore;

	public MyCardsView() {
	}

	class PresentationComboContributionItem extends ComboContributionItem {
		protected PresentationComboContributionItem(String string) {
			super("pres_id");
			ArrayList<String> list = new ArrayList<>();
			for (final Presentation rt : Presentation.values()) {
				list.add(rt.getLabel());
			}
			setLabels(list);
			setSelection(string);
		}

		@Override
		protected int computeWidth(Control control) {
			return 110;
		}

		@Override
		protected void onSelect(String text) {
			getPageGroup().activate(text);
		}
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
		public void fillLocalToolBar(IToolBarManager manager) {
			manager.add(new PresentationComboContributionItem(getPresentation().getLabel()));
			super.fillLocalToolBar(manager);
		}

		@Override
		protected void makeActions() {
			super.makeActions();
			if (getPresentation() == Presentation.TABLE)
				getGroupAction().setEnabled(false);
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
		addPage(Presentation.TABLE);
		addPage(Presentation.SPLITTREE);
		addPage(Presentation.TREE);
		addPage(Presentation.GALLERY);
	}

	protected void addPage(Presentation pres) {
		getPageGroup().add(new ViewPageContribution("", pres.getLabel(), null, new MyCardPresentation(pres)));
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