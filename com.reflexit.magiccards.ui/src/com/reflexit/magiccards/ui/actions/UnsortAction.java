package com.reflexit.magiccards.ui.actions;

import org.eclipse.jface.action.Action;

import com.reflexit.magiccards.core.model.MagicCardFilter;

public class UnsortAction extends Action {
	private MagicCardFilter filter;
	private Runnable run;

	public UnsortAction(String text, MagicCardFilter filter, Runnable update) {
		super(text, Action.AS_RADIO_BUTTON);
		this.filter = filter;
		this.run = update;
		boolean checked = filter != null && filter.getSortOrder().isEmpty();
		setChecked(checked);
	}

	@Override
	public void run() {
		if (isChecked()) {
			filter.setNoSort();
			if (run != null)
				run.run();
		}
	}

	@Override
	public String getToolTipText() {
		return "Restore default sorting order";
	}
}