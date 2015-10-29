package com.reflexit.magiccards.ui.widgets;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
		final Composite textParent = new Composite(this, SWT.NONE);
		textParent.setLayout(GridLayoutFactory.fillDefaults().create());
		textParent.setBackground(parent.getBackground());
		text = new Label(textParent, SWT.NONE);
		text.setBackground(parent.getBackground());
		text.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).create());
		text.addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(Event e) {
				activateCombo();
				// e.doit = false;
			}
		});
		combo = new Combo(this, SWT.READ_ONLY);
		combo.setItems(values);
		combo.setText(text.getText());
		combo.getParent().moveBelow(text);
		combo.setBackground(parent.getBackground());
		combo.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (combo.getListVisible())
					return;
				String value = combo.getText();
				text.setText(value);
				animation.popControl(combo, 0, 1);
			}

			@Override
			public void focusGained(FocusEvent e) {
				// nothing
			}
		});
		// combo.addListener(SWT.MouseExit, new Listener() {
		// @Override
		// public void handleEvent(Event e) {
		// String value = combo.getText();
		// text.setText(value);
		// animation.popControl(combo, 0, 1);
		// // e.doit = false;
		// }
		// });
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String value = combo.getText();
				text.setText(value);
				animation.popControl(combo, 0, 1);
			}
		});
		animation.setAnimationLayoutOn(this);
		layout(true);
	}

	public void setText(String str) {
		text.setText(str);
		combo.setText(str);
	}

	public String getText() {
		if (getChildren()[0] == combo)
			return combo.getText();
		return text.getText();
	}

	public Combo getCombo() {
		return combo;
	}

	public void activateCombo() {
		combo.setText(text.getText());
		animation.pushControl(combo, 0, 1);
	}
}
