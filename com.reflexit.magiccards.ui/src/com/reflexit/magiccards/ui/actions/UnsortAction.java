package com.reflexit.magiccards.ui.actions;

import org.eclipse.jface.action.Action;

import com.reflexit.magiccards.core.model.MagicCardFilter;

public class UnsortAction extends Action {
	private MagicCardFilter filter;

	public UnsortAction(String text, MagicCardFilter filter) {
		super(text, Action.AS_PUSH_BUTTON);
		this.filter = filter;
	}

	@Override
	public void run() {
		filter.setNoSort();
		reload();
	}

	public void reload() {
		// XXX
	}

	@Override
	public String getToolTipText() {
		return "Restore default sorting order";
	}
}