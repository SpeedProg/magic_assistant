package com.reflexit.magiccards.ui.exportWizards;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.exports.IImportDelegate;
import com.reflexit.magiccards.core.exports.ImportExportFactory;
import com.reflexit.magiccards.core.exports.ImportUtils;
import com.reflexit.magiccards.core.exports.PreviewResult;
import com.reflexit.magiccards.core.exports.ReportType;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.CorrectSetDialog;
import com.reflexit.magiccards.ui.dialogs.LocationPickerDialog;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;

/**
 * First and only page of Deck Export Wizard
 */
public class DeckImportPage extends WizardDataTransferPage {
	private static final String IMPUT_FILE_SETTING = "outputFile"; //$NON-NLS-1$
	private static final String REPORT_TYPE_SETTING = "reportType"; //$NON-NLS-1$
	private static final String IMPORT_HEADER_SETTING = "headerRow"; //$NON-NLS-1$
	private static final String IMPORT_CLIPBOARD = "clipboard"; //$NON-NLS-1$
	FileFieldEditor editor;
	private String fileName;
	private IStructuredSelection initialResourceSelection;
	private Button includeHeader;
	private static final String ID = DeckImportPage.class.getName();
	private ReportType reportType;
	private PreferenceStore store;
	private boolean clipboard;
	private Combo typeCombo;
	private Button fileRadio;
	private Button clipboardRadio;
	private Composite fileSelectionArea;
	private PreviewResult previewResult;
	private CardElement element;
	private Button createNewDeck;
	private Button importIntoExisting;

	protected DeckImportPage(final String pageName, final IStructuredSelection selection) {
		super(pageName);
		initialResourceSelection = selection;
		store = new PreferenceStore();
	}

