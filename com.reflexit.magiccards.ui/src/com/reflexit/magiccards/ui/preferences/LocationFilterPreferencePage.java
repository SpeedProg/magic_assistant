package com.reflexit.magiccards.ui.preferences;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Locations;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.MagicDbContainter;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorContentProvider;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorLabelProvider;
import com.reflexit.magiccards.ui.widgets.CheckedTreeSelectionComposite;

public class LocationFilterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Composite panel;
	private TreeViewer treeViewer;
	private int mode;
	private CardOrganizer top;

	/**
	 * 
	 * @param mode
	 *            {@link SWT.SINGLE}, {@link SWT.MULTI}
	 */
	public LocationFilterPreferencePage(int mode) {
		setTitle("Location Filter");
		this.mode = mode;
	}

	@Override
	public void init(IWorkbench workbench) {
		// nothing
	}

	@Override
	public void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);
		if (panel != null)
			initializeTree();
	}

	@Override
	public void noDefaultAndApplyButton() {
		super.noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(Composite parent) {
		this.panel = new Composite(parent, SWT.NONE);
		this.panel.setFont(parent.getFont());
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.panel.setLayout(layout);
		this.panel.setFont(parent.getFont());
		if (mode == SWT.MULTI) {
			CheckedTreeSelectionComposite treeViewerComp = new CheckedTreeSelectionComposite(this.panel);
			this.treeViewer = treeViewerComp.getTreeViewer();
		} else {
			this.treeViewer = new TreeViewer(panel);
		}
		this.treeViewer.setLabelProvider(new CardsNavigatorLabelProvider());
		this.treeViewer.setContentProvider(new CardsNavigatorContentProvider());
		this.treeViewer.setComparator(new ViewerComparator());
		this.treeViewer.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof MagicDbContainter)
					return false;
				return true;
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 400;
		this.treeViewer.getControl().setLayoutData(gd);
		this.treeViewer.getControl().setFont(parent.getFont());
		initializeTree();
		treeViewer.getControl().setFocus();
		return this.panel;
	}

	@Override
	public void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		PreferenceInitializer.setToDefault(store);
		initializeTree();
		super.performDefaults();
	}

	private void initializeTree() {
		ModelRoot mroot = DataManager.getInstance().getModelRoot();
		top = mroot;
		this.treeViewer.setInput(mroot);
		treeViewer.expandToLevel(3);
		// load preferences
		load();
	}

	public void load() {
		initSelection(top);
	}

	public TreeViewer getViewer() {
		return treeViewer;
	}

	/**
	 * @param top
	 */
	private void initSelection(CardElement root) {
		String id = Locations.getInstance().getPrefConstant(root.getLocation());
		boolean checked = getPreferenceStore().getBoolean(id);
		if (checked) {
			setChecked(root, checked);
		} else if (root instanceof CardOrganizer) {
			for (Object element : ((CardOrganizer) root).getChildren()) {
				CardElement el = (CardElement) element;
				initSelection(el);
			}
		}
	}

	public void setChecked(CardElement root, boolean checked) {
		if (treeViewer instanceof CheckboxTreeViewer) {
			((CheckboxTreeViewer) treeViewer).setChecked(root, checked);
		} else {
			if (checked)
				treeViewer.setSelection(new StructuredSelection(root));
		}
	}

	@Override
	protected void performApply() {
		if (this.treeViewer == null)
			return;
		CardOrganizer root = (CardOrganizer) this.treeViewer.getInput();
		for (Object element : root.getChildren()) {
			CardElement el = (CardElement) element;
			applyElement(el);
		}
	}

	public boolean isChecked() {
		return treeViewer instanceof CheckboxTreeViewer;
	}

	public void loadPreferenceFromSelection(IStructuredSelection sel) {
		IPreferenceStore store = getPreferenceStore();
		PreferenceInitializer.setToDefault(store);
		for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
			CardElement el = (CardElement) iterator.next();
			String id = Locations.getInstance().getPrefConstant(el.getLocation());
			store.setValue(id, true);
		}
	}

	/**
	 * @param el
	 */
	private void applyElement(CardElement root) {
		boolean checked = false;
		if (isChecked()) {
			CheckboxTreeViewer box = (CheckboxTreeViewer) treeViewer;
			checked = box.getChecked(root) && !box.getGrayed(root);
		} else {
			IStructuredSelection sel = (IStructuredSelection) treeViewer.getSelection();
			for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
				CardElement el = (CardElement) iterator.next();
				if (el.equals(root)) {
					checked = true;
					break;
				}
			}
		}
		String id = Locations.getInstance().getPrefConstant(root.getLocation());
		IPreferenceStore store = getPreferenceStore();
		if (root instanceof CardOrganizer) {
			for (Object element : ((CardOrganizer) root).getChildren()) {
				CardElement el = (CardElement) element;
				applyElement(el);
			}
		} else {
			if (checked) {
				store.setValue(id, true);
			} else {
				store.setValue(id, false);
			}
		}
	}

	@Override
	public boolean performOk() {
		performApply();
		return true;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		throw new UnsupportedOperationException("Unspecified preference store");
	}

	public String getMemento() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			((PreferenceStore) getPreferenceStore()).save(out, "");
		} catch (IOException e) {
			// nope
		}
		return out.toString();
	}

	public void loadFromMemento(String ids) {
		ByteArrayInputStream in = new ByteArrayInputStream(ids.getBytes());
		try {
			((PreferenceStore) getPreferenceStore()).load(in);
		} catch (IOException e) {
			// not happening
		}
	}

	public boolean isEmptySelection() {
		if (isChecked()) {
			CheckboxTreeViewer box = (CheckboxTreeViewer) treeViewer;
			return box.getCheckedElements().length == 0;
		} else {
			return treeViewer.getSelection().isEmpty();
		}
	}
}
