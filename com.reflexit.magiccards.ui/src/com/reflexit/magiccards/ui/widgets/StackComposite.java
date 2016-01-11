/*******************************************************************************
 * Copyright (c) 2016 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.ui.widgets;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Stack Composite - Switch between panes controlled by API
 */
public class StackComposite extends Composite {
	private Composite area;
	private Map<String, Composite> tabMap; // label ==> tab
	private StackLayout layout;

	public StackComposite(Composite parent, int style) {
		super(parent, style);
		tabMap = new LinkedHashMap<String, Composite>();
		setLayout(GridLayoutFactory.fillDefaults().create());
		createContents(this).setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
	}

	public void addItem(String label, Composite tab) {
		tab.setParent(area);
		tabMap.put(label, tab);
		if (layout.topControl == null) {
			layout.topControl = tab;
		}
	}

	public void deleteItem(String label) {
		Composite tab = tabMap.get(label);
		if (tab != null) {
			tab.dispose();
			tabMap.remove(label);
		}
	}

	public void setSelection(String label) {
		setPage(label);
	}

	protected Control createContents(Composite parent) {
		area = createTabArea(this);
		return area;
	}

	public Composite getStackParent() {
		return area;
	}

	protected Composite createTabArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		layout = new StackLayout();
		comp.setLayout(layout);
		return comp;
	}

	protected void setPage(String label) {
		layout.topControl = tabMap.get(label);
		getStackParent().layout();
	}

	public Control getTopControl() {
		return layout != null ? layout.topControl : null;
	}
}
