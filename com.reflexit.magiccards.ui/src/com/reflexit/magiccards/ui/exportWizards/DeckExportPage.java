package com.reflexit.magiccards.ui.exportWizards;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.exports.ImportExportFactory;
import com.reflexit.magiccards.core.exports.ReportType;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Locations;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.LocationPickerDialog;
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
	private String fileName;
	private IStructuredSelection resourceSelection;
	private Button includeHeader;
	private final ArrayList typeButtons = new ArrayList();
	private static final String ID = DeckExportPage.class.getName();
	private ReportType reportType;
	// private LocationFilterPreferencePage locPage;
	private Combo typeCombo;
	private Button includeSideBoard;
	private StringButtonFieldEditor collection;

	protected DeckExportPage(final String pageName, final IStructuredSelection selection) {
		super(pageName);
		resourceSelection = new StructuredSelection(selection.toList());
	}

	HashMap<String, String> storeToMap(boolean sideboard) {
		HashMap<String, String> map = new HashMap<String, String>();
		Locations locs = Locations.getInstance();
		CardElement myDeck = (CardElement) resourceSelection.getFirstElement();
		String deckId = locs.getPrefConstant(myDeck.getLocation().toMainDeck());
		String sbId = locs.getPrefConstant(myDeck.getLocation().toSideboard());
		if (sideboard) {
			map.put(sbId, "true");
			map.put(deckId, "true");
		} else {
			map.put(deckId, "true");
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
		if (event.type == SWT.Selection && event.widget instanceof Combo) {
			Object data = event.widget.getData(((Combo) event.widget).getText());
			if (data instanceof ReportType) {
				fileName = fileName.replaceAll("\\Q" + getFileExtension() + "\\E$", "");
				reportType = (ReportType) data;
			}
		}
		updateWidgetEnablements();
		updatePageCompletion();
	}

	protected String getFileExtension() {
		String ext = "." + reportType.getExtension();
		return ext;
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
		if (resourceSelection != null) {
			setupBasedOnInitialSelections();
		}
		updateWidgetEnablements();
		setTitle("Export");
		defaultPrompt();
		setPageComplete(determinePageCompletion());
		setErrorMessage(null); // should not initially have error message
		setControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, MagicUIActivator.getDefault().PLUGIN_ID + ".export"); //$NON-NLS-1$
	}

	/**
	 * Creates the buttons for selecting specific types or selecting all or none of the elements.
	 * 
	 * @param parent
	 *            the parent control
	 */
	protected final void createButtonsGroup(final Composite parent) {
	}

	private void defaultPrompt() {
		setMessage("Export to " + reportType.getLabel());
	}

	@Override
	protected void restoreWidgetValues() {
		super.restoreWidgetValues();
		IDialogSettings dialogSettings = MagicUIActivator.getDefault().getDialogSettings(ID);
		// restore file
		String file = dialogSettings.get(OUTPUT_FILE_SETTING);
		if (file != null) {
			setFileName(file);
			editor.setStringValue(file);
		}
		// restore selection
		String ids = dialogSettings.get(EXPORTED_RESOURCES_SETTING);
		if (ids != null) {
			loadFromMemento(ids);
		}
		// restore options
		String type = dialogSettings.get(REPORT_TYPE_SETTING);
		if (type != null)
			selectReportType(ReportType.valueOf(type));
		else
			selectReportType(ReportType.CSV);
		if (dialogSettings.get(INCLUDE_HEADER_SETTING) != null) {
			includeHeader.setSelection(dialogSettings.getBoolean(INCLUDE_HEADER_SETTING));
		}
		if (dialogSettings.get(INCLUDE_SIDEBOARD) != null) {
			includeSideBoard.setSelection(dialogSettings.getBoolean(INCLUDE_SIDEBOARD));
		}
	}

	private void loadFromMemento(String ids) {
		// TODO Auto-generated method stub
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
			// save pref page
			IDialogSettings dialogSettings = MagicUIActivator.getDefault().getDialogSettings(ID);
			// save file name
			dialogSettings.put(OUTPUT_FILE_SETTING, fileName);
			// save selection
			dialogSettings.put(EXPORTED_RESOURCES_SETTING, collection.getStringValue());
			// save options
			dialogSettings.put(REPORT_TYPE_SETTING, reportType.toString());
			dialogSettings.put(INCLUDE_HEADER_SETTING, includeHeader.getSelection());
			dialogSettings.put(INCLUDE_SIDEBOARD, includeSideBoard.getSelection());
			// save into file
			MagicUIActivator.getDefault().saveDialogSetting(dialogSettings);
		} catch (IOException e) {
			MagicUIActivator.log(e);
		}
	}

	protected void createResourcesGroup(final Composite parent2) {
		Composite parent = new Composite(parent2, SWT.NONE);
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		collection = new StringButtonFieldEditor("deckSelect", "Collection:", parent) {
			@Override
			protected String changePressed() {
				LocationPickerDialog dialog = new LocationPickerDialog(getShell(), SWT.SINGLE);
				dialog.setSelection(resourceSelection);
				if (dialog.open() == Window.OK) {
					if (dialog.getSelection() != null) {
						resourceSelection = dialog.getSelection();
					}
					return resourceSelection.getFirstElement().toString();
				}
				return null;
			}
		};
		collection.getTextControl(parent).addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updatePageCompletion();
				updateWidgetEnablements();
			}
		});
	}

	/**
	 * Set the initial selections in the resource group.
	 */
	protected void setupBasedOnInitialSelections() {
		collection.setStringValue(resourceSelection.getFirstElement().toString());
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
		Collection<ReportType> types = new ImportExportFactory<IMagicCard>().getExportTypes();
		types.add(ReportType.XML);
		for (ReportType reportType : types) {
			addComboType(reportType);
		}
		selectReportType(ReportType.CSV);
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		gd1.horizontalSpan = 1;
		typeCombo.setLayoutData(gd1);
		// options to include header
		includeHeader = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
		includeHeader.setText("Generate header row");
		includeHeader.setSelection(true);
		// options to include sideboard
		includeSideBoard = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
		includeSideBoard.setText("Include sideboard");
		includeSideBoard.setSelection(true);
	}

	private void addComboType(ReportType reportType) {
		typeCombo.add(reportType.getLabel());
		typeCombo.setData(reportType.getLabel(), reportType);
		typeCombo.addListener(SWT.Selection, this);
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
			setMessage("Select an element to export");
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
	}

	/**
	 * Creates a new button with the given id.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method creates a standard push
	 * button, registers for selection events including button presses and registers default buttons
	 * with its shell. The button id is stored as the buttons client data. Note that the parent's
	 * layout is assumed to be a GridLayout and the number of columns in this layout is incremented.
	 * Subclasses may override.
	 * </p>
	 * 
	 * @param parent
	 *            the parent composite
	 * @param id
	 *            the id of the button (see <code>IDialogConstants.*_ID</code> constants for
	 *            standard dialog button ids)
	 * @param label
	 *            the label from the button
	 * @param defaultButton
	 *            <code>true</code> if the button is to be the default button, and
	 *            <code>false</code> otherwise
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
		includeHeader.setEnabled(!reportType.isXmlFormat());
		String ext = getFileExtension();
		if (!fileName.endsWith(ext)) {
			fileName = fileName + ext;
			editor.setStringValue(fileName);
		}
		editor.setFileExtensions(new String[] { ext });
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
}
