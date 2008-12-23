package com.reflexit.magiccards.ui.exportWizards;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.exports.ExportWorker;
import com.reflexit.magiccards.core.exports.FileUtils;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorContentProvider;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorLabelProvider;

/**
 * First and only page of Deck Export Wizard
 */
public class DeckExportPage extends WizardDataTransferPage implements ICheckStateListener {
	private static final String EXPORTED_RESOURCES_SETTING = "exportedResources"; //$NON-NLS-1$
	private static final String OUTPUT_FILE_SETTING = "outputFile"; //$NON-NLS-1$
	private static final String REPORT_TYPE_SETTING = "reportType"; //$NON-NLS-1$
	private static final String INCLUDE_HEADER_SETTING = "includeHeader"; //$NON-NLS-1$
	FileFieldEditor editor;
	private String fileName;
	private IStructuredSelection initialResourceSelection;
	private CheckboxTreeViewer listViewer;
	private Button selectButton;
	private Button deselectButton;
	private Button includeHeader;
	private Button exportXml;
	private final ArrayList typeButtons = new ArrayList();
	private static final String ID = DeckExportPage.class.getName();
	private final static String SELECT_ALL_TITLE = "Select All";
	private final static String DESELECT_ALL_TITLE = "Deselect All";
	private ReportType reportType;

	protected DeckExportPage(final String pageName, final IStructuredSelection selection) {
		super(pageName);
		initialResourceSelection = selection;
	}

