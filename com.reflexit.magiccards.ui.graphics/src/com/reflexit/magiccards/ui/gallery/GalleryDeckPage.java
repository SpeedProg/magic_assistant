package com.reflexit.magiccards.ui.gallery;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;

import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.actions.CopyPasteActionGroup;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.analyzers.AbstractDeckListPage;
import com.reflexit.magiccards.ui.widgets.ImageAction;

public class GalleryDeckPage extends AbstractDeckListPage {
	protected IAction actionRefresh;
	private CopyPasteActionGroup actionGroupCopyPaste;

	public GalleryDeckPage() {
	}

	@Override
	public Composite createContents(Composite parent) {
		Composite area = super.createContents(parent);
		area.setLayout(new GridLayout());
		createCardsTree(area);
		makeActions();
		return area;
	}

	protected void makeActions() {
		actionGroupCopyPaste = new CopyPasteActionGroup(getSelectionProvider());
		actionRefresh = new ImageAction("Refresh", "icons/clcl16/refresh.gif", this::activate);
	}

	private MagicCardFilter getFilter() {
		return getFilteredStore().getFilter();
	}

	private IFilteredCardStore<ICard> getFilteredStore() {
		return getListControl().getFilteredStore();
	}

	@Override
	public void createCardsTree(Composite parent) {
		super.createCardsTree(parent);
	}

	@Override
	public void setGlobalControlHandlers(IActionBars bars) {
		super.setGlobalControlHandlers(bars);
		actionGroupCopyPaste.setGlobalControlHandlers(bars);
	}

	@Override
	public void fillLocalPullDown(IMenuManager mm) {
		mm.add(actionRefresh);
		super.fillLocalPullDown(mm);
	}

	@Override
	public void fillContextMenu(IMenuManager manager) {
		actionGroupCopyPaste.fillContextMenu(manager);
		// manager.add(view.actionCopy);
		super.fillContextMenu(manager);
	}

	@Override
	public void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
		manager.add(actionRefresh);
	}

	public void reloadData() {
		activate();
	}

	@Override
	public AbstractMagicCardsListControl doGetMagicCardListControl() {
		return new GalleryListControl(view);
	}

	@Override
	public void setFilteredStore(IFilteredCardStore parentfstore) {
		super.setFilteredStore(parentfstore);
	}

	public void updateStore() {
		if (getCardStore() == null || getListControl() == null)
			return;
		IFilteredCardStore<ICard> fistore = getFilteredStore();
		// fistore.clear();
		fistore.setLocation(getCardStore().getLocation());
		// fistore.getCardStore().addAll(getCardStore().getCards());
		// fistore.update();
	}

	@Override
	public void activate() {
		super.activate();
		updateStore();
		if (getFilteredStore().getCardStore() != null) {
			getListControl().loadData(null);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
