package com.reflexit.magiccards.ui.actions;

import java.util.function.Supplier;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class MenuCreator implements IMenuCreator {
	private MenuManager dropDownMenuMgr;
	private Supplier<MenuManager> creator;

	public MenuCreator(MenuManager manager) {
		dropDownMenuMgr = manager;
	}

	public MenuCreator(Supplier<MenuManager> creator) {
		this.creator = creator;
	}

	/**
	 * Creates the menu manager for the drop-down.
	 */
	private void createDropDownMenuMgr() {
		if (dropDownMenuMgr == null) {
			dropDownMenuMgr = createMenuManager();
		}
	}

	private Menu listMenu;

	@Override
	public void dispose() {
		if (listMenu != null)
			listMenu.dispose();
	}

	@Override
	public Menu getMenu(Control parent) {
		createDropDownMenuMgr();
		if (listMenu != null)
			listMenu.dispose();
		listMenu = dropDownMenuMgr.createContextMenu(parent);
		return listMenu;
	}

	protected MenuManager createMenuManager() {
		return creator.get();
	}

	@Override
	public Menu getMenu(Menu parent) {
		createDropDownMenuMgr();
		Menu menu = new Menu(parent);
		IContributionItem[] items = dropDownMenuMgr.getItems();
		for (int i = 0; i < items.length; i++) {
			IContributionItem item = items[i];
			IContributionItem newItem = item;
			if (item instanceof ActionContributionItem) {
				newItem = new ActionContributionItem(((ActionContributionItem) item).getAction());
			}
			newItem.fill(menu, -1);
		}
		return menu;
	}
}
