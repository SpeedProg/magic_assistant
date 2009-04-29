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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.FilterHelper;

/**
 * Composite that contains checked tree selection for editions.
 * If supplied with preferenceStore can be also used as field editor
 * 
 * <code>
 * c = new EditionsComposite(parent,SWT.CHECK | SWT.BORDER);
 * c.setPreferenceStore(store);
 * c.initialize();
 * ...
 * // when user pressed ok in dialog call this to store values in preference store
 * c.performApply(); 
 * </code>
 * @author Alena
 *
 */
public class EditionsComposite extends Composite {
	public EditionsComposite(Composite parent) {
		this(parent, SWT.CHECK | SWT.BORDER);
	}

	/**
	 * @param parent
	 * @param treeStyle
	 */
	public EditionsComposite(Composite parent, int treeStyle) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout());
		Composite one = (Composite) createContents(this, treeStyle);
		one.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	public class EditionsContextProvider implements ITreeContentProvider {
		ArrayList editions = new ArrayList();

		public void dispose() {
			// TODO Auto-generated method stub
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof Editions) {
				this.editions.clear();
				this.editions.addAll(((Editions) newInput).getEditions());
			}
		}

		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof Editions) {
				return true;
			}
			return false;
		}

		public Object[] getElements(Object inputElement) {
			return this.editions.toArray();
		}
	}
	public class EditionsLabelProvider extends LabelProvider {
	}
	private TreeViewer treeViewer;
	private Composite panel;
	private IPreferenceStore prefStore;
	private boolean checkedTree = false;

	protected Control createContents(Composite parent, int treeStyle) {
		this.panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		this.panel.setLayout(layout);
		this.panel.setFont(parent.getFont());
		if ((treeStyle & SWT.CHECK) != 0) {
			this.checkedTree = true;
			this.treeViewer = new CheckboxTreeViewer(this.panel, treeStyle);
		} else {
			this.checkedTree = false;
			this.treeViewer = new TreeViewer(this.panel, treeStyle);
		}
		this.treeViewer.setLabelProvider(new EditionsLabelProvider());
		this.treeViewer.setContentProvider(new EditionsContextProvider());
		this.treeViewer.setComparator(new ViewerComparator());
		this.treeViewer.setInput(Editions.getInstance());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 300;
		this.treeViewer.getTree().setLayoutData(gd);
		return this.panel;
	}

	public void initialize() {
		Collection names = Editions.getInstance().getNames();
		ArrayList sel = new ArrayList();
		for (Iterator iterator = names.iterator(); iterator.hasNext();) {
			String ed = (String) iterator.next();
			String abbr = Editions.getInstance().getAbbrByName(ed);
			if (abbr == null)
				abbr = ed.replaceAll("\\W", "_");
			String id = FilterHelper.getPrefConstant(FilterHelper.EDITION, abbr);
			boolean checked = getPreferenceStore().getBoolean(id);
			if (checked) {
				if (this.checkedTree) {
					((CheckboxTreeViewer) this.treeViewer).setChecked(ed, checked);
				} else {
					sel.add(ed);
				}
			}
		}
		if (!this.checkedTree) {
			this.treeViewer.setSelection(new StructuredSelection(sel));
		}
	}

	/**
	 * @return
	 */
	public IPreferenceStore getPreferenceStore() {
		return this.prefStore;
	}

	public void setPreferenceStore(IPreferenceStore s) {
		this.prefStore = s;
	}

	public void performApply() {
		if (this.treeViewer == null)
			return;
		Collection names = Editions.getInstance().getNames();
		for (Iterator iterator = names.iterator(); iterator.hasNext();) {
			String ed = (String) iterator.next();
			boolean checked = false;
			if (this.checkedTree) {
				checked = ((CheckboxTreeViewer) this.treeViewer).getChecked(ed);
			}
			String abbr = Editions.getInstance().getAbbrByName(ed);
			String id = FilterHelper.getPrefConstant(FilterHelper.EDITION, abbr);
			getPreferenceStore().setValue(id, checked);
		}
		if (!this.checkedTree) {
			IStructuredSelection selection = (IStructuredSelection) this.treeViewer.getSelection();
			for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
				String ed = (String) iterator.next();
				String abbr = Editions.getInstance().getAbbrByName(ed);
				String id = FilterHelper.getPrefConstant(FilterHelper.EDITION, abbr);
				getPreferenceStore().setValue(id, true);
			}
		}
	}

	public IStructuredSelection getSelection() {
		return (IStructuredSelection) this.treeViewer.getSelection();
	}
}
