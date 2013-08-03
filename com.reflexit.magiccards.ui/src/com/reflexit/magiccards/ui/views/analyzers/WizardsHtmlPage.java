package com.reflexit.magiccards.ui.views.analyzers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.exports.WizardsHtmlExportDelegate;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.StoredSelectionProvider;

public class WizardsHtmlPage extends AbstractDeckPage {
	private Browser textBrowser;
	private ISelectionProvider selProvider = new StoredSelectionProvider();
	private IFilteredCardStore fstore;
	private IAction save;
	private String textResult;

	@Override
	public Composite createContents(Composite parent) {
		super.createContents(parent);
		makeActions();
		Composite area = getArea();
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
		textBrowser.setLayoutData(new GridData(GridData.FILL_BOTH));
		return getArea();
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.save);
		super.fillLocalToolBar(manager);
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(save);
		manager.add(new Separator());
		super.fillContextMenu(manager);
	}

	protected void makeActions() {
		this.save = new Action("Save") {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/save.png"));
				setToolTipText("Save As...");
			}

			@Override
			public void run() {
				saveAs();
			}
		};
	}

	@Override
	protected ISelectionProvider getSelectionProvider() {
		return selProvider;
	}

	@Override
	public void activate() {
		textResult = null;
		super.activate();
		try {
			textResult = getHtml();
			// System.err.println(textResult);
			this.textBrowser.setText(textResult);
		} catch (InvocationTargetException e) {
			this.textBrowser.setText("Error: " + e.getCause());
		} catch (InterruptedException e) {
			this.textBrowser.setText("Cancelled");
		}
	}

	@Override
	public void setFilteredStore(IFilteredCardStore store) {
		this.fstore = store;
		super.setFilteredStore(store);
	}

	private String getHtml() throws InvocationTargetException, InterruptedException {
		ByteArrayOutputStream byteSt = new ByteArrayOutputStream();
		WizardsHtmlExportDelegate<IMagicCard> ex = new WizardsHtmlExportDelegate<IMagicCard>();
		ex.init(byteSt, false, fstore);
		// ex.setReportType(ReportType.createReportType("html"));
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
				try {
					File file = new File(fileStr);
					if (file.exists()) {
						if (queryOverwrite(fileStr) == YES)
							FileUtils.saveString(textResult, file);
					}
				} catch (IOException e) {
					MessageDialog.openError(getArea().getShell(), "Error", "Cannot save file: " + fileStr);
				}
			}
		}
	}
}
