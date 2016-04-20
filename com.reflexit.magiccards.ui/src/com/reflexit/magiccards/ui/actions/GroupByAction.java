package com.reflexit.magiccards.ui.actions;

import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;

import com.reflexit.magiccards.core.model.GroupOrder;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;

public class GroupByAction extends Action {
	private Runnable reload;
	private MagicCardFilter filter;
	private IPreferenceStore store;
	private Collection<GroupOrder> groups;

	public GroupByAction(Collection<GroupOrder> groups, MagicCardFilter filter, IPreferenceStore store,
			Runnable reload) {
		super("Group By", IAction.AS_DROP_DOWN_MENU);
		this.filter = filter;
		this.reload = reload;
		this.store = store;
		this.groups = groups;
		setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/group_by.png"));
		setMenuCreator(new MenuCreator(this::createMenuManager));
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (enabled)
			setToolTipText(getText());
		else
			setToolTipText(getText() + ": disabled because table presentation does not support groupping");
	}

	public void setFilter(MagicCardFilter filter) {
		this.filter = filter;
	}

	@Override
	public void run() { // group button itself
		if (filter != null && !filter.isGroupped())
			actionGroupBy(new GroupOrder(MagicCardField.CMC));
		else
			actionGroupBy(new GroupOrder());
	}

	public GroupAction createGroupActionNone() {
		return new GroupAction(new GroupOrder(), !filter.isGroupped(), this::actionGroupBy);
	}

	protected void populateGroupMenu(IMenuManager groupMenu) {
		for (GroupOrder groupOrder : groups) {
			groupMenu.add(createGroupAction(groupOrder));
		}
	}

	public MenuManager createMenuManager() {
		MenuManager groupMenu = new MenuManager(getText(), getImageDescriptor(), null);
		groupMenu.setRemoveAllWhenShown(true);
		groupMenu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				populateGroupMenu(manager);
			}
		});
		return groupMenu;
	}

	public GroupAction createGroupAction(GroupOrder order) {
		boolean checked = filter == null ? false : filter.getGroupOrder().equals(order);
		return new GroupAction(order, checked, this::actionGroupBy);
	}

	private void actionGroupBy(GroupOrder order) {
		store.setValue(PreferenceConstants.GROUP_FIELD, order.getKey());
		if (filter != null) {
			filter.setGroupOrder(order);
			order.sortByGroupOrder(filter.getSortOrder());
			store.setValue(PreferenceConstants.SORT_ORDER, filter.getSortOrder().getStringValue());
		}
		reload();
	}

	private void reload() {
		if (reload != null)
			reload.run();
	}
}