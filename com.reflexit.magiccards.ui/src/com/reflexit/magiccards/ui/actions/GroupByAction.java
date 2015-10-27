package com.reflexit.magiccards.ui.actions;

import java.util.Arrays;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.core.model.GroupOrder;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class GroupByAction extends Action {
	private Runnable reload;
	private MagicCardFilter filter;
	private IPreferenceStore store;

	public GroupByAction(MagicCardFilter filter, IPreferenceStore store, Runnable reload) {
		super("Group By", IAction.AS_DROP_DOWN_MENU);
		this.filter = filter;
		this.reload = reload;
		this.store = store;
		setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/group_by.png"));
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
				listMenu = createMenuManager().createContextMenu(parent);
				return listMenu;
			}

			@Override
			public Menu getMenu(Menu parent) {
				return null;
			}
		});
	}

	@Override
	public void run() { // group button itself
		if (!filter.isGroupped())
			actionGroupBy(new GroupOrder(MagicCardField.CMC));
		else
			actionGroupBy(new GroupOrder());
	}

	public GroupAction createGroupActionNone() {
		return new GroupAction("None", null, !filter.isGroupped(), this::actionGroupBy);
	}

	protected String createGroupName(ICardField[] fields) {
		String res = "";
		for (int i = 0; i < fields.length; i++) {
			ICardField field = fields[i];
			if (i != 0) {
				res += "/";
			}
			res += field.toString();
		}
		return res;
	}

	protected void populateGroupMenu(IMenuManager groupMenu) {
		groupMenu.add(createGroupActionNone());
		groupMenu.add(createGroupAction("Color", MagicCardField.COST));
		groupMenu.add(createGroupAction("Cost", MagicCardField.CMC));
		groupMenu.add(createGroupAction(MagicCardField.TYPE));
		groupMenu.add(createGroupAction("Core/Block/Set/Rarity", new ICardField[] { MagicCardField.SET_CORE,
				MagicCardField.SET_BLOCK, MagicCardField.SET, MagicCardField.RARITY }));
		groupMenu.add(createGroupAction(MagicCardField.SET));
		groupMenu.add(createGroupAction("Set/Rarity", new ICardField[] { MagicCardField.SET, MagicCardField.RARITY }));
		groupMenu.add(createGroupAction(MagicCardField.RARITY));
		groupMenu.add(createGroupAction(MagicCardField.NAME));
	}

	public MenuManager createMenuManager() {
		MenuManager groupMenu = new MenuManager("Group By",
				MagicUIActivator.getImageDescriptor("icons/clcl16/group_by.png"), null);
		groupMenu.setRemoveAllWhenShown(true);
		groupMenu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				populateGroupMenu(manager);
			}
		});
		return groupMenu;
	}

	public GroupAction createGroupAction(ICardField field) {
		return createGroupAction(field.getLabel(), field);
	}

	public GroupAction createGroupAction(String name, ICardField[] fields) {
		boolean checked = Arrays.equals(filter.getGroupFields(), fields);
		return new GroupAction(name, fields, checked, this::actionGroupBy);
	}

	public GroupAction createGroupAction(String name, ICardField field) {
		return createGroupAction(name, new ICardField[] { field });
	}

	private void actionGroupBy(GroupOrder order) {
		ICardField[] fields = order.getFields();
		store.setValue(FilterField.GROUP_FIELD.toString(), fields == null ? "" : createGroupName(fields));
		updateGroupBy(fields);
		reload();
	}

	private void reload() {
		if (reload != null)
			reload.run();
	}

	public void updateGroupBy(ICardField[] fields) {
		if (filter == null)
			return;
		ICardField[] oldIndex = filter.getGroupFields();
		if (Arrays.equals(oldIndex, fields))
			return;
		if (fields != null) {
			filter.setSortField(fields[0], true);
			filter.setGroupFields(fields);
		} else {
			filter.setGroupFields(null);
		}
	}
}