	public boolean performImport(final boolean preview) {
		boolean res = false;
		try {
			final boolean header = includeHeader.getSelection();
			final InputStream st = openInputStream();
			try {
				IRunnableWithProgress work = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						IImportDelegate<IMagicCard> worker;
						try {
							worker = new ImportExportFactory<IMagicCard>().getImportWorker(reportType);
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}
						if (preview) {
							// if error occurs previewResult.error would be set
							// to exception
							previewResult = ImportUtils.performPreview(st, worker, header, new CoreMonitorAdapter(monitor));
							((DeckImportWizard) getWizard()).setData(previewResult);
						} else {
							Location selectedLocation = getSelectedLocation();
							if (selectedLocation == null) {
								// create a new deck
								createNewDeck(getNewDeckName());
								selectedLocation = getSelectedLocation();
							}
							Collection<IMagicCard> result = ImportUtils.performPreImport(st, worker, header, selectedLocation,
									new CoreMonitorAdapter(monitor));
							if (fixErrors(result)) {
								ImportUtils.performImport(result, DataManager.getCardHandler().getLibraryCardStore());
							}
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

	private boolean fixErrors(final Collection<IMagicCard> result) {
		getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
				int size = result.size();
				int errors = 0;
				Editions editions = Editions.getInstance();
				HashMap<String, String> badSets = new HashMap<String, String>();
				for (Iterator iterator = result.iterator(); iterator.hasNext();) {
					IMagicCard card = (IMagicCard) iterator.next();
					if (card.getCardId() == 0) {
						errors++;
						String set = card.getSet();
						Edition eset = editions.getEditionByName(set);
						if (eset == null)
							badSets.put(set, set);
					}
				}
				if (errors != 0) {
					boolean yes = MessageDialog.openQuestion(getShell(), "Import Error", "Cannot resolve " + errors + " cards of " + size
							+ ". The following sets are not found: " + badSets.keySet()
							+ ".\n Do you want to attempt to fix import errors?");
					if (yes) {
						List x = new ArrayList(badSets.keySet());
						// ...
						for (Iterator<String> iterator = x.iterator(); iterator.hasNext();) {
							String set = iterator.next();
							CorrectSetDialog dialog = new CorrectSetDialog(getShell(), set);
							if (dialog.open() == Window.OK) {
								String newSet = dialog.getSet();
								badSets.put(set, newSet);
							} else {
								break;
							}
						}
						IFilteredCardStore magicDbHandler = DataManager.getCardHandler().getMagicDBFilteredStore();
						for (Iterator iterator = result.iterator(); iterator.hasNext();) {
							IMagicCard card = (IMagicCard) iterator.next();
							if (card.getCardId() == 0 && card instanceof MagicCardPhisical) {
								String set = card.getSet();
								String corr = badSets.get(set);
								if (corr != null) {
									if (!corr.equals(CorrectSetDialog.SKIP)) {
										MagicCard newCard = card.getBase();
										newCard.setSet(corr);
										ImportUtils.updateCardReference((MagicCardPhisical) card, magicDbHandler.getCardStore());
										if (card.getCardId() != 0) {
											errors--;
										}
									} else {
										iterator.remove();
										errors--;
									}
								}
							}
						}
						if (errors != 0) {
							MessageDialog.openInformation(getShell(), "Import", "After all this effort I cannot resolve " + errors
									+ " cards of " + size);
						}
					}
				}
			}
		});
		return true;
	}

	protected void createNewDeck(final String newDeckName) {
		// create a sample file
		ModelRoot root = DataManager.getModelRoot();
		final CardElement resource = root.getDeckContainer();
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				CardCollection element = CardsNavigatorView.createNewDeckAction((CollectionsContainer) resource, newDeckName, page);
				element.setVirtual(true);
				setElement(element);
			}
		});
	}

	protected String getNewDeckName() {
		if (clipboard) {
			return "clipboardDeck" + new Random().nextInt(1000);
		} else {
			String basename = new File(fileName).getName();
			int k = basename.lastIndexOf('.');
			if (k >= 0)
				basename = basename.substring(0, k);
			return basename;
		}
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
		if (element != null)
			return element.getLocation();
		return null;
	}

	protected IPreferenceStore getPreferenceStore() {
		return store;
	}

	protected IRunnableContext getRunnableContext() {
		return getContainer();
	}

	protected void createDestinationGroup(final Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayoutData(GridDataFactory.fillDefaults().create());
		group.setText("Import into");
		group.setLayout(new GridLayout(3, false));
		createNewDeck = new Button(group, SWT.RADIO);
		createNewDeck.setText("Create a new deck");
		createNewDeck.setLayoutData(GridDataFactory.swtDefaults().span(3, 1).create());
		createNewDeck.setSelection(true);
		importIntoExisting = new Button(group, SWT.RADIO);
		importIntoExisting.setText("Import into existing deck/collection");
		final Text text = new Text(group, SWT.BORDER);
		text.setEditable(false);
		text.setLayoutData(GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.BEGINNING).create());
		Button browse = new Button(group, SWT.PUSH);
		browse.setText("Browse...");
		browse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LocationPickerDialog dialog = new LocationPickerDialog(getShell(), SWT.SINGLE);
				dialog.setSelection(initialResourceSelection);
				if (dialog.open() == Window.OK) {
					if (dialog.getSelection() != null && !dialog.getSelection().isEmpty()) {
						element = (CardElement) dialog.getSelection().getFirstElement();
						text.setText(element.getName());
					}
					importIntoExisting.setSelection(true);
					createNewDeck.setSelection(false);
					updatePageCompletion();
					updateWidgetEnablements();
				}
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
		restoreWidgetValues();
		updateWidgetEnablements();
		setTitle("Import to a Deck or Collection");
		defaultPrompt();
		setPageComplete(determinePageCompletion());
		setErrorMessage(null); // should not initially have error message
		setControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, MagicUIActivator.PLUGIN_ID + ".export");
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
		IDialogSettings dialogSettings = MagicUIActivator.getDefault().getDialogSettings(ID);
		// restore file
		String file = dialogSettings.get(IMPUT_FILE_SETTING);
		if (file != null) {
			setFileName(file);
			editor.setStringValue(file);
		}
		clipboard = dialogSettings.getBoolean(IMPORT_CLIPBOARD);
		fileRadio.setSelection(!clipboard);
		clipboardRadio.setSelection(clipboard);
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
			IDialogSettings dialogSettings = MagicUIActivator.getDefault().getDialogSettings(ID);
			// save file name
			dialogSettings.put(IMPUT_FILE_SETTING, fileName);
			dialogSettings.put(IMPORT_CLIPBOARD, clipboard);
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
		if (element == null && importIntoExisting.getSelection()) {
			setMessage("Select a deck or collection to import data into or select to import to a new deck.");
			return false;
		}
		return true;
	}

	@Override
	protected boolean validateSourceGroup() {
		if (clipboard == false && ((fileName == null) || (fileName.length() == 0) || (editor.getStringValue().length() == 0))) {
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
	 * @param element
	 *            the element to set
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
