package com.reflexit.magiccards.ui.preferences;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
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

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Languages;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.commands.UpdateDbHandler;
import com.reflexit.magiccards.ui.preferences.feditors.SpecialComboFieldEditor;
import com.reflexit.magiccards.ui.widgets.ContextAssist;

public class MagicGathererPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final String STANDARD = "Standard";
	public static final String ALL = "All";
	private SpecialComboFieldEditor fSet;
	private IHandlerService service;
	private boolean hasUpdateButton = true;

	public MagicGathererPreferencePage() {
		super(GRID);
		setPreferenceStore(MagicUIActivator.getDefault().getPreferenceStore());
		// setDescription("Update settings");
	}

	@Override
	public void noDefaultAndApplyButton() {
		super.noDefaultAndApplyButton();
	}

	public void noUpdateButton() {
		hasUpdateButton = false;
	}

	@Override
	protected void createFieldEditors() {
		// addField(this.fUrl = new
		// StringFieldEditor(PreferenceConstants.GATHERER_UPDATE,
		// "Gatherer update query:",
		// getFieldEditorParent()) {
		// @Override
		// protected boolean checkState() {
		// if (!super.checkState())
		// return false;
		// try {
		// new URL(getStringValue());
		// return true;
		// } catch (Exception e) {
		// setErrorMessage(e.getMessage());
		// return false;
		// }
		// }
		// });
		String[][] array = createSetArray();
		addField(this.fSet = new SpecialComboFieldEditor(PreferenceConstants.GATHERER_UPDATE_SET, "Set:", array,
				getFieldEditorParent(), SWT.DROP_DOWN));
		addField(new BooleanFieldEditor(PreferenceConstants.GATHERER_UPDATE_LAND,
				"Load all versions of art for basic lands", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.GATHERER_UPDATE_PRINT,
				"Load all printed versions of the same card (vs only version for latest set)", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.GATHERER_UPDATE_SPECIAL, "Load special items",
				getFieldEditorParent()));
		addField(new SpecialComboFieldEditor(PreferenceConstants.GATHERER_UPDATE_LANGUAGE,
				"Also load localized version in:", createLanguagesArray(), getFieldEditorParent(), SWT.DROP_DOWN));
		Collection<String> names = Editions.getInstance().getNames();
		String proposals[] = new String[names.size()];
		int i = 0;
		for (String type : names) {
			proposals[i++] = type;
		}
		ContextAssist.addContextAssist(fSet.getComboControl(), proposals, false);
	}

	/**
	 * @return
	 */
	private String[][] createSetArray() {
		Collection names1 = Editions.getInstance().getNames();
		ArrayList names = new ArrayList(names1);
		Collections.sort(names);
		String[][] res = new String[names.size() + 2][2];
		int i = 0;
		res[i][0] = STANDARD;
		res[i][1] = STANDARD;
		i++;
		res[i][0] = ALL;
		res[i][1] = ALL;
		i++;
		for (Iterator iterator = names.iterator(); iterator.hasNext(); i++) {
			String s = (String) iterator.next();
			res[i][0] = s;
			res[i][1] = s;
		}
		return res;
	}

	private String[][] createLanguagesArray() {
		Collection names1 = Languages.getInstance().getNames();
		ArrayList names = new ArrayList(names1);
		Collections.sort(names);
		String[][] res = new String[names.size() + 1][2];
		int i = 0;
		res[i][0] = "";
		res[i][1] = "";
		i++;
		// res[i][0] = ALL;
		// res[i][1] = ALL;
		// i++;
		for (Iterator iterator = names.iterator(); iterator.hasNext(); i++) {
			String s = (String) iterator.next();
			res[i][0] = s;
			res[i][1] = s;
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange
	 * (org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// if (event.getSource() == this.fSet) {
		// updateUrl();
		// this.fUrl.load();
		// }
		super.propertyChange(event);
	}

	/*
	 * (non-Javadoc)
	 * 
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
		// parent.setLayout(new GridLayout());
		// com.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite com = new Composite(parent, SWT.NONE);
		com.setLayout(new GridLayout());
		Control fields = super.createContents(com);
		fields.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (hasUpdateButton) {
			Button button = new Button(com, SWT.PUSH);
			button.setText("Update Now...");
			GridDataFactory.fillDefaults()//
					.align(SWT.END, SWT.END)//
					.applyTo(button);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					MessageDialog.openInformation(getShell(), "Info", "Update is scheduled");
					performUpdate();
				}
			});
		}
		return com;
	}

	@Override
	public void init(IWorkbench wb) {
		if (wb != null) {
			Object serviceObject = wb.getAdapter(IHandlerService.class);
			if (serviceObject != null) {
				this.service = (IHandlerService) serviceObject;
			}
		}
	}

	private void propagateParam(HashMap parameters, String prop) {
		parameters.put(prop, getPreferenceStore().getString(prop).trim());
	}

	public void performUpdate() {
		performApply();
		try {
			HashMap parameters = new HashMap();
			propagateParam(parameters, PreferenceConstants.GATHERER_UPDATE_SET);
			propagateParam(parameters, PreferenceConstants.GATHERER_UPDATE_PRINT);
			propagateParam(parameters, PreferenceConstants.GATHERER_UPDATE_LAND);
			propagateParam(parameters, PreferenceConstants.GATHERER_UPDATE_SPECIAL);
			propagateParam(parameters, PreferenceConstants.GATHERER_UPDATE_LANGUAGE);
			new UpdateDbHandler().execute(new ExecutionEvent(null, parameters, null,
					MagicGathererPreferencePage.this.service.getCurrentState()));
		} catch (ExecutionException e1) {
			MessageDialog.openError(MagicUIActivator.getShell(), "Error", e1.getMessage());
		}
	}
}
