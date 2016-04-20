package com.reflexit.magiccards.ui.views.lib;

import java.util.Collection;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;

import com.reflexit.magiccards.core.model.GroupOrder;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.actions.CopyPasteActionGroup;
import com.reflexit.magiccards.ui.actions.RefreshAction;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.Presentation;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public abstract class MyCardsListControl extends AbstractMagicCardsListControl {
	protected IAction actionRefresh;
	private CopyPasteActionGroup actionGroupCopyPaste;

	public MyCardsListControl(Presentation pres) {
		super(pres);
	}

	public MyCardsListControl() {
		super(false);
	}

	@Override
	public void handleEvent(CardEvent event) {
		mcpEventHandler(event);
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		actionGroupCopyPaste = new CopyPasteActionGroup(getSelectionProvider());
		actionRefresh = new RefreshAction(this::refresh);
	}

	@Override
	public void setGlobalHandlers(IActionBars bars) {
		super.setGlobalHandlers(bars);
		actionGroupCopyPaste.setGlobalControlHandlers(bars);
	}

	@Override
	public void fillLocalPullDown(IMenuManager mm) {
		// mm.add(actionRefresh);
		super.fillLocalPullDown(mm);
	}

	@Override
	public void fillContextMenu(IMenuManager manager) {
		actionGroupCopyPaste.fillContextMenu(manager);
		super.fillContextMenu(manager);
	}

	@Override
	public void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
		manager.add(actionRefresh);
	}

	protected String getPresentationPreferenceId() {
		String id = getPreferencePageId();
		if (id != null && getPresentation() != null) {
			return id + "." + getPresentation().name();
		}
		return id;
	}

	@Override
	protected String getPreferencePageId() {
		return getViewPreferencePageId();
	}

	@Override
	public Collection<GroupOrder> getGroups() {
		Collection<GroupOrder> res = super.getGroups();
		res.add(new GroupOrder(MagicCardField.LOCATION));
		return res;
	}

	@Override
	public ColumnCollection getSortColumnCollection() {
		return new MagicColumnCollection("");
	}

	@Override
	protected abstract IFilteredCardStore<ICard> doGetFilteredStore();
}
