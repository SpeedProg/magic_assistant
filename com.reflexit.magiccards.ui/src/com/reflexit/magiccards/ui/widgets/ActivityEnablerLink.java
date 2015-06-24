package com.reflexit.magiccards.ui.widgets;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;

import com.reflexit.magiccards.ui.MagicUIActivator;

public class ActivityEnablerLink extends Composite {
	public ActivityEnablerLink(Composite parent, String id, String label, boolean enable) {
		super(parent, SWT.NONE);
		setLayout(GridLayoutFactory.fillDefaults().create());
		Link link = new Link(this, SWT.NONE);
		link.setLayoutData(GridDataFactory.swtDefaults().create());
		if (label.contains("</a>")) {
			link.setText(label);
		} else {
			link.setText("<a>" + label + "</a>");
		}
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clicked(id, enable);
			}
		});
	}

	protected void clicked(String id, boolean enable) {
		MagicUIActivator.setActivityEnabled(id, enable);
	}
}