	public boolean saveFile() {
		if (new File(fileName).exists()) {
			String res = queryOverwrite(fileName);
			if (res == CANCEL)
				return false;
			if (res == NO)
				return false;
		}
		boolean res = false;
		try {
			//			final ExportWork work = new ExportWork(listViewer.getCheckedElements(), // 
			//			        fileName, //
			//			        reportType, includeHeader.getSelection(), getTimeUnitsName());
			if (reportType == reportType.CSV) {
				// TODO: export selection only
				final boolean header = includeHeader.getSelection();
				IRunnableWithProgress work = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						ExportWorker exportWorker = new ExportWorker(new File(fileName), header, DataManager
						        .getCardHandler().getMagicLibraryHandler().getCardStore());
						exportWorker.run(monitor);
					}
				};
				getRunnableContext().run(true, true, work);
			} else {
				// TODO: export multiple files? zip?
				Object[] el = listViewer.getCheckedElements();
				CardElement ce = (CardElement) el[0];
				try {
					FileUtils.copyFile(ce.getFile(), new File(fileName));
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			}
			return true;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			if (e.getTargetException() instanceof InterruptedException) {
				displayErrorDialog("Export cancelled");
			} else
				displayErrorDialog(e.getCause());
		} catch (InterruptedException e) {
			displayErrorDialog("Export cancelled");
		}
		return res;
	}

	protected IRunnableContext getRunnableContext() {
		return getContainer();
	}

	protected void createDestinationGroup(final Composite parent) {
		Composite fileSelectionArea = new Composite(parent, SWT.NONE);
		GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		fileSelectionArea.setLayoutData(fileSelectionData);
		GridLayout fileSelectionLayout = new GridLayout();
		fileSelectionLayout.numColumns = 3;
		fileSelectionLayout.makeColumnsEqualWidth = false;
		fileSelectionLayout.marginWidth = 0;
		fileSelectionLayout.marginHeight = 0;
		fileSelectionArea.setLayout(fileSelectionLayout);
		editor = new FileFieldEditor("fileSelect", "Select output file", fileSelectionArea); // NON-NLS-1
		// //NON-NLS-2
		// //$NON-NLS-1$
		editor.getTextControl(fileSelectionArea).addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				File file = new File(editor.getStringValue());
				setFileName(file.getPath());
				updatePageCompletion();
			}
		});
		String[] extensions = new String[] { "*.csv" }; // NON-NLS-1 //$NON-NLS-1$
		editor.setFileExtensions(extensions);
		setFileName("");
		// fileSelectionArea.moveAbove(null);
	}

	protected void setFileName(final String string) {
		fileName = string;
	}

	@Override
	protected boolean allowNewContainerName() {
		return true;
	}

	public void handleEvent(final Event event) {
		if (event.widget instanceof Button) {
			if (event.type == SWT.Selection) {
				Object data = event.widget.getData();
				if (data instanceof ReportType
				// && ((Button) event.widget).getSelection()
				) {
					reportType = (ReportType) data;
				}
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
		// WizardExportResourcesPage page;
		createButtonsGroup(composite);
		createOptionsGroup(composite);
		createDestinationGroup(composite);
		// restoreResourceSpecificationWidgetValues(); // ie.- local
		restoreWidgetValues(); // ie.- subclass hook
		if (initialResourceSelection != null) {
			setupBasedOnInitialSelections();
		}
		updateWidgetEnablements();
		setTitle("Export");
		defaultPrompt();
		setPageComplete(determinePageCompletion());
		setErrorMessage(null); // should not initially have error message
		setControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
		        MagicUIActivator.getDefault().PLUGIN_ID + ".export"); //$NON-NLS-1$
	}

	/**
	 * Creates the buttons for selecting specific types or selecting all or none of the elements.
	 * 
	 * @param parent
	 *            the parent control
	 */
	protected final void createButtonsGroup(final Composite parent) {
		Font font = parent.getFont();
		// top level group
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, SELECT_ALL_TITLE, false);
		SelectionAdapter listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectAll();
				updatePageCompletion();
			}
		};
		selectButton.addSelectionListener(listener);
		selectButton.setFont(font);
		setButtonLayoutData(selectButton);
		deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, DESELECT_ALL_TITLE, false);
		listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				deselectAll();
				updatePageCompletion();
			}
		};
		deselectButton.addSelectionListener(listener);
		deselectButton.setFont(font);
		setButtonLayoutData(deselectButton);
	}

	private void defaultPrompt() {
		if (reportType == ReportType.XML)
			setMessage("Export to XML");
		else if (reportType == ReportType.CSV)
			setMessage("Export to SCV");
	}

	@Override
	protected void restoreWidgetValues() {
		super.restoreWidgetValues();
		try {
			DialogSettings dialogSettings = MagicUIActivator.getDefault().getDialogSettings(ID);
			// restore file
			String file = dialogSettings.get(OUTPUT_FILE_SETTING);
			if (file != null) {
				setFileName(file);
				editor.setStringValue(file);
			}
			// restore selection
			String ids[] = dialogSettings.getArray(EXPORTED_RESOURCES_SETTING);
			if (ids != null)
				selectIds(ids);
			// restore options
			String type = dialogSettings.get(REPORT_TYPE_SETTING);
			if (type != null)
				selectTypeButtons(type);
			if (dialogSettings.get(INCLUDE_HEADER_SETTING) != null) {
				includeHeader.setSelection(dialogSettings.getBoolean(INCLUDE_HEADER_SETTING));
			}
		} catch (IOException e) {
			MagicUIActivator.log(e);
		}
	}

	private void selectTypeButtons(final String type) {
		if ((type == null) || (type.length() == 0))
			return;
		for (Iterator<Button> iter = typeButtons.iterator(); iter.hasNext();) {
			Button element = iter.next();
			Object data = element.getData();
			if (data instanceof ReportType) {
				ReportType rt = (ReportType) data;
				if (rt.toString().equals(type)) {
					element.setSelection(true);
					reportType = rt;
				} else {
					element.setSelection(false);
				}
			}
		}
	}

	@Override
	protected void saveWidgetValues() {
		try {
			DialogSettings dialogSettings = MagicUIActivator.getDefault().getDialogSettings(ID);
			// save file name
			dialogSettings.put(OUTPUT_FILE_SETTING, fileName);
			// save selection
			Object[] checked = listViewer.getCheckedElements();
			String[] ids = new String[checked.length];
			for (int i = 0; i < checked.length; i++) {
				String id = String.valueOf(((CardElement) checked[i]).getLocation());
				ids[i] = id;
			}
			dialogSettings.put(EXPORTED_RESOURCES_SETTING, ids);
			// save options
			dialogSettings.put(REPORT_TYPE_SETTING, reportType.toString());
			dialogSettings.put(INCLUDE_HEADER_SETTING, includeHeader.getSelection());
			// save into file
			MagicUIActivator.getDefault().saveDialogSetting(dialogSettings);
		} catch (IOException e) {
			MagicUIActivator.log(e);
		}
	}

	private void createResourcesGroup(final Composite parent) {
		listViewer = new ContainerCheckedTreeViewer(parent, SWT.BORDER | SWT.SCROLL_PAGE);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 200;
		data.widthHint = 300;
		listViewer.getControl().setLayoutData(data);
		listViewer.getControl().setFont(parent.getFont());
		listViewer.setContentProvider(new CardsNavigatorContentProvider());
		listViewer.setLabelProvider(new CardsNavigatorLabelProvider());
		listViewer.addCheckStateListener(this);
		listViewer.setInput(DataManager.getModelRoot());
		listViewer.expandToLevel(2);
	}

	public void checkStateChanged(final CheckStateChangedEvent event) {
		// in case we need some restrictions on checked elements, for
		// example only one at a time allowed
		// Object[] data = listViewer.getCheckedElements();
		// for (int i = 0; i < data.length; ++i) {
		// listViewer.setChecked(data[i], false);
		// }
		// listViewer.setCheckedElements(NO_OBJECTS);
		// event.getCheckable().setChecked(event.getElement(), true);
		updatePageCompletion();
		updateWidgetEnablements();
	}

	/**
	 * Set the initial selections in the resource group.
	 */
	protected void setupBasedOnInitialSelections() {
		Iterator it = initialResourceSelection.iterator();
		if (it.hasNext()) {
			deselectAll();
		}
		while (it.hasNext()) {
			Object currentResource = it.next();
			if (currentResource instanceof CardElement) {
				setCheckedSession((CardElement) currentResource);
			}
		}
	}

	void deselectAll() {
		listViewer.setAllChecked(false);
		updateWidgetEnablements();
	}

	void selectAll() {
		listViewer.setAllChecked(true);
		updateWidgetEnablements();
	}

	private void setCheckedSession(final CardElement session) {
		listViewer.setChecked(session, true);
	}

	private void selectIds(final String[] ids) {
		listViewer.setAllChecked(false);
		TreeItem[] children = listViewer.getTree().getItems();
		for (TreeItem control : children) {
			CardElement session = (CardElement) control.getData();
			for (String id : ids) {
				if (session.getLocation().equals(id)) {
					listViewer.setChecked(session, true);
				}
			}
		}
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
		buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// csv
		Object exportCsv = createTypeChoice(buttonComposite, "Export to CSV", ReportType.CSV);
		// xml type
		exportXml = createTypeChoice(buttonComposite, "Export XML", ReportType.XML);
		exportXml.setSelection(true);
		reportType = ReportType.XML;
		// options to include header
		includeHeader = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
		includeHeader.setText("Generate header row");
		includeHeader.setSelection(true);
	}

	private Button createTypeChoice(final Composite optionsGroup, final String text, final ReportType reportType) {
		Button button = new Button(optionsGroup, SWT.RADIO | SWT.LEFT);
		button.setText(text);
		button.setData(reportType);
		button.addListener(SWT.Selection, this);
		typeButtons.add(button);
		return button;
	}

	@Override
	protected String getErrorDialogTitle() {
		return "Error";
	}

	@Override
	protected boolean validateSourceGroup() {
		if (listViewer.getCheckedElements().length == 0) {
			setMessage("Select an element to export");
			return false;
		}
		return true;
	}

	@Override
	protected boolean validateDestinationGroup() {
		if ((fileName == null) || (fileName.length() == 0) || (editor.getStringValue().length() == 0)) {
			setMessage("File not selected");
			return false;
		}
		if (isExportCsvFlag() && !(fileName.endsWith(".csv"))) { //$NON-NLS-1$
			setErrorMessage("File should have .csv extension");
			return false;
		}
		if (!isExportCsvFlag() && !(fileName.endsWith(".xml"))) { //$NON-NLS-1$
			setErrorMessage("File should have .xml extension");
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
		// disableAll/enableAll
		int size = listViewer.getTree().getItemCount();
		if (size == 0) {
			selectButton.setEnabled(false);
			deselectButton.setEnabled(false);
		} else {
			int checked = listViewer.getCheckedElements().length;
			if (checked == size)
				selectButton.setEnabled(false);
			else
				selectButton.setEnabled(true);
			if (checked == 0)
				deselectButton.setEnabled(false);
			else
				deselectButton.setEnabled(true);
		}
		// type
		includeHeader.setEnabled(isExportCsvFlag());
		// file extension
		if (isExportCsvFlag() && fileName.endsWith(".xml")) { //$NON-NLS-1$
			fileName = fileName.replaceAll(".xml$", ".csv"); //$NON-NLS-1$ //$NON-NLS-2$
			editor.setStringValue(fileName);
		} else if (!isExportCsvFlag() && fileName.endsWith(".csv")) { //$NON-NLS-1$
			fileName = fileName.replaceAll(".csv$", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
			editor.setStringValue(fileName);
		}
		if (isExportCsvFlag()) {
			editor.setFileExtensions(new String[] { "*.csv" }); //$NON-NLS-1$
		} else {
			editor.setFileExtensions(new String[] { "*.xml" }); //$NON-NLS-1$
		}
	}

	private boolean isExportCsvFlag() {
		return !exportXml.getSelection();
	}

	public ReportType getReportType() {
		return reportType;
	}
}
