package com.reflexit.magiccards.ui.actions;

import java.util.function.Consumer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import com.reflexit.magiccards.core.model.GroupOrder;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class GroupAction extends Action {
	private GroupOrder groups;
	private Consumer<GroupOrder> consumer;

	public GroupAction(String name, ICardField fields[], boolean checked, Consumer<GroupOrder> run) {
		super(name, IAction.AS_RADIO_BUTTON);
		this.groups = new GroupOrder(fields);
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