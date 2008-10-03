package com.reflexit.magiccards.ui.preferences;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.handlers.IHandlerService;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.commands.UpdateDbHandler;
import com.reflexit.magiccards.ui.preferences.feditors.SpecialComboFieldEditor;

public class MagicGathererPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String ALL = "All";
	private StringFieldEditor fUrl;
	private SpecialComboFieldEditor fSet;

	public MagicGathererPreferencePage() {
		super(GRID);
		setPreferenceStore(MagicUIActivator.getDefault().getPreferenceStore());
		setDescription("Gatherer web-site settings");
	}

	@Override
	protected void createFieldEditors() {
		addField(this.fUrl = new StringFieldEditor(PreferenceConstants.GATHERER_UPDATE, "Gatherer update query:",
		        getFieldEditorParent()) {
			@Override
			protected boolean checkState() {
				if (!super.checkState())
					return false;
				try {
					new URL(getStringValue());
					return true;
				} catch (Exception e) {
					setErrorMessage(e.getMessage());
					return false;
				}
			}
		});
		String[][] array = createSetArray();
		addField(this.fSet = new SpecialComboFieldEditor(PreferenceConstants.GATHERER_UPDATE_SET, "Set:", array,
		        getFieldEditorParent(), SWT.DROP_DOWN));
	}

	/**
	 * @return
	 */
	private String[][] createSetArray() {
		Collection names = Editions.getInstance().getNames();
		String[][] res = new String[names.size() + 1][2];
		int i = 1;
		res[0][0] = "Standard";
		res[0][1] = "Standard";
		for (Iterator iterator = names.iterator(); iterator.hasNext(); i++) {
			String s = (String) iterator.next();
			res[i][0] = s;
			res[i][1] = s;
		}
		return res;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getSource() == this.fSet) {
			updateUrl();
			this.fUrl.load();
		}
		super.propertyChange(event);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	protected void performApply() {
		super.performApply();
	}

	protected void updateUrl() {
		String set = this.fSet.getStringValue();
		String urls = getPreferenceStore().getString(PreferenceConstants.GATHERER_UPDATE);
		String setu = set;
		try {
			setu = URLEncoder.encode(set, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = urls.replaceAll("setfilter=[^&]*", "setfilter=" + setu);
		getPreferenceStore().setValue(PreferenceConstants.GATHERER_UPDATE, url);
	}

	@Override
	protected Control createContents(final Composite parent) {
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
			@Override
			public void widgetSelected(SelectionEvent e) {
				performApply();
				try {
					;
					HashMap parameters = new HashMap();
					parameters.put(PreferenceConstants.GATHERER_UPDATE, getPreferenceStore().getString(
					        PreferenceConstants.GATHERER_UPDATE));
					new UpdateDbHandler().execute(new ExecutionEvent(null, parameters, null,
					        MagicGathererPreferencePage.this.service.getCurrentState()));
				} catch (ExecutionException e1) {
					MessageDialog.openError(parent.getShell(), "Error", e1.getMessage());
				}
			}
		});
		return com;
	}
	IHandlerService service;

	public void init(IWorkbench wb) {
		if (wb != null) {
			Object serviceObject = wb.getAdapter(IHandlerService.class);
			if (serviceObject != null) {
				this.service = (IHandlerService) serviceObject;
			}
		}
	}
}
