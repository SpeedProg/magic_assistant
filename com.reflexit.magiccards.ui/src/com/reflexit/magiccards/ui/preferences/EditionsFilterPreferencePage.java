package com.reflexit.magiccards.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.FilterHelper;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class EditionsFilterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
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
	private Composite panel;
	private CheckboxTreeViewer treeViewer;

	public EditionsFilterPreferencePage() {
		setTitle("Set Filter");
		// setDescription("A demonstration of a preference page
		// implementation");
	}

	public void init(IWorkbench workbench) {
		// nothing
	}

	public void noDefaultAndApplyButton() {
		super.noDefaultAndApplyButton();
	}

	protected Control createContents(Composite parent) {
		this.panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		this.panel.setLayout(layout);
		this.panel.setFont(parent.getFont());
		this.treeViewer = new CheckboxTreeViewer(this.panel, SWT.CHECK | SWT.BORDER);
		this.treeViewer.setLabelProvider(new EditionsLabelProvider());
		this.treeViewer.setContentProvider(new EditionsContextProvider());
		this.treeViewer.setComparator(new ViewerComparator());
		this.treeViewer.setInput(Editions.getInstance());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 400;
		this.treeViewer.getTree().setLayoutData(gd);
		initializeTree();
		return this.panel;
	}

	private void initializeTree() {
		// TODO Auto-generated method stub
		Collection names = Editions.getInstance().getNames();
		for (Iterator iterator = names.iterator(); iterator.hasNext();) {
			String ed = (String) iterator.next();
			String abbr = Editions.getInstance().getAbbrByName(ed);
			String id = FilterHelper.getPrefConstant(FilterHelper.EDITION, abbr);
			boolean checked = getPreferenceStore().getBoolean(id);
			if (checked)
				this.treeViewer.setChecked(ed, checked);
		}
	}

	protected void performApply() {
		if (this.treeViewer == null)
			return;
		Collection names = Editions.getInstance().getNames();
		for (Iterator iterator = names.iterator(); iterator.hasNext();) {
			String ed = (String) iterator.next();
			boolean checked = this.treeViewer.getChecked(ed);
			String abbr = Editions.getInstance().getAbbrByName(ed);
			String id = FilterHelper.getPrefConstant(FilterHelper.EDITION, abbr);
			if (checked) {
				getPreferenceStore().setValue(id, true);
			} else {
				getPreferenceStore().setValue(id, "");
			}
		}
	}

	public boolean performOk() {
		performApply();
		return true;
	}

	protected IPreferenceStore doGetPreferenceStore() {
		return MagicUIActivator.getDefault().getPreferenceStore();
	}
}
