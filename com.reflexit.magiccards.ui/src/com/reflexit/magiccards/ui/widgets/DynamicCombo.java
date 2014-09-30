package com.reflexit.magiccards.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class DynamicCombo extends Composite {
	private Label text;
	private Combo combo;
	private SlidingPaneAnimation animation;

	public DynamicCombo(Composite parent, int style, String... values) {
		super(parent, style);
		animation = new SlidingPaneAnimation();
		text = new Label(this, SWT.NONE);
		text.addListener(SWT.MouseEnter, new Listener() {
			@Override
			public void handleEvent(Event e) {
				combo.setText(text.getText());
				animation.pushControl(combo, 0, 1);
				e.doit = false;
			}
		});
		combo = new Combo(text.getParent(), SWT.READ_ONLY);
		combo.setItems(values);
		combo.setText(text.getText());
		combo.getParent().moveBelow(text);
		combo.addListener(SWT.MouseExit, new Listener() {
			@Override
			public void handleEvent(Event e) {
				String value = combo.getText();
				text.setText(value);
				animation.popControl(combo, 0, 1);
				e.doit = false;
			}
		});
		animation.setFormLayout(this);
	}

	public void setText(String str) {
		text.setText(str);
		combo.setText(str);
	}

	public String getText() {
		if (getChildren()[0] == text)
			return text.getText();
		return combo.getText();
	}

	public Combo getCombo() {
		return combo;
	}
}
