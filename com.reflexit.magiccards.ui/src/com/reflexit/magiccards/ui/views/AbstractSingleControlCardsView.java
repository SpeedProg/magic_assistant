package com.reflexit.magiccards.ui.views;

import java.io.IOException;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.WaitUtils;

public abstract class AbstractSingleControlCardsView extends AbstractCardsView {
	private IMagicControl magicControl;

	public AbstractSingleControlCardsView() {
		setMagicControl(createViewControl());
	}

	protected abstract AbstractMagicCardsListControl createViewControl();

	protected IMagicControl getMagicControl() {
		return magicControl;
	}

	protected void setMagicControl(IMagicControl magicControl) {
		this.magicControl = magicControl;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getMagicControl().init(this);
	}

	@Override
	protected boolean hookContextMenu(MenuManager menuMgr) {
		return getMagicControl().hookContextMenu(menuMgr);
	}

	@Override
	protected void createMainControl(Composite parent) {
		getMagicControl().createContents(parent);
	}

	@Override
	protected void activate() {
		super.activate();
		getMagicControl().activate();
	}

	@Override
	protected ISelectionProvider getSelectionProvider() {
		return getMagicControl().getSelectionProvider();
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		manager.remove(actionRefresh.getId());
		super.fillLocalPullDown(manager);
	}

	@Override
	public void refreshView() {
		WaitUtils.syncExec(() -> getMagicControl().refresh());
	}

	@Override
	public void dispose() {
		getMagicControl().dispose();
		super.dispose();
	}

	@Override
	public void setFocus() {
		getMagicControl().getControl().setFocus();
	}

	@Override
	public void reloadData() {
		getMagicControl().reloadData();
	}

	/**
	 * @return
	 */
	public IPersistentPreferenceStore getLocalPreferenceStore() {
		if (getMagicControl() instanceof IMagicCardListControl)
			return ((IMagicCardListControl) getMagicControl()).getColumnsPreferenceStore();
		return null;
	}

	@Override
	public IPersistentPreferenceStore getFilterPreferenceStore() {
		if (getMagicControl() instanceof IMagicCardListControl)
			return ((IMagicCardListControl) getMagicControl()).getElementPreferenceStore();
		return null;
	}

	@Override
	public IFilteredCardStore getFilteredStore() {
		return ((IMagicCardListControl) getMagicControl()).getFilteredStore();
	}

	@Override
	public MagicCardFilter getFilter() {
		return getFilteredStore().getFilter();
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		saveColumnLayout();
		try {
			getFilterPreferenceStore().save();
		} catch (IOException e) {
			MagicUIActivator.log(e);
		}
	}

	protected void saveColumnLayout() {
		if (!getViewSite().getPage().isPartVisible(getViewSite().getPart()))
			return;
		String id = getPreferencePageId();
		if (id != null && getMagicControl() instanceof AbstractMagicCardsListControl) {
			((AbstractMagicCardsListControl) getMagicControl()).saveColumnLayout();
		}
	}

	protected void updateViewer() {
		getMagicControl().updateViewer();
	}
}
