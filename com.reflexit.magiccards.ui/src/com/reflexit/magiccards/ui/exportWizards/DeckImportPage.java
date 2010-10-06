package com.reflexit.magiccards.ui.exportWizards;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.exports.ImportExportFactory;
import com.reflexit.magiccards.core.exports.ImportUtils;
import com.reflexit.magiccards.core.exports.PreviewResult;
import com.reflexit.magiccards.core.exports.ReportType;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.Locations;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.LocationFilterPreferencePage;
import com.reflexit.magiccards.ui.wizards.NewCardElementWizard;
import com.reflexit.magiccards.ui.wizards.NewCollectionContainerWizard;
import com.reflexit.magiccards.ui.wizards.NewDeckWizard;

/**
 * First and only page of Deck Export Wizard
 */
public class DeckImportPage extends WizardDataTransferPage {
	private static final String IMPORTED_RESOURCES_SETTING = "importedResources"; //$NON-NLS-1$
	private static final String IMPUT_FILE_SETTING = "outputFile"; //$NON-NLS-1$
	private static final String REPORT_TYPE_SETTING = "reportType"; //$NON-NLS-1$
	private static final String IMPORT_HEADER_SETTING = "headerRow"; //$NON-NLS-1$
	private static final String IMPORT_CLIPBOARD = "clipboard"; //$NON-NLS-1$
	FileFieldEditor editor;
	private String fileName;
	private IStructuredSelection initialResourceSelection;
	private TreeViewer listViewer;
	private Button includeHeader;
	private static final String ID = DeckImportPage.class.getName();
	private ReportType reportType;
	private PreferenceStore store;
	private LocationFilterPreferencePage locPage;
	private boolean clipboard;
	private Combo typeCombo;
	private Button fileRadio;
	private Button clipboardRadio;
	private Composite fileSelectionArea;
	private PreviewResult previewResult;
	private CardElement element;

	protected DeckImportPage(final String pageName, final IStructuredSelection selection) {
		super(pageName);
		initialResourceSelection = selection;
		store = new PreferenceStore();
	}

