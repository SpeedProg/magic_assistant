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

import java.util.List;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.reflexit.magiccards.ui.views.nav.CardsNavigatiorManager;

/**
 * @author Alena
 * 
 */
public class CardNavigatorSelectionDialog extends SelectionDialog {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	// the widget group;
	CardsNavigatiorManager manager;
	// the root resource to populate the viewer with
	private Object root;
	// the validation message
	Label statusMessage;
	// for validating the selection
	ISelectionValidator validator;
	private ViewerFilter[] filters;

	/**
	 * Creates a resource container selection dialog rooted at the given
	 * resource. All selections are considered valid.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param root
	 *            the initial root in the tree
	 * @param allowNewContainerName
	 *            <code>true</code> to enable the user to type in a new
	 *            container name, and <code>false</code> to restrict the user to
	 *            just selecting from existing ones
	 * @param message
	 *            the message to be displayed at the top of this dialog, or
	 *            <code>null</code> to display a default message
	 */
	public CardNavigatorSelectionDialog(Shell parentShell, Object initialRoot, boolean allowNewContainerName,
			String message) {
		super(parentShell);
		setTitle("Container Selection");
		this.root = initialRoot;
		if (message != null) {
			setMessage(message);
		} else {
			setMessage("Select a container");
		}
		setTitle(message);
		this.manager = new CardsNavigatiorManager();
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		// create composite
		Composite area = (Composite) super.createDialogArea(parent);
		ISelectionChangedListener listener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (statusMessage != null && validator != null) {
					String errorMsg = validator.isValid(getSelection());
					if (errorMsg == null || errorMsg.equals(EMPTY_STRING)) {
						CardNavigatorSelectionDialog.this.statusMessage.setText(EMPTY_STRING);
						getOkButton().setEnabled(true);
					} else {
						CardNavigatorSelectionDialog.this.statusMessage.setText(errorMsg);
						getOkButton().setEnabled(false);
					}
				}
			}
		};
		this.manager.createContents(area, SWT.NONE);
		getViewer().addSelectionChangedListener(listener);
		if (this.filters != null)
			getViewer().setFilters(this.filters);
		if (this.root != null) {
			getViewer().setInput(this.root);
		}
		if (getInitialElementSelections() != null) {
			getViewer().setSelection(new StructuredSelection(getInitialElementSelections()), true);
		}
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		((Composite) getViewer().getControl()).setLayoutData(gd);
		this.statusMessage = new Label(area, SWT.WRAP);
		this.statusMessage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.statusMessage.setText(" \n "); //$NON-NLS-1$
		this.statusMessage.setFont(parent.getFont());
		return this.dialogArea;
	}

	public ColumnViewer getViewer() {
		return this.manager.getViewer();
	}

	IStructuredSelection getSelection() {
		return (IStructuredSelection) getViewer().getSelection();
	}

	/**
	 * The <code>ContainerSelectionDialog</code> implementation of this
	 * <code>Dialog</code> method builds a list of the selected resource
	 * containers for later retrieval by the client and closes this dialog.
	 */
	@Override
	protected void okPressed() {
		setResult(getSelectionAsList());
		super.okPressed();
	}

	/**
	 * @return
	 */
	private List getSelectionAsList() {
		return getSelection().toList();
	}

	/**
	 * Sets the validator to use.
	 * 
	 * @param validator
	 *            A selection validator
	 */
	public void setValidator(ISelectionValidator validator) {
		this.validator = validator;
	}

	/**
	 * @param viewerFilters
	 */
	public void setFilters(ViewerFilter[] viewerFilters) {
		this.filters = viewerFilters;
	}
}
