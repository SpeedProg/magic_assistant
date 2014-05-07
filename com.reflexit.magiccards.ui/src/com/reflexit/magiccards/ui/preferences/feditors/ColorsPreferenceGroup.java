package com.reflexit.magiccards.ui.preferences.feditors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.reflexit.magiccards.core.model.ColorTypes;
import com.reflexit.magiccards.core.model.Colors;

public class ColorsPreferenceGroup extends MFieldEditorPreferencePage {
	private Collection<String> ids = new ArrayList<String>(6);

	@Override
	public Collection<String> getIds() {
		return ids;
	}

	private Group group;

	@Override
	protected void createFieldEditors() {
		this.group = new Group(getFieldEditorParent(), SWT.NONE);
		this.group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.group.setText("Color");
		this.group.setLayout(new GridLayout(2, false));
		Font font = getFieldEditorParent().getFont();
		this.group.setFont(font);
		Composite left = new Composite(group, SWT.NONE);
		Composite right = new Composite(group, SWT.NONE);
		left.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		left.setFont(font);
		right.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		right.setFont(font);
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
		ids.add(id);
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
