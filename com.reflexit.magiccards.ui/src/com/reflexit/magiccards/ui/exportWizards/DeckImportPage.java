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
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.exports.IImportDelegate;
import com.reflexit.magiccards.core.exports.ImportExportFactory;
import com.reflexit.magiccards.core.exports.ImportResult;
import com.reflexit.magiccards.core.exports.ImportUtils;
import com.reflexit.magiccards.core.exports.ReportType;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.CorrectSetDialog;
import com.reflexit.magiccards.ui.dialogs.LocationPickerDialog;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;
import com.reflexit.magiccards.ui.utils.WaitUtils;
import com.reflexit.magiccards.ui.widgets.MagicToolkit;

/**
 * First and only page of Deck Export Wizard
 */
public class DeckImportPage extends WizardDataTransferPage implements Listener {
	private static final String IMPUT_FILE_SETTING = "outputFile"; //$NON-NLS-1$
	private static final String REPORT_TYPE_SETTING = "reportType"; //$NON-NLS-1$
	private static final String IMPORT_HEADER_SETTING = "headerRow"; //$NON-NLS-1$
	private static final String FROM_CHOICE = "from"; //$NON-NLS-1$
	private static final String INTO_CHOICE = "into"; //$NON-NLS-1$
	private final String ID = getClass().getName();
	private Text editor;
	private String fileName;
	private IStructuredSelection initialResourceSelection;
	private Button includeHeader;
	private ReportType reportType;
	private PreferenceStore store;
	private Combo typeCombo;
	private Button fileRadio;
	private Button clipboardRadio;
	private Button inputRadio;
	private Composite fileSelectionArea;
	private ImportResult previewResult;
	private CardElement element;
	protected Button createNewDeck;
	protected Button importIntoExisting;
	protected Button importIntoDb;
	private Collection<ReportType> types;
	protected Button virtualCards;

	enum InputChoice {
		FILE,
		CLIPBOARD,
		INPUT
	};

	private InputChoice inputChoice;

	protected DeckImportPage(final String pageName, final IStructuredSelection selection) {
		super(pageName);
		initialResourceSelection = selection;
		store = new PreferenceStore();
	}

	public ImportResult performImport(final boolean preview) {
		final boolean header = includeHeader.getSelection();
		int choice = getIntoChoice();
		final boolean newdeck = choice == 1;
		final boolean dbImport = choice == 3;
		final boolean virtual = virtualCards.getSelection();
		if (previewResult == null)
			previewResult = new ImportResult();
		try {
			IRunnableWithProgress work = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
					monitor.beginTask("Importing", 100);
					try (final InputStream st = openInputStream()) {
						monitor.worked(10);
						IImportDelegate<IMagicCard> worker;
						try {
							worker = reportType.getImportDelegate();
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}
						if (worker == null)
							throw new IllegalArgumentException("Importer is not defined for "
									+ reportType.getLabel());
						CoreMonitorAdapter monitor2 = new CoreMonitorAdapter(new SubProgressMonitor(monitor,
								50));
						boolean resolve = !dbImport;
						if (preview) {
							String text = "";
							try {
								text = FileUtils.readStreamAsStringAndClose(openInputStream());
							} catch (Exception e) {
								// ignore
							}
							monitor.worked(10);
							// if error occurs importResult.error would be set
							// to exception
							Location location = Location.createLocation("preview");
							try {
								previewResult = ImportUtils.performPreImport(st, worker, header, virtual,
										location, resolve, monitor2);
							} finally {
								previewResult.setText(text);
							}
							if (dbImport) {
								ImportUtils.performPreImportWithDb((Collection<IMagicCard>) previewResult
										.getList(), new ArrayList<>(),
										previewResult.getFields());
								monitor.worked(10);
							}
						} else {
							Location location = getSelectedLocation();
							if (newdeck) {
								// create a new deck
								createNewDeck(getNewDeckName(), virtual);
								location = getSelectedLocation();
							}
							if (!previewResult.isOk()) {
								previewResult = ImportUtils.performPreImport(st, worker, header, virtual,
										location, resolve, monitor2);
							}
							Collection<IMagicCard> result = (Collection<IMagicCard>) previewResult.getList();
							if (resolve) {
								ImportUtils.fixLocations(location, result);
								monitor.worked(10);
							}
							if (fixErrors(result, dbImport)) {
								monitor.worked(10);
								if (resolve)
									ImportUtils.performImport(result, DataManager.getCardHandler()
											.getLibraryCardStore());
							}
						}
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			};
			getRunnableContext().run(true, true, work);
		} catch (InvocationTargetException e) {
			previewResult.setError(e.getCause());
			displayErrorDialog(e.getCause());
		} catch (InterruptedException e) {
			previewResult.setError(e);
			displayErrorDialog(e);
		}
		return previewResult;
	}

