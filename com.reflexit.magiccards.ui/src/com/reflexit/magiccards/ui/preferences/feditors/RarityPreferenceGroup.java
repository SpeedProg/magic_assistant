package com.reflexit.magiccards.ui.preferences.feditors;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.reflexit.magiccards.core.model.ISearchableProperty;
import com.reflexit.magiccards.core.model.Rarity;

public class RarityPreferenceGroup extends MFieldEditorPreferencePage {
	private Group group;

	@Override
	protected void createFieldEditors() {
		this.group = new Group(getFieldEditorParent(), SWT.NONE);
		this.group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.group.setText("Rarity");
		this.group.setFont(getFieldEditorParent().getFont());
		Composite parent = this.group;
		Rarity coreTypes = Rarity.getInstance();
		for (Iterator iterator = coreTypes.getIds().iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			addCheckBox(id, coreTypes.getNameById(id), parent);
		}
	}

	public ISearchableProperty getSearchablePropery() {
		Rarity coreTypes = Rarity.getInstance();
		return coreTypes;
	}

	@Override
	public Collection<String> getIds() {
		return getSearchablePropery().getIds();
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
