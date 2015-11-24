package com.reflexit.magiccards.ui.exportWizards;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.NewSetDialog;
import com.reflexit.magiccards.ui.widgets.EditionTextControl;

public class SetImportWizard extends DeckImportWizard implements IImportWizard {
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Import Set");
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		MagicUIActivator.setActivityEnabled(MagicUIActivator.ACTIVITY_DB_EXTEND, true);
		mainPage = new DeckImportPage("Import", selection) {
			@Override
			protected Group createDestinationGroup(Composite parent) {
				Group group = super.createDestinationGroup(parent);
				setTitle("Import a new set(s) into the database");
				group.setVisible(false);
				group.setLayoutData(GridDataFactory.swtDefaults().hint(0, 0).create());
				Composite edition = createEditionGroup(parent);
				edition.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
				return group;
			}

			@Override
			public int getIntoChoice() {
				return 3;
			}
		};
		previewPage = new DeckImportPreviewPage("Preview");
	}

	@Override
	public boolean canFinish() {
		if (mainPage.isPageComplete())
			return true;
		if (previewPage.isPageComplete())
			return true;
		return false;
	}

	public Composite createEditionGroup(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).margins(0, 0).create());
		Label desc = new Label(comp, SWT.WRAP);
		desc.setText("Select default set where cards would be added if not specificed in the data source.\n"
				+ "Start typing to trigger auto-complete.");
		desc.setLayoutData(GridDataFactory.fillDefaults().span(3, 0).create());
		Label label = new Label(comp, SWT.NONE);
		label.setText("Card Set:");
		EditionTextControl setCombo = new EditionTextControl(comp, SWT.BORDER);
		setCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				mainPage.getImportData().setProperty(MagicCardField.SET.name(), setCombo.getText());
			}
		});
		setCombo.setToolTipText("Set into which cards will be imported");
		setCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button button = new Button(comp, SWT.PUSH);
		button.setText("New Set...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewSetDialog d = new NewSetDialog(getShell(), setCombo.getText());
				if (d.open() == Window.OK) {
					setCombo.setText(d.getSet().getName());
				}
			}
		});
		return comp;
	}
}
