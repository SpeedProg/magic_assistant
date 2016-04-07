package com.reflexit.magiccards.ui.actions;

import java.util.function.Consumer;

import com.reflexit.magiccards.core.model.GroupOrder;

public class GroupAction extends RadioAction<GroupOrder> {
	public GroupAction(GroupOrder order, boolean checked, Consumer<GroupOrder> onSelect) {
		super(order, checked, onSelect, (o) -> order.getLabel());
	}
}