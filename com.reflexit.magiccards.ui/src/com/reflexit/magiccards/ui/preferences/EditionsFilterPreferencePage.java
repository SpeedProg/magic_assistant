package com.reflexit.magiccards.ui.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.ui.dialogs.CardFilterDialog;
import com.reflexit.magiccards.ui.preferences.feditors.MFieldEditorPreferencePage;
import com.reflexit.magiccards.ui.views.editions.EditionsComposite;

public class EditionsFilterPreferencePage extends AbstractFilterPreferencePage {
	private static final String FORMAT_NONE = "None";
	public static final String LAST_SET = "onlyLastSet";
	private EditionsComposite comp;
	private Button onlyLastSet;

	public EditionsFilterPreferencePage(CardFilterDialog cardFilterDialog) {
		super(cardFilterDialog);
		setTitle("Set Filter");
		// setDescription("A demonstration of a preference page
		// implementation");
	}

	@Override
	public void setPreferenceStore(IPreferenceStore store) {
		store.setDefault(FilterField.FORMAT.getPrefConstant(), "");
		super.setPreferenceStore(store);
		if (comp != null)
			comp.initialize();
	}

	@Override
	protected Control createContents(Composite parent) {
		this.onlyLastSet = new Button(parent, SWT.CHECK);
		this.onlyLastSet.setFont(parent.getFont());
		this.onlyLastSet.setText("Only show the card from the latest set if multiple available");
		this.onlyLastSet.setSelection(getPreferenceStore().getBoolean(LAST_SET));
		final String[][] fs = new String[3][2];
		fs[0][0] = FORMAT_NONE;
		fs[0][1] = "";
		int i = 1;
		for (Iterator<Format> iterator = Format.getFormats().iterator(); iterator.hasNext() && i < 3; i++) {
			Format f = iterator.next();
			fs[i][0] = fs[i][1] = f.name();
		}
		// format
		createAndAdd(new MFieldEditorPreferencePage() {
			@Override
			protected void createFieldEditors() {
				addField(new ComboFieldEditor(FilterField.FORMAT.getPrefConstant(), "Format: ", fs,
						getFieldEditorParent()));
			}

			@Override
			public Collection<String> getIds() {
				ArrayList<String> s = new ArrayList<String>();
				s.add(FilterField.FORMAT.getPrefConstant());
				return s;
			}
		}, parent);
		// editions
		Group editions = new Group(parent, SWT.NONE);
		editions.setFont(parent.getFont());
		editions.setText("Select visible sets");
		editions.setLayout(new FillLayout());
		this.comp = new EditionsComposite(editions, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION, true);
		this.comp.setPreferenceStore(getPreferenceStore());
		this.comp.initialize();
		setPreferenceStore(getPreferenceStore());
		return editions;
	}

	@Override
	public boolean performOk() {
		super.performOk();
		if (this.comp != null) {
			this.comp.performApply();
			getPreferenceStore().setValue(LAST_SET, onlyLastSet.getSelection());
		}
		return true;
	}

	@Override
	public void performDefaults() {
		if (this.comp != null) {
			comp.setToDefaults();
		}
		super.performDefaults();
	}
}
