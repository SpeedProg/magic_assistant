package com.reflexit.magiccards.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import com.reflexit.magiccards.ui.dialogs.CardFilterDialog;
import com.reflexit.magiccards.ui.views.editions.EditionsComposite;

public class EditionsFilterPreferencePage extends AbstractFilterPreferencePage {
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
		Group editions = new Group(parent, SWT.NONE);
		editions.setFont(parent.getFont());
		editions.setText("Select visible sets");
		editions.setLayout(new FillLayout());
		this.comp = new EditionsComposite(editions, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION, true);
		this.comp.setPreferenceStore(getPreferenceStore());
		this.comp.initialize();
		return editions;
	}

	@Override
	public boolean performOk() {
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
