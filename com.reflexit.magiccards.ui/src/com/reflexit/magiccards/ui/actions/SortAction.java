package com.reflexit.magiccards.ui.actions;

import java.util.function.Consumer;

import org.eclipse.jface.action.Action;

import com.reflexit.magiccards.core.model.GroupOrder;
import com.reflexit.magiccards.core.model.SortOrder;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class SortAction extends Action {
	private ICardField sortField;
	private String name;
	private SortOrder sortOrder;
	private Consumer<SortOrder> callback;
	private GroupOrder groupOrder;

	public SortAction(String name, ICardField sortField, SortOrder porder, GroupOrder primary,
			Consumer<SortOrder> run) {
		super(name, Action.AS_PUSH_BUTTON);
		this.sortField = sortField;
		this.name = name;
		this.sortOrder = porder == null ? new SortOrder() : porder;
		this.callback = run;
		this.groupOrder = primary;
		update();
	}

	public final SortOrder getSortOrder() {
		return sortOrder;
	}

	public Consumer<SortOrder> getCallback() {
		return callback;
	}

	public final ICardField getSortField() {
		return sortField;
	}

	@Override
	public void run() {
		if (sortField == null)
			sortOrder.clear();
		else {
			boolean newOrder = !sortOrder.isAccending(sortField);
			sortOrder.setSortField(sortField, newOrder);
		}
		if (groupOrder != null)
			groupOrder.sortByGroupOrder(sortOrder);
		if (getCallback() != null)
			getCallback().accept(sortOrder);
	}

	public void update() {
		ICardField sf = getSortField();
		if (sf == null)
			return;
		boolean acc = getSortOrder().isAccending(sf);
		int pos = getSortOrder().getPosition(sf);
		int gpos = groupOrder == null ? -1 : groupOrder.getPosition(sf);
		String g = gpos == -1 ? (pos >= 0 ? pos + "" : "") : (gpos + 1) + "";
		if (!g.isEmpty()) {
			String sortLabel = (acc ? "ACC" : "DEC");
			setText(name + " (#" + g + " " + (gpos >= 0 ? " Group " : "") + sortLabel + ")");
		} else
			setText(name);
	}

	public void force() {
		run();
	}
}