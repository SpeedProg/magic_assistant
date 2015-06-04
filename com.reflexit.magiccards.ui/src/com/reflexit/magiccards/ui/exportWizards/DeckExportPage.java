package com.reflexit.magiccards.ui.exportWizards;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.exports.CustomExportDelegate;
import com.reflexit.magiccards.core.exports.IExportDelegate;
import com.reflexit.magiccards.core.exports.ImportExportFactory;
import com.reflexit.magiccards.core.exports.ReportType;
import com.reflexit.magiccards.core.model.Locations;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.LocationPickerDialog;
import com.reflexit.magiccards.ui.dialogs.MagicFieldSelectorDialog;
import com.reflexit.magiccards.ui.jobs.ExportDeckJob;
import com.reflexit.magiccards.ui.preferences.feditors.FileSaveFieldEditor;

/**
 * First and only page of Deck Export Wizard
 */
public class DeckExportPage extends WizardDataTransferPage {
	private static final String EXPORTED_RESOURCES_SETTING = "exportedResources"; //$NON-NLS-1$
	private static final String OUTPUT_FILE_SETTING = "outputFile"; //$NON-NLS-1$
	private static final String REPORT_TYPE_SETTING = "reportType"; //$NON-NLS-1$
	private static final String INCLUDE_HEADER_SETTING = "includeHeader"; //$NON-NLS-1$
	private static final String INCLUDE_SIDEBOARD = "includeSideBoard"; //$NON-NLS-1$
	FileFieldEditor editor;
	private String fileName = "";
	private IStructuredSelection resourceSelection;
	private Button includeHeader;
	private static final String ID = DeckExportPage.class.getName();
	private ReportType reportType;
	// private LocationFilterPreferencePage locPage;
	private Combo typeCombo;
	private Button includeSideBoard;
	private StringButtonFieldEditor collection;
	private Text previewText;
	private Job previewJob;
	private StringButtonFieldEditor columnsChoice;

	protected DeckExportPage(final String pageName, final IStructuredSelection selection) {
		super(pageName);
		resourceSelection = selection == null ? null : new StructuredSelection(selection.toList());
	}

