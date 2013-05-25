package com.reflexit.magiccards.ui.dialogs;

import java.io.IOException;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.LocationFilterPreferencePage;
import com.reflexit.magiccards.ui.wizards.NewCardElementWizard;
import com.reflexit.magiccards.ui.wizards.NewCollectionContainerWizard;
import com.reflexit.magiccards.ui.wizards.NewDeckWizard;

public class LocationPickerDialog extends TrayDialog {
	private static final String IMPORTED_RESOURCES_SETTING = "importedResources"; //$NON-NLS-1$
	private static final String ID = LocationPickerDialog.class.getName();
	private LocationFilterPreferencePage locPage;
	private TreeViewer listViewer;
	private Composite area;
	private int mode;
	private IStructuredSelection selection;
	private IStructuredSelection initialResourceSelection;

	/**
	 * 
	 * @param mode
	 *            {@link SWT.SINGLE}, {@link SWT.MULTI}
	 */
	public LocationPickerDialog(Shell parentShell, int mode) {
		super(parentShell);
		this.mode = mode;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Select a deck or collection");
		area = (Composite) super.createDialogArea(parent);
		locPage = new LocationFilterPreferencePage(mode);
		locPage.noDefaultAndApplyButton();
		locPage.setPreferenceStore(new PreferenceStore());
		locPage.createControl(area);
		listViewer = locPage.getViewer();
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 200;
		data.widthHint = 300;
		listViewer.getControl().setLayoutData(data);
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selection = (IStructuredSelection) event.getSelection();
			}
		});
		createButtonsGroup(area);
		restoreWidgetValues();
		if (initialResourceSelection != null)
			setupBasedOnInitialSelections();
		return area;
	}

	@Override
	protected void okPressed() {
		saveWidgetValues();
		super.okPressed();
	}

	protected void restoreWidgetValues() {
		// restore selection
		IDialogSettings dialogSettings = MagicUIActivator.getDefault().getDialogSettings(ID);
		String ids = dialogSettings.get(IMPORTED_RESOURCES_SETTING);
		if (ids != null) {
			locPage.loadFromMemento(ids);
			locPage.load();
		}
	}

	protected void saveWidgetValues() {
		try {
			IDialogSettings dialogSettings = MagicUIActivator.getDefault().getDialogSettings(ID);
			// save selection
			locPage.performOk();
			String ids = locPage.getMemento();
			dialogSettings.put(IMPORTED_RESOURCES_SETTING, ids);
			// save into file
			MagicUIActivator.getDefault().saveDialogSetting(dialogSettings);
		} catch (IOException e) {
			MagicUIActivator.log(e);
		}
	}

	/**
	 * Creates the buttons for creating new deck or collection
	 * 
	 * @param parent
	 *            the parent control
	 */
	protected final void createButtonsGroup(final Composite parent) {
		Composite buttons = new Composite(parent, SWT.NONE);
		buttons.setLayout(new GridLayout());
		Button button1 = createButton(buttons, 1, "Create new deck...", true);
		Button button2 = createButton(buttons, 2, "Create new collection...", false);
		button1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openWizard(new NewDeckWizard(), listViewer.getSelection());
			}
		});
		button2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openWizard(new NewCollectionContainerWizard(), listViewer.getSelection());
			}
		});
	}

	protected void openWizard(NewCardElementWizard wizard, ISelection selection) {
		// Get the workbench and initialize, the wizard.
		IWorkbench workbench = PlatformUI.getWorkbench();
		wizard.init(workbench, (IStructuredSelection) selection);
		// Open the wizard dialog with the given wizard.
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
		dialog.open();
		CardElement element = wizard.getElement();
		listViewer.refresh(true);
		listViewer.setSelection(new StructuredSelection(element));
	}

	public IStructuredSelection getSelection() {
		return selection;
	}

	/**
	 * Set the initial selections in the resource group.
	 */
	protected void setupBasedOnInitialSelections() {
		locPage.loadPreferenceFromSelection(initialResourceSelection);
		locPage.load();
	}

	public void setSelection(IStructuredSelection sel) {
		initialResourceSelection = sel;
	}
}
