/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.ui.dialogs;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorContentProvider;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorLabelProvider;

/**
 * @author Alena
 *
 */
public class LocationDialog extends SelectionDialog {
	private TreeViewer treeViewer;
	private String iniLoc;

	/**
	 * @param parentShell
	 */
	public LocationDialog(Shell parentShell) {
		super(parentShell);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(3, false);
		panel.setLayout(layout);
		panel.setFont(parent.getFont());
		this.treeViewer = new TreeViewer(panel, SWT.FULL_SELECTION);
		this.treeViewer.setLabelProvider(new CardsNavigatorLabelProvider());
		this.treeViewer.setContentProvider(new CardsNavigatorContentProvider());
		this.treeViewer.setComparator(new ViewerComparator());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 400;
		panel.setLayoutData(gd);
		initializeTree();
		return panel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		setSelectionResult(new Object[] { ((IStructuredSelection) this.treeViewer.getSelection()).getFirstElement() });
		super.okPressed();
	}

	private void initializeTree() {
		CardOrganizer root = new CardOrganizer("fake", null);
		CardOrganizer top = new CardOrganizer("All Cards", root);
		ModelRoot mroot = DataManager.getModelRoot();
		top.addChild(mroot.getCollectionsContainer());
		top.addChild(mroot.getDeckContainer());
		this.treeViewer.setInput(root);
		this.treeViewer.setExpandedState(top, true);
		// load preferences
		if (this.iniLoc != null)
			initSelection(top);
	}

	private void initSelection(CardElement root) {
		boolean checked = root.getLocation().equals(this.iniLoc);
		if (checked) {
			this.treeViewer.setSelection(new StructuredSelection(root), true);
		} else if (root instanceof CardOrganizer) {
			for (Iterator iterator = ((CardOrganizer) root).getChildren().iterator(); iterator.hasNext();) {
				CardElement el = (CardElement) iterator.next();
				initSelection(el);
			}
		}
	}

	/**
	 * @param iniLoc
	 */
	public void setInitialLocation(String iniLoc) {
		this.iniLoc = iniLoc;
	}

	/**
	 * @return
	 */
	public String getLocation() {
		Object[] result = getResult();
		if (result.length == 0)
			return null;
		Object res = result[0];
		if (res instanceof CardOrganizer) {
			return ((CardOrganizer) res).getLocation();
		}
		return null;
	}
}
