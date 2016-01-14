package com.reflexit.magiccards.ui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ComboContributionItem extends ControlContribution {
	private Combo control;
	private Consumer<String> onClick;
	private List<String> labels;
	private String sel;

	@Override
	protected int computeWidth(Control control) {
		return 200;
	}

	public ComboContributionItem(String id) {
		super(id);
		this.labels = new ArrayList<>();
	}

	public ComboContributionItem(String id, Consumer<String> onClick) {
		this(id);
		this.onClick = onClick;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
		if (control != null) {
			control.removeAll();
			for (final String label : labels) {
				control.add(label);
			}
			if (sel != null)
				control.setText(sel);
			else if (!labels.isEmpty())
				control.setText(labels.iterator().next());
		}
	}

	public void setSelection(String label) {
		sel = label;
		if (control != null)
			control.setText(label);
	}

	@Override
	public boolean isSeparator() {
		return false;
	}

	@Override
	protected Control createControl(Composite parent) {
		control = new Combo(parent, SWT.READ_ONLY);
		control.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSelect(control.getText());
			}
		});
		setLabels(getLabels());
		control.setEnabled(true);
		control.setSize(computeWidth(control), 24);
		return control;
	}

	protected void onSelect(String text) {
		if (onClick != null)
			onClick.accept(text);
	}

	protected List<String> getLabels() {
		return labels;
	}

	public Combo getControl() {
		return control;
	}
}