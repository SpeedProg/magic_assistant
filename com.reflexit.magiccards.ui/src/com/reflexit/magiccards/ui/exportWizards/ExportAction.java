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
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * Action to create Export Wizard from buttons and menus
 */
public class ExportAction extends Action implements ISelectionChangedListener {
	public final static String ID = ExportAction.class.getName();
	private IStructuredSelection selection;

	public ExportAction() {
		super("Export...");
		setId(ID);
		setToolTipText("Export data to a file");
		setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_EXPORT_WIZ));
	}

	@Override
	public void run() {
		IStructuredSelection selection = getStructuredSelection();
		if (!canImport(selection))
			return;
		runOpenWizard(selection);
	}

	public IStructuredSelection getStructuredSelection() {
		return selection;
	}

	private boolean canImport(final IStructuredSelection selection) {
		return true;
	}

	private boolean runOpenWizard(final IStructuredSelection selection) {
		DeckExportWizard wizard = new DeckExportWizard();
		wizard.init(PlatformUI.getWorkbench(), selection);
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		dialog.create();
		dialog.getShell().setText(wizard.getWindowTitle());
		int result = dialog.open();
		boolean succ = result == Window.OK;
		return succ;
	}

	public void dispose() {
		// nothing to do
	}

	public void selectionChanged(IStructuredSelection selection) {
		this.selection = selection;
	}

	public void selectionChanged(SelectionChangedEvent event) {
		if (event.getSelection() instanceof IStructuredSelection) {
			selectionChanged((IStructuredSelection) event.getSelection());
		}
	}
}
