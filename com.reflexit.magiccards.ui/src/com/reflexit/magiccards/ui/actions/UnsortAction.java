package com.reflexit.magiccards.ui.actions;

import java.util.function.Consumer;

import com.reflexit.magiccards.core.model.SortOrder;

public class UnsortAction extends SortAction {
	public UnsortAction(String text, SortOrder order, Consumer<SortOrder> update) {
		super(text, null, order, update);
		boolean checked = order.isEmpty();
		setChecked(checked);
	}

	@Override
	public void run() {
		if (isChecked()) {
			getSortOrder().clear();
			if (getCallback() != null)
				getCallback().accept(getSortOrder());
		}
	}

	@Override
	public String getToolTipText() {
		return "Restore default sorting order";
	}
}