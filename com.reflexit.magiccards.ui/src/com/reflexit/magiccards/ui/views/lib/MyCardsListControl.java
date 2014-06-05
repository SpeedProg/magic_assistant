package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.jface.action.MenuManager;

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.commands.ShowFilterHandler;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.CompositeViewerManager;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public abstract class MyCardsListControl extends AbstractMagicCardsListControl {
	public MyCardsListControl(AbstractCardsView abstractCardsView) {
		super(abstractCardsView);
	}

	@Override
	public IMagicColumnViewer createViewerManager() {
		CompositeViewerManager cm = new CompositeViewerManager(getPreferencePageId());
		try {
			MagicColumnCollection treeColumns = (MagicColumnCollection) cm.getTreeViewerManager().getColumnsCollection();
			treeColumns.getGroupColumn().setShowImage(false);
			treeColumns.getSetColumn().setShowImage(true);
			MagicColumnCollection tColumns = (MagicColumnCollection) cm.getTableViewerManager().getColumnsCollection();
			tColumns.getGroupColumn().setShowImage(true);
			tColumns.getSetColumn().setShowImage(false);
		} catch (Exception e) {
			MagicLogger.log(e);
		}
		return cm;
	}

	@Override
	protected MenuManager createGroupMenu() {
		MenuManager x = super.createGroupMenu();
		x.add(createGroupAction(MagicCardField.LOCATION));
		return x;
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
