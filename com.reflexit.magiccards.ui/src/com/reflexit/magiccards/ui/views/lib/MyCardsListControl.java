package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.jface.action.IMenuManager;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.actions.GroupByAction;
import com.reflexit.magiccards.ui.commands.ShowFilterHandler;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.CompositeViewerManager;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;

public abstract class MyCardsListControl extends AbstractMagicCardsListControl {
	public MyCardsListControl(AbstractCardsView abstractCardsView) {
		super(abstractCardsView);
	}

	@Override
	public IMagicColumnViewer createViewerManager() {
		return new CompositeViewerManager(getPreferencePageId());
	}

	@Override
	protected String getPreferencePageId() {
		return getViewPreferencePageId();
	}

	@Override
	protected void createGroupAction() {
		this.actionGroupBy = new GroupByAction(getFilter(), getLocalPreferenceStore(), () -> {
			manager.setGrouppingEnabled(getFilter().isGroupped());
			reloadData();
		}) {
			@Override
			protected void populateGroupMenu(IMenuManager groupMenu) {
				super.populateGroupMenu(groupMenu);
				groupMenu.add(createGroupAction(MagicCardField.LOCATION));
			}
		};
	}

	@Override
	protected void runShowFilter() {
		if (ShowFilterHandler.execute()) {
			reloadData();
		}
		// CardFilter.open(getViewSite().getShell());
		// MyCardsFilterDialog cardFilterDialog = new
		// MyCardsFilterDialog(getShell(),
		// getFilterPreferenceStore());
		// if (cardFilterDialog.open() == IStatus.OK)
		// reloadData();
	}

	@Override
	protected abstract IFilteredCardStore<ICard> doGetFilteredStore();
}
