package com.reflexit.magiccards.ui.actions;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.reflexit.magiccards.core.model.MagicCardComparator;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.widgets.ImageAction;

public class SortByAction extends ImageAction {
	private MagicCardFilter filter;
	private ColumnCollection sortColumns;
	private Runnable reload;

	public SortByAction(ColumnCollection sortColumns, MagicCardFilter filter, Runnable reload) {
		super("Sort By", "icons/clcl16/sort.gif", IAction.AS_DROP_DOWN_MENU);
		this.filter = filter;
		this.sortColumns = sortColumns;
		this.reload = reload;
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
				MenuManager menuSortLocal = createMenuManager();
				listMenu = menuSortLocal.createContextMenu(parent);
				return listMenu;
			}

			@Override
			public Menu getMenu(Menu parent) {
				return null;
			}
		});
	}

	public void populateSortMenu(IMenuManager menuSort) {
		menuSort.removeAll();
		MagicCardFilter filter = getFilter();
		menuSort.add(new UnsortAction("Unsort", filter, this::reloadData));
		Collection<AbstractColumn> columns = sortColumns.getColumns();
		for (Iterator<AbstractColumn> iterator = columns.iterator(); iterator.hasNext();) {
			final AbstractColumn man = iterator.next();
			String name = man.getColumnFullName();
			ICardField sortField = man.getSortField();
			menuSort.add(new SortAction(name, sortField, filter, this::reloadData));
		}
	}

	public MagicCardFilter getFilter() {
		return filter;
	}

	@Override
	public void run() {
		MagicCardFilter filter = getFilter();
		MagicCardComparator peek = filter.getSortOrder().peek();
		peek.reverse();
		reloadData();
	}

	protected void reloadData() {
		if (reload != null)
			reload.run();
	}

	public MenuManager createMenuManager() {
		MenuManager menuSortLocal = new MenuManager("Sort By");
		menuSortLocal.setRemoveAllWhenShown(true);
		menuSortLocal.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				populateSortMenu(manager);
			}
		});
		return menuSortLocal;
	}
}