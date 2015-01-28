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
package com.reflexit.magiccards.ui.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * @author Alena
 * 
 */
public class CheckedTreeSelectionComposite extends Composite {
	private CheckboxTreeViewer fViewer;
	private ArrayList fExpandedElements;
	private IStructuredSelection result;
	private ArrayList fInitialSelections;

	/**
	 * Constructs an instance of <code>ElementTreeSelectionDialog</code>.
	 * 
	 * @param parent
	 *            The shell to parent from.
	 * @param labelProvider
	 *            the label provider to render the entries
	 * @param contentProvider
	 *            the content provider to evaluate the tree structure
	 */
	public CheckedTreeSelectionComposite(Composite parent) {
		super(parent, SWT.NONE);
		setResult(new ArrayList(0));
		this.fExpandedElements = new ArrayList(0);
		this.fInitialSelections = new ArrayList(0);
		createCompositeArea();
	}

	/**
	 * @param arrayList
	 */
	protected void setResult(Collection col) {
		this.result = new StructuredSelection(col);
	}

	public IStructuredSelection getResult() {
		return this.result;
	}

	/**
	 * Sets the initial selection. Convenience method.
	 * 
	 * @param selection
	 *            the initial selection.
	 */
	public void setInitialSelection(Object selection) {
		this.fInitialSelections = new ArrayList();
		this.fInitialSelections.add(selection);
	}

	/**
	 * Sets the initial selection in this selection dialog to the given elements.
	 * 
	 * @param selectedElements
	 *            the array of elements to select
	 */
	public void setInitialSelections(Collection selected) {
		this.fInitialSelections = new ArrayList(selected);
	}

	/**
	 * Sets the tree input.
	 * 
	 * @param input
	 *            the tree input.
	 */
	public void setInput(Object input) {
		this.fViewer.setInput(input);
	}

	/**
	 * Expands elements in the tree.
	 * 
	 * @param elements
	 *            The elements that will be expanded.
	 */
	public void setExpandedElements(Collection elements) {
		this.fExpandedElements = new ArrayList(elements);
	}

	/*
	 * @see SelectionStatusDialog#computeResult()
	 */
	protected void computeResult() {
		setResult(Arrays.asList(this.fViewer.getCheckedElements()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void initialize() {
		BusyIndicator.showWhile(null, new Runnable() {
			@Override
			public void run() {
				CheckedTreeSelectionComposite.this.fViewer
						.setCheckedElements(CheckedTreeSelectionComposite.this.fInitialSelections
								.toArray());
				if (CheckedTreeSelectionComposite.this.fExpandedElements != null) {
					CheckedTreeSelectionComposite.this.fViewer
							.setExpandedElements(CheckedTreeSelectionComposite.this.fExpandedElements
									.toArray());
				}
				updateOKStatus();
			}
		});
	}

	protected Control createCompositeArea() {
		Composite composite = this;
		composite.setLayout(new GridLayout());
		CheckboxTreeViewer treeViewer = createTreeViewer(composite, SWT.BORDER | SWT.SCROLL_PAGE);
		Control buttonComposite = createSelectionButtons(composite);
		GridData data = new GridData(GridData.FILL_BOTH);
		Tree treeWidget = treeViewer.getTree();
		treeWidget.setLayoutData(data);
		treeWidget.setFont(composite.getFont());
		return composite;
	}

	/**
	 * Creates the tree viewer.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param flags
	 *            TODO
	 * @return the tree viewer
	 */
	protected CheckboxTreeViewer createTreeViewer(Composite parent, int flags) {
		this.fViewer = new ContainerCheckedTreeViewer(parent, flags);
		this.fViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateOKStatus();
			}
		});
		return this.fViewer;
	}

	protected void updateOKStatus() {
		// can override to validate and update dialog buttons
	}

	/**
	 * Returns the tree viewer.
	 * 
	 * @return the tree viewer
	 */
	public CheckboxTreeViewer getTreeViewer() {
		return this.fViewer;
	}

	/**
	 * Adds the selection and deselection buttons to the dialog.
	 * 
	 * @param composite
	 *            the parent composite
	 * @return Composite the composite the buttons were created in.
	 */
	protected Composite createSelectionButtons(Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		buttonComposite.setFont(composite.getFont());
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		composite.setData(data);
		Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID,
				WorkbenchMessages.CheckedTreeSelectionDialog_select_all, false);
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] viewerElements = getTreeContentProvider().getElements(getTreeViewer().getInput());
				CheckedTreeSelectionComposite.this.fViewer.setCheckedElements(viewerElements);
				updateOKStatus();
			}
		};
		selectButton.addSelectionListener(listener);
		Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID,
				WorkbenchMessages.CheckedTreeSelectionDialog_deselect_all, false);
		listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CheckedTreeSelectionComposite.this.fViewer.setCheckedElements(new Object[0]);
				updateOKStatus();
			}
		};
		deselectButton.addSelectionListener(listener);
		return buttonComposite;
	}

	protected boolean evaluateIfTreeEmpty(Object input) {
		Object[] elements = getTreeContentProvider().getElements(input);
		if (elements.length > 0) {
			ViewerFilter[] filters = this.fViewer.getFilters();
			if (filters != null) {
				for (ViewerFilter curr : filters) {
					elements = curr.filter(this.fViewer, input, elements);
				}
			}
		}
		return elements.length == 0;
	}

	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setFont(parent.getFont());
		button.setData(new Integer(id));
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		// buttons.put(new Integer(id), button);
		setButtonLayoutData(button);
		return button;
	}

	protected void setButtonLayoutData(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		button.setLayoutData(data);
	}

	protected ITreeContentProvider getTreeContentProvider() {
		return ((ITreeContentProvider) getTreeViewer().getContentProvider());
	}
}