	private boolean fixErrors(final Collection<IMagicCard> result, final boolean dbImport) {
		final boolean yesres[] = new boolean[] { true };
		getControl().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				int size = result.size();
				Map<String, String> badSets = ImportUtils.getSetCandidates(result);
				if (badSets.size() > 0) {
					boolean yes = MessageDialog.openQuestion(getShell(), "Import Error", "Cannot resolve "
							+ badSets.size()
							+ " set(s). The following sets are not found or ambigues: " + badSets.keySet()
							+ ".\n Do you want to fix these?");
					if (yes) {
						// ask user to fix sets
						for (Iterator<String> iterator = badSets.keySet().iterator(); iterator.hasNext();) {
							String set = iterator.next();
							CorrectSetDialog dialog = new CorrectSetDialog(getShell(), set, badSets.get(set));
							if (dialog.open() == Window.OK) {
								String newSet = dialog.getSet();
								badSets.put(set, newSet);
							} else {
								break;
							}
						}
						// fix cards for these sets
						ImportUtils.fixSets(result, badSets);
					}
				}
				ArrayList<IMagicCard> newdbrecords = new ArrayList<IMagicCard>();
				ImportUtils.performPreImportWithDb(result, newdbrecords, reportType.getImportDelegate()
						.getResult().getFields());
				ArrayList<String> lerrors = new ArrayList<String>();
				ImportUtils.validateDbRecords(newdbrecords, lerrors);
				if (newdbrecords.size() > 0 && lerrors.size() == 0) {
					boolean yes2 = dbImport;
					if (yes2 == false) {
						yes2 = MessageDialog.openQuestion(
								getShell(),
								"Import into DB",
								newdbrecords.size()
										+ " cards are not found in the database. Do you want to add new cards into database?");
					}
					if (yes2)
						ImportUtils.importIntoDb(newdbrecords);
				} else if (lerrors.size() > 0 && dbImport) {
					String message = newdbrecords.size() + " cards are not found in the database "
							+ lerrors.size()
							+ "\nThe following errors preventing import all of them into database:\n";
					for (Iterator iterator = lerrors.iterator(); iterator.hasNext();) {
						String str = (String) iterator.next();
						message += str + "\n";
					}
					message += "Do you want to proceed with importing cards partually? No would abort the import into DB";
					boolean yes2 = MessageDialog.openQuestion(getShell(), "Import into DB", message);
					if (yes2)
						ImportUtils.importIntoDb(newdbrecords);
				}
				ArrayList<String> cerrors = new ArrayList<String>();
				ICardStore magicDb = DataManager.getCardHandler().getMagicDBStore();
				for (Iterator iterator = result.iterator(); iterator.hasNext();) {
					IMagicCard card = (IMagicCard) iterator.next();
					if (card.getCardId() == 0 || magicDb.getCard(card.getCardId()) == null) {
						iterator.remove();
						cerrors.add(card.getName() + " (" + card.getSet() + ")");
					}
				}
				if (cerrors.size() != 0) {
					String message = "After all this effort I cannot resolve " + cerrors.size()
							+ " cards of " + size + ":\n";
					int i = 0;
					for (Iterator iterator = cerrors.iterator(); iterator.hasNext() && i < 10; i++) {
						String str = (String) iterator.next();
						message += str + "\n";
					}
					if (i < cerrors.size()) {
						message += "... + " + (cerrors.size() - i) + " more\n";
						message += "Full error log can be found at <workspace>/.metadata/.log\n";
					}
					if (cerrors.size() >= size) {
						message += "No good cards to import :(";
						yesres[0] = false;
						MessageDialog.openError(getShell(), "Import", message);
					} else {
						message += "Do you want to proceed with importing cards partually? No would abort the import";
						yesres[0] = MessageDialog.openQuestion(getShell(), "Import", message);
					}
				}
			}
		});
		return yesres[0];
	}

	protected void createNewDeck(final String newDeckName, boolean virtual) {
		ModelRoot root = DataManager.getInstance().getModelRoot();
		final CollectionsContainer resource = root.getDeckContainer();
		CardCollection element = resource.addDeck(newDeckName + ".xml", virtual);
		setElement(element);
	}

	protected String getNewDeckName() {
		if (inputChoice != InputChoice.FILE) {
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
		String text = "";
		switch (inputChoice) {
			case FILE:
				return new FileInputStream(fileName);
			case CLIPBOARD:
				text = getClipboardText();
				break;
			case INPUT:
				if (previewResult != null)
					text = previewResult.getText();
				break;
			default:
				break;
		}
		return new ByteArrayInputStream(text.getBytes());
	}

	public String getClipboardText() {
		String text[] = new String[] { "" };
		WaitUtils.syncExec(() -> {
			final Clipboard cb = new Clipboard(PlatformUI.getWorkbench().getDisplay());
			Object clipboardText = cb.getContents(TextTransfer.getInstance());
			if (clipboardText == null)
				clipboardText = "";
			text[0] = clipboardText.toString();
		});
		return text[0];
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
		group.setText("Import choice");
		group.setLayout(new GridLayout(3, false));
		SelectionAdapter updateListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePageCompletion();
				updateWidgetEnablements();
			}
		};
		createNewDeck = new Button(group, SWT.RADIO);
		createNewDeck.setText("Create a new deck");
		createNewDeck.setLayoutData(GridDataFactory.swtDefaults().span(3, 1).create());
		createNewDeck.setSelection(true);
		createNewDeck.addSelectionListener(updateListener);
		importIntoExisting = new Button(group, SWT.RADIO);
		importIntoExisting.setText("Add cards into existing deck/collection");
		importIntoExisting.addSelectionListener(updateListener);
		final Text text = new Text(group, SWT.BORDER);
		text.setEditable(false);
		text.setLayoutData(GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.BEGINNING)
				.create());
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
		importIntoDb = new Button(group, SWT.RADIO);
		importIntoDb.setText("Extends cards database (do not create new deck or collection)");
		importIntoDb.setLayoutData(GridDataFactory.swtDefaults().span(3, 1).create());
		importIntoDb.setSelection(false);
		importIntoDb.addSelectionListener(updateListener);
	}

	private void setFileName(final String string) {
		fileName = string;
	}

	@Override
	protected boolean allowNewContainerName() {
		return true;
	}

	@Override
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
	@Override
	public void createControl(final Composite parent) {
		setTitle("Import into a Deck or Collection");
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
		defaultPrompt();
		setPageComplete(determinePageCompletion());
		setErrorMessage(null); // should not initially have error message
		setControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, MagicUIActivator.PLUGIN_ID + ".export");
	}

	private void defaultPrompt() {
		String mess = "You have selected '" + reportType.getLabel() + "' format.\n";
		if (reportType == ImportExportFactory.XML)
			setMessage(mess);
		else if (reportType == ImportExportFactory.CSV)
			setMessage(mess
					+ "Columns: ID,NAME,COST,TYPE,P,T,TEXT,SET,RARITY,DBPRICE,LANG,COUNT,PRICE,COMMENT");
		else if (reportType == ImportExportFactory.TEXT_DECK_CLASSIC)
			setMessage(mess + "Lines like 'Quagmire Druid x 3' or 'Diabolic Tutor (Tenth Edition) x4'");
		else if (reportType == ImportExportFactory.TABLE_PIPED)
			setMessage(mess
					+ "Columns: ID|NAME|COST|TYPE|P|T|TEXT|SET|RARITY|RESERVED|LANG|COUNT|PRICE|COMMENT");
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
			editor.setText(file);
		}
		String sfrom = dialogSettings.get(FROM_CHOICE);
		inputChoice = InputChoice.FILE;
		if (sfrom != null) {
			try {
				inputChoice = InputChoice.valueOf(sfrom);
			} catch (Exception e) {
				// ignore
			}
		}
		setInputChoice(inputChoice);
		// restore options
		String stype = dialogSettings.get(REPORT_TYPE_SETTING);
		ReportType type = ImportExportFactory.getByLabel(stype);
		if (type != null && type.getImportDelegate() != null) {
			selectReportType(type);
		} else {
			selectReportType(ImportExportFactory.TEXT_DECK_CLASSIC);
		}
		if (dialogSettings.get(IMPORT_HEADER_SETTING) != null) {
			includeHeader.setSelection(dialogSettings.getBoolean(IMPORT_HEADER_SETTING));
		}
		int choice = 0;
		try {
			choice = dialogSettings.getInt(INTO_CHOICE);
		} catch (Exception e) {
			// ignore
		}
		if (choice == 0) choice = 1;
		if (createNewDeck != null) createNewDeck.setSelection(choice == 1);
		if (importIntoExisting != null) importIntoExisting.setSelection(choice == 2);
		if (importIntoDb != null) importIntoDb.setSelection(choice == 3);
		updateWidgetEnablements();
	}

	public void setInputChoice(InputChoice inputChoice) {
		this.inputChoice = inputChoice;
		fileRadio.setSelection(inputChoice == InputChoice.FILE);
		clipboardRadio.setSelection(inputChoice == InputChoice.CLIPBOARD);
		inputRadio.setSelection(inputChoice == InputChoice.INPUT);
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
			dialogSettings.put(FROM_CHOICE, inputChoice.name());
			// save options
			dialogSettings.put(REPORT_TYPE_SETTING, reportType.getLabel());
			dialogSettings.put(IMPORT_HEADER_SETTING, includeHeader.getSelection());
			// into options
			int choice = getIntoChoice();
			dialogSettings.put(INTO_CHOICE, choice);
			// save into file
			MagicUIActivator.getDefault().saveDialogSetting(dialogSettings);
		} catch (IOException e) {
			MagicUIActivator.log(e);
		}
	}

	public int getIntoChoice() {
		int choice = 0;
		if (createNewDeck != null && createNewDeck.getSelection())
			choice = 1;
		else if (importIntoExisting != null && importIntoExisting.getSelection())
			choice = 2;
		else if (importIntoDb != null && importIntoDb.getSelection())
			choice = 3;
		return choice;
	}

	protected void createResourcesGroup(final Composite parent) {
		MagicToolkit toolkit = MagicToolkit.getInstance();
		fileSelectionArea = toolkit.createComposite(parent);
		fileSelectionArea.setLayoutData(GridDataFactory.fillDefaults().create());
		fileSelectionArea.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).margins(0, 0).create());
		fileRadio = toolkit.createButton(fileSelectionArea, "File", SWT.RADIO,
				(e) -> {
					inputChoice = InputChoice.FILE;
					updateWidgetEnablements();
					autoDetectFormat();
					updatePageCompletion();
				}
				);
		fileRadio.setSelection(true);
		editor = toolkit.createText(fileSelectionArea, "", SWT.BORDER);
		editor.addModifyListener(
				(e) -> {
					File file = new File(editor.getText());
					setFileName(file.getPath());
					autoDetectFormat();
					updatePageCompletion();
				}
				);
		editor.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		setFileName("");
		toolkit.createButton(fileSelectionArea, "Browse...", SWT.PUSH,
				(e) -> {
					FileDialog fileDialog = new FileDialog(parent.getShell());
					fileDialog.setFileName(editor.getText());
					String file = fileDialog.open();
					if (file != null) {
						editor.setText(file);
						setFileName(file);
						inputChoice = InputChoice.FILE;
						updateWidgetEnablements();
						autoDetectFormat();
						updatePageCompletion();
					}
				}
				);
		// clipboard control
		clipboardRadio = toolkit.createButton(fileSelectionArea, "Clipboard",
				SWT.RADIO | SWT.LEFT,
				(e) -> {
					inputChoice = InputChoice.CLIPBOARD;
					autoDetectFormat();
					updateWidgetEnablements();
					updatePageCompletion();
				}
				);
		clipboardRadio.setLayoutData(GridDataFactory.fillDefaults().create());
		inputRadio = toolkit.createButton(fileSelectionArea, "Manual Input",
				SWT.RADIO | SWT.LEFT,
				(e) -> {
					inputChoice = InputChoice.INPUT;
					updateWidgetEnablements();
					updatePageCompletion();
				}
				);
		inputRadio.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());
	}

	protected void autoDetectFormat() {
		ReportType type = null;
		if (inputChoice != InputChoice.FILE) {
			type = ReportType.autoDetectType(getClipboardText(), types);
		} else {
			type = ReportType.autoDetectType(new File(fileName), types);
		}
		if (type != null)
			selectReportType(type);
	}

	@Override
	protected void createOptionsGroupButtons(final Group optionsPanel) {
		// top level group
		Composite buttonComposite = new Composite(optionsPanel, SWT.NONE);
		buttonComposite.setFont(optionsPanel.getFont());
		GridLayout layout = new GridLayout(3, false);
		buttonComposite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		buttonComposite.setLayoutData(gd);
		// create report type
		Label label = new Label(buttonComposite, SWT.NONE);
		label.setText("Import Type:");
		typeCombo = new Combo(buttonComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
		types = ImportExportFactory.getImportTypes();
		for (ReportType reportType : types) {
			addComboType(reportType);
		}
		selectReportType(ImportExportFactory.TEXT_DECK_CLASSIC);
		typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// options to include header
		includeHeader = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
		includeHeader.setText("Data has a header row");
		includeHeader.setSelection(true);
		// deck options
		virtualCards = new Button(buttonComposite, SWT.CHECK);
		virtualCards.setText("Imported cards will be virtual");
		virtualCards.setSelection(false);
		virtualCards.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).create());
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
		if (importIntoExisting.getSelection()) {
			if (element == null) {
				setErrorMessage("Select a deck or collection to import data into or select to import to a new deck.");
				return false;
			}
			Location loc = getSelectedLocation();
			CardElement cont = DataManager.getInstance().getModelRoot().findElement(loc.toString());
			if (cont instanceof CardOrganizer) {
				setErrorMessage("Invalid location selection to import cards into. Select a deck or collection");
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean validateSourceGroup() {
		if (inputChoice == InputChoice.FILE
				&& ((fileName == null) || (fileName.length() == 0) || (editor.getText().length() == 0))) {
			setErrorMessage("Imput file is not selected");
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
		includeHeader.setEnabled(isExportCsvFlag());
		includeHeader.setVisible(isExportCsvFlag());
		editor.setEnabled(inputChoice == InputChoice.FILE);
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

	public void setPreviewResult(ImportResult previewResult2) {
		this.previewResult = previewResult2;
	}

	public ImportResult getPreviewResult() {
		return previewResult;
	}

	public InputChoice getInputChoice() {
		return inputChoice;
	}
}
