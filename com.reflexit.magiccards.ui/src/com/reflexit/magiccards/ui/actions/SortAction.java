package com.reflexit.magiccards.ui.actions;

import org.eclipse.jface.action.Action;

import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class SortAction extends Action {
	private ICardField sortField;
	private String name;
	private MagicCardFilter filter;
	private Runnable run;

	public SortAction(String name, ICardField sortField, MagicCardFilter filter, Runnable run) {
		super(name, Action.AS_RADIO_BUTTON);
		this.sortField = sortField;
		this.name = name;
		this.filter = filter;
		this.run = run;
		boolean checked = filter != null && !filter.getSortOrder().isEmpty() && filter.getSortOrder().isTop(sortField);
		System.err.println(checked + " " + sortField);
		setChecked(checked);
	}

	@Override
	public void run() {
		if (isChecked()) {
			if (filter != null) {
				// MagicCardComparator peek = filter.getSortOrder().peek();
				// if (peek.getField() == sortField) {
				// peek.reverse();
				// } else {
				boolean newOrder = !filter.getSortOrder().isAccending(sortField);
				filter.setSortField(sortField, newOrder);
				// }
			}
			if (run != null)
				run.run();
		}
	}

	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		if (filter != null) {
			boolean acc = filter.getSortOrder().isAccending(sortField);
			int prio = filter.getSortOrder().getPriority(sortField);
			if (checked) {
				prio = 1;
				String sortLabel = " (" + prio + " " + (acc ? "ACC" : "DEC") + ")";
				setText(name + sortLabel);
			} else {
				if (prio > 0) {
					String sortLabel = " (" + prio + " " + (acc ? "ACC" : "DEC") + ")";
					setText(name + sortLabel);
				} else
					setText(name);
			}
		}
	}
}