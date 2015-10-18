package com.reflexit.magiccards.ui.gallery;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;

import com.reflexit.magiccards.core.model.MagicCardComparator;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.actions.CopyPasteActionGroup;
import com.reflexit.magiccards.ui.actions.SortAction;
import com.reflexit.magiccards.ui.actions.UnsortAction;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.analyzers.AbstractDeckListPage;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;
import com.reflexit.magiccards.ui.widgets.ImageAction;

public class GalleryDeckPage extends AbstractDeckListPage {
	protected IAction actionRefresh;
	private CopyPasteActionGroup actionGroupCopyPaste;
	private ImageAction actionSort;

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
		actionRefresh = new ImageAction("Refresh", "icons/clcl16/refresh.gif", () -> activate());
		this.actionSort = new ImageAction("Sort By", "icons/clcl16/sort.gif", IAction.AS_DROP_DOWN_MENU) {
			{
				setMenuCreator(new IMenuCreator() {
					private Menu listMenu;

					@Override
					public void dispose() {
						if (listMenu != null)
							listMenu.dispose();
					}

					@Override
					public Menu getMenu(Control parent) {
						if (listMenu != null)
							listMenu.dispose();
						MenuManager menuSortLocal = new MenuManager("Sort By");
						populateSortMenu(menuSortLocal);
						listMenu = menuSortLocal.createContextMenu(parent);
						return listMenu;
					}

					@Override
					public Menu getMenu(Menu parent) {
						return null;
					}
				});
			}

			@Override
			public void run() {
				MagicCardFilter filter = getFilter();
				MagicCardComparator peek = filter.getSortOrder().peek();
				peek.reverse();
				reloadData();
			}
		};
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
		MenuManager menuSort = new MenuManager("Sort By");
		populateSortMenu(menuSort);
		mm.add(menuSort);
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
		manager.add(actionSort);
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

	public void populateSortMenu(MenuManager menuSort) {
		menuSort.removeAll();
		MagicCardFilter filter = getFilter();
		menuSort.add(new UnsortAction("Unsort", filter) {
			@Override
			public void reload() {
				reloadData();
			}
		});
		MagicColumnCollection magicColumnCollection = new MagicColumnCollection(GalleryPreferencePage.getId());
		Collection<AbstractColumn> columns = magicColumnCollection.getColumns();
		for (Iterator<AbstractColumn> iterator = columns.iterator(); iterator.hasNext();) {
			final AbstractColumn man = iterator.next();
			String name = man.getColumnFullName();
			ICardField sortField = man.getSortField();
			menuSort.add(new SortAction(name, sortField, filter) {
				@Override
				public void reload() {
					reloadData();
				}
			});
		}
	}
}
