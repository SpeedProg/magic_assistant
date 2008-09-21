package com.reflexit.magiccards.ui.preferences.feditors;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.Rarity;

public class RarityPreferenceGroup extends FieldEditorPreferencePage {
	private Group group;

	public RarityPreferenceGroup() {
		super(GRID);
		noDefaultAndApplyButton();
	}

	@Override
	protected void createFieldEditors() {
		this.group = new Group(getFieldEditorParent(), SWT.NONE);
		this.group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.group.setText("Rarity");
		Composite parent = this.group;
		Rarity coreTypes = Rarity.getInstance();
		for (Iterator iterator = coreTypes.getIds().iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			addCheckBox(id, coreTypes.getNameById(id), parent);
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
