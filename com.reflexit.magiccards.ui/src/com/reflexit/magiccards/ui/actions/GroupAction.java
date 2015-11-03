package com.reflexit.magiccards.ui.actions;

import java.util.function.Consumer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import com.reflexit.magiccards.core.model.GroupOrder;

public class GroupAction extends Action {
	private GroupOrder groups;
	private Consumer<GroupOrder> consumer;

	public GroupAction(GroupOrder order, boolean checked, Consumer<GroupOrder> run) {
		super(order.getLabel(), IAction.AS_RADIO_BUTTON);
		this.groups = order;
		if (checked) {
			setChecked(true);
		}
		this.consumer = run;
	}

	@Override
	public void run() {
		if (isChecked() && consumer != null)
			consumer.accept(groups);
	}
}