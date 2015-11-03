package com.reflexit.magiccards.ui.actions;

import java.util.function.Consumer;

import org.eclipse.jface.action.Action;

import com.reflexit.magiccards.core.model.SortOrder;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class SortAction extends Action {
	private ICardField sortField;
	private String name;
	private SortOrder sortOrder;
	private Consumer<SortOrder> callback;

	public SortAction(String name, ICardField sortField, SortOrder porder, Consumer<SortOrder> run) {
		super(name, Action.AS_RADIO_BUTTON);
		this.sortField = sortField;
		this.name = name;
		this.sortOrder = porder == null ? new SortOrder() : porder;
		this.callback = run;
		boolean checked = !sortOrder.isEmpty() && sortOrder.isTop(sortField);
		setChecked(checked);
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	public Consumer<SortOrder> getCallback() {
		return callback;
	}

	public ICardField getSortField() {
		return sortField;
	}

	@Override
	public void run() {
		if (isChecked()) {
			// MagicCardComparator peek = filter.getSortOrder().peek();
			// if (peek.getField() == sortField) {
			// peek.reverse();
			// } else {
			boolean newOrder = !getSortOrder().isAccending(getSortField());
			getSortOrder().setSortField(getSortField(), newOrder);
			// }
			if (getCallback() != null)
				getCallback().accept(getSortOrder());
		}
	}

	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		if (getSortField() == null)
			return;
		boolean acc = getSortOrder().isAccending(getSortField());
		int pos = getSortOrder().getPosition(getSortField());
		if (checked) {
			pos = 1;
			String sortLabel = " (" + pos + " " + (acc ? "ACC" : "DEC") + ")";
			setText(name + sortLabel);
		} else {
			if (pos > 0) {
				String sortLabel = " (" + pos + " " + (acc ? "ACC" : "DEC") + ")";
				setText(name + sortLabel);
			} else
				setText(name);
		}
	}

	public void force() {
		setChecked(true);
		run();
	}
}