package com.reflexit.magiccards.ui.views.lib;

import java.util.Collection;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;

import com.reflexit.magiccards.core.model.GroupOrder;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.actions.CopyPasteActionGroup;
import com.reflexit.magiccards.ui.actions.RefreshAction;
import com.reflexit.magiccards.ui.commands.ShowFilterHandler;
import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.ExtendedTreeViewer;
import com.reflexit.magiccards.ui.views.IMagicViewer;
import com.reflexit.magiccards.ui.views.LazyTableViewer;
import com.reflexit.magiccards.ui.views.SplitViewer;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public abstract class MyCardsListControl extends AbstractMagicCardsListControl {
	public enum Presentation {
		TABLE, TREE, SPLITTREE, MIX, OTHER
	};

	private Presentation pres = Presentation.TABLE;
	protected IAction actionRefresh;
	private CopyPasteActionGroup actionGroupCopyPaste;

	public MyCardsListControl(AbstractCardsView abstractCardsView, Presentation pres) {
		super(abstractCardsView);
		this.pres = pres;
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		actionGroupCopyPaste = new CopyPasteActionGroup(getSelectionProvider());
		actionRefresh = new RefreshAction(this::reloadData);
	}

	@Override
	public void setGlobalControlHandlers(IActionBars bars) {
		super.setGlobalControlHandlers(bars);
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
	public IPersistentPreferenceStore getPresentaionPreferenceStore() {
		return PreferenceInitializer.getLocalStore(getPresentationPreferenceId());
	}

	public Presentation getPresentation() {
		return pres;
	}

	@Override
	public IMagicViewer createViewer(Composite parent) {
		MagicColumnCollection columns = new MagicColumnCollection(getPreferencePageId());
		if (pres == Presentation.TABLE) {
			LazyTableViewer v = new LazyTableViewer(parent, columns);
			v.hookDragAndDrop();
			return v;
		}
		if (pres == Presentation.TREE) {
			ExtendedTreeViewer v = new ExtendedTreeViewer(parent, columns);
			v.hookDragAndDrop();
			// v.setContentProvider(new RootTreeViewerContentProvider());
			return v;
		}
		if (pres == Presentation.SPLITTREE)
			return new SplitViewer(parent, getPreferencePageId());
		if (pres == Presentation.MIX) {
			MagicCardFilter filter = getFilter();
			if (filter != null && filter.isGroupped()) {
				return new SplitViewer(parent, getPreferencePageId());
			} else {
				return new LazyTableViewer(parent, columns);
			}
		}
		throw new IllegalArgumentException(pres.name());
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
