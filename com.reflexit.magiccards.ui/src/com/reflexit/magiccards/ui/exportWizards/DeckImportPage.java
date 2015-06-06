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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
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
public class DeckImportPage extends WizardDataTransferPage {
	private static final String IMPUT_FILE_SETTING = "outputFile"; //$NON-NLS-1$
	private static final String REPORT_TYPE_SETTING = "reportType"; //$NON-NLS-1$
	private static final String FROM_CHOICE = "from"; //$NON-NLS-1$
	private static final String INTO_CHOICE = "into"; //$NON-NLS-1$
	protected static final String AUTO_NAME = "<auto-generate-name>";
	private final String ID = getClass().getName();
	private Text editor;
	private String fileName = "";
	private IStructuredSelection initialResourceSelection;
	private ReportType reportType;
	private PreferenceStore store;
	private Combo typeCombo;
	private Button fileRadio;
	private Button clipboardRadio;
	private Button inputRadio;
	private Composite fileSelectionArea;
	private ImportResult previewResult;
	private CardElement element;
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
	private Text importIntoText;

	protected DeckImportPage(final String pageName, final IStructuredSelection selection) {
		super(pageName);
		initialResourceSelection = selection;
		store = new PreferenceStore();
		element = getDefaultElement();
		types = ImportExportFactory.getImportTypes();
	}

