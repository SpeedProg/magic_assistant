/**
 * $QNXLicenseC$
 *
 * Created by: Elena Laskavaia
 * Created on: 13-Feb-07
 * Last modified by: $Author: elaskavaia $
 */
package com.reflexit.magiccards.ui.exportWizards;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;

import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

/**
 * Action to create Export Wizard from buttons and menus
 */
public class ExportAction extends Action implements ISelectionChangedListener {
	public final static String ID = ExportAction.class.getName();
	private IStructuredSelection selection;
	private String prefId;

	public ExportAction(IStructuredSelection selection) {
		this();
		selectionChanged(selection);
	}

	public ExportAction() {
		super("Export...");
		setId(ID);
		setToolTipText("Export data to a file");
		setImageDescriptor(WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_EXPORT_WIZ));
		// prefId = DeckViewPreferencePage.class.getName();
	}

	public ExportAction(StructuredSelection structuredSelection, String prefId) {
		this(structuredSelection);
		if (prefId == null)
			throw new NullPointerException();
		this.prefId = prefId;
	}

	@Override
	public void run() {
		IStructuredSelection selection = getStructuredSelection();
		if (!canExport(selection))
			return;
		runOpenWizard(selection);
	}

	public IStructuredSelection getStructuredSelection() {
		return selection;
	}

	private boolean canExport(final IStructuredSelection selection) {
		return true;
	}

	private boolean runOpenWizard(final IStructuredSelection selection) {
		String selcolumns = PreferenceInitializer.getLocalStore(prefId).getString(
				PreferenceConstants.LOCAL_COLUMNS);
		ICardField[] columns = new MagicColumnCollection(prefId).getSelectedColumnFields(selcolumns);
		DeckExportWizard wizard = new DeckExportWizard();
		wizard.init(PlatformUI.getWorkbench(), selection);
		wizard.setColumns(columns);
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell(), wizard);
		dialog.create();
		dialog.getShell().setText(wizard.getWindowTitle());
		int result = dialog.open();
		boolean succ = result == Window.OK;
		if (succ) {
			try {
				java.awt.Desktop.getDesktop().open(wizard.getFile());
			} catch (Throwable e) {
				MagicUIActivator.log(e);
			}
		}
		return succ;
	}

	public void dispose() {
		// nothing to do
	}

	public void selectionChanged(IStructuredSelection selection) {
		this.selection = selection;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (event.getSelection() instanceof IStructuredSelection) {
			selectionChanged((IStructuredSelection) event.getSelection());
		}
	}
}
