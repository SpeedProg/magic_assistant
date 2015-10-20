package com.reflexit.magiccards.ui.dialogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
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

import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.LocationFilterPreferencePage;
import com.reflexit.magiccards.ui.wizards.NewCardCollectionWizard;
import com.reflexit.magiccards.ui.wizards.NewCardElementWizard;
import com.reflexit.magiccards.ui.wizards.NewDeckWizard;

public class LocationPickerDialog extends TitleAreaDialog {
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
		setTitle("Select a deck or collection");
		area = (Composite) super.createDialogArea(parent);
		locPage = new LocationFilterPreferencePage(mode);
		locPage.noDefaultAndApplyButton();
		locPage.setPreferenceStore(new PreferenceStore());
		locPage.createControl(area);
		locPage.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		listViewer = locPage.getViewer();
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 300;
		listViewer.getControl().setLayoutData(data);
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selection = (IStructuredSelection) event.getSelection();
			}
		});
		listViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
		if ((mode & SWT.READ_ONLY) == 0)
			createButtonsGroup(area);
		restoreWidgetValues();
		if (initialResourceSelection != null)
			setupBasedOnInitialSelections();
		listViewer.getControl().setFocus();
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
		Button button1 = createButton(buttons, 3, "Create new deck...", true);
		Button button2 = createButton(buttons, 4, "Create new collection...", false);
		button1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openWizard(new NewDeckWizard(), listViewer.getSelection());
			}
		});
		button2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openWizard(new NewCardCollectionWizard(), listViewer.getSelection());
			}
		});
	}

	protected void openWizard(NewCardElementWizard wizard, ISelection selection) {
		// Get the workbench and initialize, the wizard.
		IWorkbench workbench = PlatformUI.getWorkbench();
		wizard.init(workbench, (IStructuredSelection) selection);
		// Open the wizard dialog with the given wizard.
		WizardDialog dialog = new WizardDialog(listViewer.getControl().getShell(), wizard);
		dialog.open();
		CardElement element = wizard.getElement();
		if (element != null) {
			listViewer.refresh(true);
			listViewer.setSelection(new StructuredSelection(element));
		}
	}

	public IStructuredSelection getSelection() {
		return selection;
	}

	public List<CardCollection> getSelectedCardCollections() {
		ArrayList<CardCollection> collections = new ArrayList<>();
		for (Object coll : selection.toList()) {
			if (coll instanceof CardCollection)
				collections.add((CardCollection) coll);
			else if (coll instanceof CardOrganizer) {
				collections.addAll(((CardOrganizer) coll).getAllElements());
			}
		}
		return collections;
	}

	public String getStringValue() {
		String res = "";
		if (selection == null)
			return "";
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			CardElement deck = (CardElement) iterator.next();
			res += deck.getLocation().getPath();
			if (iterator.hasNext())
				res += ",";
		}
		return res;
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