	HashMap<String, String> storeToMap(boolean sideboard, boolean sideboardSupported) {
		HashMap<String, String> map = new HashMap<String, String>();
		if (resourceSelection == null || resourceSelection.isEmpty())
			return map;
		Locations locs = Locations.getInstance();
		CardElement myDeck = (CardElement) resourceSelection.getFirstElement();
		if (!sideboardSupported) {
			String deckId = locs.getPrefConstant(myDeck.getLocation());
			map.put(deckId, "true");
		} else {
			String deckId = locs.getPrefConstant(myDeck.getLocation().toMainDeck());
			String sbId = locs.getPrefConstant(myDeck.getLocation().toSideboard());
			if (sideboard) {
				map.put(sbId, "true");
				map.put(deckId, "true");
			} else {
				map.put(deckId, "true");
			}
		}
		return map;
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
		editor = new FileSaveFieldEditor("fileSelect", "Select output file", fileSelectionArea); // NON-NLS-1
		// //NON-NLS-2
		// //$NON-NLS-1$
		editor.getTextControl(fileSelectionArea).addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				setFileName(editor.getStringValue());
				updatePageCompletion();
			}
		});
		// fileSelectionArea.moveAbove(null);
	}

	protected void setFileName(final String string) {
		fileName = string;
	}

	@Override
	protected boolean allowNewContainerName() {
		return true;
	}

	// public void handleEvent(final Event event) {
	// if (event.type == SWT.Selection && event.widget instanceof Combo) {
	// Object data = event.widget.getData(((Combo) event.widget).getText());
	// if (data instanceof ReportType) {
	// reportType = (ReportType) data;
	// }
	// }
	// updateWidgetEnablements();
	// updatePageCompletion();
	// }
	protected String getFileExtension() {
		String ext = "." + reportType.getExtension();
		return ext;
	}

	/**
	 * (non-Javadoc) Method declared on IDialogPage.
	 */
	@Override
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());
		createResourcesGroup(composite);
		createDestinationGroup(composite);
		createOptionsGroup(composite);
		createPreviewGroup(composite);
		restoreWidgetValues(); // ie.- subclass hook
		setTextFromSelection();
		updateWidgetEnablements();
		setTitle("Export");
		defaultPrompt();
		setPageComplete(determinePageCompletion());
		setErrorMessage(null); // should not initially have error message
		setControl(composite);
		MagicUIActivator.getDefault();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, MagicUIActivator.PLUGIN_ID + ".export"); //$NON-NLS-1$
		generatePreview();
	}

	protected void createPreviewGroup(Composite parent) {
		Group previewGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		previewGroup.setLayout(layout);
		previewGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		previewGroup.setText("Preview");
		previewGroup.setFont(parent.getFont());
		previewText = new Text(previewGroup, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		previewText.setText("preview...");
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 100;
		previewText.setLayoutData(layoutData);
	}

	private void defaultPrompt() {
		setMessage("Export to " + reportType.getLabel());
	}

	@Override
	protected void restoreWidgetValues() {
		super.restoreWidgetValues();
		IDialogSettings dialogSettings = MagicUIActivator.getDefault().getDialogSettings(ID);
		// restore selection
		String ids = dialogSettings.get(EXPORTED_RESOURCES_SETTING);
		if (ids != null) {
			loadFromMemento(ids);
		}
		// restore options
		String stype = dialogSettings.get(REPORT_TYPE_SETTING);
		ReportType type = ImportExportFactory.getByLabel(stype);
		if (type != null && type.getExportDelegate() != null) {
			selectReportType(type);
		} else
			selectReportType(ImportExportFactory.CSV);
		// restore file
		String file = dialogSettings.get(OUTPUT_FILE_SETTING);
		if (file != null) {
			setFileName(file);
			editor.setStringValue(file);
		}
		if (dialogSettings.get(INCLUDE_HEADER_SETTING) != null) {
			includeHeader.setSelection(dialogSettings.getBoolean(INCLUDE_HEADER_SETTING));
		}
		if (dialogSettings.get(INCLUDE_SIDEBOARD) != null) {
			includeSideBoard.setSelection(dialogSettings.getBoolean(INCLUDE_SIDEBOARD));
		}
	}

	private void loadFromMemento(String ids) {
		if (ids != null) {
			collection.setStringValue(ids);
		} else {
			collection.setStringValue("");
		}
	}

	@Override
	protected void saveWidgetValues() {
		try {
			// save pref page
			IDialogSettings dialogSettings = MagicUIActivator.getDefault().getDialogSettings(ID);
			// save file name
			dialogSettings.put(OUTPUT_FILE_SETTING, fileName);
			// save selection
			dialogSettings.put(EXPORTED_RESOURCES_SETTING, collection.getStringValue());
			// save options
			dialogSettings.put(REPORT_TYPE_SETTING, reportType.getLabel());
			dialogSettings.put(INCLUDE_HEADER_SETTING, includeHeader.getSelection());
			dialogSettings.put(INCLUDE_SIDEBOARD, includeSideBoard.getSelection());
			// save into file
			MagicUIActivator.getDefault().saveDialogSetting(dialogSettings);
		} catch (IOException e) {
			MagicUIActivator.log(e);
		}
	}

	public void setDeckSelection() {
		try {
			CardElement element = DataManager.getInstance().getModelRoot()
					.findElement(collection.getStringValue());
			if (element != null)
				resourceSelection = new StructuredSelection(element);
			else
				resourceSelection = null;
		} catch (Exception e) {
			MagicUIActivator.log(e);
		}
	}

	protected void createResourcesGroup(final Composite parent2) {
		Composite parent = new Composite(parent2, SWT.NONE);
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		collection = new StringButtonFieldEditor("deckSelect", "From Collection:", parent) {
			@Override
			protected String changePressed() {
				LocationPickerDialog dialog = new LocationPickerDialog(getShell(), SWT.SINGLE);
				dialog.setSelection(resourceSelection);
				if (dialog.open() == Window.OK) {
					if (dialog.getSelection() != null) {
						return dialog.getStringValue();
					}
				}
				return null;
			}
		};
		collection.getTextControl(parent).addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setDeckSelection();
				updatePageCompletion();
				updateWidgetEnablements();
			}
		});
	}

	/**
	 * Set the initial selections in the resource group.
	 */
	protected void setTextFromSelection() {
		if (resourceSelection != null && !resourceSelection.isEmpty()) {
			Object firstElement = resourceSelection.getFirstElement();
			if (firstElement instanceof ILocatable) {
				collection.setStringValue(((ILocatable) firstElement).getLocation().toString());
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
		// create report type
		Label label = new Label(buttonComposite, SWT.NONE);
		label.setText("Export Type:");
		typeCombo = new Combo(buttonComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
		Collection<ReportType> types = ImportExportFactory.getExportTypes();
		for (ReportType rt : types) {
			addComboType(rt);
		}
		selectReportType(ImportExportFactory.CSV);
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		gd1.horizontalSpan = 1;
		typeCombo.setLayoutData(gd1);
		typeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object data = e.widget.getData(((Combo) e.widget).getText());
				if (data instanceof ReportType) {
					reportType = (ReportType) data;
					updateWidgetEnablements();
					updatePageCompletion();
				}
			}
		});
		// options to include header
		includeHeader = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
		includeHeader.setText("Generate header row");
		includeHeader.setSelection(true);
		includeHeader.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePageCompletion();
			}
		});
		// options to include sideboard
		includeSideBoard = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
		includeSideBoard.setText("Include sideboard");
		includeSideBoard.setSelection(true);
		includeSideBoard.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePageCompletion();
			}
		});
		createFieldsControl(buttonComposite);
	}

	public void createFieldsControl(Composite area) {
		final PreferenceStore store = new PreferenceStore();
		columnsChoiceParent = new Composite(area, SWT.NONE);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = ((GridLayout) area.getLayout()).numColumns;
		columnsChoiceParent.setLayoutData(layoutData);
		columnsChoice = new StringButtonFieldEditor(CustomExportDelegate.ROW_FIELDS, "Columns:",
				columnsChoiceParent) {
			@Override
			protected String changePressed() {
				new MagicFieldSelectorDialog(getShell(), store).open();
				// validate();
				String fields = store.getString(CustomExportDelegate.ROW_FIELDS);
				columns = MagicCardField.toFields(fields, ",");
				generatePreview();
				return fields;
			}
		};
		columnsChoice.setTextLimit(60);
		columnsChoice.setPreferenceStore(store);
		if (columns != null) {
			columnsChoice.getTextControl(columnsChoiceParent).setEditable(false);
			String value = "";
			for (int i = 0; i < columns.length; i++) {
				ICardField field = columns[i];
				if (i != 0)
					value += ",";
				value += field.name();
			}
			columnsChoice.getPreferenceStore().setValue(CustomExportDelegate.ROW_FIELDS, value);
		}
		columnsChoice.load();
	}

	private void addComboType(ReportType rt) {
		typeCombo.add(rt.getLabel());
		typeCombo.setData(rt.getLabel(), rt);
	}

	private void selectReportType(final ReportType type) {
		if (type == null)
			return;
		reportType = type;
		typeCombo.setText(type.getLabel());
	}

	@Override
	protected String getErrorDialogTitle() {
		return "Error";
	}

	@Override
	protected boolean validateSourceGroup() {
		if (collection.getStringValue().equals("")) {
			setErrorMessage("Select an element to export");
			return false;
		}
		if (resourceSelection == null) {
			setErrorMessage("Invalid deck/collection selected");
			return false;
		}
		return true;
	}

	@Override
	protected boolean validateDestinationGroup() {
		if ((fileName == null) || (fileName.length() == 0) || (editor.getStringValue().length() == 0)) {
			setMessage("File is not selected");
			return false;
		}
		String ext = getFileExtension();
		if (!(fileName.endsWith(ext))) {
			setMessage("File should have " + ext + " extension");
			return true;
		}
		return true;
	}

	@Override
	protected void updatePageCompletion() {
		super.updatePageCompletion();
		if (isPageComplete()) {
			defaultPrompt(); // set default prompt, otherwise it empty ugly
		}
		generatePreview();
	}

	/**
	 * Creates a new button with the given id.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method creates a standard push button,
	 * registers for selection events including button presses and registers default buttons with its shell.
	 * The button id is stored as the buttons client data. Note that the parent's layout is assumed to be a
	 * GridLayout and the number of columns in this layout is incremented. Subclasses may override.
	 * </p>
	 *
	 * @param parent
	 *            the parent composite
	 * @param id
	 *            the id of the button (see <code>IDialogConstants.*_ID</code> constants for standard dialog
	 *            button ids)
	 * @param label
	 *            the label from the button
	 * @param defaultButton
	 *            <code>true</code> if the button is to be the default button, and <code>false</code>
	 *            otherwise
	 */
	protected Button createButton(final Composite parent, final int id, final String label,
			final boolean defaultButton) {
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
		includeHeader.setEnabled(!reportType.isXmlFormat());
		String ext = getFileExtension();
		editor.setFileExtensions(new String[] { "*" + ext });
		if (fileName.length() > 0) {
			File file = new File(fileName);
			String name = new File(collection.getStringValue()).getName();
			if (file.getParent() != null) {
				String fileName2 = file.getParent() + File.separator + name + ext;
				if (!fileName2.equals(fileName)) {
					fileName = fileName2;
					editor.setStringValue(fileName);
				}
			}
		}
		IExportDelegate delegate = reportType.getExportDelegate();
		if (delegate != null) {
			includeSideBoard.setEnabled(delegate.isMultipleLocationSupported());
			columnsChoice.setEnabled(delegate.isColumnChoiceSupported(), columnsChoiceParent);
		} else {
			includeSideBoard.setEnabled(false);
			columnsChoice.setEnabled(false, columnsChoiceParent);
		}
	}

	public ReportType getReportType() {
		return reportType;
	}

	public String getFileName() {
		return fileName;
	}

	public boolean getIncludeHeader() {
		return includeHeader.getSelection();
	}

	public boolean getIncludeSideBoard() {
		return includeSideBoard.getSelection();
	}

	public CardElement getFirstCardElement() {
		CardElement ce = null;
		for (Object object : resourceSelection.toList()) {
			if (object instanceof CardOrganizer)
				continue;
			if (ce != null)
				throw new IllegalArgumentException("Select only one element");
			ce = (CardElement) object;
		}
		return ce;
	}

	public void generatePreview() {
		if (resourceSelection == null) {
			updatePreview("");
			return;
		}
		final OutputStream outStream = new ByteArrayOutputStream(1024 * 4);
		saveWidgetValues();
		final boolean header = getIncludeHeader();
		final boolean sideboard = getIncludeSideBoard();
		final ReportType type = getReportType();
		if (previewJob != null) {
			previewJob.cancel();
		}
		previewJob = new Job("Generating preview") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					exportDeck(outStream, monitor, type, header, sideboard);
					if (!monitor.isCanceled())
						updatePreview(outStream.toString());
				} catch (InvocationTargetException e) {
					if (e.getTargetException() instanceof InterruptedException) {
						//
					} else if (!monitor.isCanceled())
						updatePreview(e.getCause().getMessage());
				} catch (InterruptedException e) {
					//
				} catch (Exception e) {
					if (!monitor.isCanceled())
						updatePreview(e.getMessage());
				}
				return Status.OK_STATUS;
			}
		};
		previewJob.schedule();
	}

	protected void updatePreview(final String string) {
		if (getControl() == null)
			return;
		getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				previewText.setText(string);
				previewText.getParent().layout(true, true);
			}
		});
	}

	public boolean saveFile() {
		final DeckExportPage mainPage = this;
		final String fileName = mainPage.getFileName();
		if (new File(fileName).exists()) {
			String res = mainPage.queryOverwrite(fileName);
			if (res == IOverwriteQuery.CANCEL)
				return false;
			if (res == IOverwriteQuery.NO)
				return false;
		}
		boolean res = false;
		try {
			final OutputStream outStream = new FileOutputStream(fileName);
			final boolean header = getIncludeHeader();
			final boolean sideboard = getIncludeSideBoard();
			IRunnableWithProgress work = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						exportDeck(outStream, monitor, reportType, header, sideboard);
						outStream.close();
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					}
				}
			};
			getRunnableContext().run(true, true, work);
			return true;
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof InterruptedException) {
				mainPage.displayErrorDialog("Export cancelled");
			} else
				mainPage.displayErrorDialog(e.getCause());
		} catch (InterruptedException e) {
			mainPage.displayErrorDialog("Export cancelled");
		} catch (Exception e) {
			mainPage.displayErrorDialog(e);
		}
		return res;
	}

	public void exportDeck(final OutputStream outStream, IProgressMonitor monitor, ReportType reportType,
			boolean header, boolean sideboard)
			throws InvocationTargetException, InterruptedException {
		// TODO: export selection only
		IExportDelegate exportDelegate = reportType.getExportDelegate();
		final HashMap<String, String> map = storeToMap(sideboard, exportDelegate.isSideboardSupported());
		IFilteredCardStore filteredLibrary = DataManager.getCardHandler()
				.getLibraryFilteredStoreWorkingCopy();
		MagicCardFilter locFilter = filteredLibrary.getFilter();
		locFilter.update(map);
		if (sideboard)
			locFilter.getSortOrder().setSortField(MagicCardField.SIDEBOARD, true);
		filteredLibrary.update();
		new ExportDeckJob(outStream, reportType, header, filteredLibrary, columns).syncRun();
	}

	private ICardField[] columns;
	private Composite columnsChoiceParent;

	public void setColumns(ICardField[] columns2) {
		this.columns = columns2;
	}
}
