package com.reflexit.magiccards.ui.actions;

import java.util.function.Consumer;

import com.reflexit.magiccards.core.model.GroupOrder;
import com.reflexit.magiccards.core.model.SortOrder;

public class UnsortAction extends SortAction {
	public UnsortAction(SortOrder order, GroupOrder groupOrder, Consumer<SortOrder> update) {
		super("Unsort", null, order, groupOrder, update);
	}

	@Override
	public String getToolTipText() {
		return "Restore default sorting order";
	}
}