	public boolean performImport(final boolean preview) {
		boolean res = false;
		try {
			//			final ExportWork work = new ExportWork(listViewer.getCheckedElements(), //
			//			        fileName, //
			//			        reportType, includeHeader.getSelection(), getTimeUnitsName());
			locPage.performOk();
			final boolean header = includeHeader.getSelection();
			final InputStream st = openInputStream();
			try {
				IRunnableWithProgress work = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						if (preview) {
							// if error occurs previewResult.error would be set to exception
							previewResult = ImportUtils.performPreview(st, reportType, header, monitor);
							((DeckImportWizard) getWizard()).setData(previewResult);
						} else {
							ImportUtils.performImport(st, reportType, header, getSelectedLocation(), monitor);
						}
					}
				};
				getRunnableContext().run(true, true, work);
			} finally {
				if (st != null)
					st.close();
			}
			return true;
		} catch (Exception e) {
			displayErrorDialog(e);
		}
		return res;
	}

	InputStream openInputStream() throws FileNotFoundException {
		InputStream st = null;
		if (clipboard) {
			final Clipboard cb = new Clipboard(PlatformUI.getWorkbench().getDisplay());
			final Object clipboardText = cb.getContents(TextTransfer.getInstance());
			if (clipboardText != null)
				st = new ByteArrayInputStream(clipboardText.toString().getBytes());
		} else
			st = new FileInputStream(fileName);
		return st;
	}

	private Location getSelectedLocation() {
		IPreferenceStore store = getPreferenceStore();
		Collection<String> col = Locations.getInstance().getIds();
		for (Iterator<String> iterator = col.iterator(); iterator.hasNext();) {
			String id = iterator.next();
			String value = store.getString(id);
			if (Boolean.valueOf(value)) {
				return Locations.getInstance().findLocation(id);
			}
		}
		return null;
	}

	protected IPreferenceStore getPreferenceStore() {
		return store;
	}

	protected IRunnableContext getRunnableContext() {
		return getContainer();
	}

	protected void createDestinationGroup(final Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Select an existing deck/collection to import into:");
		locPage = new LocationFilterPreferencePage(SWT.SINGLE);
		locPage.noDefaultAndApplyButton();
		locPage.setPreferenceStore(store);
		locPage.createControl(parent);
		listViewer = locPage.getViewer();
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 200;
		data.widthHint = 300;
		listViewer.getControl().setLayoutData(data);
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (!event.getSelection().isEmpty())
					element = (CardElement) ((IStructuredSelection) event.getSelection()).getFirstElement();
				updatePageCompletion();
				updateWidgetEnablements();
			}
		});
	}

	protected void setFileName(final String string) {
		fileName = string;
	}

	@Override
	protected boolean allowNewContainerName() {
		return true;
	}

	public void handleEvent(final Event event) {
		if (event.type == SWT.Selection && event.widget instanceof Combo) {
			Object data = event.widget.getData(((Combo) event.widget).getText());
			if (data instanceof ReportType
			// && ((Button) event.widget).getSelection()
			) {
				reportType = (ReportType) data;
			}
		}
		updateWidgetEnablements();
		updatePageCompletion();
	}

	/**
	 * (non-Javadoc) Method declared on IDialogPage.
	 */
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());
		createResourcesGroup(composite);
		createOptionsGroup(composite);
		createDestinationGroup(composite);
		createButtonsGroup(composite);
		restoreWidgetValues();
		if (initialResourceSelection != null) {
			setupBasedOnInitialSelections();
		}
		updateWidgetEnablements();
		setTitle("Import to a Deck or Collection");
		defaultPrompt();
		setPageComplete(determinePageCompletion());
		setErrorMessage(null); // should not initially have error message
		setControl(composite);
		PlatformUI.getWorkbench().getHelpSystem()
		        .setHelp(composite, MagicUIActivator.getDefault().PLUGIN_ID + ".export");
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
			};
		});
		button2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openWizard(new NewCollectionContainerWizard(), listViewer.getSelection());
			};
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

	private void defaultPrompt() {
		String mess = "You have selected '" + reportType.getLabel() + "' format. ";
		if (reportType == ReportType.XML)
			setMessage(mess);
		else if (reportType == ReportType.CSV)
			setMessage(mess + "Columns: ID,NAME,COST,TYPE,P,T,TEXT,SET,RARITY,DBPRICE,LANG,COUNT,PRICE,COMMENT");
		else if (reportType == ReportType.TEXT_DECK_CLASSIC)
			setMessage(mess + "Lines like 'Quagmire Druid x 3' or 'Diabolic Tutor (Tenth Edition) x4'");
		else if (reportType == ReportType.TABLE_PIPED)
			setMessage(mess + "Columns: ID|NAME|COST|TYPE|P|T|TEXT|SET|RARITY|RESERVED|LANG|COUNT|PRICE|COMMENT");
		else
			setMessage(mess);
	}

	@Override
	protected void restoreWidgetValues() {
		super.restoreWidgetValues();
		try {
			DialogSettings dialogSettings = MagicUIActivator.getDefault().getDialogSettings(ID);
			// restore file
			String file = dialogSettings.get(IMPUT_FILE_SETTING);
			if (file != null) {
				setFileName(file);
				editor.setStringValue(file);
			}
			clipboard = dialogSettings.getBoolean(IMPORT_CLIPBOARD);
			fileRadio.setSelection(!clipboard);
			clipboardRadio.setSelection(clipboard);
			// restore selection
			String ids = dialogSettings.get(IMPORTED_RESOURCES_SETTING);
			if (ids != null) {
				locPage.loadFromMemento(ids);
				locPage.load();
			}
			// restore options
			String type = dialogSettings.get(REPORT_TYPE_SETTING);
			if (type != null)
				selectReportType(ReportType.valueOf(type));
			else
				selectReportType(ReportType.TEXT_DECK_CLASSIC);
			if (dialogSettings.get(IMPORT_HEADER_SETTING) != null) {
				includeHeader.setSelection(dialogSettings.getBoolean(IMPORT_HEADER_SETTING));
			}
			updateWidgetEnablements();
		} catch (IOException e) {
			MagicUIActivator.log(e);
		}
	}

	private void selectReportType(final ReportType type) {
		if (type == null)
			return;
		reportType = type;
		typeCombo.setText(type.getLabel());
	}

	@Override
	protected void saveWidgetValues() {
		try {
			DialogSettings dialogSettings = MagicUIActivator.getDefault().getDialogSettings(ID);
			// save file name
			dialogSettings.put(IMPUT_FILE_SETTING, fileName);
			dialogSettings.put(IMPORT_CLIPBOARD, clipboard);
			// save selection
			locPage.performOk();
			String ids = locPage.getMemento();
			dialogSettings.put(IMPORTED_RESOURCES_SETTING, ids);
			// save options
			dialogSettings.put(REPORT_TYPE_SETTING, reportType.toString());
			dialogSettings.put(IMPORT_HEADER_SETTING, includeHeader.getSelection());
			// save into file
			MagicUIActivator.getDefault().saveDialogSetting(dialogSettings);
		} catch (IOException e) {
			MagicUIActivator.log(e);
		}
	}

	protected void createResourcesGroup(final Composite parent) {
		fileSelectionArea = new Composite(parent, SWT.NONE);
		GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		fileSelectionArea.setLayoutData(fileSelectionData);
		GridLayout fileSelectionLayout = new GridLayout();
		fileSelectionLayout.numColumns = 3;
		fileSelectionLayout.makeColumnsEqualWidth = false;
		fileSelectionLayout.marginWidth = 0;
		fileSelectionLayout.marginHeight = 0;
		fileSelectionArea.setLayout(fileSelectionLayout);
		fileRadio = new Button(fileSelectionArea, SWT.RADIO | SWT.LEFT);
		fileRadio.setText("File");
		fileRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clipboard = false;
				updateWidgetEnablements();
				updatePageCompletion();
			}
		});
		fileRadio.setSelection(true);
		clipboard = false;
		clipboardRadio = new Button(fileSelectionArea, SWT.RADIO | SWT.LEFT);
		clipboardRadio.setText("Clipboard");
		clipboardRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clipboard = true;
				updateWidgetEnablements();
				updatePageCompletion();
			}
		});
		GridData bgd = new GridData();
		bgd.horizontalSpan = 2;
		clipboardRadio.setLayoutData(bgd);
		editor = new FileFieldEditor("fileSelect", "Select input file", fileSelectionArea); // NON-NLS-1
		// //NON-NLS-2
		//
		editor.getTextControl(fileSelectionArea).addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				File file = new File(editor.getStringValue());
				setFileName(file.getPath());
				updatePageCompletion();
			}
		});
		String[] extensions = new String[] { "*" }; // NON-NLS-1 //$NON-NLS-1$
		editor.setFileExtensions(extensions);
		setFileName("");
		// fileSelectionArea.moveAbove(null);
	}

	/**
	 * Set the initial selections in the resource group.
	 */
	protected void setupBasedOnInitialSelections() {
		locPage.loadPreferenceFromSelection(initialResourceSelection);
		locPage.load();
	}

	@Override
	protected void createOptionsGroupButtons(final Group optionsPanel) {
		// top level group
		Composite buttonComposite = new Composite(optionsPanel, SWT.NONE);
		buttonComposite.setFont(optionsPanel.getFont());
		GridLayout layout1 = new GridLayout();
		layout1.numColumns = 2;
		layout1.makeColumnsEqualWidth = true;
		buttonComposite.setLayout(layout1);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		buttonComposite.setLayoutData(gd);
		// create report type
		Label label = new Label(buttonComposite, SWT.NONE);
		label.setText("Import Type:");
		typeCombo = new Combo(buttonComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
		Collection<ReportType> types = new ImportExportFactory<IMagicCard>().getImportTypes();
		for (ReportType reportType : types) {
			addComboType(reportType);
		}
		selectReportType(ReportType.TEXT_DECK_CLASSIC);
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		gd1.horizontalSpan = 1;
		typeCombo.setLayoutData(gd1);
		// options to include header
		includeHeader = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
		includeHeader.setText("Data has a header row");
		includeHeader.setSelection(true);
	}

	private void addComboType(ReportType reportType) {
		typeCombo.add(reportType.getLabel());
		typeCombo.setData(reportType.getLabel(), reportType);
		typeCombo.addListener(SWT.Selection, this);
	}

	@Override
	protected String getErrorDialogTitle() {
		return "Error";
	}

	@Override
	protected boolean validateDestinationGroup() {
		if (locPage.isEmptySelection()) {
			setMessage("Select a deck or collection to import data into. To import into new deck, create it first.");
			return false;
		}
		return true;
	}

	@Override
	protected boolean validateSourceGroup() {
		if (clipboard == false
		        && ((fileName == null) || (fileName.length() == 0) || (editor.getStringValue().length() == 0))) {
			setMessage("Imput file is not selected");
			return false;
		}
		return true;
	}

	@Override
	protected void updatePageCompletion() {
		super.updatePageCompletion();
		if (isPageComplete()) {
			defaultPrompt(); // set default prompt, otherwise it empty ugly
		}
	}

	/**
	 * Creates a new button with the given id.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method creates a standard push button, registers for selection
	 * events including button presses and registers default buttons with its shell. The button id is stored as the buttons client
	 * data. Note that the parent's layout is assumed to be a GridLayout and the number of columns in this layout is incremented.
	 * Subclasses may override.
	 * </p>
	 *
	 * @param parent
	 *            the parent composite
	 * @param id
	 *            the id of the button (see <code>IDialogConstants.*_ID</code> constants for standard dialog button ids)
	 * @param label
	 *            the label from the button
	 * @param defaultButton
	 *            <code>true</code> if the button is to be the default button, and <code>false</code> otherwise
	 */
	protected Button createButton(final Composite parent, final int id, final String label, final boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.PUSH);
		GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
		button.setLayoutData(buttonData);
		button.setData(new Integer(id));
		button.setText(label);
		button.setFont(parent.getFont());
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
			button.setFocus();
		}
		button.setFont(parent.getFont());
		setButtonLayoutData(button);
		return button;
	}

	@Override
	protected void updateWidgetEnablements() {
		// type
		includeHeader.setEnabled(isExportCsvFlag());
		includeHeader.setVisible(isExportCsvFlag());
		editor.setEnabled(!clipboard, fileSelectionArea);
	}

	private boolean isExportCsvFlag() {
		return !getReportType().isXmlFormat();
	}

	public boolean hasHeaderRow() {
		return includeHeader.getSelection();
	}

	public ReportType getReportType() {
		return reportType;
	}

	/**
	 * @param element the element to set
	 */
	public void setElement(CardElement element) {
		this.element = element;
	}

	/**
	 * @return the element
	 */
	public CardElement getElement() {
		return element;
	}
}
