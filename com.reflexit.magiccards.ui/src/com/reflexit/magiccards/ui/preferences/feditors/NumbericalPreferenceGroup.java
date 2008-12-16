package com.reflexit.magiccards.ui.preferences.feditors;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.FilterHelper;

public class NumbericalPreferenceGroup extends FieldEditorPreferencePage {
	//private Group group;
	public NumbericalPreferenceGroup() {
		super(GRID);
		noDefaultAndApplyButton();
	}

	@Override
	protected void createFieldEditors() {
		//		this.group = new Group(getFieldEditorParent(), SWT.NONE);
		//		this.group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//		this.group.setText("Subtype");
		//		Composite parent = this.group;
		// addCheckBox("Any", parent);
		String id = FilterHelper.getPrefConstant(FilterHelper.POWER, FilterHelper.NUMERIC_POSTFIX);
		getPreferenceStore().setDefault(id, "0");
		addField(new NumericalComparisonFieldEditor(id, "Power", getFieldEditorParent()));
		id = FilterHelper.getPrefConstant(FilterHelper.TOUGHNESS, FilterHelper.NUMERIC_POSTFIX);
		getPreferenceStore().setDefault(id, "0");
		addField(new NumericalComparisonFieldEditor(id, "Toughness", getFieldEditorParent()));
		id = FilterHelper.getPrefConstant(FilterHelper.CCC, FilterHelper.NUMERIC_POSTFIX);
		getPreferenceStore().setDefault(id, "0");
		addField(new NumericalComparisonFieldEditor(id, "Converted CC", getFieldEditorParent()));
	}

	@Override
	protected void adjustGridLayout() {
		GridLayout layout = (GridLayout) ((Composite) this.getControl()).getLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		super.adjustGridLayout();
	}
}
