package com.reflexit.magiccards.ui.views.analyzers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.exports.AbstractExportDelegate;
import com.reflexit.magiccards.core.exports.IExportDelegate;
import com.reflexit.magiccards.core.exports.ImportExportFactory;
import com.reflexit.magiccards.core.exports.ReportType;
import com.reflexit.magiccards.core.exports.WizardsHtmlExportDelegate;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dnd.CopySupport;
import com.reflexit.magiccards.ui.preferences.DeckViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.utils.StoredSelectionProvider;
import com.reflexit.magiccards.ui.views.IMagicControl;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public class ExportDeckPage extends AbstractDeckPage implements IMagicControl {
	private Browser textBrowser;
	private ISelectionProvider selProvider = new StoredSelectionProvider();
	private IFilteredCardStore fstore;
	private IAction save;
	private String textResult;
	protected ReportType reportType;
	private Text textArea;
	private ComboContributionItem typeSelector;
	StatusLineContributionItem a;
	private ImageAction sideboard;
	private ImageAction header;
	private boolean includeSideboard = true;
	private boolean includeHeader = true;
	private Action actionShowPrefs;

	class ComboContributionItem extends ControlContribution {
		private Combo control;

		@Override
		protected int computeWidth(Control control) {
			return 200;
		}

		protected ComboContributionItem(String id) {
			super(id);
		}

		@Override
		protected Control createControl(Composite parent) {
			control = new Combo(parent, SWT.READ_ONLY);
			control.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setReportType(ReportType.createReportType(control.getText()));
				}
			});
			Collection<ReportType> types = new ImportExportFactory<IMagicCard>().getExportTypes();
			for (final ReportType rt : types) {
				control.add(rt.getLabel());
			}
			control.setText(reportType.getLabel());
			return control;
		}

		public Combo getControl() {
			return control;
		}
	}

	class ImageAction extends Action {
		public ImageAction(String name, String iconKey, int style) {
			super(name, style);
			setImageDescriptor(MagicUIActivator.getImageDescriptor(iconKey));
			setToolTipText(name);
		}
	}

	@Override
	public Composite createContents(Composite parent) {
		super.createContents(parent);
		makeActions();
		Composite area = getArea();
		stackLayout = new StackLayout();
		area.setLayout(stackLayout);
		this.textBrowser = new Browser(area, SWT.WRAP | SWT.INHERIT_DEFAULT);
		this.textBrowser.setFont(area.getFont());
		textBrowser.addLocationListener(new LocationAdapter() {
			@Override
			public void changing(LocationEvent event) {
				String location = event.location;
				if (location.startsWith(WizardsHtmlExportDelegate.CARD_URI)) {
					location = location.substring(WizardsHtmlExportDelegate.CARD_URI.length());
					if (location.endsWith("/")) {
						location = location.substring(0, location.length() - 1);
					}
					String params[] = location.split("&");
					for (int i = 0; i < params.length; i++) {
						String string = params[i];
						if (string.startsWith(WizardsHtmlExportDelegate.CARDID)) {
							event.doit = false;
							String value = string.substring(WizardsHtmlExportDelegate.CARDID.length());
							int cardId = Integer.valueOf(value).intValue();
							IDbCardStore magicDBStore = DataManager.getCardHandler().getMagicDBStore();
							IMagicCard card = (IMagicCard) magicDBStore.getCard(cardId);
							if (card != null)
								selProvider.setSelection(new StructuredSelection(card));
						}
					}
					event.doit = false;
				}
			}
		});
		textArea = new Text(area, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		initReportTypes();
		setTopControl();
		return getArea();
	}

	protected void initReportTypes() {
		Collection<ReportType> types = new ImportExportFactory<IMagicCard>().getExportTypes();
		for (final ReportType rt : types) {
			if (rt.getLabel().contains("HTML")) {
				reportType = rt; // last
			}
		}
		setReportType(reportType);
	}

	protected void setTopControl() {
		if (reportType.getLabel().contains("HTML")) {
			stackLayout.topControl = textBrowser;
		} else {
			stackLayout.topControl = textArea;
		}
	}

	@Override
	public void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.sideboard);
		// manager.add(this.header);
		manager.add(this.actionShowPrefs);
		manager.add(this.typeSelector);
		manager.add(this.save);
		super.fillLocalToolBar(manager);
	}

	protected void makeActions() {
		this.save = new ImageAction("Save As...", "icons/clcl16/save.png", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				saveAs();
			}
		};
		this.typeSelector = new ComboContributionItem("xxx");
		this.sideboard = new ImageAction("Include Sideboard", "icons/obj16/sideboard16.png", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				triggerSideboard(!isInludeSideboard());
			}
		};
		this.sideboard.setChecked(isInludeSideboard());
		this.header = new ImageAction("Include Header", "icons/obj16/header16.png", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				triggerHeader(!isInludeHeader());
			}
		};
		this.header.setChecked(isInludeHeader());
		this.actionShowPrefs = new ImageAction("Preferences...", "icons/clcl16/table.gif", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				String id = DeckViewPreferencePage.class.getName();
				if (id != null) {
					PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getArea().getShell(), id, new String[] { id }, null);
					dialog.open();
					activate();
				}
			}
		};
	}

	protected boolean isInludeSideboard() {
		return includeSideboard;
	}

	public void triggerSideboard(boolean mode) {
		includeSideboard = mode;
		sideboard.setChecked(mode);
		if (!mode)
			sideboard.setToolTipText("Include sideboard");
		else
			sideboard.setToolTipText("Do not include sideboard");
		activate();
	}

	protected boolean isInludeHeader() {
		return includeHeader;
	}

	public void triggerHeader(boolean mode) {
		includeHeader = mode;
		header.setChecked(mode);
		activate();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return selProvider;
	}

	@Override
	public void activate() {
		textResult = null;
		setTopControl();
		super.activate();
		try {
			textResult = getText();
			this.textArea.setText(textResult);
			// System.err.println(textResult);
			this.textBrowser.setText(textResult);
		} catch (InvocationTargetException e) {
			this.textBrowser.setText("Error: " + e.getCause());
		} catch (InterruptedException e) {
			this.textBrowser.setText("Cancelled");
		}
		getArea().layout();
	}

	@Override
	public void setFilteredStore(IFilteredCardStore fstore) {
		super.setFilteredStore(fstore);
		setFStore();
	}

	public void setFStore() {
		if (getCardStore() == null)
			return;
		if (includeSideboard) {
			Location loc = store.getLocation();
			ICardStore mainStore = DataManager.getCardStore(loc.toMainDeck());
			ICardStore sideStore = DataManager.getCardStore(loc.toSideboard());
			MemoryFilteredCardStore<IMagicCard> mstore = new MemoryFilteredCardStore<IMagicCard>();
			mstore.addAll(mainStore);
			if (sideStore != null)
				mstore.addAll(sideStore);
			mstore.setLocation(loc.toMainDeck());
			mstore.update(view.getFilter());
			this.fstore = mstore;
		} else {
			Location loc = store.getLocation();
			ICardStore mainStore = DataManager.getCardStore(loc);
			MemoryFilteredCardStore<IMagicCard> mstore = new MemoryFilteredCardStore<IMagicCard>();
			mstore.addAll(mainStore);
			mstore.setLocation(loc);
			mstore.update(view.getFilter());
			this.fstore = mstore;
		}
	}

	private String getText() throws InvocationTargetException, InterruptedException {
		setFStore();
		if (fstore == null)
			return "";
		ByteArrayOutputStream byteSt = new ByteArrayOutputStream();
		IExportDelegate ex = reportType.getExportDelegate();
		String selcolumns = view.getLocalPreferenceStore().getString(PreferenceConstants.LOCAL_COLUMNS);
		MagicColumnCollection magicColumnCollection = new MagicColumnCollection(null);
		magicColumnCollection.createColumnLabelProviders();
		magicColumnCollection.updateColumnsFromPropery(selcolumns);
		if (includeSideboard) {
			magicColumnCollection.getColumn(MagicCardFieldPhysical.SIDEBOARD).setVisible(true);
		}
		ICardField[] columns = magicColumnCollection.getColumnFields();
		ex.setColumns(columns);
		ex.init(byteSt, includeHeader, fstore);
		ex.run(null);
		return byteSt.toString();
	}

	/**
	 * Return code indicating the operation should be canceled.
	 */
	public static final String CANCEL = "CANCEL"; //$NON-NLS-1$
	/**
	 * Return code indicating the entity should not be overwritten, but operation should not be
	 * canceled.
	 */
	public static final String NO = "NO"; //$NON-NLS-1$
	/**
	 * Return code indicating the entity should be overwritten.
	 */
	public static final String YES = "YES"; //$NON-NLS-1$
	private StackLayout stackLayout;

	public String queryOverwrite(String pathString) {
		Path path = new Path(pathString);
		String messageString;
		// Break the message up if there is a file name and a directory
		// and there are at least 2 segments.
		if (path.getFileExtension() == null || path.segmentCount() < 2) {
			messageString = NLS.bind("File {0} already exists, overwrite?", pathString);
		} else {
			messageString = NLS.bind("File {0} already exists in directory {1}, owerwrite?", path.lastSegment(), path.removeLastSegments(1)
					.toOSString());
		}
		final MessageDialog dialog = new MessageDialog(getArea().getShell(), "Question", null, messageString, MessageDialog.QUESTION,
				new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
		String[] response = new String[] { YES, NO, CANCEL };
		// run in syncExec because callback is from an operation,
		// which is probably not running in the UI thread.
		getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
				dialog.open();
			}
		});
		return dialog.getReturnCode() < 0 ? CANCEL : response[dialog.getReturnCode()];
	}

	public void saveAs() {
		if (textResult != null) {
			FileDialog fileDialog = new FileDialog(getArea().getShell(), SWT.SAVE | SWT.SHEET);
			String fileStr = fileDialog.open();
			if (fileStr != null) {
				boolean succ = false;
				try {
					File file = new File(fileStr);
					if (!file.exists() || queryOverwrite(fileStr) == YES) {
						FileUtils.saveString(textResult, file);
						succ = true;
					}
					if (succ) {
						try {
							java.awt.Desktop.getDesktop().open(file);
						} catch (Throwable e) {
							MagicUIActivator.log(e);
						}
					}
				} catch (IOException e) {
					MessageDialog.openError(getArea().getShell(), "Error", "Cannot save file: " + fileStr);
				}
			}
		}
	}

	public void setReportType(final ReportType rt) {
		reportType = rt;
		actionShowPrefs.setEnabled(true);
		sideboard.setEnabled(true);
		if (reportType.getExportDelegate() instanceof AbstractExportDelegate) {
			AbstractExportDelegate<IMagicCard> delegate = (AbstractExportDelegate<IMagicCard>) reportType.getExportDelegate();
			actionShowPrefs.setEnabled(delegate.isColumnChoiceSupported());
			sideboard.setEnabled(delegate.isMultipleLocationSupported());
		}
		activate();
	}

	@Override
	public Control createPartControl(Composite parent) {
		return createContents(parent);
	}

	@Override
	public void hookContextMenu(MenuManager menuMgr) {
		// TODO Auto-generated method stub
	}

	@Override
	public void init(IViewSite site) {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCopy() {
		CopySupport.runCopy(getArea().getDisplay().getFocusControl());
	}

	@Override
	public void runPaste() {
		CopySupport.runPaste(getArea().getDisplay().getFocusControl());
	}

	@Override
	public void updateViewer() {
		// TODO Auto-generated method stub
	}

	@Override
	public void reloadData() {
		// TODO Auto-generated method stub
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
	}
}
