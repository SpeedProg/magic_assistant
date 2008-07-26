package com.reflexit.magiccards.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.FilterHelper;

public class TextSearchPreferenceGroup extends FieldEditorPreferencePage {
	//private Group group;
	public TextSearchPreferenceGroup() {
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
		String id = FilterHelper.getPrefConstant(FilterHelper.SUBTYPE, FilterHelper.TEXT_POSTFIX);
		getPreferenceStore().setDefault(id, "");
		addField(new StringFieldEditor(id, "Type", getFieldEditorParent()));
		id = FilterHelper.getPrefConstant(FilterHelper.TEXT_LINE, FilterHelper.TEXT_POSTFIX);
		getPreferenceStore().setDefault(id, "");
		addField(new StringFieldEditor(id, "Text", getFieldEditorParent()));
		id = FilterHelper.getPrefConstant(FilterHelper.NAME_LINE, FilterHelper.TEXT_POSTFIX);
		getPreferenceStore().setDefault(id, "");
		addField(new StringFieldEditor(id, "Name", getFieldEditorParent()));
	}

	@Override
	protected void adjustGridLayout() {
		GridLayout layout = (GridLayout) ((Composite) this.getControl()).getLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		super.adjustGridLayout();
	}
}
