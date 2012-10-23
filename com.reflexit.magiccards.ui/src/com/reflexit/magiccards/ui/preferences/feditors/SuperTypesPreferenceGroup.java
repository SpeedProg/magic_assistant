package com.reflexit.magiccards.ui.preferences.feditors;

import java.util.Iterator;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.reflexit.magiccards.core.model.SuperTypes;

public class SuperTypesPreferenceGroup extends FieldEditorPreferencePage {
	private Group group;

	public SuperTypesPreferenceGroup() {
		super(GRID);
		noDefaultAndApplyButton();
	}

	@Override
	protected void createFieldEditors() {
		this.group = new Group(getFieldEditorParent(), SWT.NONE);
		this.group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.group.setText("Supertype");
		this.group.setFont(getFieldEditorParent().getFont());
		Composite parent = this.group;
		SuperTypes coreSuperTypes = SuperTypes.getInstance();
		for (Iterator iterator = coreSuperTypes.getIds().iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			addCheckBox(id, coreSuperTypes.getNameById(id), parent);
		}
	}

	private FieldEditor addCheckBox(String id, String name, Composite parent) {
		BooleanFieldEditor editor = new BooleanFieldEditor(id, name, parent);
		addField(editor);
		return editor;
	}

	@Override
	protected void adjustGridLayout() {
		GridLayout layout = (GridLayout) this.group.getLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		super.adjustGridLayout();
	}
}
