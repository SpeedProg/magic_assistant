package com.reflexit.magiccards.ui.preferences.feditors;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.FilterHelper;

public class NumbericalPreferenceGroup extends MFieldEditorPreferencePage {
	private Collection<String> ids = new ArrayList<String>(6);

	@Override
	public Collection<String> getIds() {
		return ids;
	}

	// private Group group;
	@Override
	protected void createFieldEditors() {
		// this.group = new Group(getFieldEditorParent(), SWT.NONE);
		// this.group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// this.group.setText("Subtype");
		// Composite parent = this.group;
		// addCheckBox("Any", parent);
		String id = FilterHelper.getPrefConstant(FilterHelper.POWER, FilterHelper.NUMERIC_POSTFIX);
		getPreferenceStore().setDefault(id, "0");
		addField(new NumericalComparisonFieldEditor(id, "Power", getFieldEditorParent()));
		ids.add(id);
		id = FilterHelper.getPrefConstant(FilterHelper.TOUGHNESS, FilterHelper.NUMERIC_POSTFIX);
		getPreferenceStore().setDefault(id, "0");
		addField(new NumericalComparisonFieldEditor(id, "Toughness", getFieldEditorParent()));
		ids.add(id);
		id = FilterHelper.getPrefConstant(FilterHelper.CCC, FilterHelper.NUMERIC_POSTFIX);
		getPreferenceStore().setDefault(id, "0");
		addField(new NumericalComparisonFieldEditor(id, "Converted CC", getFieldEditorParent()));
		ids.add(id);
		id = FilterHelper.getPrefConstant(FilterHelper.DBPRICE, FilterHelper.NUMERIC_POSTFIX);
		getPreferenceStore().setDefault(id, "0");
		addField(new NumericalComparisonFieldEditor(id, "Seller's Price", getFieldEditorParent()));
		ids.add(id);
		id = FilterHelper.getPrefConstant(FilterHelper.COMMUNITYRATING, FilterHelper.NUMERIC_POSTFIX);
		getPreferenceStore().setDefault(id, "0");
		addField(new NumericalComparisonFieldEditor(id, "Community Rating", getFieldEditorParent()));
		ids.add(id);
		id = FilterHelper.getPrefConstant(FilterHelper.COLLNUM, FilterHelper.NUMERIC_POSTFIX);
		getPreferenceStore().setDefault(id, "0");
		addField(new NumericalComparisonFieldEditor(id, "Collector's Number", getFieldEditorParent()));
		ids.add(id);
	}

	@Override
	protected void adjustGridLayout() {
		GridLayout layout = (GridLayout) ((Composite) this.getControl()).getLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		super.adjustGridLayout();
	}
}
