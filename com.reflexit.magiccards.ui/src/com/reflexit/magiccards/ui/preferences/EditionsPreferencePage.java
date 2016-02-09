package com.reflexit.magiccards.ui.preferences;

import java.io.FileNotFoundException;
import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
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

import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.ParseSetLegality;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.NewSetDialog;
import com.reflexit.magiccards.ui.views.editions.EditionsComposite;
import com.reflexit.magiccards.ui.widgets.ActivityEnablerLink;

public class EditionsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private EditionsComposite comp;
	private Button update;
	private Button addSet;
	private Button delSet;
	private Control link;

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
		addSet = new Button(panel, SWT.PUSH);
		addSet.setText("Add...");
		addSet.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addSet();
				comp.performApply();
				comp.initialize();
			}
		});
		addSet.setFont(panel.getFont());
		delSet = new Button(panel, SWT.PUSH);
		delSet.setText("Remove");
		delSet.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteSets();
				comp.performApply();
				comp.initialize();
			}
		});
		delSet.setFont(panel.getFont());
		update = new Button(panel, SWT.PUSH);
		update.setText("Update from Internet");
		update.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ParseSetLegality.loadAllFormats(ICoreProgressMonitor.NONE);
				comp.performApply();
				comp.initialize();
			}
		});
		link = new ActivityEnablerLink(panel, MagicUIActivator.ACTIVITY_DB_EXTEND,
				"Enable <a>Editing Database</a> activity to add more sets", true) {
			@Override
			protected void clicked(String id, boolean enabled) {
				super.clicked(id, enabled);
				updateWidgetEnablement();
			};
		};
		updateWidgetEnablement();
	}

	public void updateWidgetEnablement() {
		boolean dben = MagicUIActivator.isActivityEnabled(MagicUIActivator.ACTIVITY_DB_EXTEND);
		addSet.setLayoutData(GridDataFactory.swtDefaults().exclude(!dben).create());
		addSet.setVisible(dben);
		delSet.setLayoutData(GridDataFactory.swtDefaults().exclude(!dben).create());
		delSet.setVisible(dben);
		link.setLayoutData(GridDataFactory.swtDefaults().exclude(dben).create());
		link.setVisible(!dben);
		link.getParent().getParent().layout(true);
	}

	protected void addSet() {
		// .. create new set
		NewSetDialog newdia = new NewSetDialog(getShell(), "");
		newdia.open();
	}

	protected void deleteSets() {
		IStructuredSelection selection = comp.getSelection();
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			Edition ed = (Edition) iterator.next();
			deleteSet(ed);
		}
	}

	private void deleteSet(Edition ed) {
		if (ed.isUsedByPrintings()) {
			if (ed.isUsedByInstances()) {
				MessageDialog.openInformation(getShell(), "Info", ed.getName()
						+ " has card instances, delete cards that use that set first.");
				return;
			}
			MessageDialog.openInformation(getShell(), "Info", ed.getName()
					+ " is not empty, it will be hidden instead. "
					+ "Restart Magic Assistant to take effect.");
			ed.setHidden(true);
		} else if (!ed.isHidden()) {
			Editions.getInstance().remove(ed);
		}
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
