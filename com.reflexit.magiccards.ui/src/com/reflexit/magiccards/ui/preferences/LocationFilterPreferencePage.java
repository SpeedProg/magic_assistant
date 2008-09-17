package com.reflexit.magiccards.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.FilterHelper;
import com.reflexit.magiccards.core.model.Locations;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorContentProvider;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorLabelProvider;
import com.reflexit.magiccards.ui.widgets.CheckedTreeSelectionComposite;

public class LocationFilterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Composite panel;
	private CheckboxTreeViewer treeViewer;
	private CheckedTreeSelectionComposite treeViewerComp;

	public LocationFilterPreferencePage() {
		setTitle("Location Filter");
	}

	public void init(IWorkbench workbench) {
		// nothing
	}

	@Override
	public void noDefaultAndApplyButton() {
		super.noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(Composite parent) {
		this.panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		this.panel.setLayout(layout);
		this.panel.setFont(parent.getFont());
		this.treeViewerComp = new CheckedTreeSelectionComposite(this.panel);
		this.treeViewer = this.treeViewerComp.getTreeViewer();
		this.treeViewer.setLabelProvider(new CardsNavigatorLabelProvider());
		this.treeViewer.setContentProvider(new CardsNavigatorContentProvider());
		this.treeViewer.setComparator(new ViewerComparator());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 400;
		this.treeViewerComp.setLayoutData(gd);
		initializeTree();
		return this.panel;
	}

	private void initializeTree() {
		CardOrganizer root = new CardOrganizer("fake", null);
		CardOrganizer top = new CardOrganizer("All Cards", root);
		ModelRoot mroot = DataManager.getModelRoot();
		top.addChild(mroot.getCollectionsContainer());
		top.addChild(mroot.getDeckContainer());
		this.treeViewer.setInput(root);
		this.treeViewer.setChecked(root, true);
		this.treeViewer.setExpandedState(top, true);
		// load preferences
		initSelection(top);
	}

	/**
	 * @param top
	 */
	private void initSelection(CardElement root) {
		String id = FilterHelper.getPrefConstant(FilterHelper.LOCATION, root.getName());
		boolean checked = getPreferenceStore().getBoolean(id);
		if (checked) {
			this.treeViewer.setChecked(root, checked);
		} else if (root instanceof CardOrganizer) {
			for (Iterator iterator = ((CardOrganizer) root).getChildren().iterator(); iterator.hasNext();) {
				CardElement el = (CardElement) iterator.next();
				initSelection(el);
			}
		}
	}

	@Override
	protected void performApply() {
		if (this.treeViewer == null)
			return;
		CardOrganizer root = (CardOrganizer) this.treeViewer.getInput();
		for (Iterator iterator = root.getChildren().iterator(); iterator.hasNext();) {
			CardElement el = (CardElement) iterator.next();
			applyElement(el);
		}
	}

	/**
	 * @param el
	 */
	private void applyElement(CardElement root) {
		boolean checked = this.treeViewer.getChecked(root) && !this.treeViewer.getGrayed(root);
		String id = Locations.getInstance().getPrefConstant(root.getPath().toPortableString());
		IPreferenceStore store = getPreferenceStore();
		if (checked) {
			store.setValue(id, true);
		} else if (root instanceof CardOrganizer) {
			store.setValue(id, false);
			for (Iterator iterator = ((CardOrganizer) root).getChildren().iterator(); iterator.hasNext();) {
				CardElement el = (CardElement) iterator.next();
				applyElement(el);
			}
		} else {
			store.setValue(id, false);
		}
	}

	@Override
	public boolean performOk() {
		performApply();
		return true;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return MagicUIActivator.getDefault().getPreferenceStore();
	}
}