	public ImportResult performImport(final boolean preview) {
		final boolean header = true;
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
							if (element instanceof CollectionsContainer || newdeck) {
								createNewDeck(getNewDeckName(), virtual, (CollectionsContainer) element);
							}
							if (!(element instanceof CardCollection)) {
								throw new IllegalArgumentException("Cannot import into " + element);
							}
							Location location = getSelectedLocation();
							if (!previewResult.isOk()) {
								previewResult = ImportUtils.performPreImport(st, worker, header, virtual,
										location, resolve, monitor2);
							}
							Collection<IMagicCard> result = (Collection<IMagicCard>) previewResult.getList();
							if (resolve) {
								ImportUtils.splitToSideboard(location, result);
								monitor.worked(10);
							}
							if (fixErrors(result, dbImport)) {
								monitor.worked(10);
								if (resolve)
									ImportUtils.performImport(result, DataManager.getCardHandler()
											.getLibraryCardStore());
							}
						}
					} catch (InvocationTargetException e) {
						throw e;
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
					if (previewResult.isOk())
						return;
					if (previewResult.getError() != null)
						throw new InvocationTargetException(previewResult.getError());
					else
						throw new InvocationTargetException(new IllegalArgumentException("Cannot import"));
				}
			};
			getRunnableContext().run(true, true, work);
		} catch (InvocationTargetException ite) {
			Throwable e = ite.getCause();
			previewResult.setError(e);
			setErrorMessage(e.getMessage());
			MagicUIActivator.log(e);
		} catch (InterruptedException e) {
			previewResult.setError(e);
			setErrorMessage(e.getMessage());
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

	protected void createNewDeck(final String newDeckName, boolean virtual, CollectionsContainer resource) {
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

	private CollectionsContainer getDefaultElement() {
		return DataManager.getInstance().getModelRoot().getDeckContainer();
	}

	private Location getSelectedLocation() {
		return getElement().getLocation();
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
		group.setText("Import Destination");
		group.setLayout(new GridLayout(3, false));
		SelectionAdapter updateListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePageCompletion();
				updateWidgetEnablements();
			}
		};
		importIntoExisting = new Button(group, SWT.RADIO);
		importIntoExisting.setText("Add cards into");
		importIntoExisting.addSelectionListener(updateListener);
		importIntoText = new Text(group, SWT.BORDER);
		importIntoText.setEditable(false);
		importIntoText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
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
						if (element instanceof CardCollection) {
							virtualCards.setSelection(((CardCollection) element).isVirtual());
						}
						updateImportIntoText();
					}
					importIntoExisting.setSelection(true);
					importIntoDb.setSelection(false);
					updatePageCompletion();
					updateWidgetEnablements();
				}
			}
		});
		// deck options
		virtualCards = new Button(group, SWT.CHECK);
		virtualCards.setText("Imported cards will be virtual");
		virtualCards.setSelection(false);
		virtualCards.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).indent(15, 0).create());
		// db import
		importIntoDb = new Button(group, SWT.RADIO);
		importIntoDb.setText("Extends cards database (do not create new deck or collection)");
		importIntoDb.setLayoutData(GridDataFactory.swtDefaults().span(3, 1).create());
		importIntoDb.setSelection(false);
		importIntoDb.addSelectionListener(updateListener);
	}

	private void updateImportIntoText() {
		importIntoText.setText(element.getLocation().getPath());
		if (element instanceof CardOrganizer) {
			importIntoText.setText(element.getLocation().getPath() + "/" + AUTO_NAME);
		}
	}

	private void setFileName(final String string) {
		fileName = string;
	}

	@Override
	protected boolean allowNewContainerName() {
		return true;
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
			editor.setSelection(file.length(), file.length());
		}
		String sfrom = dialogSettings.get(FROM_CHOICE);
		inputChoice = InputChoice.CLIPBOARD;
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
		int choice = 0;
		try {
			choice = dialogSettings.getInt(INTO_CHOICE);
		} catch (Exception e) {
			// ignore
		}
		if (choice < 2) choice = 2;
		if (importIntoExisting != null) importIntoExisting.setSelection(choice == 2);
		if (importIntoDb != null) importIntoDb.setSelection(choice == 3);
		updateImportIntoText();
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
		if (importIntoExisting != null && importIntoExisting.getSelection())
			choice = 2;
		else if (importIntoDb != null && importIntoDb.getSelection())
			choice = 3;
		return choice;
	}

	protected void createResourcesGroup(final Composite parent) {
		MagicToolkit toolkit = MagicToolkit.getInstance();
		fileSelectionArea = toolkit.createGroup(parent, "Import Source");
		fileSelectionArea.setLayoutData(GridDataFactory.fillDefaults().create());
		fileSelectionArea.setLayout(GridLayoutFactory.swtDefaults().numColumns(5).create());
		// clipboard control
		clipboardRadio = toolkit.createButton(fileSelectionArea, "Clipboard",
				SWT.RADIO | SWT.LEFT,
				(e) -> onInputChoice(InputChoice.CLIPBOARD));
		clipboardRadio.setLayoutData(GridDataFactory.fillDefaults().create());
		// editor controls
		inputRadio = toolkit.createButton(fileSelectionArea, "Editor",
				SWT.RADIO | SWT.LEFT,
				(e) -> onInputChoice(InputChoice.INPUT));
		inputRadio.setLayoutData(GridDataFactory.fillDefaults().create());
		// file selector
		fileRadio = toolkit.createButton(fileSelectionArea, "File", SWT.RADIO,
				(e) -> onInputChoice(InputChoice.FILE));
		fileRadio.setSelection(true);
		editor = toolkit.createText(fileSelectionArea, "", SWT.BORDER);
		editor.addModifyListener(
				(e) -> {
					setFileName(editor.getText());
					onInputChoice(InputChoice.FILE);
				}
				);
		editor.setLayoutData(GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).grab(true, false).create());
		toolkit.createButton(fileSelectionArea, "Browse...", SWT.PUSH,
				(e) -> {
					FileDialog fileDialog = new FileDialog(parent.getShell());
					fileDialog.setFileName(editor.getText());
					String file = fileDialog.open();
					if (file != null) {
						setInputChoice(InputChoice.FILE);
						editor.setText(file);
						editor.setSelection(file.length(), file.length());
					}
				}
				);
	}

	public void onInputChoice(InputChoice choice) {
		inputChoice = choice;
		autoDetectFormat();
		updateWidgetEnablements();
		updatePageCompletion();
	}

	protected void autoDetectFormat() {
		ReportType type = null;
		switch (inputChoice) {
			case FILE:
				type = ReportType.autoDetectType(new File(fileName), types);
				break;
			case CLIPBOARD:
				type = ReportType.autoDetectType(getClipboardText(), types);
				break;
			case INPUT:
				if (previewResult != null) {
					String text = previewResult.getText();
					type = ReportType.autoDetectType(text, types);
				}
				break;
			default:
				break;
		}
		if (type != null)
			selectReportType(type);
	}

	@Override
	protected void createOptionsGroupButtons(final Group optionsPanel) {
		MagicToolkit toolkit = MagicToolkit.getInstance();
		// top level group
		Composite buttonComposite = toolkit.createComposite(optionsPanel);
		buttonComposite.setLayout(GridLayoutFactory.swtDefaults().numColumns(3).create());
		// create report type
		toolkit.createLabel(buttonComposite, "Import Format:");
		typeCombo = new Combo(buttonComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
		for (ReportType reportType : types) {
			typeCombo.add(reportType.getLabel());
		}
		typeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				reportType = ImportExportFactory.getByLabel(typeCombo.getText());
				updateWidgetEnablements();
				updatePageCompletion();
			}
		});
		typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	@Override
	protected String getErrorDialogTitle() {
		return "Error";
	}

	@Override
	protected boolean validateDestinationGroup() {
		if (importIntoExisting.getSelection()) {
			CardElement element = getElement();
			if (element instanceof CardOrganizer) {
				setMessage("Cards will be imported into a deck with auto-generated name");
				return true;
			}
		}
		return true;
	}

	@Override
	protected boolean validateSourceGroup() {
		if (inputChoice == InputChoice.FILE) {
			if (((fileName == null) || (fileName.length() == 0) || (editor.getText().length() == 0))) {
				setErrorMessage("Input file is not selected");
				return false;
			}
			File file = new File(fileName);
			if (!file.exists()) {
				setErrorMessage("File does not exists");
				return false;
			}
			if (!file.isFile()) {
				setErrorMessage("Not a file");
				return false;
			}
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
		editor.setEnabled(inputChoice == InputChoice.FILE);
		virtualCards.setEnabled(!importIntoDb.getSelection());
	}

	public ReportType getReportType() {
		return reportType;
	}

	/**
	 * @param element
	 *            the element to set
	 */
	public void setElement(CardElement element) {
		if (element == null)
			element = getDefaultElement();
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
