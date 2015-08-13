package com.reflexit.magiccards.ui.actions;

import org.eclipse.jface.action.Action;

import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class SortAction extends Action {
	private ICardField sortField;
	private String name;
	private MagicCardFilter filter;

	public SortAction(String name, ICardField sortField, MagicCardFilter filter) {
		super(name, Action.AS_RADIO_BUTTON);
		this.sortField = sortField;
		this.name = name;
		this.filter = filter;
		if (filter != null && filter.getSortOrder().isTop(sortField)) {
			setChecked(true);
		} else
			setChecked(false);
	}

	@Override
	public void run() {
		if (isChecked()) {
			if (filter != null)
				filter.setSortField(sortField, !filter.getSortOrder().isAccending(sortField));
			reload();
		}
	}

	public void reload() {
		// XXX
	}

	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		if (filter != null) {
			if (checked) {
				String sortLabel = filter.getSortOrder().isAccending() ? "ACC" : "DEC";
				setText(name + " (" + sortLabel + ")");
			} else {
				setText(name);
			}
		}
	}
}