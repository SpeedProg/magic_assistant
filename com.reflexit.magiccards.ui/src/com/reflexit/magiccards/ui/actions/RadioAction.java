package com.reflexit.magiccards.ui.actions;

import java.util.function.Consumer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

public class RadioAction<T> extends Action {
	private T element;
	private Consumer<T> consumer;

	public RadioAction(T element, boolean checked, Consumer<T> run, IPlainLabelProvider labelProvider) {
		super(labelProvider.getText(element), IAction.AS_RADIO_BUTTON);
		this.element = element;
		this.consumer = run;
		setChecked(checked);
	}

	@Override
	public void run() {
		if (isChecked() && consumer != null)
			consumer.accept(element);
	}
}