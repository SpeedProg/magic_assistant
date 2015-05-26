package com.reflexit.magiccards.ui.exportWizards;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.reflexit.magiccards.ui.dialogs.NewSetDialog;
import com.reflexit.magiccards.ui.widgets.EditionTextControl;

public class SetImportWizard extends Wizard implements IImportWizard {
	private DeckImportPage mainPage;
	private DeckImportPreviewPage previewPage;
	private Object data;

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public SetImportWizard() {
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		addPage(mainPage);
		addPage(previewPage);
	}

	@Override
	public boolean performFinish() {
		mainPage.saveWidgetValues();
		boolean ok = mainPage.performImport(false);
		if (ok)
			return true;
		return false;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Import Set");
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		mainPage = new DeckImportPage("Import", selection) {
			@Override
			protected void createDestinationGroup(Composite parent) {
				super.createDestinationGroup(parent);
				setTitle("Import a new set(s) into the database");
				importIntoDb.getParent().setVisible(false);
				importIntoDb.getParent().setLayoutData(GridDataFactory.swtDefaults().hint(0, 0).create());
				virtualCards.setSelection(true);
				virtualCards.setVisible(false);
				virtualCards.setLayoutData(GridDataFactory.swtDefaults().hint(0, 0).create());
				createEditionGroup(parent).setLayoutData(
						GridDataFactory.fillDefaults().grab(true, false).create());
			}

			@Override
			public int getIntoChoice() {
				return 3;
			}
		};
		previewPage = new DeckImportPreviewPage("Preview");
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
		setCombo.setToolTipText("Set into which cards will be imported");
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
