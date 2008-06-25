package com.reflexit.magiccards.ui.preferences;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import java.lang.reflect.InvocationTargetException;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class MagicGathererPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public MagicGathererPreferencePage() {
		super(GRID);
		setPreferenceStore(MagicUIActivator.getDefault().getPreferenceStore());
		setDescription("Gatherer web-site settings");
	}

	protected void createFieldEditors() {
		addField(new StringFieldEditor(PreferenceConstants.GATHERER_UPDATE, "Gatherer update query:",
		        getFieldEditorParent()));
	}

	protected Control createContents(Composite parent) {
		//parent.setLayout(new GridLayout());
		//com.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite com = new Composite(parent, SWT.NONE);
		com.setLayout(new GridLayout());
		Control fields = super.createContents(com);
		fields.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button button = new Button(com, SWT.PUSH);
		button.setText("Update Now...");
		GridDataFactory.fillDefaults()//
		        .align(SWT.END, SWT.END)//
		        .applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performUpdate();
			}
		});
		return com;
	}

	protected void performUpdate() {
		performApply();
		final Integer res[] = new Integer[1];
		final Shell shell = getShell();
		IWorkbench wb = PlatformUI.getWorkbench();
		//	IProgressService ps = wb.getProgressService();
		try {
			IRunnableWithProgress op = new IRunnableWithProgress() {
				public void run(IProgressMonitor pm) {
					try {
						ICardHandler ch = DataManager.getCardHandler();
						String url = getPreferenceStore().getString(PreferenceConstants.GATHERER_UPDATE);
						int rec = ch.downloadFromUrl(url, pm);
						res[0] = new Integer(rec);
					} catch (final Exception e) {
						e.printStackTrace();
						shell.getDisplay().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openError(shell, "Error", e.getMessage());
							}
						});
						res[0] = new Integer(0);
						return;
					} finally {
						pm.done();
					}
				}
			};
			new ProgressMonitorDialog(shell).run(true, true, op);
			//ps.busyCursorWhile(op);
		} catch (InvocationTargetException e) {
			MessageDialog.openError(shell, "Error", e.getMessage());
			return;
		} catch (InterruptedException e) {
			// cancel
		}
		MessageDialog.openInformation(shell, getTitle(), "Data updated: " + res[0] + " new records");
	}

	public void init(IWorkbench workbench) {
		// nothing
	}
}
