package com.reflexit.magiccards.ui.preferences;

import java.io.FileNotFoundException;
import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.NewSetDialog;
import com.reflexit.magiccards.ui.views.editions.EditionsComposite;

public class EditionsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private EditionsComposite comp;
	private Button addSet;
	private Button delSet;

	public EditionsPreferencePage() {
		setTitle("Magic Card Sets");
		setDescription("You can edit set information using inline cell editor");
	}

	@Override
	public void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);
		if (comp != null)
			comp.initialize();
	}

	@Override
	protected Control createContents(Composite parent) {
		this.comp = new EditionsComposite(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, false) {
			@Override
			protected void createButtonsControls(Composite panel) {
				super.createButtonsControls(panel);
				EditionsPreferencePage.this.createButtonsControls(panel);
			}
		};
		this.comp.setPreferenceStore(getPreferenceStore());
		this.comp.initialize();
		return comp;
	}

	protected void createButtonsControls(Composite panel) {
		this.addSet = new Button(panel, SWT.PUSH);
		this.addSet.setText("Add Set...");
		this.addSet.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addSet();
				comp.performApply();
				comp.initialize();
			}
		});
		addSet.setFont(panel.getFont());
		this.delSet = new Button(panel, SWT.PUSH);
		this.delSet.setText("Delete Selected");
		this.delSet.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteSets();
				comp.performApply();
				comp.initialize();
			}
		});
		delSet.setFont(panel.getFont());
	}

	protected void addSet() {
		// .. create new set
		NewSetDialog newdia = new NewSetDialog(getShell(), "");
		newdia.open();
	}

	protected void deleteSets() {
		IStructuredSelection selection = comp.getSelection();
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			Editions.Edition ed = (Edition) iterator.next();
			deleteSet(ed);
		}
	}

	private void deleteSet(Edition ed) {
		if (ed.isUsed()) {
			MessageDialog.openInformation(getShell(), "No Way", ed.getName() + " is used in database by some cards, cannot delete");
			return;
		}
		Editions.getInstance().remove(ed);
		try {
			Editions.getInstance().save();
		} catch (FileNotFoundException e) {
			MagicUIActivator.log(e);
		}
	}

	@Override
	public boolean performOk() {
		if (this.comp != null) {
			this.comp.performApply();
		}
		return true;
	}

	@Override
	public void performDefaults() {
		if (this.comp != null) {
			comp.setToDefaults();
		}
		super.performDefaults();
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(MagicUIActivator.getDefault().getPreferenceStore());
	}
}
