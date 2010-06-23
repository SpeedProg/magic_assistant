package com.reflexit.magiccards.ui.preferences.feditors;

import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.FilterHelper;

public class UserFieldsPreferenceGroup extends MFieldEditorPreferencePage {
	//private Group group;
	@Override
	protected void createFieldEditors() {
		String id = FilterHelper.getPrefConstant(FilterHelper.PRICE, FilterHelper.NUMERIC_POSTFIX);
		getPreferenceStore().setDefault(id, "0");
		addField(new NumericalComparisonFieldEditor(id, "Price", getFieldEditorParent()));
		id = FilterHelper.getPrefConstant(FilterHelper.COUNT, FilterHelper.NUMERIC_POSTFIX);
		getPreferenceStore().setDefault(id, "0");
		addField(new NumericalComparisonFieldEditor(id, "Count", getFieldEditorParent()));
		id = FilterHelper.getPrefConstant(FilterHelper.COMMENT, FilterHelper.TEXT_POSTFIX);
		getPreferenceStore().setDefault(id, "");
		StringFieldEditor nameSfe = new StringFieldEditor(id, "Comment", getFieldEditorParent());
		addField(nameSfe);
		String toolTip = "Search expression can contain words separated by spaces,\n"
		        + "which would be searched using AND connector.\n" // 
		        + "Adding '-' in front of the word makes it NOT.\n";
		addTooltip(nameSfe, toolTip);
		id = FilterHelper.getPrefConstant(FilterHelper.OWNERSHIP, FilterHelper.TEXT_POSTFIX);
		RadioGroupFieldEditor radios = new RadioGroupFieldEditor(id, "Ownership", 1, new String[][] {
		        { "Show all cards", "", },
		        { "Show only own cards (determined by ownership attribute)", "true", },
		        { "Show only virtual cards", "false", }, }, getFieldEditorParent(), true);
		//getPreferenceStore().setDefault(id, Boolean.FALSE);
		//BooleanFieldEditor x = new BooleanFieldEditor(id, "Only Own", getFieldEditorParent());
		addField(radios);
	}

	@Override
	protected void adjustGridLayout() {
		GridLayout layout = (GridLayout) ((Composite) this.getControl()).getLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		super.adjustGridLayout();
	}
}
