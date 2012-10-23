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

import com.reflexit.magiccards.core.model.CardTypes;
import com.reflexit.magiccards.core.model.ISearchableProperty;

public class TypesPreferenceGroup extends MFieldEditorPreferencePage {
	private Group group;

	@Override
	protected void createFieldEditors() {
		this.group = new Group(getFieldEditorParent(), SWT.NONE);
		this.group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.group.setText("Type");
		this.group.setFont(getFieldEditorParent().getFont());
		Composite parent = this.group;
		// artifact, creature, enchantment, instant, land, or sorcery.
		// addCheckBox("Any", parent);
		CardTypes coreTypes = (CardTypes) getSearchablePropery();
		Collection ids = coreTypes.getIds();
		for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			addCheckBox(id, coreTypes.getLocalizedNameById(id), parent);
		}
	}

	public ISearchableProperty getSearchablePropery() {
		CardTypes coreTypes = CardTypes.getInstance();
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
