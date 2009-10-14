package com.reflexit.magiccards.ui.preferences.feditors;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.ColorTypes;
import com.reflexit.magiccards.core.model.Colors;

public class ColorsPreferenceGroup extends MFieldEditorPreferencePage {
	private Group group;

	@Override
	protected void createFieldEditors() {
		this.group = new Group(getFieldEditorParent(), SWT.NONE);
		this.group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.group.setText("Color");
		this.group.setLayout(new GridLayout(2, false));
		Composite left = new Composite(group, SWT.NONE);
		Composite right = new Composite(group, SWT.NONE);
		left.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		right.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Colors coreColors = Colors.getInstance();
		for (Iterator iterator = coreColors.getIds().iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			addCheckBox(id, coreColors.getNameById(id), left);
		}
		ColorTypes colorTypes = ColorTypes.getInstance();
		for (Iterator iterator = colorTypes.getIds().iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			addCheckBox(id, colorTypes.getNameById(id), right);
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
