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
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.exports.IImportDelegate;
import com.reflexit.magiccards.core.exports.ImportExportFactory;
import com.reflexit.magiccards.core.exports.ImportResult;
import com.reflexit.magiccards.core.exports.ImportUtils;
import com.reflexit.magiccards.core.exports.ReportType;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
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
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;

/**
 * First and only page of Deck Export Wizard
 */
public class DeckImportPage extends WizardDataTransferPage implements Listener {
	private static final String IMPUT_FILE_SETTING = "outputFile"; //$NON-NLS-1$
	private static final String REPORT_TYPE_SETTING = "reportType"; //$NON-NLS-1$
	private static final String IMPORT_HEADER_SETTING = "headerRow"; //$NON-NLS-1$
	private static final String IMPORT_CLIPBOARD = "clipboard"; //$NON-NLS-1$
	Text editor;
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
	private ImportResult previewResult;
	private CardElement element;
	private Button createNewDeck;
	private Button importIntoExisting;
	private Button importIntoDb;
	private Collection<ReportType> types;

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
			final boolean newdeck = createNewDeck.getSelection();
			final boolean dbImport = importIntoDb.getSelection();
			try {
				IRunnableWithProgress work = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						IImportDelegate<IMagicCard> worker;
						try {
							worker = reportType.getImportDelegate();
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}
						if (worker == null)
							throw new IllegalArgumentException("Import is not defined for " + reportType.getLabel());
						worker.setResolveDb(!dbImport);
						if (preview) {
							// if error occurs importResult.error would be set
							// to exception
							previewResult = ImportUtils.performPreview(st, worker, header, Location.createLocation("preview"),
									new CoreMonitorAdapter(monitor));
							((DeckImportWizard) getWizard()).setData(previewResult);
						} else {
							Location selectedLocation = getSelectedLocation();
							if (newdeck) {
								// create a new deck
								createNewDeck(getNewDeckName());
								selectedLocation = getSelectedLocation();
							}
							Collection<IMagicCard> result;
							if (previewResult == null) {
								ImportResult result1 = ImportUtils.performPreImport(st, worker, header, selectedLocation,
										new CoreMonitorAdapter(monitor));
								result = (Collection<IMagicCard>) result1.getList();
							} else {
								result = (List<IMagicCard>) previewResult.getList();
								if (!dbImport) {
									Location sideboard = selectedLocation.toSideboard();
									for (Iterator iterator = result.iterator(); iterator.hasNext();) {
										IMagicCard iMagicCard = (IMagicCard) iterator.next();
										if (iMagicCard instanceof MagicCardPhysical) {
											MagicCardPhysical mcp = (MagicCardPhysical) iMagicCard;
											if (!mcp.isSideboard())
												mcp.setLocation(selectedLocation);
											else
												mcp.setLocation(sideboard);
										}
									}
								}
							}
							if (fixErrors(result, dbImport)) {
								if (!dbImport)
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
		} catch (InvocationTargetException e) {
			displayErrorDialog(e.getCause());
		} catch (Exception e) {
			displayErrorDialog(e);
		}
		return res;
	}

	private boolean fixErrors(final Collection<IMagicCard> result, final boolean dbImport) {
		final boolean yesres[] = new boolean[] { true };
		getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
				int size = result.size();
				Map<String, String> badSets = ImportUtils.getSetCandidates(result);
				if (badSets.size() > 0) {
					boolean yes = MessageDialog.openQuestion(getShell(), "Import Error", "Cannot resolve " + badSets.size()
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
				ImportUtils.performPreImportWithDb(result, newdbrecords, reportType.getImportDelegate().getPreview().getFields());
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
					String message = "After all this effort I cannot resolve " + cerrors.size() + " cards of " + size + ":\n";
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
			Object clipboardText = cb.getContents(TextTransfer.getInstance());
			if (clipboardText == null)
				clipboardText = "";
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
		importIntoDb = new Button(group, SWT.RADIO);
		importIntoDb.setText("Extends cards database (do not create new deck or collection)");
		importIntoDb.setLayoutData(GridDataFactory.swtDefaults().span(3, 1).create());
		importIntoDb.setSelection(false);
		importIntoDb.addSelectionListener(updateListener);
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
		String mess = "You have selected '" + reportType.getLabel() + "' format.\n";
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
			editor.setText(file);
		}
		clipboard = dialogSettings.getBoolean(IMPORT_CLIPBOARD);
		fileRadio.setSelection(!clipboard);
		clipboardRadio.setSelection(clipboard);
		// restore options
		String stype = dialogSettings.get(REPORT_TYPE_SETTING);
		ReportType type = ReportType.getByLabel(stype);
		if (type != null && type.getImportDelegate() != null) {
			selectReportType(type);
		} else
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
			dialogSettings.put(REPORT_TYPE_SETTING, reportType.getLabel());
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
		GridLayout fileSelectionLayout = new GridLayout(3, false);
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
		editor = new Text(fileSelectionArea, SWT.BORDER);
		editor.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				File file = new File(editor.getText());
				setFileName(file.getPath());
				updateTypeSelection();
				updatePageCompletion();
			}
		});
		editor.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final Button browse = new Button(fileSelectionArea, SWT.PUSH);
		browse.setText("Browse...");
		browse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				super.widgetSelected(e);
				FileDialog fileDialog = new FileDialog(browse.getShell());
				fileDialog.setFileName(editor.getText());
				String file = fileDialog.open();
				if (file != null) {
					editor.setText(file);
					setFileName(file);
					clipboard = false;
					fileRadio.setSelection(!clipboard);
					clipboardRadio.setSelection(clipboard);
					updateWidgetEnablements();
					updateTypeSelection();
					updatePageCompletion();
				}
			}
		});
		setFileName("");
		// clipboard control
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
		bgd.horizontalSpan = 3;
		clipboardRadio.setLayoutData(bgd);
		// fileSelectionArea.moveAbove(null);
	}

	protected void updateTypeSelection() {
		if (fileName == null || fileName.trim().length() == 0)
			return;
		int k = fileName.lastIndexOf('.');
		String ext = "";
		if (k > 0 && k < fileName.length() - 1) {
			ext = fileName.substring(k + 1, fileName.length());
		}
		for (ReportType reportType : types) {
			if (ext.equalsIgnoreCase(reportType.getExtension())) {
				selectReportType(reportType);
				break;
			}
		}
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
		selectReportType(ReportType.TEXT_DECK_CLASSIC);
		typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
		if (importIntoExisting.getSelection()) {
			if (element == null) {
				setErrorMessage("Select a deck or collection to import data into or select to import to a new deck.");
				return false;
			}
			Location loc = getSelectedLocation();
			CardElement cont = DataManager.getModelRoot().findElement(loc.toString());
			if (cont instanceof CardOrganizer) {
				setErrorMessage("Invalid location selection to import cards into. Select a deck or collection");
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean validateSourceGroup() {
		if (clipboard == false && ((fileName == null) || (fileName.length() == 0) || (editor.getText().length() == 0))) {
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
		editor.setEnabled(!clipboard);
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