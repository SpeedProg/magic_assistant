package com.reflexit.magiccards.ui.exportWizards;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.exports.IImportDelegate;
import com.reflexit.magiccards.core.exports.ImportData;
import com.reflexit.magiccards.core.exports.ImportExportFactory;
import com.reflexit.magiccards.core.exports.ImportSource;
import com.reflexit.magiccards.core.exports.ImportUtils;
import com.reflexit.magiccards.core.exports.ReportType;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.sync.WebUtils;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.CorrectSetDialog;
import com.reflexit.magiccards.ui.dialogs.EditTextDialog;
import com.reflexit.magiccards.ui.dialogs.LocationPickerDialog;
import com.reflexit.magiccards.ui.dnd.CopySupport;
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
	private static final String AUTO_NAME = "<auto-generated-name>";
	private final String ID = getClass().getName();
	// ui elements
	private Button fileRadio;
	private Text fileText;
	private Button clipboardRadio;
	private Button intoDeck;
	private Button intoCollection;
	private Button intoExisting;
	private Combo typeCombo;
	private Button virtualCards;
	private Text deckText;
	private MagicToolkit toolkit;
	// data elements
	private PreferenceStore store;
	private ImportSource inputChoice;
	private CardElement element;
	private Collection<ReportType> types;
	private ImportData importData;
	private String fileName;
	private ReportType reportType;
	private Button urlRadio;
	private Text urlText;
	private String urlName;
	private Text clipboardPreviewText;

	protected DeckImportPage(final String pageName, final IStructuredSelection selection) {
		super(pageName);
		store = new PreferenceStore();
		types = ImportExportFactory.getImportTypes();
		if (selection != null && selection.getFirstElement() instanceof CardElement) {
			element = (CardElement) selection.getFirstElement();
			if (!(element instanceof CardOrganizer))
				element = element.getParent();
		} else {
			element = getDeckContainer();
		}
		importData = new ImportData();
	}

	public void performImport(final boolean preview) {
		importData.setVirtual(virtualCards.getSelection());
		int choice = getIntoChoice();
		final boolean dbImport = choice == 3;
		try {
			IRunnableWithProgress work = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					importRunnable(preview, dbImport, monitor);
				}
			};
			getRunnableContext().run(true, true, work);
		} catch (InvocationTargetException ite) {
			Throwable e = ite.getCause();
			importData.setError(e);
			if (e instanceof RuntimeException && !(e instanceof MagicException))
				MagicUIActivator.log(e);
		} catch (InterruptedException e) {
			importData.setError(e);
		}
	}

	private boolean fixErrors(final Collection<IMagicCard> result, final boolean dbImport) {
		final boolean yesres[] = new boolean[] { true };
		getControl().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				int size = result.size();
				Map<String, String> badSets = ImportUtils.getSetCandidates(result);
				if (badSets.size() > 0) {
					boolean yes = MessageDialog.openQuestion(getShell(), "Import Error",
							"Cannot resolve " + badSets.size()
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
				ImportUtils.performPreImportWithDb(result, newdbrecords,
						reportType.getImportDelegate().getResult().getFields());
				ArrayList<String> lerrors = new ArrayList<String>();
				ImportUtils.validateDbRecords(newdbrecords, lerrors);
				if (newdbrecords.size() > 0 && lerrors.size() == 0) {
					boolean yes2 = dbImport;
					if (yes2 == false) {
						yes2 = MessageDialog.openQuestion(getShell(), "Import into DB", newdbrecords.size()
								+ " cards are not found in the database. Do you want to add new cards into database?");
					}
					if (yes2)
						ImportUtils.importIntoDb(newdbrecords);
				} else if (lerrors.size() > 0 && dbImport) {
					String message = newdbrecords.size() + " cards are not found in the database " + lerrors.size()
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
					String message = "After all this effort I cannot resolve " + cerrors.size() + " cards of " + size
							+ ":\n";
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

	protected void createNewDeck(final String base, boolean virtual, CollectionsContainer resource) {
		int attempts = 1000;
		Location newloc = Location.createLocation(base);
		while (resource.contains(newloc) && attempts-- > 0) {
			newloc = Location.createLocation(base + new Random().nextInt(1000));
		}
		if (attempts <= 0)
			throw new IllegalArgumentException("Cannot generate deck name");
		this.element = resource.addDeck(newloc.getBaseFileName(), virtual);
	}

	protected String getNewDeckName() {
		if (inputChoice != ImportSource.FILE) {
			return "imported";
		} else {
			String basename = new File(fileName).getName();
			int k = basename.lastIndexOf('.');
			if (k >= 0)
				basename = basename.substring(0, k);
			return basename;
		}
	}

	String readSource() throws IOException {
		String text = "";
		importData.setImportSource(inputChoice);
		switch (inputChoice) {
		case FILE:
			if (fileName != null) {
				text = FileUtils.readFileAsString(new File(fileName));
				importData.setText(text);
				importData.setProperty(inputChoice.name(), fileName);
			}
			break;
		case TEXT:
			text = getClipboardText();
			importData.setText(text);
			importData.setProperty(inputChoice.name(), text);
			break;
		case URL:
			if (urlName != null) {
				text = WebUtils.openUrlText(new URL(urlName));
				importData.setText(text);
				importData.setProperty(inputChoice.name(), urlName);
			}
			break;
		default:
			break;
		}
		return text;
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

	private CollectionsContainer getDeckContainer() {
		return DataManager.getInstance().getModelRoot().getDeckContainer();
	}

	private CollectionsContainer getCollectionContainer() {
		return DataManager.getInstance().getModelRoot().getCollectionsContainer();
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

	protected Group createDestinationGroup(final Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayoutData(GridDataFactory.fillDefaults().create());
		group.setText("Import Destination");
		group.setLayout(new GridLayout(2, false));
		GridDataFactory spanAll = GridDataFactory.fillDefaults().grab(true, false).span(2, 1);
		Composite buttons = new Composite(group, SWT.NONE);
		buttons.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		buttons.setLayoutData(spanAll.create());
		intoDeck = new Button(buttons, SWT.RADIO);
		intoDeck.setText("New Deck");
		intoDeck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (intoDeck.getSelection())
					deckText.setText(getDeckContainer().getLocation().getPath() + "/" + AUTO_NAME);
			}
		});
		intoCollection = new Button(buttons, SWT.RADIO);
		intoCollection.setText("New Collection");
		intoCollection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (intoCollection.getSelection())
					deckText.setText(getCollectionContainer().getLocation().getPath() + "/" + AUTO_NAME);
			}
		});
		intoExisting = new Button(group, SWT.RADIO);
		intoExisting.setText("Existing Deck/Collection");
		intoExisting.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (intoExisting.getSelection())
					openImportIntoElementSelectionDialog();
			}
		});
		// toolkit.createLabel(group, "Add cards into");
		deckText = new Text(group, SWT.BORDER);
		deckText.setEditable(false);
		deckText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		deckText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(final MouseEvent e) {
				openImportIntoElementSelectionDialog();
			}
		});
		// Button browse = new Button(group, SWT.PUSH);
		// browse.setText("Browse...");
		// browse.addSelectionListener(new SelectionAdapter() {
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// openImportIntoElementSelectionDialog();
		// }
		// });
		// deck options
		virtualCards = new Button(group, SWT.CHECK);
		virtualCards.setText("Imported cards will be virtual");
		virtualCards.setSelection(false);
		virtualCards.setLayoutData(spanAll.create());
		// db import
		Hyperlink hyperlink = toolkit.createHyperlink(group,
				"Extends cards database (do not create new deck or collection)", SWT.NONE);
		hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				getWizard().performCancel();
				getContainer().getShell().close();
				openWizard(new SetImportWizard(), new StructuredSelection(getElement()));
			}
		});
		hyperlink.setLayoutData(spanAll.create());
		return group;
	}

	private int openWizard(IWorkbenchWizard wizard, final IStructuredSelection selection) {
		wizard.init(PlatformUI.getWorkbench(), selection);
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		dialog.create();
		dialog.getShell().setText(wizard.getWindowTitle());
		return dialog.open();
	}

	private void resetInto() {
		intoDeck.setSelection(false);
		intoCollection.setSelection(false);
		intoExisting.setSelection(false);
	}

	private void updateImportIntoText() {
		resetInto();
		deckText.setText(element.getLocation().getPath());
		if (element instanceof CardOrganizer) {
			deckText.setText(element.getLocation().getPath() + "/" + AUTO_NAME);
			CollectionsContainer deckCon = getDeckContainer();
			if (deckCon == element || element.isAncestor(deckCon)) {
				intoDeck.setSelection(true);
			} else {
				intoCollection.setSelection(true);
			}
		} else {
			intoExisting.setSelection(true);
		}
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
		toolkit = MagicToolkit.getInstance();
		setTitle("Import into a Deck or Collection");
		initializeDialogUnits(parent);
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());
		setControl(composite);
		createResourcesGroup(composite);
		createOptionsGroup(composite);
		createDestinationGroup(composite);
		restoreWidgetValues();
		updateWidgetEnablements();
		defaultPrompt();
		setPageComplete(determinePageCompletion());
		// setErrorMessage(null); // should not initially have error message
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, MagicUIActivator.PLUGIN_ID + ".export");
	}

	private void defaultPrompt() {
		if (reportType == null)
			reportType = ImportExportFactory.TEXT_DECK_CLASSIC;
		String mess = "You have selected '" + reportType.getLabel() + "' format.\n";
		if (importData.getError() != null) {
			mess += "Warning: cannot parse data (" + importData.getError().getMessage() + "). ";
		} else {
			int errcount = importData.getErrorCount();
			mess += "Found " + importData.size() + " record(s) and " + errcount + " error(s).";
		}
		mess += " Press Next to preview.\n";
		if (reportType == ImportExportFactory.XML)
			setMessage(mess);
		else if (reportType == ImportExportFactory.CSV)
			setMessage(mess + "Columns: ID,NAME,COST,TYPE,P,T,TEXT,SET,RARITY,DBPRICE,LANG,COUNT,PRICE,COMMENT");
		else if (reportType == ImportExportFactory.TEXT_DECK_CLASSIC)
			setMessage(mess + "Lines like 'Quagmire Druid x 3' or 'Diabolic Tutor (Tenth Edition) x4'");
		else if (reportType == ImportExportFactory.TABLE_PIPED)
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
			final String string = file;
			fileName = string;
			fileText.setText(file);
			fileText.setSelection(file.length(), file.length());
		}
		String sfrom = dialogSettings.get(FROM_CHOICE);
		inputChoice = ImportSource.TEXT;
		if (sfrom != null) {
			try {
				inputChoice = ImportSource.valueOf(sfrom);
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
		updateImportIntoText();
		updateWidgetEnablements();
	}

	public void setInputChoice(ImportSource inputChoice) {
		this.inputChoice = inputChoice;
		fileRadio.setSelection(inputChoice == ImportSource.FILE);
		clipboardRadio.setSelection(inputChoice == ImportSource.TEXT);
		// inputRadio.setSelection(inputChoice == ImportSource.INPUT);
		urlRadio.setSelection(inputChoice == ImportSource.URL);
	}

	private void selectReportType(final ReportType type) {
		if (type == null)
			return;
		reportType = type;
		typeCombo.setText(type.getLabel());
		typeCombo.setToolTipText(type.getExample());
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
			// save into file
			MagicUIActivator.getDefault().saveDialogSetting(dialogSettings);
		} catch (IOException e) {
			MagicUIActivator.log(e);
		}
	}

	public int getIntoChoice() {
		return 2;
	}

	protected void createResourcesGroup(final Composite parent) {
		Composite fileSelectionArea = toolkit.createGroup(parent, "Import Source");
		fileSelectionArea.setLayoutData(GridDataFactory.fillDefaults().create());
		fileSelectionArea.setLayout(GridLayoutFactory.swtDefaults().numColumns(3).create());
		// clipboard control
		clipboardRadio = toolkit.createButton(fileSelectionArea, "Clipboard", SWT.RADIO,
				(e) -> onInputChoice(e, ImportSource.TEXT));
		clipboardRadio.setLayoutData(GridDataFactory.fillDefaults().create());
		clipboardPreviewText = toolkit.createText(fileSelectionArea, "", SWT.BORDER);
		clipboardPreviewText.setEditable(false);
		clipboardPreviewText.setText(getClipboardClipped());
		clipboardPreviewText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				openEditDialog();
			}
		});
		GridDataFactory textBoxFc = GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).grab(true, false);
		GridDataFactory buttFc = GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).grab(true, false);
		clipboardPreviewText.setLayoutData(textBoxFc.create());
		Button edit = toolkit.createButton(fileSelectionArea, "Edit...", SWT.PUSH, (e) -> {
			openEditDialog();
		});
		edit.setLayoutData(buttFc.create());
		// file selector
		fileRadio = toolkit.createButton(fileSelectionArea, "File", SWT.RADIO,
				(e) -> onInputChoice(e, ImportSource.FILE));
		fileRadio.setSelection(true);
		fileText = toolkit.createText(fileSelectionArea, "", SWT.BORDER);
		fileText.addModifyListener((e) -> {
			fileName = fileText.getText();
			if (inputChoice != ImportSource.FILE) {
				setInputChoice(ImportSource.FILE);
			}
			onInputChoice(null, ImportSource.FILE);
		});
		fileText.setLayoutData(textBoxFc.create());
		Button browse = toolkit.createButton(fileSelectionArea, "Browse...", SWT.PUSH, (e) -> {
			FileDialog fileDialog = new FileDialog(parent.getShell());
			fileDialog.setFileName(fileText.getText());
			String file = fileDialog.open();
			if (file != null) {
				setInputChoice(ImportSource.FILE);
				fileText.setText(file);
				fileText.setSelection(file.length(), file.length());
			}
		});
		browse.setLayoutData(buttFc.create());
		// editor controls
		// inputRadio = toolkit.createButton(fileSelectionArea, "Editor",
		// SWT.RADIO,
		// (e) -> onInputChoice(e, ImportSource.INPUT));
		// inputRadio.setToolTipText("Text editor will appear on the next page
		// where you enter the text (or paste");
		// inputRadio.setLayoutData(GridDataFactory.fillDefaults().create());
		// url selector
		urlRadio = toolkit.createButton(fileSelectionArea, "URL", SWT.RADIO, (e) -> onInputChoice(e, ImportSource.URL));
		urlText = toolkit.createText(fileSelectionArea, "", SWT.BORDER);
		urlText.setToolTipText(
				"You can select an url by copying it from browser, but only if there is a parser that understans its format it can be parsed");
		urlText.addModifyListener((e) -> {
			urlName = urlText.getText();
			if (inputChoice != ImportSource.URL) {
				setInputChoice(ImportSource.URL);
			}
			onInputChoice(null, ImportSource.URL);
		});
		urlText.setLayoutData(textBoxFc.create());
	}

	private void openEditDialog() {
		EditTextDialog dialog = new EditTextDialog(new SameShellProvider(getShell()));
		dialog.setContents(getClipboardText());
		if (dialog.open() == Window.OK) {
			String text = dialog.getText();
			CopySupport.runCopy(text);
			clipboardPreviewText.setText(getClipboardClipped());
			setInputChoice(ImportSource.TEXT);
			onInputChoice(null, inputChoice);
		}
	}

	private String getClipboardClipped() {
		String cl = getClipboardText();
		String clipped = cl.length() > 80 ? cl.subSequence(0, 80) + "..." : cl;
		return clipped;
	}

	public void onInputChoice(SelectionEvent event, ImportSource choice) {
		if (event == null || ((Button) event.widget).getSelection()) {
			inputChoice = choice;
			autoDetectFormat();
			updateWidgetEnablements();
			updatePageCompletion();
		}
	}

	protected void autoDetectFormat() {
		ReportType type = null;
		switch (inputChoice) {
		case FILE:
			if (fileName != null)
				type = ReportType.autoDetectType(new File(fileName), types);
			break;
		case TEXT:
			type = ReportType.autoDetectType(getClipboardText(), types);
			break;
		case URL:
			try {
				if (urlName != null)
					type = ReportType.autoDetectType(new URL(urlName), types);
			} catch (MalformedURLException e) {
				return;
			}
			break;
		default:
			break;
		}
		selectReportType(type);
		performImport(true);
	}

	@Override
	protected void createOptionsGroupButtons(final Group optionsPanel) {
		// top level group
		Composite buttonComposite = new Composite(optionsPanel, SWT.NONE);
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
				typeCombo.setToolTipText(reportType.getExample());
				performImport(true);
				updateWidgetEnablements();
				updatePageCompletion();
			}
		});
		typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		toolkit.createHyperlink(buttonComposite, "Example...", SWT.NONE).addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				InputDialog inputDialog = new InputDialog(getShell(), "Example", reportType.getLabel(),
						reportType.getExample(), null) {
					@Override
					protected int getShellStyle() {
						return super.getShellStyle() | SWT.RESIZE;
					}

					@Override
					protected int getInputTextStyle() {
						return SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.WRAP;
					}

					@Override
					protected Control createDialogArea(Composite parent) {
						Control x = super.createDialogArea(parent);
						getText().setLayoutData(GridDataFactory.fillDefaults().hint(600, 400).create());
						return x;
					}
				};
				inputDialog.setBlockOnOpen(false);
				inputDialog.open();
			}
		});
	}

	@Override
	protected String getErrorDialogTitle() {
		return "Error";
	}

	@Override
	protected boolean validateDestinationGroup() {
		if (!importData.isOk() && importData.getError() != null) {
			setErrorMessage(importData.getError().getMessage());
			return false;
		}
		return true;
	}

	@Override
	public boolean canFlipToNextPage() {
		return getNextPage() != null;
	}

	@Override
	protected boolean validateOptionsGroup() {
		if (reportType == null) {
			setErrorMessage("Reporter is not defined");
			return false;
		}
		IImportDelegate worker = reportType.getImportDelegate();
		if (worker == null) {
			setErrorMessage("Importer is not defined for " + reportType.getLabel());
			return false;
		}
		return true;
	}

	@Override
	protected boolean validateSourceGroup() {
		if (inputChoice == ImportSource.FILE) {
			if (((fileName == null) || (fileName.length() == 0) || (fileText.getText().length() == 0))) {
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
			return true;
		}
		if (inputChoice == ImportSource.URL) {
			if (((urlName == null) || (urlName.length() == 0) || (urlText.getText().length() == 0))) {
				setErrorMessage("URL is selected but empty");
				return false;
			}
			try {
				URL url = new URL(urlName);
			} catch (MalformedURLException e) {
				setErrorMessage("Malformed URL: " + e.getMessage());
				return false;
			}
		}
		return true;
	}

	@Override
	protected void updatePageCompletion() {
		super.updatePageCompletion();
		if (isPageComplete() || getErrorMessage() == null) {
			defaultPrompt(); // set default prompt, otherwise it empty ugly
		}
	}

	@Override
	protected void updateWidgetEnablements() {
		fileText.setEnabled(inputChoice == ImportSource.FILE);
		urlText.setEnabled(inputChoice == ImportSource.URL);
	}

	public ReportType getReportType() {
		return reportType;
	}

	public CardElement getElement() {
		return element;
	}

	public ImportData getImportData() {
		return importData;
	}

	public ImportSource getInputChoice() {
		return inputChoice;
	}

	public void importRunnable(final boolean preview, final boolean dbImport, IProgressMonitor monitor)
			throws InvocationTargetException {
		monitor.beginTask("Importing", 100);
		try {
			readSource();
			monitor.worked(10);
			IImportDelegate worker = reportType.getImportDelegate();
			if (worker == null)
				throw new IllegalArgumentException("Importer is not defined for " + reportType.getLabel());
			boolean resolve = !dbImport;
			if (preview) {
				monitor.worked(10);
				// if error occurs importResult.error would be set
				// to exception
				ImportUtils.performPreImport(worker, importData, CoreMonitorAdapter.submon(monitor, 50));
				if (!dbImport) {
					ImportUtils.resolve(importData.getList());
				} else {
					ImportUtils.performPreImportWithDb((Collection<IMagicCard>) importData.getList(), new ArrayList<>(),
							importData.getFields());
				}
				monitor.worked(10);
			} else {
				if (!importData.isOk()) {
					ImportUtils.performPreImport(worker, importData, CoreMonitorAdapter.submon(monitor, 50));
				}
				if (importData.isOk()) {
					Collection<IMagicCard> result = (Collection<IMagicCard>) importData.getList();
					if (resolve) {
						ImportUtils.resolve(importData.getList());
						if (element instanceof CollectionsContainer) {
							createNewDeck(getNewDeckName(), importData.isVirtual(), (CollectionsContainer) element);
						}
						if (!(element instanceof CardCollection)) {
							throw new IllegalArgumentException("Cannot import into " + element);
						}
						Location location = getSelectedLocation();
						importData.setLocation(location);
						ImportUtils.updateLocation(result, location);
						monitor.worked(10);
					}
					if (fixErrors(result, dbImport)) {
						monitor.worked(10);
						if (resolve)
							ImportUtils.performImport(result, DataManager.getCardHandler().getLibraryCardStore());
					}
				}
			}
		} catch (InvocationTargetException e) {
			throw e;
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
		if (importData.isOk())
			return;
		if (importData.getError() != null)
			throw new InvocationTargetException(importData.getError());
		else
			throw new InvocationTargetException(new IllegalArgumentException("Cannot import"));
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible)
			updatePageCompletion();
		super.setVisible(visible);
	}

	protected void openImportIntoElementSelectionDialog() {
		LocationPickerDialog dialog = new LocationPickerDialog(getShell(), SWT.SINGLE) {
			@Override
			protected Control createDialogArea(Composite parent) {
				Control x = super.createDialogArea(parent);
				setMessage(
						"Select a deck or collection to add cards into,\nor select card folder to have auto-generated deck name");
				return x;
			}
		};
		dialog.setSelection(new StructuredSelection(getElement()));
		if (dialog.open() == Window.OK) {
			if (dialog.getSelection() != null && !dialog.getSelection().isEmpty()) {
				element = (CardElement) dialog.getSelection().getFirstElement();
				if (element instanceof CardCollection) {
					virtualCards.setSelection(((CardCollection) element).isVirtual());
				}
			}
		}
		updateImportIntoText();
		updatePageCompletion();
		updateWidgetEnablements();
	}
}
